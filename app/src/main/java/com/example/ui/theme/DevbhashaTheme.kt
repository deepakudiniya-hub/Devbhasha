package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

// ==========================================
// DEVBHASHA BRAND: TEAL #0D656C + GOLD #D0BF75 + CREAM #FAF6EA
// ==========================================

// Backgrounds:
// --bg-primary: #FFFFFF (pure white, all screens)
// --bg-secondary: #FAFAFA (search bars, input fields, inactive pills)
// --bg-card: #FFFFFF with 1px border #EFEFEF and shadow 0 2px 12px rgba(0,0,0,0.05)
val BgPrimary = DevColorTokens.brandCream
val BgSecondary = DevColorTokens.bgSecondary
val BgCard = DevColorTokens.bgCard

// Text:
// --text-primary: #1F2937 (headings, names, prices)
// --text-secondary: #49454F (subtitles, expertise, bio, timestamps)
// --text-tertiary: #5F6368 (placeholders, disabled)
val TextPrimary = DevColorTokens.textBrandPrimary
val TextSecondary = DevColorTokens.textBrandSecondary
val TextTertiary = DevColorTokens.textBrandTertiary

// Accent (ONLY FOR CTA):
// Soothing Muted Terracotta (#E76F51) & Soft Peach (#F4A261) - Easier on the eyes
val Saffron = DevColorTokens.brandTeal
val SaffronGradientStart = DevColorTokens.brandGold
val SaffronGradientEnd = DevColorTokens.brandTeal
val SaffronLight = DevColorTokens.bgPrimary

// Neutral:
// --black-pill-active: #000000 (active filter pill text white)
// --border-light: #EFEFEF
// --border-medium: #DBDBDB (bottom nav top border)
// --gray-nav-pill: #F5F5F5
val BlackPillActive = DevColorTokens.neutralPillActive
val BorderLight = DevColorTokens.borderLight
val BorderMedium = DevColorTokens.borderMedium
val GrayNavPill = DevColorTokens.neutralNavPill

// Compatibility tokens mapped strictly to Pure White + Black + Saffron
val StageBg = DevColorTokens.brandCream
val PaperBg = DevColorTokens.brandCream
val PaperDeep = DevColorTokens.bgSecondary
val PaperCard = DevColorTokens.bgCard
val CardBg = DevColorTokens.bgCard
val Background = DevColorTokens.brandCream
val PageBg = DevColorTokens.bgSecondary

val Ink = DevColorTokens.textPrimary
val InkPrimary = DevColorTokens.textPrimary
val InkSoft = DevColorTokens.textSecondary
val InkSecondary = DevColorTokens.textSecondary
val InkFaint = DevColorTokens.textTertiary
val TextDark = DevColorTokens.textPrimary
val TextLight = DevColorTokens.textInverse
val TextMuted = DevColorTokens.textSecondary

val Terra = DevColorTokens.brandTeal
val TerraDeep = DevColorTokens.brandTealDeep
val DevOrange = DevColorTokens.brandTeal
val SaffronPrimary = DevColorTokens.brandTeal
val SaffronDeep = DevColorTokens.brandTealDeep
val SaffronGold = DevColorTokens.brandGold
val SaffronSoftBg = DevColorTokens.brandCream
val GoldStamp = DevColorTokens.brandTeal
val GoldStampSoft = DevColorTokens.brandCream
val Sage = DevColorTokens.statusSuccess
val SageDeep = DevColorTokens.statusSuccessDeep
val GreenPrimary = DevColorTokens.statusSuccess

val EditorialLine = DevColorTokens.borderLight
val EditorialLineStrong = DevColorTokens.borderMedium
val Border = DevColorTokens.borderLight

// Legacy Brand Palette
val CosmicBlack = DevColorTokens.textPrimary
val CosmicIndigo = DevColorTokens.textPrimary
val GlassBackground = DevColorTokens.bgPrimary
val GlassBorder = DevColorTokens.borderLight

// Shapes
val ShapeCard = RoundedCornerShape(DevRadiusTokens.themeCard)
val ShapeDialog = RoundedCornerShape(DevRadiusTokens.themeDialog)
val ShapePill = RoundedCornerShape(DevRadiusTokens.themePill)
val ShapeChip = RoundedCornerShape(DevRadiusTokens.themeChip)

