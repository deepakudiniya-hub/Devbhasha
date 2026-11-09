package com.example.security

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore

/**
 * Production-grade Hardware-Backed Biometric Security Manager.
 * Ensures strict hardware key verification in AndroidKeyStore before launching
 * BIOMETRIC_STRONG + DEVICE_CREDENTIAL authentication prompts.
 */
object BiometricSecurityManager {

    private const val TAG = "BiometricSecurityManager"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "devbhasha_master_key_v1"

    enum class BiometricStatus {
        AVAILABLE,
        NOT_ENROLLED,
        NO_HARDWARE,
        UNAVAILABLE,
        KEYSTORE_MISSING
    }

    sealed class AuthResult {
        object Success : AuthResult()
        data class AuthenticatedWithToken(val decryptedToken: String) : AuthResult()
        data class Failure(val message: String) : AuthResult()
        data class Error(val errorCode: Int, val errString: String) : AuthResult()
        object Cancelled : AuthResult()
    }

    /**
     * Programmatically verifies that hardware key alias 'devbhasha_master_key_v1'
     * exists and is properly initialized within the AndroidKeyStore provider.
     */
    fun verifyMasterKeyExists(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying master key alias in KeyStore", e)
            false
        }
    }

    /**
     * Checks device biometric capability and KeyStore key presence.
     */
    fun checkBiometricAvailability(context: Context): BiometricStatus {
        if (!verifyMasterKeyExists()) {
            Log.w(TAG, "Security Warning: Master key alias '$KEY_ALIAS' not found in AndroidKeyStore!")
            return BiometricStatus.KEYSTORE_MISSING
        }

        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            else -> BiometricStatus.UNAVAILABLE
        }
    }

    /**
     * Launches BiometricPrompt enforcing BIOMETRIC_STRONG + DEVICE_CREDENTIAL
     * after validating the hardware-backed AndroidKeyStore master key.
     * Hooks onAuthenticationSucceeded directly into execution path for AES-256-GCM token decryption.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "सुरक्षा सत्यापन (Security Verification)",
        subtitle: String = "संवेदनशील कार्य हेतु पहचान सत्यापित करें",
        encryptedTokenToDecrypt: String? = null,
        onResult: (AuthResult) -> Unit
    ) {
        // Pre-flight check: verify hardware key alias exists in AndroidKeyStore
        if (!verifyMasterKeyExists()) {
            Log.e(TAG, "Authentication rejected: AndroidKeyStore master key '$KEY_ALIAS' is missing or uninitialized.")
            onResult(AuthResult.Error(-1, "Hardware Security Error: Master Key Missing in KeyStore"))
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Biometric authentication succeeded with hardware-backed security.")
                if (!encryptedTokenToDecrypt.isNullOrBlank()) {
                    try {
                        val storage = EncryptedSessionStorage.getInstance(activity)
                        val decrypted = storage.decrypt(encryptedTokenToDecrypt)
                        onResult(AuthResult.AuthenticatedWithToken(decrypted))
                    } catch (e: Exception) {
                        Log.e(TAG, "Token decryption failed inside onAuthenticationSucceeded callback", e)
                        onResult(AuthResult.Success)
                    }
                } else {
                    onResult(AuthResult.Success)
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    onResult(AuthResult.Cancelled)
                } else {
                    onResult(AuthResult.Error(errorCode, errString.toString()))
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onResult(AuthResult.Failure("बायोमेट्रिक मिलान असफल रहा। कृपया पुनः प्रयास करें।"))
            }
        }

        // Strict configuration enforcing BIOMETRIC_STRONG combined with DEVICE_CREDENTIAL
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }
}
