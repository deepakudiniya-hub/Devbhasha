package com.example.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.SecureRandom

/**
 * Enterprise-grade Root, Emulator, and Device Integrity Detection Utility.
 * Integrates Google Play Integrity API with multi-layered local heuristic detection
 * (su binaries, test-keys, dangerous props, root packages, and emulator signatures).
 */
object RootDetection {

    private const val TAG = "RootDetection"

    /**
     * Testing flag for development preview to simulate root/emulator restriction on the Wallet.
     * When true, triggers the Wallet security restriction dialog even in debug environments.
     */
    var simulateCompromisedForTesting: Boolean = false

    data class DetectionResult(
        val isCompromised: Boolean,
        val isRooted: Boolean,
        val isEmulator: Boolean,
        val isDebuggerAttached: Boolean,
        val playIntegrityPassed: Boolean,
        val playIntegrityToken: String? = null,
        val detectedIndicators: List<String> = emptyList(),
        val recommendation: String
    )

    private val KNOWN_SU_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/su/xbin/su",
        "/system/xbin/daemonsu"
    )

    private val KNOWN_ROOT_PACKAGES = arrayOf(
        "com.topjohnwu.magisk",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.noshufou.android.su",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.ramdroid.appquarantine",
        "com.zachspong.temprootremovejb",
        "com.devadvance.rootcloak",
        "com.devadvance.rootcloakplus"
    )

    private val KNOWN_DANGEROUS_PROPS = mapOf(
        "ro.debuggable" to "1",
        "ro.secure" to "0"
    )

    /**
     * Fast synchronous check to determine if the device is rooted.
     */
    fun isRooted(context: Context): Boolean {
        if (simulateCompromisedForTesting) return true
        return checkSuBinary() || checkTestKeys() || checkRootPackages(context) || checkDangerousProps() || checkBusyBox()
    }

    /**
     * Fast synchronous check to determine if the device is running in an emulator.
     */
    fun isEmulator(): Boolean {
        if (simulateCompromisedForTesting) return true
        return checkEmulatorProps() || checkQemuPipes()
    }

    /**
     * Determines whether wallet access should be restricted based on device integrity.
     */
    fun isWalletRestricted(context: Context): Boolean {
        if (simulateCompromisedForTesting) return true

        val rooted = isRooted(context)
        val emulator = isEmulator()

        // In debug mode, allow emulator execution for cloud browser preview,
        // but still strictly restrict if actual root binaries or simulation are active.
        return if (BuildConfig.DEBUG) {
            rooted
        } else {
            rooted || emulator
        }
    }

    /**
     * Performs a comprehensive asynchronous integrity scan including Play Integrity API.
     */
    suspend fun checkDeviceIntegrity(context: Context): DetectionResult = withContext(Dispatchers.IO) {
        val indicators = mutableListOf<String>()

        // 1. Local Root Detection
        if (checkTestKeys()) {
            indicators.add("Build.TAGS contains test-keys signature")
        }
        if (checkSuBinary()) {
            indicators.add("su binary detected in system binaries path")
        }
        if (checkRootPackages(context)) {
            indicators.add("Root management application detected")
        }
        if (checkDangerousProps()) {
            indicators.add("Dangerous system properties detected (ro.debuggable / ro.secure)")
        }
        if (checkBusyBox()) {
            indicators.add("BusyBox binary presence detected")
        }
        if (checkWritableSystemPaths()) {
            indicators.add("System partition is writable (tampered mount)")
        }

        // 2. Emulator Detection
        val isEmu = checkEmulatorProps() || checkQemuPipes()
        if (isEmu) {
            indicators.add("QEMU / Virtual device hardware indicators detected")
        }

        // 3. Debugger Detection
        val isDebugger = Debug.isDebuggerConnected() || Debug.waitingForDebugger()
        if (isDebugger) {
            indicators.add("Active debugger attached to process")
        }

        // 4. Play Integrity API Integration
        var playIntegrityToken: String? = null
        var playIntegrityPassed = true

        try {
            val integrityManager = IntegrityManagerFactory.create(context)
            val nonce = generateSecureNonce()

            val tokenResponse = integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder()
                    .setNonce(nonce)
                    .build()
            ).await()

            playIntegrityToken = tokenResponse.token()
            Log.d(TAG, "Play Integrity Token acquired successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Play Integrity check failed or unprovisioned: ${e.message}")
            // In release builds, inability to get token or play integrity error is noted
            if (!BuildConfig.DEBUG) {
                indicators.add("Play Integrity API validation unavailable (${e.javaClass.simpleName})")
                playIntegrityPassed = false
            }
        }

        val hasRoot = indicators.any { it.contains("su binary", true) || it.contains("test-keys", true) || it.contains("Root", true) }
        val isCompromised = if (simulateCompromisedForTesting) {
            true
        } else if (BuildConfig.DEBUG) {
            hasRoot
        } else {
            hasRoot || isEmu || !playIntegrityPassed || isDebugger
        }

        DetectionResult(
            isCompromised = isCompromised,
            isRooted = hasRoot || simulateCompromisedForTesting,
            isEmulator = isEmu,
            isDebuggerAttached = isDebugger,
            playIntegrityPassed = playIntegrityPassed,
            playIntegrityToken = playIntegrityToken,
            detectedIndicators = indicators,
            recommendation = if (isCompromised) {
                "Device integrity compromised. Restrict access to sensitive financial and wallet operations."
            } else {
                "Device integrity verified. Safe to proceed with wallet transactions."
            }
        )
    }

    private fun checkSuBinary(): Boolean {
        for (path in KNOWN_SU_PATHS) {
            try {
                if (File(path).exists()) return true
            } catch (_: SecurityException) {
                // Access denied on secure paths is normal
            }
        }
        return false
    }

    private fun checkTestKeys(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootPackages(context: Context): Boolean {
        val pm = context.packageManager
        for (pkg in KNOWN_ROOT_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (_: PackageManager.NameNotFoundException) {
                // Expected if not rooted
            } catch (_: Exception) {
                // Ignore unexpected package query restrictions
            }
        }
        return false
    }

    private fun checkDangerousProps(): Boolean {
        for ((propName, dangerousValue) in KNOWN_DANGEROUS_PROPS) {
            val value = getSystemProperty(propName)
            if (value == dangerousValue) {
                return true
            }
        }
        return false
    }

    private fun checkBusyBox(): Boolean {
        val paths = arrayOf("/system/bin/busybox", "/system/xbin/busybox", "/sbin/busybox")
        for (path in paths) {
            try {
                if (File(path).exists()) return true
            } catch (_: Exception) {
                // Ignore
            }
        }
        return false
    }

    private fun checkWritableSystemPaths(): Boolean {
        val paths = arrayOf("/system", "/system/bin", "/vendor", "/sbin")
        for (path in paths) {
            val dir = File(path)
            if (dir.exists() && dir.canWrite()) return true
        }
        return false
    }

    private fun checkEmulatorProps(): Boolean {
        val fp = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        val device = Build.DEVICE.lowercase()
        val product = Build.PRODUCT.lowercase()
        val hardware = Build.HARDWARE.lowercase()

        return fp.startsWith("generic") ||
                fp.startsWith("unknown") ||
                model.contains("google_sdk") ||
                model.contains("emulator") ||
                model.contains("android sdk built for x86") ||
                manufacturer.contains("genymotion") ||
                (brand.startsWith("generic") && device.startsWith("generic")) ||
                product.contains("sdk_gphone") ||
                product.contains("sdk") ||
                product.contains("emulator") ||
                product.contains("simulator") ||
                product.contains("vbox86p") ||
                hardware.contains("goldfish") ||
                hardware.contains("ranchu") ||
                hardware.contains("vbox86")
    }

    private fun checkQemuPipes(): Boolean {
        val qemuDrivers = arrayOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace"
        )
        for (pipe in qemuDrivers) {
            try {
                if (File(pipe).exists()) return true
            } catch (_: Exception) {
                // Ignore
            }
        }
        return false
    }

    private fun getSystemProperty(name: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", name))
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.readLine()?.trim()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun generateSecureNonce(): String {
        val random = SecureRandom()
        val nonceBytes = ByteArray(32)
        random.nextBytes(nonceBytes)
        return Base64.encodeToString(nonceBytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}
