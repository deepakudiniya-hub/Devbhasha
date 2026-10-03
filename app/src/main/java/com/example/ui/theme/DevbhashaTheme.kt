package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
// ... (rest of the file remains same, keeping the AppTypography defined earlier)

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
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DevbhashaColorScheme,
        typography = AppTypography,
        content = content
    )
}
