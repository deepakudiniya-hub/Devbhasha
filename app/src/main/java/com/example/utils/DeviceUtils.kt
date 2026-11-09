package com.example.utils

import android.os.Build
import java.io.File

object DeviceUtils {

    /**
     * Accurately determines if the app is currently running on an emulator
     * (e.g. streaming Android emulator, cloud container, QEMU/Ranchu, Genymotion, or SDK image).
     */
    fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        val device = Build.DEVICE.lowercase()
        val product = Build.PRODUCT.lowercase()
        val hardware = Build.HARDWARE.lowercase()
        val board = Build.BOARD.lowercase()

        return fingerprint.startsWith("generic") ||
                fingerprint.startsWith("unknown") ||
                fingerprint.contains("google/sdk_gphone") ||
                fingerprint.contains("sdk_gphone") ||
                fingerprint.contains("vbox") ||
                fingerprint.contains("emulator") ||
                model.contains("google_sdk") ||
                model.contains("emulator") ||
                model.contains("android sdk built for") ||
                model.contains("sdk_gphone") ||
                model.contains("subsystem for android") ||
                hardware.contains("goldfish") ||
                hardware.contains("ranchu") ||
                board.contains("goldfish") ||
                board.contains("ranchu") ||
                product.contains("sdk_gphone") ||
                product.contains("google_sdk") ||
                product.contains("sdk") ||
                product.contains("vbox") ||
                product.contains("simulator") ||
                manufacturer.contains("genymotion") ||
                (brand.startsWith("generic") && device.startsWith("generic")) ||
                try {
                    File("/dev/socket/qemud").exists() || File("/dev/qemu_pipe").exists()
                } catch (_: Exception) {
                    false
                }
    }
}
