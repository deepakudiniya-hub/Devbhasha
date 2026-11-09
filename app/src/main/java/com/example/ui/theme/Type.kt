package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ==========================================
// DEVBHASHA TYPOGRAPHY SYSTEM
// ==========================================

// Keep existing screen imports as aliases to the approved bilingual family.
val NotoSansDevanagari = ManropeHindFontFamily

val TiroDevanagariHindi = ManropeHindFontFamily

// Legacy alias
val AppFontFamily = NotoSansDevanagari
val TiroFontFamily = TiroDevanagariHindi

val DevbhashaTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = TiroDevanagariHindi,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 38.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = TiroDevanagariHindi,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = TiroDevanagariHindi,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = TiroDevanagariHindi,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = TiroDevanagariHindi,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 32.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansDevanagari,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 28.sp
    ),
    titleSmall = TextStyle(
        fontFamily = NotoSansDevanagari,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansDevanagari,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansDevanagari,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = NotoSansDevanagari,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NotoSansDevanagari,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSansDevanagari,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSansDevanagari,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp
    )
).withManropeHindFontFamily()

val AppTypography = DevbhashaTypography
