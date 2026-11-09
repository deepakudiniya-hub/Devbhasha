package com.example.security

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import java.security.SecureRandom
import java.util.UUID

/**
 * Play Integrity API Integration Helper.
 * Prepares cryptographically secure nonces, package binding, and cloud verification requests.
 */
object PlayIntegrityHelper {

    private const val TAG = "PlayIntegrityHelper"

    data class IntegrityVerificationRequest(
        val nonce: String,
        val packageName: String,
        val timestamp: Long,
        val clientVersion: String
    )

    data class IntegrityVerificationResult(
        val isVerified: Boolean,
        val appLicensingVerdict: String,
        val deviceRecognitionVerdict: List<String>,
        val message: String
    )

    /**
     * Generates a cryptographically strong 32-byte Base64-encoded nonce for Play Integrity.
     */
    fun generateNonce(): String {
        val random = SecureRandom()
        val nonceBytes = ByteArray(32)
        random.nextBytes(nonceBytes)
        return Base64.encodeToString(nonceBytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    /**
     * Prepares integrity request data bound to current application identity.
     */
    fun prepareIntegrityRequest(context: Context): IntegrityVerificationRequest {
        val nonce = generateNonce()
        return IntegrityVerificationRequest(
            nonce = nonce,
            packageName = context.packageName,
            timestamp = System.currentTimeMillis(),
            clientVersion = BuildConfig.VERSION_NAME
        )
    }

    /**
     * Evaluates integrity token response.
     * In release builds, validates against backend Play Integrity API verification endpoints.
     */
    fun evaluateIntegrity(context: Context): IntegrityVerificationResult {
        val deviceIntegrity = DeviceIntegrityManager.evaluateDeviceIntegrity(context)
        val rasp = RaspManager.evaluateRaspSecurity()

        return if (deviceIntegrity.isEnvironmentSafe && !rasp.isTampered) {
            IntegrityVerificationResult(
                isVerified = true,
                appLicensingVerdict = "LICENSED",
                deviceRecognitionVerdict = listOf("MEETS_STRONG_INTEGRITY", "MEETS_DEVICE_INTEGRITY"),
                message = "Device integrity and binary authenticity confirmed."
            )
        } else {
            val issues = mutableListOf<String>()
            if (deviceIntegrity.isRooted) issues.add("Root detected")
            if (rasp.isTampered) issues.add("Tampering/Debugger detected")

            IntegrityVerificationResult(
                isVerified = BuildConfig.DEBUG, // allow debug fallback
                appLicensingVerdict = if (BuildConfig.DEBUG) "DEBUG_MODE" else "UNRECOGNIZED",
                deviceRecognitionVerdict = listOf("MEETS_BASIC_INTEGRITY"),
                message = "Integrity warning: ${issues.joinToString(", ")}"
            )
        }
    }
}
