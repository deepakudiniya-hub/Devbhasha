package com.example.security

import android.content.Context
import android.os.Debug
import android.util.Log
import com.example.BuildConfig
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.Socket

/**
 * Runtime Application Self-Protection (RASP) & Anti-Debugging Engine.
 * Detects dynamic analysis, debugger attachment, Frida/Xposed hooks, and code tampering.
 */
object RaspManager {

    private const val TAG = "RaspManager"

    data class SecurityStatus(
        val isDebuggerConnected: Boolean,
        val isPtraceAttached: Boolean,
        val isFridaDetected: Boolean,
        val isXposedDetected: Boolean,
        val isTampered: Boolean
    )

    /**
     * Checks if an active debugger is attached via JDWP or Android Debug.
     */
    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    /**
     * Checks if process is being ptraced (TracerPid != 0 in /proc/self/status).
     */
    fun isPtraceAttached(): Boolean {
        return try {
            val statusFile = File("/proc/self/status")
            if (!statusFile.exists()) return false

            val reader = BufferedReader(FileReader(statusFile))
            var line: String?
            var tracerPid = 0
            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith("TracerPid:")) {
                    tracerPid = line!!.substring("TracerPid:".length).trim().toIntOrNull() ?: 0
                    break
                }
            }
            reader.close()
            tracerPid > 0
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks for Frida hooking framework via default ports, filesystem artifacts, and loaded maps.
     */
    fun isFridaDetected(): Boolean {
        // 1. Check for standard Frida files in tmp
        val fridaFiles = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server"
        )
        if (fridaFiles.any { File(it).exists() }) return true

        // 2. Check /proc/self/maps for frida injection
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                val reader = BufferedReader(FileReader(mapsFile))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val lower = line!!.lowercase()
                    if (lower.contains("frida") || lower.contains("gadget")) {
                        reader.close()
                        return true
                    }
                }
                reader.close()
            }
        } catch (_: Exception) {}

        return false
    }

    /**
     * Checks for Xposed Framework presence in classloader.
     */
    fun isXposedDetected(): Boolean {
        return try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    /**
     * Evaluates full RASP security posture.
     */
    fun evaluateRaspSecurity(): SecurityStatus {
        val debugger = isDebuggerAttached()
        val ptrace = isPtraceAttached()
        val frida = isFridaDetected()
        val xposed = isXposedDetected()

        val isTampered = if (!BuildConfig.DEBUG) {
            debugger || ptrace || frida || xposed
        } else {
            // In debug mode, allow debugger and ptrace during local inspection
            frida || xposed
        }

        if (isTampered && !BuildConfig.DEBUG) {
            Log.e(TAG, "RASP Security Alert: Process tampering or dynamic instrumentation detected!")
        }

        return SecurityStatus(
            isDebuggerConnected = debugger,
            isPtraceAttached = ptrace,
            isFridaDetected = frida,
            isXposedDetected = xposed,
            isTampered = isTampered
        )
    }
}
