package com.example.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.util.Log
import com.example.BuildConfig
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Production-grade Root, Emulator, and Device Integrity Detection Suite.
 * Detects compromised runtime environments without breaking browser-based development emulators in Debug mode.
 */
object DeviceIntegrityManager {

    private const val TAG = "DeviceIntegrityManager"

    data class IntegrityEvaluation(
        val isRooted: Boolean,
        val isEmulator: Boolean,
        val isDebuggerAttached: Boolean,
        val rootIndicators: List<String>,
        val emulatorIndicators: List<String>,
        val isEnvironmentSafe: Boolean
    )

    private val SU_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )

    private val ROOT_PACKAGES = arrayOf(
        "com.noshufou.android.su",
        "com.thirdparty.superuser",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.zachspong.temprootremovejb",
        "com.ramdroid.appquarantine",
        "com.topjohnwu.magisk"
    )

    /**
     * Comprehensive device integrity check.
     */
    fun evaluateDeviceIntegrity(context: Context): IntegrityEvaluation {
        val rootIndicators = mutableListOf<String>()
        val emulatorIndicators = mutableListOf<String>()

        // 1. Root Checks
        if (checkTestKeys()) {
            rootIndicators.add("Build.TAGS contains test-keys")
        }
        if (checkSuBinary()) {
            rootIndicators.add("su binary located on filesystem")
        }
        if (checkRootPackages(context)) {
            rootIndicators.add("Root management package detected")
        }
        if (checkDangerousProps()) {
            rootIndicators.add("Dangerous system properties detected (ro.debuggable / ro.secure)")
        }

        // 2. Emulator Checks
        checkEmulatorBuildProps(emulatorIndicators)
        if (checkQemuPipes()) {
            emulatorIndicators.add("QEMU communication pipes/socket present")
        }

        // 3. Debugger Check
        val isDebuggerAttached = Debug.isDebuggerConnected() || Debug.waitingForDebugger()

        val isRooted = rootIndicators.isNotEmpty()
        val isEmulator = emulatorIndicators.isNotEmpty()

        // In debug builds, emulator presence is expected (cloud streaming emulator).
        // Only consider the environment unsafe in release builds or when root tampering is confirmed.
        val isEnvironmentSafe = if (BuildConfig.DEBUG) {
            !isRooted
        } else {
            !isRooted && !isDebuggerAttached
        }

        val evaluation = IntegrityEvaluation(
            isRooted = isRooted,
            isEmulator = isEmulator,
            isDebuggerAttached = isDebuggerAttached,
            rootIndicators = rootIndicators,
            emulatorIndicators = emulatorIndicators,
            isEnvironmentSafe = isEnvironmentSafe
        )

        logIntegritySummary(evaluation)
        return evaluation
    }

    private fun checkTestKeys(): Boolean {
        val tags = Build.TAGS
        return tags != null && tags.contains("test-keys")
    }

    private fun checkSuBinary(): Boolean {
        for (path in SU_PATHS) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }

    private fun checkRootPackages(context: Context): Boolean {
        val pm = context.packageManager
        for (pkg in ROOT_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (_: PackageManager.NameNotFoundException) {
                // Expected when package is not installed
            }
        }
        return false
    }

    private fun checkDangerousProps(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getprop ro.debuggable")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            reader.close()
            process.destroy()
            line == "1" && !BuildConfig.DEBUG
        } catch (_: Exception) {
            false
        }
    }

    private fun checkEmulatorBuildProps(indicators: MutableList<String>) {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        val device = Build.DEVICE.lowercase()
        val product = Build.PRODUCT.lowercase()
        val hardware = Build.HARDWARE.lowercase()

        if (fingerprint.startsWith("generic") || fingerprint.startsWith("unknown") ||
            fingerprint.contains("google/sdk_gphone") || fingerprint.contains("vbox")
        ) {
            indicators.add("Fingerprint indicates emulator: $fingerprint")
        }

        if (model.contains("google_sdk") || model.contains("emulator") ||
            model.contains("android sdk built for")
        ) {
            indicators.add("Model indicates emulator: $model")
        }

        if (hardware.contains("goldfish") || hardware.contains("ranchu")) {
            indicators.add("Hardware indicates QEMU: $hardware")
        }

        if (manufacturer.contains("genymotion") || brand.startsWith("generic") && device.startsWith("generic")) {
            indicators.add("Manufacturer/brand indicates generic VM: $manufacturer")
        }
    }

    private fun checkQemuPipes(): Boolean {
        val qemuPipes = arrayOf("/dev/socket/qemud", "/dev/qemu_pipe")
        return qemuPipes.any { File(it).exists() }
    }

    private fun logIntegritySummary(evaluation: IntegrityEvaluation) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Device Integrity Checked: Rooted=${evaluation.isRooted}, Emulator=${evaluation.isEmulator}, Debugger=${evaluation.isDebuggerAttached}")
            if (evaluation.rootIndicators.isNotEmpty()) {
                Log.w(TAG, "Root Indicators: ${evaluation.rootIndicators}")
            }
            if (evaluation.emulatorIndicators.isNotEmpty()) {
                Log.i(TAG, "Emulator Indicators: ${evaluation.emulatorIndicators}")
            }
        } else {
            if (!evaluation.isEnvironmentSafe) {
                Log.w(TAG, "Security Alert: Compromised runtime detected in release build.")
            }
        }
    }
}
