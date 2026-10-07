package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DevLogoIcon
import com.example.ui.components.DevWatermarkLogo
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*

// =============================================================================
// ASTROTALK HOME SCREEN VIEW COMPONENTS
// Exact Astrotalk layout with our warm deep saffron/gold color system
// =============================================================================

/**
 * Astrotalk Header: Avatar, "Welcome [Name]!", "What's on your mind today?", Wallet chip
 */
@Composable
fun AstrotalkHomeHeader(
    userName: String,
    balanceAmount: Double,
    isHindi: Boolean,
    onWalletClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLanguageToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFFF97316)
    val textColor = Color(0xFF2B2B2B)
    val textSub = Color(0xFF6E6E6E)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Profile & Welcome Greeting
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick() }
        ) {
            // Avatar with online green ring
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF7ED))
                    .border(1.5.dp, primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🙏",
                    fontSize = 22.sp
                )
                // Small green dot
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16A34A))
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                val displayName = if (userName.isBlank()) "दीपक जी" else userName
                Text(
                    text = if (isHindi) "नमस्ते, $displayName!" else "Welcome, $displayName!",
                    fontFamily = AppFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isHindi) "आज आप क्या जानना चाहते हैं?" else "What's on your mind today?",
                    fontFamily = AppFontFamily,
                    fontSize = 11.5.sp,
                    color = textSub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Actions: Language Switch + Saffron Wallet Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Language chip
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onLanguageToggle() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = "Language",
                        tint = primaryColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isHindi) "हिन्दी" else "ENG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            // Wallet Chip with "+" icon (Astrotalk signature)
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = primaryColor,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onWalletClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "₹${balanceAmount.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Astrotalk Signature: "Live Astrologers" Circular Story Strip with LIVE Badge
 */
@Composable
fun AstrotalkLiveStoriesStrip(
    sadhaks: List<SadhakItem>,
    isHindi: Boolean,
    onSadhakClick: (SadhakItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val liveAstrologers = remember(sadhaks) { sadhaks.filter { it.isOnline }.take(8) }
    if (liveAstrologers.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_live")
    val livePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isHindi) "लाइव साधक" else "Live Sadhaks",
                    fontFamily = AppFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B2B2B)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "🔴 LIVE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(liveAstrologers) { sadhak ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(64.dp)
                        .clickable { onSadhakClick(sadhak) }
                ) {
                    // Avatar with glowing pulsing ring
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(livePulse),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF7ED),
                            border = BorderStroke(2.dp, Color(0xFF16A34A)),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = sadhak.initialHi,
                                    fontSize = 24.sp
                                )
                            }
                        }

                        // Bottom LIVE pill badge
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFF16A34A),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 4.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHindi) sadhak.nameHi.split(" ").first() else sadhak.nameEn.split(" ").first(),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2B2B2B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Astrotalk 6 Core Services Grid:
 * 1. चैट करें (Chat with Astrologer)
 * 2. कॉल करें (Call Astrologer)
 * 3. मुफ्त कुंडली (Free Kundli)
 * 4. दैनिक राशिफल (Daily Horoscope)
 * 5. कुंडली मिलान (Kundli Matching)
 * 6. आज का पंचांग (Today's Panchang)
 */
@Composable
fun AstrotalkQuickServicesGrid(
    isHindi: Boolean,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    onKundliClick: () -> Unit,
    onHoroscopeClick: () -> Unit,
    onMatchClick: () -> Unit,
    onPanchangClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFFF97316)
    val textColor = Color(0xFF2B2B2B)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isHindi) "प्रमुख सेवाएं" else "Explore Services",
                fontFamily = AppFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 1: Chat, Call, Kundli
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AstrotalkServiceItem(
                title = if (isHindi) "चैट करें" else "Chat Astrologer",
                subtitle = if (isHindi) "तुरंत उत्तर" else "Instant Chat",
                iconText = "💬",
                iconColor = Color(0xFFF97316),
                onClick = onChatClick,
                modifier = Modifier.weight(1f)
            )
            AstrotalkServiceItem(
                title = if (isHindi) "कॉल करें" else "Call Astrologer",
                subtitle = if (isHindi) "सीधी बात" else "Audio Call",
                iconText = "📞",
                iconColor = Color(0xFF16A34A),
                onClick = onCallClick,
                modifier = Modifier.weight(1f)
            )
            AstrotalkServiceItem(
                title = if (isHindi) "मुफ्त कुंडली" else "Free Kundli",
                subtitle = if (isHindi) "जन्म विवरण" else "Birth Chart",
                iconText = "📜",
                iconColor = Color(0xFFEAB308),
                onClick = onKundliClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Horoscope, Matchmaking, Panchang
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AstrotalkServiceItem(
                title = if (isHindi) "दैनिक राशिफल" else "Horoscope",
                subtitle = if (isHindi) "12 राशियां" else "Daily Forecast",
                iconText = "♈",
                iconColor = Color(0xFFF97316),
                onClick = onHoroscopeClick,
                modifier = Modifier.weight(1f)
            )
            AstrotalkServiceItem(
                title = if (isHindi) "कुंडली मिलान" else "Matchmaking",
                subtitle = if (isHindi) "गुण मिलान" else "Compatibility",
                iconText = "💑",
                iconColor = Color(0xFFEC4899),
                onClick = onMatchClick,
                modifier = Modifier.weight(1f)
            )
            AstrotalkServiceItem(
                title = if (isHindi) "दैनिक पंचांग" else "Panchang",
                subtitle = if (isHindi) "शुभ मुहूर्त" else "Muhurat & Tithi",
                iconText = "📅",
                iconColor = Color(0xFF0EA5E9),
                onClick = onPanchangClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AstrotalkServiceItem(
    title: String,
    subtitle: String,
    iconText: String,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = iconText, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontFamily = AppFontFamily,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2B2B2B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF6E6E6E),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Astrotalk Signature Astrologer Card (Used in Consultation Tab & Home Tab)
 * Left: Photo with green online badge & verified check
 * Center: Name, Specialization, Languages, Experience, Rating
 * Right: Price per min + Green "Chat" / "Call" button
 */
@Composable
fun AstrotalkAstrologerCard(
    sadhak: SadhakItem,
    isHindi: Boolean,
    onChatClick: (SadhakItem) -> Unit,
    onCallClick: (SadhakItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFFF97316)
    val onlineGreen = Color(0xFF16A34A)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Photo Avatar with Green Online indicator
            Box(
                modifier = Modifier.size(62.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFF7ED),
                    border = BorderStroke(2.dp, if (sadhak.isOnline) onlineGreen else Color(0xFFCBD5E1)),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = sadhak.initialHi,
                            fontSize = 28.sp
                        )
                    }
                }

                // Green dot badge
                if (sadhak.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(onlineGreen)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center: Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isHindi) sadhak.nameHi else sadhak.nameEn,
                        fontFamily = AppFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B2B2B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isHindi) sadhak.titleHi else sadhak.titleEn,
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "हिन्दी, English • 12+ वर्ष अनुभव",
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rating & Orders
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFEAB308),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${sadhak.rating}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B2B2B)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• 2.4k परामर्श",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Pricing & Action Button (Astrotalk style)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹${sadhak.consultationFee.filter { it.isDigit() }.toIntOrNull() ?: 21}/min",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Astrotalk Outlined / Filled Chat Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (sadhak.isOnline) onlineGreen else Color(0xFF94A3B8),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onChatClick(sadhak) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "चैट" else "Chat",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
