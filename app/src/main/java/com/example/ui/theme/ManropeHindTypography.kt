package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

/**
 * Approved UI pairing: Manrope Latin + Hind Devanagari in one offline font face.
 * The composite resources retain both scripts' glyphs and Indic shaping on API 24+.
 * See assets/font_licenses for the original SIL Open Font License notices.
 */
internal val ManropeHindFontFamily = FontFamily(
    Font(R.font.manrope_hind_200, FontWeight.ExtraLight),
    Font(R.font.manrope_hind_300, FontWeight.Light),
    Font(R.font.manrope_hind_400, FontWeight.Normal),
    Font(R.font.manrope_hind_500, FontWeight.Medium),
    Font(R.font.manrope_hind_600, FontWeight.SemiBold),
    Font(R.font.manrope_hind_700, FontWeight.Bold),
    Font(R.font.manrope_hind_800, FontWeight.ExtraBold)
)

/** Preserve every existing typography metric and weight; replace the family only. */
internal fun Typography.withManropeHindFontFamily(): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = ManropeHindFontFamily),
    displayMedium = displayMedium.copy(fontFamily = ManropeHindFontFamily),
    displaySmall = displaySmall.copy(fontFamily = ManropeHindFontFamily),
    headlineLarge = headlineLarge.copy(fontFamily = ManropeHindFontFamily),
    headlineMedium = headlineMedium.copy(fontFamily = ManropeHindFontFamily),
    headlineSmall = headlineSmall.copy(fontFamily = ManropeHindFontFamily),
    titleLarge = titleLarge.copy(fontFamily = ManropeHindFontFamily),
    titleMedium = titleMedium.copy(fontFamily = ManropeHindFontFamily),
    titleSmall = titleSmall.copy(fontFamily = ManropeHindFontFamily),
    bodyLarge = bodyLarge.copy(fontFamily = ManropeHindFontFamily),
    bodyMedium = bodyMedium.copy(fontFamily = ManropeHindFontFamily),
    bodySmall = bodySmall.copy(fontFamily = ManropeHindFontFamily),
    labelLarge = labelLarge.copy(fontFamily = ManropeHindFontFamily),
    labelMedium = labelMedium.copy(fontFamily = ManropeHindFontFamily),
    labelSmall = labelSmall.copy(fontFamily = ManropeHindFontFamily)
)

/** Apply the approved family inside the app's existing, otherwise unchanged theme. */
@Composable
internal fun ManropeHindTypography(content: @Composable () -> Unit) {
    val originalTypography = MaterialTheme.typography
    val approvedTypography = remember(originalTypography) {
        originalTypography.withManropeHindFontFamily()
    }
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = approvedTypography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
