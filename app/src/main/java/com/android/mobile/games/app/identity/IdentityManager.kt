package com.android.mobile.games.app.identity

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.util.UUID

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
        Log.d("QA-AUTH", "Active Identity initialized as $id via $provider")
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
            Log.d("QA-AUTH", "Active Identity initialized as $existingId via $provider")
            return existing
        }

        val newId = "PROG-${generateSuffix(context)}"
        val newSession = IdentitySession(newId, AuthProvider.ANONIMO)

        dataStore.edit { mutablePrefs ->
            mutablePrefs[PROGRAMMER_ID_KEY] = newId
            mutablePrefs[AUTH_PROVIDER_KEY] = AuthProvider.ANONIMO.name
        }

        _session.value = newSession
        Log.d("QA-AUTH", "Active Identity initialized as $newId via ${AuthProvider.ANONIMO}")
        return newSession
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

    // Supabase Ready: placeholder para vincular cuenta anónima con cuenta real
    fun linkAccount(provider: AuthProvider) {
        // TODO Supabase: cuando provider == GOOGLE → client.auth.signInWithOAuth(OAuthProvider.GOOGLE)
        // TODO Supabase: cuando provider == CORREO  → client.auth.signUpWith(Email) { ... }
    }

    // Supabase Ready: placeholder para migrar datos de MySQL → Supabase PostgreSQL
    fun syncWithCloud() {
        // TODO Supabase: iterar scores locales y subirlos a las tablas de Supabase Database
    }
}
