package com.android.mobile.games.app.identity

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val Context.identityDataStore: DataStore<Preferences> by preferencesDataStore(name = "identity_prefs")

class IdentityManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: IdentityManager? = null

        fun getInstance(context: Context): IdentityManager =
            instance ?: synchronized(this) {
                instance ?: IdentityManager(context.applicationContext).also { instance = it }
            }

        private val PROGRAMMER_ID_KEY = stringPreferencesKey("programmer_id")
        private val AUTH_PROVIDER_KEY = stringPreferencesKey("auth_provider")
    }

    private val dataStore = context.identityDataStore

    private val _session = MutableStateFlow<IdentitySession?>(null)
    val session: StateFlow<IdentitySession?> = _session.asStateFlow()

    val programmerId: String get() = _session.value?.programmerId ?: "ANON"

    suspend fun loadSession() {
        val prefs = dataStore.data.first()
        val id = prefs[PROGRAMMER_ID_KEY] ?: return
        val provider = prefs[AUTH_PROVIDER_KEY]
            ?.let { runCatching { AuthProvider.valueOf(it) }.getOrDefault(AuthProvider.NINGUNO) }
            ?: AuthProvider.NINGUNO
        _session.value = IdentitySession(id, provider)
        Log.d("QA-FIREBASE", "Sesión local restaurada: $id via $provider")
    }

    fun hasSession(): Boolean = _session.value != null

    suspend fun createAnonymousSession(): IdentitySession {
        // CRÍTICO: Verificar siempre que no exista un ID antes de generar uno nuevo
        val prefs = dataStore.data.first()
        val existingId = prefs[PROGRAMMER_ID_KEY]
        if (existingId != null) {
            val provider = prefs[AUTH_PROVIDER_KEY]
                ?.let { runCatching { AuthProvider.valueOf(it) }.getOrDefault(AuthProvider.NINGUNO) }
                ?: AuthProvider.NINGUNO
            val existing = IdentitySession(existingId, provider)
            _session.value = existing
            Log.d("QA-FIREBASE", "Sesión local restaurada: $existingId via $provider")
            signInFirebaseAnonymously()
            return existing
        }

        val newId = "PROG-${generateSuffix(context)}"
        val newSession = IdentitySession(newId, AuthProvider.ANONIMO)

        dataStore.edit { mutablePrefs ->
            mutablePrefs[PROGRAMMER_ID_KEY] = newId
            mutablePrefs[AUTH_PROVIDER_KEY] = AuthProvider.ANONIMO.name
        }

        _session.value = newSession
        Log.d("QA-FIREBASE", "Identidad generada: $newId via ${AuthProvider.ANONIMO}")
        signInFirebaseAnonymously()
        return newSession
    }

    private fun signInFirebaseAnonymously() {
        runCatching {
            if (FirebaseAuth.getInstance().currentUser != null) {
                Log.d("QA-FIREBASE", "Firebase Auth: sesión activa ${FirebaseAuth.getInstance().currentUser?.uid}")
                return
            }
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener { result ->
                    Log.d("QA-FIREBASE", "Firebase Auth: inicio anónimo OK uid=${result.user?.uid}")
                }
                .addOnFailureListener { e ->
                    Log.d("QA-FIREBASE", "Firebase Auth: no disponible (sin google-services.json?): ${e.message}")
                }
        }.onFailure {
            Log.d("QA-FIREBASE", "Firebase Auth: SDK no inicializado: ${it.message}")
        }
    }

    suspend fun syncWithCloud(collection: String, score: Int, difficulty: String) {
        runCatching {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: run {
                    Log.d("QA-FIREBASE", "Firestore: sin uid Firebase, omitiendo sync")
                    return
                }
            val document = mapOf(
                "programmerId" to programmerId,
                "uid" to uid,
                "score" to score,
                "difficulty" to difficulty,
                "timestamp" to System.currentTimeMillis()
            )
            suspendCoroutine { cont ->
                Firebase.firestore.collection(collection)
                    .add(document)
                    .addOnSuccessListener {
                        Log.d("QA-FIREBASE", "Firestore sync OK: collection=$collection programmerId=$programmerId score=$score")
                        cont.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        Log.d("QA-FIREBASE", "Firestore sync failed: ${e.message}")
                        cont.resume(Unit)
                    }
            }
        }.onFailure {
            Log.d("QA-FIREBASE", "Firebase Firestore: no disponible: ${it.message}")
        }
    }

    fun linkAccount(provider: AuthProvider) {
        runCatching {
            when (provider) {
                AuthProvider.GOOGLE -> Log.d("QA-FIREBASE", "Firebase Auth: Google Sign-In pendiente de configuración")
                AuthProvider.CORREO -> Log.d("QA-FIREBASE", "Firebase Auth: Email Sign-In pendiente de configuración")
                else -> Unit
            }
        }
    }

    private fun generateSuffix(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        // ANDROID_ID puede ser null o el valor genérico de emuladores; en ese caso usamos UUID
        val seed = if (androidId.isNullOrBlank() || androidId == "9774d56d682e549c") {
            UUID.randomUUID().toString()
        } else {
            androidId
        }
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return seed.filter { it.isLetterOrDigit() }.uppercase().take(6).padEnd(6, chars.random())
    }
}
