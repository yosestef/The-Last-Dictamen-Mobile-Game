package com.android.mobile.games.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mobile.games.app.identity.AuthViewModel
import kotlinx.coroutines.delay

private val AuthDarkBg = Color(0xFF0D0221)
private val AuthNeonCyan = Color(0xFF00F5FF)
private val AuthNeonPurple = Color(0xFFBF00FF)
private val AuthDarkCard = Color(0xFF1B065E)
private val AuthTerminalGreen = Color(0xFF00FF88)

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onNavigateToMain: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkSession()
    }

    LaunchedEffect(state) {
        when (state) {
            is AuthViewModel.AuthState.SessionExists -> {
                delay(1500L)
                onNavigateToMain()
            }
            is AuthViewModel.AuthState.Identified -> {
                delay(3000L)
                onNavigateToMain()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AuthDarkBg, Color(0xFF0A0118), AuthDarkBg)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "// THE LAST DICTAMEN",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AuthNeonCyan,
                    letterSpacing = 2.sp,
                    shadow = Shadow(
                        color = AuthNeonCyan.copy(alpha = 0.8f),
                        offset = Offset(0f, 0f),
                        blurRadius = 28f
                    )
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "SISTEMA DE AUTENTICACIÓN v1.0",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = AuthNeonPurple.copy(alpha = 0.7f),
                    letterSpacing = 1.5.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            when (val s = state) {
                is AuthViewModel.AuthState.Loading ->
                    TerminalBlink(message = "Verificando credenciales del sistema...")

                is AuthViewModel.AuthState.SessionExists ->
                    SplashPanel(programmerId = s.programmerId)

                is AuthViewModel.AuthState.NewUser ->
                    LoginPanel(onGuestClick = { viewModel.continueAsGuest() })

                is AuthViewModel.AuthState.GeneratingId ->
                    TerminalBlink(message = "Generando ID de acceso...")

                is AuthViewModel.AuthState.Identified ->
                    TerminalSuccess(programmerId = s.programmerId)
            }
        }
    }
}

@Composable
private fun TerminalBlink(message: String) {
    val transition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "> $message",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = AuthNeonCyan
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "█",
            color = AuthNeonCyan.copy(alpha = cursorAlpha),
            fontFamily = FontFamily.Monospace,
            fontSize = 22.sp
        )
    }
}

@Composable
private fun SplashPanel(programmerId: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "> Sesión detectada.",
            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = AuthTerminalGreen)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "> Cargando perfil: $programmerId",
            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = AuthNeonCyan)
        )
        Spacer(modifier = Modifier.height(20.dp))
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(0.7f),
            color = AuthNeonCyan,
            trackColor = AuthDarkCard
        )
    }
}

@Composable
private fun LoginPanel(onGuestClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuthDarkCard, RoundedCornerShape(6.dp))
                .border(1.dp, AuthNeonCyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "> Sistema de Identidad ARCADE",
                    fontFamily = FontFamily.Monospace,
                    color = AuthNeonCyan,
                    fontSize = 13.sp
                )
                Text(
                    "> Sin sesión activa. Selecciona método de acceso:",
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onGuestClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthNeonCyan,
                contentColor = AuthDarkBg
            ),
            shape = RoundedCornerShape(4.dp),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Text(
                text = "[ CONTINUAR COMO INVITADO ]",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
        }

        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(52.dp),
            border = BorderStroke(1.5.dp, AuthNeonPurple.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AuthNeonPurple)
        ) {
            Text(
                text = "[ GOOGLE SIGN-IN ]",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                color = AuthNeonPurple.copy(alpha = 0.5f)
            )
        }

        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(52.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text(
                text = "[ CORREO ELECTRÓNICO ]",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.25f)
            )
        }

        Text(
            text = "Google y Correo disponibles próximamente (Supabase)",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.25f)
        )
    }
}

@Composable
private fun TerminalSuccess(programmerId: String) {
    val fullText = "Asignando Credenciales de Acceso...\nID: $programmerId GENERADO.\nBienvenido al Arcade."
    var displayed by remember { mutableStateOf("") }

    LaunchedEffect(programmerId) {
        fullText.forEachIndexed { i, _ ->
            displayed = fullText.substring(0, i + 1)
            delay(26L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF000D07), RoundedCornerShape(6.dp))
            .border(1.dp, AuthTerminalGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "> $displayed█",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                color = AuthTerminalGreen,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        )
    }
}
