package com.example.security

import android.content.Context
import android.os.Build
import android.os.Debug
import android.util.Log
import com.example.BuildConfig
import java.io.File

/**
 * RuntimeSecurityManager checks for common indicators of tampering or active debugging
 * during the application startup sequence, specifically inspecting:
 * 1. Active debugging via [android.os.Debug.isDebuggerConnected] and [android.os.Debug.waitingForDebugger].
 * 2. Common rooting and superuser binaries in /system/bin and related critical system paths.
 * 3. System build properties for test-keys and dangerous signatures.
 */
class RuntimeSecurityManager {

    data class SecurityStatus(
        val isDebuggerConnected: Boolean,
        val isRootDetected: Boolean,
        val isTampered: Boolean,
        val detectedIndicators: List<String>
    )

    companion object {
        private const val TAG = "RuntimeSecurity"

        // Common rooting binaries, particularly focusing on /system/bin as requested
        private val SYSTEM_BIN_ROOT_FILES = arrayOf(
            "/system/bin/su",
            "/system/bin/busybox",
            "/system/bin/failsafe/su",
            "/system/bin/magisk",
            "/system/bin/daemonsu"
        )

        private val ADDITIONAL_SYSTEM_ROOT_PATHS = arrayOf(
            "/system/xbin/su",
            "/system/xbin/busybox",
            "/sbin/su",
            "/su/bin/su",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/system/app/Superuser.apk"
        )

        /**
         * Singleton-style entry point for startup integration.
         */
        fun checkSecurity(context: Context): SecurityStatus {
            return RuntimeSecurityManager().performStartupCheck(context)
        }
    }

    /**
     * Checks if a debugger is currently connected or actively waiting for attachment.
     */
    fun isDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    /**
     * Checks for common rooting and superuser files, specifically in /system/bin and critical paths.
     */
    fun hasRootingFiles(): Boolean {
        for (path in SYSTEM_BIN_ROOT_FILES) {
            try {
                val file = File(path)
                if (file.exists()) {
                    Log.w(TAG, "Root file detected in /system/bin: $path")
                    return true
                }
            } catch (_: SecurityException) {
                // Access restricted by SELinux is normal on uncompromised paths
            }
        }

        for (path in ADDITIONAL_SYSTEM_ROOT_PATHS) {
            try {
                val file = File(path)
                if (file.exists()) {
                    Log.w(TAG, "Root file detected in system path: $path")
                    return true
                }
            } catch (_: SecurityException) {
                // Ignore
            }
        }

        return false
    }

    /**
     * Checks if OS build tags indicate a test-keys or custom ROM environment.
     */
    fun hasTestKeys(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    /**
     * Runs all runtime security diagnostics and returns a comprehensive [SecurityStatus].
     */
    fun performStartupCheck(context: Context): SecurityStatus {
        val indicators = mutableListOf<String>()

        val debuggerAttached = isDebuggerConnected()
        if (debuggerAttached) {
            indicators.add("Active debugger attached (Debug.isDebuggerConnected)")
        }

        val rootDetected = hasRootingFiles()
        if (rootDetected) {
            indicators.add("Common root binaries identified in /system/bin or system paths")
        }

        val testKeysDetected = hasTestKeys()
        if (testKeysDetected) {
            indicators.add("OS build tags contain test-keys")
        }

        // In debug builds, don't flag the environment as maliciously tampered for development debuggers
        val isTampered = if (BuildConfig.DEBUG) {
            rootDetected
        } else {
            debuggerAttached || rootDetected || testKeysDetected
        }

        val status = SecurityStatus(
            isDebuggerConnected = debuggerAttached,
            isRootDetected = rootDetected,
            isTampered = isTampered,
            detectedIndicators = indicators
        )

        if (status.isTampered) {
            Log.w(TAG, "Runtime security check flagged indicators: ${indicators.joinToString("; ")}")
        } else {
            Log.i(TAG, "Runtime security check passed successfully on startup.")
        }

        return status
    }
}
