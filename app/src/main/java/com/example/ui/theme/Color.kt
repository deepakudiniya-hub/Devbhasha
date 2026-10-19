package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// Professional Teal & Slate Palette (No Yellow, No Saffron)
val PrimaryTeal = Color(0xFF0F5C5C)
val PrimaryContainerTeal = Color(0xFFD6EDEA)
val SpiritualOrange = PrimaryTeal
val Gold = PrimaryTeal
val GoldContainer = PrimaryContainerTeal
val SlateDark = Color(0xFF1E293B)
val SlateLight = Color(0xFF64748B)
val SurfaceWhite = Color(0xFFFFFFFF)
val OutlineGray = Color(0xFFE2E8F0)
val MainText = Color(0xFF1E293B)
val SecondaryText = Color(0xFF64748B)
val ErrorRed = Color(0xFFB3261E)
val VerifiedGreen = Color(0xFF1E6B42)
val OnlineGreen = Color(0xFF2E8B57)

// M3 Standard
val PrimaryLight = PrimaryTeal
val OnPrimaryLight = Color.White
val PrimaryContainerLight = PrimaryContainerTeal
val OnPrimaryContainerLight = PrimaryTeal
val SecondaryLight = SlateDark
val OnSecondaryLight = Color.White
val SecondaryContainerLight = OutlineGray
val OnSecondaryContainerLight = SlateDark
val BackgroundLight = SurfaceWhite
val OnBackgroundLight = MainText
val SurfaceLight = SurfaceWhite
val OnSurfaceLight = MainText
val OnSurfaceVariantLight = SecondaryText
val OutlineLight = OutlineGray
val ErrorLight = ErrorRed
val OnErrorLight = Color.White

// Dark Scheme
val PrimaryDark = Color(0xFF10B981)
val OnPrimaryDark = Color(0xFF09090B)
val PrimaryContainerDark = Color(0xFF0D5C46)
val SecondaryDark = SlateLight
val OnSecondaryDark = Color.White
val SecondaryContainerDark = Color(0xFF334155)
val BackgroundDark = Color(0xFF09090B)
val SurfaceDark = Color(0xFF18181B)
val OnBackgroundDark = Color(0xFFF4F4F5)
val OnSurfaceDark = Color(0xFFF4F4F5)
val OnSurfaceVariantDark = Color(0xFFA1A1AA)
val OutlineDark = Color(0x1AFFFFFF)
val ErrorDark = Color(0xFFF2B8B5)

// Extension for online status
val ColorScheme.online: Color
    get() = if (this.background == BackgroundDark || this.surface == SurfaceDark) Color(0xFF34D399) else OnlineGreen

// Aliases for compatibility
val BorderLight = OutlineLight
val BorderMedium = OutlineLight
val BorderSoft = OutlineLight
val BorderBeige = OutlineLight
val EditorialLine = OutlineLight
val EditorialLineStrong = OutlineLight
val Saffron = PrimaryTeal
val SaffronPrimary = PrimaryTeal
val SaffronDeep = PrimaryTeal
val SaffronGold = PrimaryTeal
val SaffronSoft = PrimaryContainerTeal
val SaffronSoftBg = PrimaryContainerTeal
val SaffronLight = SurfaceWhite
val Ink = MainText
val InkSoft = SecondaryText
val InkSecondary = SecondaryText
val InkFaint = SecondaryText.copy(alpha = 0.5f)
val Terra = PrimaryTeal
val TerraDeep = PrimaryTeal
val GoldStamp = PrimaryTeal
val GoldStampSoft = PrimaryContainerTeal
val PaperCard = SurfaceWhite
val PaperBg = SurfaceWhite
val PaperDeep = PrimaryContainerTeal
val SurfaceAlt = PrimaryContainerTeal
val TextDark = MainText
val TextSecondary = SecondaryText
val TextTertiary = SecondaryText
val TextSub = SecondaryText
val TextMuted = SecondaryText
val OnlineGreenAlias = OnlineGreen
val GreenPrimary = OnlineGreen
val Green700 = VerifiedGreen
val WarningAmberDeep = PrimaryTeal
val Neutral50 = SurfaceWhite
val Neutral100 = PrimaryContainerTeal
val Neutral500 = SecondaryText
val PrimaryPeacock = PrimaryTeal
val LightTeal = PrimaryContainerTeal
val Sage = VerifiedGreen
val SageDeep = VerifiedGreen
