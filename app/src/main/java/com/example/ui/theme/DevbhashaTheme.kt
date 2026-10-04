package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================
// PURE WHITE + BLACK + SAFFRON DESIGN SYSTEM
// ==========================================

// Backgrounds:
// --bg-primary: #FFFFFF (pure white, all screens)
// --bg-secondary: #FAFAFA (search bars, input fields, inactive pills)
// --bg-card: #FFFFFF with 1px border #EFEFEF and shadow 0 2px 12px rgba(0,0,0,0.05)
val BgPrimary = Color(0xFFFFFFFF)
val BgSecondary = Color(0xFFFAFAFA)
val BgCard = Color(0xFFFFFFFF)

// Text:
// --text-primary: #000000 (headings, names, prices)
// --text-secondary: #737373 (subtitles, expertise, bio, timestamps)
// --text-tertiary: #A8A8A8 (placeholders, disabled)
val TextPrimary = Color(0xFF000000)
val TextSecondary = Color(0xFF737373)
val TextTertiary = Color(0xFFA8A8A8)

// Accent (ONLY FOR CTA):
// Strict Pure Saffron Accent (#FF6B00) - No beige, no gold
val Saffron = Color(0xFFFF6B00)
val SaffronGradientStart = Color(0xFFFF8A00)
val SaffronGradientEnd = Color(0xFFFF6B00)
val SaffronLight = Color(0xFFFFFFFF)

// Neutral:
// --black-pill-active: #000000 (active filter pill text white)
// --border-light: #EFEFEF
// --border-medium: #DBDBDB (bottom nav top border)
// --gray-nav-pill: #F5F5F5
val BlackPillActive = Color(0xFF000000)
val BorderLight = Color(0xFFEFEFEF)
val BorderMedium = Color(0xFFDBDBDB)
val GrayNavPill = Color(0xFFF5F5F5)

// Compatibility tokens mapped strictly to Pure White + Black + Saffron
val StageBg = Color(0xFFFFFFFF)
val PaperBg = Color(0xFFFFFFFF)
val PaperDeep = Color(0xFFFAFAFA)
val PaperCard = Color(0xFFFFFFFF)
val CardBg = Color(0xFFFFFFFF)
val Background = Color(0xFFFFFFFF)
val PageBg = Color(0xFFFAFAFA)

val Ink = Color(0xFF000000)
val InkPrimary = Color(0xFF000000)
val InkSoft = Color(0xFF737373)
val InkSecondary = Color(0xFF737373)
val InkFaint = Color(0xFFA8A8A8)
val TextDark = Color(0xFF000000)
val TextLight = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF737373)

val Terra = Color(0xFFFF6B00)
val TerraDeep = Color(0xFFFF6B00)
val DevOrange = Color(0xFFFF6B00)
val SaffronPrimary = Color(0xFFFF6B00)
val SaffronDeep = Color(0xFFFF6B00)
val SaffronGold = Color(0xFFFF8A00)
val SaffronSoftBg = Color(0xFFFFFFFF)
val GoldStamp = Color(0xFFFF6B00)
val GoldStampSoft = Color(0xFFFFFFFF)
val Sage = Color(0xFF16A34A)
val SageDeep = Color(0xFF15803D)
val GreenPrimary = Color(0xFF16A34A)

val EditorialLine = Color(0xFFEFEFEF)
val EditorialLineStrong = Color(0xFFDBDBDB)
val Border = Color(0xFFEFEFEF)

// Legacy Brand Palette
val CosmicBlack = Color(0xFF000000)
val CosmicIndigo = Color(0xFF000000)
val NeonViolet = Color(0xFFFF6B00)
val NeonIndigo = Color(0xFFFF6B00)
val GlassBackground = Color.White
val GlassBorder = Color(0xFFEFEFEF)

// Shapes
val ShapeCard = RoundedCornerShape(24.dp)
val ShapePill = RoundedCornerShape(999.dp)
val ShapeChip = RoundedCornerShape(12.dp)

private val DevbhashaColorScheme = lightColorScheme(
    primary = Saffron, // Using new Saffron accent
    onPrimary = Color.White,
    background = Background,
    onBackground = InkPrimary,
    surface = CardBg,
    onSurface = InkPrimary,
    surfaceVariant = PageBg,
    onSurfaceVariant = InkSecondary,
    outline = Border
)

// Dark scheme — the app was light-only (no darkColorScheme existed). Mirrors
// the light scheme's role mapping (saffron accent, near-black surfaces, light
// text). Kept OPT-IN via DevbhashaTheme(darkTheme = true) so it changes nothing
// yet: most screens still paint literal white backgrounds and would clash until
// the per-screen hardcoded-colour migration lands.
private val DevbhashaDarkColorScheme = darkColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF161616),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF2A2A2A)
)

private val AppTypography = Typography(
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
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
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DevbhashaDarkColorScheme else DevbhashaColorScheme,
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
val Neutral900 = Color(0xFF0F172A)
val Neutral800 = Color(0xFF1E293B)
val Neutral700 = Color(0xFF334155)
val Neutral600 = Color(0xFF475569)
val Neutral500 = Color(0xFF64748B)
val Neutral400 = Color(0xFF94A3B8)
val Neutral300 = Color(0xFFCBD5E1)
val Neutral200 = Color(0xFFE2E8F0)
val Neutral100 = Color(0xFFF1F5F9)
val Neutral50  = Color(0xFFF8FAFC)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceAlt   = Color(0xFFFAFAFA)
val BorderSoft   = Color(0xFFE5E5E5)

// Brand — saffron
val SaffronBase = Color(0xFFFF6B00)
val SaffronDeep2 = Color(0xFFFF6D00)
val SaffronSoft = Color(0xFFFFF7ED)
val SaffronSoftAlt = Color(0xFFFFF8E1)

// Success — green
val GreenBase = Color(0xFF16A34A)
val GreenDeepBase = Color(0xFF15803D)
val GreenSoftBase = Color(0xFFDCFCE7)
val GreenLightBase = Color(0xFF86EFAC)
val EmeraldBase = Color(0xFF10B981)

// Danger — red
val DangerRed = Color(0xFFEF4444)
val DangerRedDeep = Color(0xFFDC2626)
val DangerSoft = Color(0xFFFEE2E2)

// Warning — amber
val WarningAmberBase = Color(0xFFF59E0B)
val WarningAmberDeep = Color(0xFFD97706)
val WarningSoft = Color(0xFFFFF3C7)

// Accents
val AccentViolet = Color(0xFF7C3AED)
val AccentVioletSoft = Color(0xFFF3E8FF)
val AccentBlue = Color(0xFF2563EB)
val AccentBlueSoft = Color(0xFFEFF6FF)
