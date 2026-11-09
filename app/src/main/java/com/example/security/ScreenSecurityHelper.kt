package com.example.security

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Screen Protector & Anti-Snoop Helper.
 * Applies WindowManager.LayoutParams.FLAG_SECURE to sensitive screens (Wallet, Profile, Consultations)
 * to prevent screenshot capture, screen recording, and system recent app thumbnail caching.
 */
object ScreenSecurityHelper {

    /**
     * Toggles FLAG_SECURE on the given Activity's Window.
     */
    fun applyScreenProtection(activity: Activity?, enable: Boolean) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) return
        try {
            if (enable) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        } catch (_: Exception) {
            // Ignore if window is detached
        }
    }

    /**
     * Returns true if FLAG_SECURE is currently active on the window.
     */
    fun isScreenProtected(activity: Activity?): Boolean {
        if (activity == null) return false
        val flags = activity.window.attributes.flags
        return (flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
    }
}

/**
 * Jetpack Compose DisposableEffect to protect a screen during its active composition lifecycle.
 * When entering the screen, FLAG_SECURE is engaged; when navigating away, it is cleaned up.
 */
@Composable
fun SecureScreenEffect(enabled: Boolean = true) {
    val context = LocalContext.current
    DisposableEffect(enabled) {
        val activity = context as? Activity
        if (enabled && activity != null) {
            ScreenSecurityHelper.applyScreenProtection(activity, true)
        }
        onDispose {
            if (enabled && activity != null) {
                ScreenSecurityHelper.applyScreenProtection(activity, false)
            }
        }
    }
}
