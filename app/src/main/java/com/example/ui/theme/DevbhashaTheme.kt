package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================
// PURE WHITE + BLACK + SAFFRON DESIGN SYSTEM
//
// MIGRATED: every colour below now points at a generated token
// (DevColorTokens, from design/tokens.json). Values are unchanged, so the
// look does not move — but the palette now has ONE source of truth shared
// with Figma. To change a colour, edit design/tokens.json and regenerate.
// ==========================================

// Backgrounds
val BgPrimary = DevColorTokens.bgPrimary
val BgSecondary = DevColorTokens.bgSecondary
val BgCard = DevColorTokens.bgCard

// Text
val TextPrimary = DevColorTokens.textPrimary
val TextSecondary = DevColorTokens.textSecondary
val TextTertiary = DevColorTokens.textTertiary

// Accent (ONLY FOR CTA) — strict pure Saffron
val Saffron = DevColorTokens.accentSaffron
val SaffronGradientStart = DevColorTokens.accentSaffronGradientStart
val SaffronGradientEnd = DevColorTokens.accentSaffronGradientEnd
val SaffronLight = DevColorTokens.bgPrimary

// Neutral
val BlackPillActive = DevColorTokens.neutralPillActive
val BorderLight = DevColorTokens.borderLight
val BorderMedium = DevColorTokens.borderMedium
val GrayNavPill = DevColorTokens.neutralNavPill

// Compatibility tokens
val StageBg = DevColorTokens.bgPrimary
val PaperBg = DevColorTokens.bgPrimary
val PaperDeep = DevColorTokens.bgSecondary
val PaperCard = DevColorTokens.bgPrimary
val CardBg = DevColorTokens.bgPrimary
val Background = DevColorTokens.bgPrimary
val PageBg = DevColorTokens.bgSecondary

val Ink = DevColorTokens.textPrimary
val InkPrimary = DevColorTokens.textPrimary
val InkSoft = DevColorTokens.textSecondary
val InkSecondary = DevColorTokens.textSecondary
val InkFaint = DevColorTokens.textTertiary
val TextDark = DevColorTokens.textPrimary
val TextLight = DevColorTokens.textInverse
val TextMuted = DevColorTokens.textSecondary

val Terra = DevColorTokens.accentSaffron
val TerraDeep = DevColorTokens.accentSaffron
val DevOrange = DevColorTokens.accentSaffron
val SaffronPrimary = DevColorTokens.accentSaffron
val SaffronDeep = DevColorTokens.accentSaffron
val SaffronGold = DevColorTokens.accentSaffronGradientStart
val SaffronSoftBg = DevColorTokens.bgPrimary
val GoldStamp = DevColorTokens.accentSaffron
val GoldStampSoft = DevColorTokens.bgPrimary
val Sage = DevColorTokens.statusSuccess
val SageDeep = DevColorTokens.statusSuccessDeep
val GreenPrimary = DevColorTokens.statusSuccess

val EditorialLine = DevColorTokens.borderLight
val EditorialLineStrong = DevColorTokens.borderMedium
val Border = DevColorTokens.borderLight

// Legacy brand palette (aliased to tokens)
val CosmicBlack = DevColorTokens.textPrimary
val CosmicIndigo = DevColorTokens.textPrimary
val NeonViolet = DevColorTokens.accentSaffron
val NeonIndigo = DevColorTokens.accentSaffron
val GlassBackground = DevColorTokens.bgPrimary
val GlassBorder = DevColorTokens.borderLight

// Shapes
val ShapeCard = RoundedCornerShape(DevRadiusTokens.card)
val ShapePill = RoundedCornerShape(DevRadiusTokens.pill)
val ShapeChip = RoundedCornerShape(12.dp)

private val DevbhashaColorScheme = lightColorScheme(
    primary = Saffron,
    onPrimary = DevColorTokens.textInverse,
    background = Background,
    onBackground = InkPrimary,
    surface = CardBg,
    onSurface = InkPrimary,
    surfaceVariant = PageBg,
    onSurfaceVariant = InkSecondary,
    outline = Border
)

private val AppTypography = Typography(
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = DevTypeTokens.bodySize,
        fontWeight = DevTypeTokens.bodyWeight,
        color = InkPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = InkPrimary
    )
)

@Composable
fun DevbhashaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DevbhashaColorScheme,
        typography = AppTypography,
        content = content
    )
}

// ==========================================
// SEMANTIC TOKEN LAYER (UI consistency pass)
// Migrated to generated tokens; values unchanged.
// ==========================================

// Neutrals (slate scale)
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

// Brand — saffron
val SaffronBase = DevColorTokens.accentSaffron
val SaffronDeep2 = DevColorTokens.accentSaffronDeep
val SaffronSoft = DevColorTokens.accentSaffronSoft
val SaffronSoftAlt = DevColorTokens.accentSaffronSoftAlt

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
val WarningAmberBase = DevColorTokens.statusWarning
val WarningAmberDeep = DevColorTokens.statusWarningDeep
val WarningSoft = DevColorTokens.statusWarningSoft

// Accents
val AccentViolet = DevColorTokens.accentViolet
val AccentVioletSoft = DevColorTokens.accentVioletSoft
val AccentBlue = DevColorTokens.accentBlue
val AccentBlueSoft = DevColorTokens.accentBlueSoft