val GoogleBlue = DevColorTokens.accentGoogleBlue
val BackgroundColor = DevColorTokens.brandCream
val SurfaceColor = DevColorTokens.bgCard
val BorderColor = DevColorTokens.borderOutline
val TextColor = DevColorTokens.textBody // Google-like dark text

private val DevbhashaColorScheme = lightColorScheme(
    primary = DevOrange,
    onPrimary = DevColorTokens.textInverse,
    background = BackgroundColor,
    onBackground = TextColor,
    surface = SurfaceColor,
    onSurface = TextColor,
    surfaceVariant = DevColorTokens.bgSurfaceVariant,
    onSurfaceVariant = TextColor,
    outline = BorderColor
)

private val DarkColorScheme = darkColorScheme(
    primary = DevOrange,
    onPrimary = DevColorTokens.textPrimary,
    background = DevColorTokens.textPrimary,
    onBackground = DevColorTokens.textInverse,
    surface = DevColorTokens.darkSurface,
    onSurface = DevColorTokens.textInverse,
    outline = DevColorTokens.darkOutline
)

val AppFontFamily = FontFamily(
    Font(R.font.poppins, FontWeight.Normal),
    Font(R.font.noto_sans_devanagari, FontWeight.Normal)
)

val RobotoFontFamily = AppFontFamily

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 57.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-0.25).sp,
        color = TextColor
    ),
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 32.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextColor
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextColor
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextColor
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        color = TextColor
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextColor
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = TextColor
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        color = TextColor
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        color = TextColor
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        color = TextColor
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = TextColor
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = TextColor
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = TextColor
    )
)

@Composable
fun DevbhashaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DevbhashaColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// ==========================================
// SEMANTIC TOKEN LAYER (UI consistency pass)
// Values match the colours already used across screens, so migrating a
// screen from a literal to a token does NOT change its look — it only
// removes the 130 one-off colours and centralises them here.
// ==========================================

// Neutrals (the slate scale the screens already use)
val Neutral900 = DevColorTokens.neutralSlate900
val Neutral800 = DevColorTokens.neutralSlate800
val Neutral700 = DevColorTokens.neutralSlate700
val Neutral600 = DevColorTokens.neutralSlate600
val Neutral500 = DevColorTokens.neutralSlate500
val Neutral400 = DevColorTokens.neutralSlate400
val Neutral300 = DevColorTokens.neutralSlate300
val Neutral200 = DevColorTokens.neutralSlate200
val Neutral100 = DevColorTokens.neutralSlate100
val Neutral50  = DevColorTokens.neutralSlate50
val SurfaceWhite = DevColorTokens.bgPrimary
val SurfaceAlt   = DevColorTokens.bgSecondary
val BorderSoft   = DevColorTokens.borderSoft

// Brand — Muted Terracotta & Soft Peach
val SaffronBase = DevColorTokens.brandTeal
val SaffronDeep2 = DevColorTokens.brandTealDeep
val SaffronSoft = DevColorTokens.brandCream
val SaffronSoftAlt = DevColorTokens.brandCream

// Success — green
val GreenBase = DevColorTokens.statusSuccess
val GreenDeepBase = DevColorTokens.statusSuccessDeep
val GreenSoftBase = DevColorTokens.statusSuccessSoft
val GreenLightBase = DevColorTokens.statusSuccessLight
val EmeraldBase = DevColorTokens.statusEmerald

// Danger — red
val DangerRed = DevColorTokens.statusDanger
val DangerRedDeep = DevColorTokens.statusDangerDeep
val DangerSoft = DevColorTokens.statusDangerSoft

// Warning — amber
val WarningAmberBase = DevColorTokens.brandGold
val WarningAmberDeep = DevColorTokens.brandTealDeep
val WarningSoft = DevColorTokens.statusWarningSoft

// Accents
val AccentViolet = DevColorTokens.accentViolet
val AccentVioletSoft = DevColorTokens.accentVioletSoft
val AccentBlue = DevColorTokens.accentBlue
val AccentBlueSoft = DevColorTokens.accentBlueSoft

