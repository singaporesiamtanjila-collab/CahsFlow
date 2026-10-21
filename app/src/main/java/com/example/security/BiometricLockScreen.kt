package com.example.security

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.components.BiometricRadarScanner
import com.example.ui.components.CadGridBackground
import com.example.ui.theme.CncBackground
import com.example.ui.theme.CncBorder
import com.example.ui.theme.CncBorderBright
import com.example.ui.theme.CncLaserCyan
import com.example.ui.theme.CncPrimary
import com.example.ui.theme.CncSurface
import com.example.ui.theme.CncTextPrimary
import com.example.ui.theme.CncTextSecondary

@Composable
fun BiometricLockScreen(
    securityManager: SecurityManager,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    fun triggerHaptic(strong: Boolean = false) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        if (strong) 80L else 30L,
                        if (strong) VibrationEffect.DEFAULT_AMPLITUDE else 100
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (strong) 80L else 30L)
            }
        } catch (_: Exception) {}
    }

    fun launchBiometricPrompt() {
        if (activity != null && securityManager.canAuthenticateWithBiometrics()) {
            securityManager.promptBiometricAuth(
                activity = activity,
                onSuccess = {
                    triggerHaptic(false)
                    onUnlocked()
                },
                onError = { err ->
                    errorMessage = err
                }
            )
        } else {
            errorMessage = "Biometrics sensor not active. Please enter PIN."
        }
    }

    LaunchedEffect(Unit) {
        if (securityManager.isBiometricEnabled && securityManager.canAuthenticateWithBiometrics()) {
            launchBiometricPrompt()
        }
    }

    fun onDigitPress(digit: String) {
        triggerHaptic(false)
        if (pinInput.length < 4) {
            val updated = pinInput + digit
            pinInput = updated
            errorMessage = null
            if (updated.length == 4) {
                if (securityManager.verifyPin(updated)) {
                    triggerHaptic(false)
                    onUnlocked()
                } else {
                    triggerHaptic(true)
                    errorMessage = "Incorrect PIN. Try again."
                    pinInput = ""
                }
            }
        }
    }

    fun onBackspace() {
        triggerHaptic(false)
        if (pinInput.isNotEmpty()) {
            pinInput = pinInput.dropLast(1)
            errorMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CncBackground)
            .statusBarsPadding()
            .testTag("biometric_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        CadGridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CncBorderBright),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secured",
                            tint = CncPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CNC CRAFT SECURITY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = CncTextPrimary
                )

                Text(
                    text = "Biometric & Vault Protection",
                    style = MaterialTheme.typography.bodySmall,
                    color = CncTextSecondary
                )
            }

            // Central Biometric Scanner & PIN Dots
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BiometricRadarScanner(
                    isScanning = true,
                    onClick = { launchBiometricPrompt() }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PIN Indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) CncPrimary else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) CncLaserCyan else CncBorder,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (errorMessage == null) {
                    Text(
                        text = "Touch scanner or enter 4-digit PIN",
                        style = MaterialTheme.typography.labelSmall,
                        color = CncTextSecondary
                    )
                }
            }

            // Futuristic Keypad
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            when (key) {
                                "BIO" -> {
                                    Surface(
                                        shape = CircleShape,
                                        color = CncSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                                        shadowElevation = 1.dp,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clickable { launchBiometricPrompt() }
                                            .testTag("keypad_bio")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Scan Fingerprint",
                                                tint = CncPrimary,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                }
                                "DEL" -> {
                                    Surface(
                                        shape = CircleShape,
                                        color = CncSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                                        shadowElevation = 1.dp,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clickable { onBackspace() }
                                            .testTag("keypad_del")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "Backspace",
                                                tint = CncTextSecondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = CircleShape,
                                        color = CncSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                                        shadowElevation = 2.dp,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clickable { onDigitPress(key) }
                                            .testTag("keypad_$key")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 24.sp
                                                ),
                                                color = CncTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Default PIN: 1234 • Tap scanner to unlock",
                    style = MaterialTheme.typography.labelSmall,
                    color = CncTextSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
