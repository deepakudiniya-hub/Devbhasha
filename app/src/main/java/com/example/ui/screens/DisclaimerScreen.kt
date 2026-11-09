package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.utils.UserSession

/**
 * Backward-compatible wrapper for DisclaimerScreen.
 * Delegates directly to the newly merged DpdpDisclaimerScreen.
 */
@Composable
fun DisclaimerScreen(
    userId: String,
    userSession: UserSession,
    onDisclaimerAccepted: () -> Unit,
    modifier: Modifier = Modifier
) {
    DpdpDisclaimerScreen(
        userId = userId,
        userSession = userSession,
        onDisclaimerAccepted = onDisclaimerAccepted,
        modifier = modifier
    )
}
