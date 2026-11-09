package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// Saffron Palette
val SaffronPrimary = Color(0xFFF18538)
val SaffronContainer = Color(0xFFFFF0D6)
val SaffronDeep = Color(0xFFCC6F2E)
val SaffronDark = Color(0xFF7A3E12)
val SaffronSurface = Color(0xFFFFF7EC)
val SaffronSoft = Color(0xFFFDD5A6)

// Aliases
val PrimarySaffron = SaffronPrimary
val PrimaryContainerSaffron = SaffronContainer
val SpiritualOrange = SaffronPrimary

val SlateDark = Color(0xFF1E293B)
val SlateLight = Color(0xFF64748B)
val SurfaceWhite = Color(0xFFFFFFFF)
val OutlineGray = Color(0xFFE2E8F0)
val MainText = Color(0xFF1E293B)
val SecondaryText = Color(0xFF64748B)
val ErrorRed = Color(0xFFB3261E)
val VerifiedGreen = Color(0xFF1E6B42)
val OnlineGreen = Color(0xFF2E8B57)

// M3 Standard Mapping
val PrimaryLight = SaffronPrimary
val OnPrimaryLight = Color.White
val PrimaryContainerLight = SaffronContainer
val OnPrimaryContainerLight = SaffronDeep

val SecondaryLight = SaffronPrimary
val OnSecondaryLight = Color.White
val SecondaryContainerLight = SaffronContainer
val OnSecondaryContainerLight = SaffronDeep

val BackgroundLight = SurfaceWhite
val OnBackgroundLight = MainText
val SurfaceLight = SurfaceWhite
val OnSurfaceLight = MainText
val OnSurfaceVariantLight = SecondaryText
val OutlineLight = OutlineGray
val ErrorLight = ErrorRed
val OnErrorLight = Color.White

// Dark Scheme Mapping
val PrimaryDark = Color(0xFFFDD5A6)
val OnPrimaryDark = Color(0xFF3E2723)
val PrimaryContainerDark = SaffronDeep
val SecondaryDark = Color(0xFFFDD5A6)
val OnSecondaryDark = Color(0xFF3E2723)
val SecondaryContainerDark = SaffronDeep
val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val OnBackgroundDark = Color(0xFFE0E0E0)
val OnSurfaceDark = Color(0xFFE0E0E0)
val OnSurfaceVariantDark = Color(0xFFBDBDBD)
val OutlineDark = Color(0x33FFFFFF)
val ErrorDark = Color(0xFFCF6679)

// Extension for online status
val ColorScheme.online: Color
    get() = if (this.background == BackgroundDark || this.surface == SurfaceDark) Color(0xFF34D399) else OnlineGreen

val BorderLight = OutlineLight
val BorderMedium = OutlineLight
val BorderSoft = OutlineLight
val BorderBeige = OutlineLight
val EditorialLine = OutlineLight
val EditorialLineStrong = OutlineLight
val Saffron = SaffronPrimary
val SaffronPrimaryAlias = SaffronPrimary
val SaffronDeepAlias = SaffronDeep
val SaffronGold = SaffronPrimary
val SaffronSoftBg = Color(0xFFFFF3E0)
val SaffronLight = SurfaceWhite
val Ink = MainText
val InkSoft = SecondaryText
val InkSecondary = SecondaryText
val InkFaint = SecondaryText.copy(alpha = 0.5f)
val Terra = SaffronPrimary
val TerraDeep = SaffronDeep
val GoldStamp = SaffronPrimary
val GoldStampSoft = SaffronContainer
val PaperCard = SurfaceWhite
val PaperBg = SurfaceWhite
val PaperDeep = Color(0xFFF8FAFC)
val SurfaceAlt = Color(0xFFF8FAFC)
val TextDark = MainText
val TextSecondary = SecondaryText
val TextTertiary = SecondaryText
val TextSub = SecondaryText
val TextMuted = SecondaryText
val OnlineGreenAlias = OnlineGreen
val GreenPrimary = OnlineGreen
val Green700 = VerifiedGreen
val WarningAmberDeep = Color(0xFFB45309)
val Neutral50 = SurfaceWhite
val Neutral100 = Color(0xFFF1F5F9)
val Neutral500 = SecondaryText
val PrimaryPeacock = SaffronPrimary
val Sage = VerifiedGreen
val SageDeep = VerifiedGreen

