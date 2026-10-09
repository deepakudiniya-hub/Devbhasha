package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.CircleCategoryItem
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*

/**
 * Circular Guidance Categories: दैनिक राशिफल, पारिवारिक समस्या, कुंडली मिलान, साधक चैट, साधक कॉल, पूजा-पाठ, स्वप्न विचार, वास्तु, मंत्र जप
 * Horizontally scrollable circular categories right below search/promo banner.
 */
@Composable
fun GuidanceCategoriesSection(
    categories: List<CircleCategoryItem>,
    selectedCategoryId: String?,
    isHindi: Boolean,
    onCategoryClick: (CircleCategoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { item ->
                val isSelected = selectedCategoryId == item.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(74.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onCategoryClick(item) }
                        .padding(vertical = 4.dp)
                ) {
                    // Circular icon container with warm aesthetic gradient/glow
                    Surface(
                        modifier = Modifier.size(62.dp),
                        shape = CircleShape,
                        color = if (isSelected) Color(0xFFFFF2E8) else item.softBgColor,
                        shadowElevation = if (isSelected) 4.dp else 1.5.dp,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.2.dp,
                            color = if (isSelected) Color(0xFF0D656C) else item.borderColor
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.icon != null) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.titleHi,
                                    tint = item.primaryColor,
                                    modifier = Modifier.size(30.dp)
                                )
                            } else {
                                Text(
                                    text = item.symbol,
                                    fontSize = 28.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) item.titleHi else item.titleEn,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) Color(0xFF0D656C) else Color(0xFF2D3748),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Prominent Dual Action Cards for "साधक चैट (Chat)" and "साधक कॉल (Call)"
 * Designed for immediate one-tap consultation access right on Home Screen cards section.
 */
@Composable
fun ChatAndCallDualActionCards(
    isHindi: Boolean,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Chat with Astrologer Card (चैट करें)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.2.dp, Color(0xFFBAE6FD)),
            shadowElevation = 3.dp,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onChatClick() }
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF0F9FF), Color.White)
                        )
                    )
                    .padding(12.dp)
            ) {
                // Top Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💬", fontSize = 18.sp)
                    }

                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isHindi) "12 लाइव" else "12 Live",
                                color = Color(0xFF15803D),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isHindi) "साधक चैट" else "Chat Astrologer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0369A1)
                )

                Text(
                    text = if (isHindi) "तुरंत चैट • ₹499 / 20 मिनट" else "Instant Chat • ₹499 / 20 min",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0284C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "चैट शुरू करें" else "Start Chat",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Call Astrologer Card (कॉल करें)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.2.dp, Color(0xFFBBF7D0)),
            shadowElevation = 3.dp,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onCallClick() }
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF0FDF4), Color.White)
                        )
                    )
                    .padding(12.dp)
            ) {
                // Top Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📞", fontSize = 18.sp)
                    }

                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isHindi) "9 उपलब्ध" else "9 Live",
                                color = Color(0xFF15803D),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isHindi) "साधक कॉल" else "Call Astrologer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )

                Text(
                    text = if (isHindi) "सीधी बात • ₹499 / 20 मिनट" else "Voice Call • ₹499 / 20 min",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF16A34A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "कॉल लगाएं" else "Call Now",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section: "अनुभवी साधक" (Verified Sadhaks)
 * With clean live status indicator (🟢 Green dot) on avatar & next to name when isOnline == true.
 * Quick 📞 Call and 💬 Chat action buttons (server-billed session).
 */
@Composable
fun VerifiedSadhaksSection(
    sadhaks: List<SadhakItem>,
    selectedSadhakId: String?,
    language: String,
    onSadhakClick: (SadhakItem) -> Unit,
    onCallClick: (SadhakItem) -> Unit = {},
    onChatClick: (SadhakItem) -> Unit = {},
    onViewAllClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedFilterChip by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("all") }

    val filterChips = listOf(
        "all" to when(language) { "hi" -> "सभी"; "hinglish" -> "All"; else -> "All" },
        "online" to when(language) { "hi" -> "🟢 ऑनलाइन"; "hinglish" -> "🟢 Live"; else -> "🟢 Live" },
        "astrology" to when(language) { "hi" -> "ज्योतिष"; "hinglish" -> "Astrology"; else -> "Astrology" },
        "vastu" to when(language) { "hi" -> "वास्तु"; "hinglish" -> "Vastu"; else -> "Vastu" },
        "karma" to when(language) { "hi" -> "कर्मकांड"; "hinglish" -> "Rituals"; else -> "Rituals" }
    )

    val displayedSadhaks = androidx.compose.runtime.remember(sadhaks, selectedFilterChip) {
        when (selectedFilterChip) {
            "online" -> sadhaks.filter { it.isOnline }
            "astrology" -> sadhaks.filter { it.titleHi.contains("ज्योतिष", true) || it.titleEn.contains("Astrology", true) }
            "vastu" -> sadhaks.filter { it.titleHi.contains("वास्तु", true) || it.titleEn.contains("Vastu", true) }
            "karma" -> sadhaks.filter { it.titleHi.contains("मंत्र", true) || it.titleHi.contains("कर्मकांड", true) || it.titleHi.contains("साधना", true) }
            else -> sadhaks
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 16.dp)
                        .background(SaffronPrimary, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when(language) { "hi" -> "लाइव साधक एवं ज्योतिषी"; "hinglish" -> "Live Astrologers & Sadhaks"; else -> "Live Astrologers & Sadhaks" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "🟢 LIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            TextButton(
                onClick = onViewAllClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = when(language) { "hi" -> "सभी देखें"; "hinglish" -> "View All"; else -> "View All" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SaffronPrimary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = SaffronPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterChips.forEach { (key, label) ->
                val isChipSelected = selectedFilterChip == key
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isChipSelected) SaffronPrimary else Color.White,
                    border = BorderStroke(
                        1.dp,
                        if (isChipSelected) SaffronPrimary else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedFilterChip = key }
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isChipSelected) Color.White else Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = SaffronPrimary,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when(language) { "hi" -> "साधक लोड हो रहे हैं..."; "hinglish" -> "Loading Sadhaks..."; else -> "Loading Sadhaks..." },
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else if (displayedSadhaks.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when(language) { "hi" -> "इस श्रेणी में कोई साधक उपलब्ध नहीं है"; "hinglish" -> "No sadhaks in this category"; else -> "No sadhaks in this category" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Horizontal LazyRow of Sadhaks
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(displayedSadhaks, key = { it.id }) { sadhak ->
                    val isSelected = selectedSadhakId == sadhak.id
                    val saffronGradient = Brush.horizontalGradient(
                        listOf(SaffronGradientStart, SaffronGradientEnd)
                    )
                    Surface(
                        modifier = Modifier
                            .width(205.dp)
                            .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onSadhakClick(sadhak) },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Saffron else BorderLight
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar circle with 2px saffron ring only if online
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.size(54.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFFAFAFA),
                                    border = if (sadhak.isOnline) BorderStroke(2.dp, Saffron) else BorderStroke(1.dp, BorderLight)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (language == "hi") sadhak.initialHi else sadhak.initialEn,
                                            color = Color(0xFF000000),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Live Status Indicator on Avatar Corner
                                if (sadhak.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Saffron)
                                            .border(2.dp, Color.White, CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Name
                            Text(
                                text = if (language == "hi") sadhak.nameHi else sadhak.nameEn,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000000),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )

                            Text(
                                text = if (language == "hi") sadhak.titleHi else sadhak.titleEn,
                                fontSize = 11.5.sp,
                                color = Color(0xFF737373),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Rating & Experience
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Saffron,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = sadhak.rating,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF000000)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• ${if (language == "hi") sadhak.experienceHi else sadhak.experienceEn}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF737373),
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Buttons (Call 📞 & Chat 💬)
                            if (sadhak.isOnline) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Chat Button (Primary - Saffron Gradient)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(saffronGradient)
                                            .clickable { onChatClick(sadhak) },
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color.Transparent
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                                contentDescription = "Chat",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = when(language) { "hi" -> "चैट"; else -> "Chat" },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Call Button (Secondary - White with Black Border)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .clickable { onCallClick(sadhak) },
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFF000000))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Phone,
                                                contentDescription = "Call",
                                                tint = Color(0xFF000000),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = when(language) { "hi" -> "कॉल"; else -> "Call" },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF000000)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color(0xFFFAFAFA),
                                    border = BorderStroke(1.dp, BorderLight)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (language == "hi") "ऑफ़लाइन" else "Offline",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFA8A8A8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
