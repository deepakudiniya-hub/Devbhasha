package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.ui.components.DevEmptyState
import com.example.ui.components.DevLoadingState
import com.example.ui.components.DevPrimaryButton
import com.example.ui.components.DevSecondaryButton
import com.example.ui.components.DevSectionHeader
import com.example.ui.models.DreamJournalEntry
import com.example.ui.models.WalletTransaction
import com.example.ui.screens.DreamJournalScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.theme.DevbhashaTheme
import com.example.ui.theme.BgPrimary
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Real UI screenshots — Roborazzi renders the ACTUAL Compose screens on the JVM
 * (no device, no mock). Run with:
 *
 *     ./gradlew recordRoborazziDebug
 *
 * Images land in `app/build/outputs/roborazzi/`. CI uploads them on every PR
 * (see .github/workflows/screenshots.yml). When the design changes on purpose,
 * re-record and commit the new baselines.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ScreenshotTests {

    @get:Rule
    val compose = createComposeRule()

    private val out = "build/outputs/roborazzi"

    @Test
    fun design_system_components() {
        compose.setContent {
            DevbhashaTheme {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(BgPrimary)
                        .padding(16.dp)
                ) {
                    DevSectionHeader(
                        title = "आज का मार्गदर्शन",
                        subtitle = "दैनिक पंचांग और साधक",
                        actionText = "सभी देखें →"
                    )
                    DevPrimaryButton(text = "नई बातचीत शुरू करें", onClick = {})
                    DevSecondaryButton(text = "बाद में", onClick = {})
                    DevEmptyState(
                        title = "अभी कोई सपना नहीं",
                        description = "अपना पहला सपना दर्ज करें और साधक से फल जानें।",
                        actionText = "सपना जोड़ें"
                    )
                    DevLoadingState()
                }
            }
        }
        compose.onRoot().captureRoboImage("$out/design-system.png")
    }

    @Test
    fun splash() {
        compose.setContent { DevbhashaTheme { SplashScreen() } }
        compose.onRoot().captureRoboImage("$out/splash.png")
    }

    @Test
    fun transaction_history() {
        val tx = listOf(
            WalletTransaction("t1", "सपना — आचार्य देव शर्मा", "पूर्ण · आज", 99.0, true),
            WalletTransaction("t2", "वॉलेट रिचार्ज", "Razorpay · कल", 500.0, true),
            WalletTransaction("t3", "बातचीत — योगी आनंद नाथ", "5 मिनट · 2 दिन पहले", 20.0, false)
        )
        compose.setContent {
            DevbhashaTheme { TransactionHistoryScreen(transactions = tx, onBackClick = {}) }
        }
        compose.onRoot().captureRoboImage("$out/transaction-history.png")
    }

    @Test
    fun dream_journal() {
        val dreams = listOf(
            DreamJournalEntry(
                id = "d1", title = "शिवलिंग पर जल", datePhase = "आज",
                description = "सपने में शिवलिंग पर जल चढ़ाते देखा।",
                tag = "शुभ", tags = listOf("शुभ", "आध्यात्मिक")
            )
        )
        compose.setContent {
            DevbhashaTheme { DreamJournalScreen(onBackClick = {}, dreamHistory = dreams) }
        }
        compose.onRoot().captureRoboImage("$out/dream-journal.png")
    }
}
