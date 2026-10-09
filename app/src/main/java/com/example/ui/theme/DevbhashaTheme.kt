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
val BgPrimary = Color(0xFFFAF6EA)
val BgSecondary = Color(0xFFFAFAFA)
val BgCard = Color(0xFFFFFFFF)

// Text:
// --text-primary: #1F2937 (headings, names, prices)
// --text-secondary: #49454F (subtitles, expertise, bio, timestamps)
// --text-tertiary: #5F6368 (placeholders, disabled)
val TextPrimary = Color(0xFF1F2937)
val TextSecondary = Color(0xFF49454F)
val TextTertiary = Color(0xFF5F6368)

// Accent (ONLY FOR CTA):
// Soothing Muted Terracotta (#E76F51) & Soft Peach (#F4A261) - Easier on the eyes
val Saffron = Color(0xFF0D656C)
val SaffronGradientStart = Color(0xFFD0BF75)
val SaffronGradientEnd = Color(0xFF0D656C)
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
val StageBg = Color(0xFFFAF6EA)
val PaperBg = Color(0xFFFAF6EA)
val PaperDeep = Color(0xFFFAFAFA)
val PaperCard = Color(0xFFFFFFFF)
val CardBg = Color(0xFFFFFFFF)
val Background = Color(0xFFFAF6EA)
val PageBg = Color(0xFFFAFAFA)

val Ink = Color(0xFF000000)
val InkPrimary = Color(0xFF000000)
val InkSoft = Color(0xFF737373)
val InkSecondary = Color(0xFF737373)
val InkFaint = Color(0xFFA8A8A8)
val TextDark = Color(0xFF000000)
val TextLight = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF737373)

val Terra = Color(0xFF0D656C)
val TerraDeep = Color(0xFF125157)
val DevOrange = Color(0xFF0D656C)
val SaffronPrimary = Color(0xFF0D656C)
val SaffronDeep = Color(0xFF125157)
val SaffronGold = Color(0xFFD0BF75)
val SaffronSoftBg = Color(0xFFFAF6EA)
val GoldStamp = Color(0xFF0D656C)
val GoldStampSoft = Color(0xFFFAF6EA)
val Sage = Color(0xFF16A34A)
val SageDeep = Color(0xFF15803D)
val GreenPrimary = Color(0xFF16A34A)

val EditorialLine = Color(0xFFEFEFEF)
val EditorialLineStrong = Color(0xFFDBDBDB)
val Border = Color(0xFFEFEFEF)

// Legacy Brand Palette
val CosmicBlack = Color(0xFF000000)
val CosmicIndigo = Color(0xFF000000)
val GlassBackground = Color.White
val GlassBorder = Color(0xFFEFEFEF)

// Shapes
val ShapeCard = RoundedCornerShape(16.dp)
val ShapeDialog = RoundedCornerShape(28.dp)
val ShapePill = RoundedCornerShape(9999.dp)
val ShapeChip = RoundedCornerShape(12.dp)

val GoogleBlue = Color(0xFF4285F4)
val BackgroundColor = Color(0xFFFAF6EA)
val SurfaceColor = Color(0xFFFFFFFF)
val BorderColor = Color(0xFFE0E0E0)
val TextColor = Color(0xFF202124) // Google-like dark text

private val DevbhashaColorScheme = lightColorScheme(
    primary = DevOrange,
    onPrimary = Color.White,
    background = BackgroundColor,
    onBackground = TextColor,
    surface = SurfaceColor,
    onSurface = TextColor,
    surfaceVariant = Color(0xFFF8F9FA),
    onSurfaceVariant = TextColor,
    outline = BorderColor
)

private val DarkColorScheme = darkColorScheme(
    primary = DevOrange,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    outline = Color(0xFF444444)
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

// Brand — Muted Terracotta & Soft Peach
val SaffronBase = Color(0xFF0D656C)
val SaffronDeep2 = Color(0xFF125157)
val SaffronSoft = Color(0xFFFAF6EA)
val SaffronSoftAlt = Color(0xFFFAF6EA)

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
val WarningAmberBase = Color(0xFFD0BF75)
val WarningAmberDeep = Color(0xFF125157)
val WarningSoft = Color(0xFFFFF3C7)

// Accents
val AccentViolet = Color(0xFF7C3AED)
val AccentVioletSoft = Color(0xFFF3E8FF)
val AccentBlue = Color(0xFF2563EB)
val AccentBlueSoft = Color(0xFFEFF6FF)
