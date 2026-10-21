package com.example.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class SecurityManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cnc_security_prefs", Context.MODE_PRIVATE)

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean("biometric_enabled", false)
        set(value) = prefs.edit().putBoolean("biometric_enabled", value).apply()

    var pinCode: String
        get() = prefs.getString("security_pin", "1234") ?: "1234"
        set(value) = prefs.edit().putString("security_pin", value).apply()

    var currencySymbol: String
        get() = prefs.getString("currency_symbol", "$") ?: "$"
        set(value) = prefs.edit().putString("currency_symbol", value).apply()

    var designerName: String
        get() = prefs.getString("designer_name", "CNC Master Studio") ?: "CNC Master Studio"
        set(value) = prefs.edit().putString("designer_name", value).apply()

    fun canAuthenticateWithBiometrics(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun promptBiometricAuth(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Fingerprint not recognized. Try again or enter PIN.")
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("CNC Craft Security")
            .setSubtitle("Authenticate via Biometrics to access your CNC income records")
            .setNegativeButtonText("Use Security PIN")
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }

    fun verifyPin(input: String): Boolean {
        return input == pinCode
    }
}
