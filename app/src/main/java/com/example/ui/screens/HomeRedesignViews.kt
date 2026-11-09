package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*
import java.util.Calendar
import java.util.Locale

/*
 * Home screen v2 (visual, low-text redesign).
 * Brand palette: Saffron, Gold, Cream.
 * Visual only: every composable here takes the same callbacks HomeScreen already had.
 */

internal object HomeV2Colors {
    val Saffron: Color = com.example.ui.theme.SaffronPrimary
    val SaffronDark: Color = com.example.ui.theme.SaffronDark
    val SaffronDeep: Color = com.example.ui.theme.SaffronDeep
    val SaffronLight: Color = com.example.ui.theme.SaffronSurface
    val Gold: Color = Color(0xFFD0BF75)
    val Cream: Color = Color(0xFFFAF6EA)
    val TextDark: Color = Color(0xFF1F2A2B)
    val TextSoft: Color = Color(0xFF6B7476)
    val PillTint: Color = com.example.ui.theme.SaffronContainer
    val PillText: Color = com.example.ui.theme.SaffronDeep
    val Online: Color = Color(0xFF22A355)
}

private fun homeLangCode(language: String, isHindi: Boolean): String = "en"

private fun tr(code: String, hi: String, hgl: String, en: String): String = en

/**
 * Today's label/title for the "today" card.
 * There is no live Panchang source on main yet, so this is date based:
 * Sharad Navratri 2026 starts 11 Oct 2026 (Ghatasthapana, Maa Shailputri) and ends with
 * Vijayadashami on 20 Oct 2026. Outside that window the card falls back to today's date.
 */
private data class HomeTodayInfo(val pill: String, val title: String)

private fun homeTodayInfo(code: String): HomeTodayInfo {
    val cal = Calendar.getInstance()
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH)
    val d = cal.get(Calendar.DAY_OF_MONTH)
    return if (y == 2026 && m == Calendar.OCTOBER && d == 11) {
        HomeTodayInfo(
            pill = tr(code, "नवरात्रि • दिन 1", "Navratri • Din 1", "Navratri • Day 1"),
            title = tr(code, "माँ शैलपुत्री", "Maa Shailputri", "Maa Shailputri")
        )
    } else if (y == 2026 && m == Calendar.OCTOBER && d in 12..20) {
        HomeTodayInfo(
            pill = tr(code, "शारदीय नवरात्रि", "Sharadiya Navratri", "Sharad Navratri"),
            title = tr(code, "माँ दुर्गा", "Maa Durga", "Maa Durga")
        )
    } else {
        val locale = if (code == "hi") Locale("hi", "IN") else Locale.ENGLISH
        val weekday = java.text.SimpleDateFormat("EEEE", locale).format(cal.time)
        val date = java.text.SimpleDateFormat("d MMMM", locale).format(cal.time)
        HomeTodayInfo(
            pill = "$weekday • $date",
            title = tr(code, "आज का पंचांग", "Aaj ka Panchang", "Today's Panchang")
        )
    }
}

/**
 * 1 + 2. Saffron gradient header: logo + wordmark, wallet chip, avatar, greeting,
 * and the white "today" card.
 */
@Composable
fun HomeV2Header(
    userName: String,
    balanceAmount: Double?,
    language: String,
    isHindi: Boolean,
    onBalanceClick: () -> Unit,
    onAddMoneyClick: () -> Unit,
    onProfileClick: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier,
    searchBar: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(bottom = 20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Devbhasha",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDeep
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wallet Chip (Adapted from original)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.clickable { onBalanceClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = balanceAmount?.let { "₹${it.toInt()}" } ?: "—",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDeep
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add money",
                            tint = SaffronDeep,
                            modifier = Modifier.size(16.dp).clickable { onAddMoneyClick() }
                        )
                    }
                }
                
                // Profile Chip (Adapted from original)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SaffronContainer)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDeep
                    )
                }
            }
        }

        // Greeting Text
        val cleanUserName = if (userName.equals("Devotee", ignoreCase = true)) "" else userName.trim()
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            if (cleanUserName.isNotBlank()) {
                Text(
                    text = cleanUserName,
                    fontSize = 22.sp,
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "How can we help you today?",
                fontSize = if (cleanUserName.isNotBlank()) 14.sp else 18.sp,
                color = if (cleanUserName.isNotBlank()) Color(0xFF64748B) else Color(0xFF1E293B),
                fontWeight = if (cleanUserName.isNotBlank()) FontWeight.Normal else FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        if (searchBar != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFBFB)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    searchBar()
                }
            }
        }
    }
}

@Composable
fun HomeV2TodayBanner(
    language: String,
    isHindi: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val code = homeLangCode(language, isHindi)
    HomeV2TodayCard(code = code, onClick = onClick, modifier = modifier)
}

@Composable
fun HomeV2TodayCard(code: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val info = remember(code) { homeTodayInfo(code) }
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .height(124.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(shape = RoundedCornerShape(999.dp), color = HomeV2Colors.PillTint) {
                    Text(
                        text = info.pill,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomeV2Colors.PillText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = info.title,
                    fontFamily = AppFontFamily,
                    fontSize = 23.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeV2Colors.TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tr(code, "आज का मंत्र और पूजा विधि", "Aaj ka mantra aur pooja vidhi", "Today's mantra & puja vidhi"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeV2Colors.Saffron,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Image(
                painter = painterResource(id = R.drawable.home_maa_shailputri),
                contentDescription = info.title,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(128.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
        }
    }
}

/**
 * 3. Saffron "मन में कोई सवाल?" ask card. The input pill opens the existing पूछो flow.
 */
@Composable
fun HomeV2AskCard(
    onAskClick: () -> Unit,
    language: String,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    val code = homeLangCode(language, isHindi)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = HomeV2Colors.Saffron,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            Text(
                text = tr(code, "मन में कोई सवाल?", "Mann mein koi sawaal?", "Something on your mind?"),
                fontFamily = AppFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = tr(
                    code,
                    "सत्यापित साधक से निजी जवाब पाएँ",
                    "Verified sadhak se niji jawaab paayein",
                    "Get a private answer from a verified sadhak"
                ),
                fontSize = 13.5.sp,
                color = Color.White.copy(alpha = 0.82f)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(HomeV2Colors.Cream)
                    .clickable { onAskClick() }
                    .padding(start = 18.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr(code, "यहाँ लिखें या बोलें...", "Yahan likhein ya bolein...", "Type or speak here..."),
                    fontSize = 15.sp,
                    color = HomeV2Colors.TextSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HomeV2Colors.Gold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = tr(code, "पूछें", "Poochhein", "Ask"),
                        tint = HomeV2Colors.SaffronDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeV2SectionTitle(
    title: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = AppFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = HomeV2Colors.TextDark,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeV2Colors.Saffron,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAction() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * 4. "किस बारे में?" quick categories section with clean vector icons and smooth click handling.
 */
@Composable
fun HomeV2CategoryRow(
    onCategoryClick: (String) -> Unit,
    language: String,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val code = homeLangCode(language, isHindi)

    data class CategoryItem(
        val id: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val labelHi: String,
        val labelHgl: String,
        val labelEn: String,
        val iconBg: Color,
        val iconTint: Color
    )

    val categories = listOf(
        CategoryItem("family", Icons.Outlined.People, "परिवार", "Family", "Family", Color(0xFFDCFCE7), Color(0xFF16A34A)),
        CategoryItem("wealth", Icons.Outlined.AccountBalanceWallet, "धन", "Wealth", "Wealth", Color(0xFFFEF3C7), Color(0xFFD97706)),
        CategoryItem("health", Icons.Outlined.Favorite, "स्वास्थ्य", "Health", "Health", Color(0xFFFEE2E2), Color(0xFFDC2626)),
        CategoryItem("negativity", Icons.Outlined.Shield, "नकारात्मकता", "Negativity", "Negativity", Color(0xFFEDE9FE), Color(0xFF7C3AED)),
        CategoryItem("pitrdosh", Icons.Outlined.SelfImprovement, "पितृदोष", "Pitra Dosh", "Pitra Dosh", Color(0xFFFCE7F3), Color(0xFFDB2777))
    )

    Column(modifier = modifier.fillMaxWidth()) {
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { item ->
                val label = tr(code, item.labelHi, item.labelHgl, item.labelEn)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            com.example.utils.HapticFeedbackHelper.playClick(haptic)
                            onCategoryClick(item.id)
                        }
                        .testTag("what_about_${item.id}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.68f)
                                    .clip(CircleShape)
                                    .background(item.iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = label,
                                    tint = item.iconTint,
                                    modifier = Modifier.fillMaxSize(0.55f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HomeV2Colors.TextDark,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 5. "अभी ऑनलाइन साधक": large provider cards. Card tap = chat, button = call
 * (same mapping the previous expert cards used).
 */
@Composable
fun HomeV2OnlineSadhaks(
    sadhaks: List<SadhakItem>,
    onChatClick: (SadhakItem) -> Unit,
    onCallClick: (SadhakItem) -> Unit,
    onSeeAllClick: () -> Unit,
    language: String,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    val code = homeLangCode(language, isHindi)
    val online = sadhaks.filter { it.isOnline }
    Column(modifier = modifier.fillMaxWidth()) {
        HomeV2SectionTitle(
            title = tr(code, "अभी ऑनलाइन साधक", "Abhi online sadhak", "Sadhaks online now"),
            actionLabel = tr(code, "सभी देखें", "Sabhi dekhein", "See all"),
            onAction = onSeeAllClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (online.isEmpty()) {
            com.example.ui.components.DevEmptyState(
                title = tr(code, "अभी कोई साधक ऑनलाइन नहीं है", "Abhi koi Sadhak online nahi hai", "No sadhaks online right now"),
                description = tr(
                    code,
                    "थोड़ी देर बाद देखें।",
                    "Thodi der baad dekhein.",
                    "Please check back in a little while."
                ),
                modifier = Modifier.padding(horizontal = 20.dp),
                icon = Icons.Default.SelfImprovement,
                actionText = tr(code, "साधक टैब देखें", "Sadhak tab dekhein", "View Sadhaks"),
                onActionClick = onSeeAllClick
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(online, key = { it.id }) { sadhak ->
                    HomeV2SadhakCard(
                        sadhak = sadhak,
                        code = code,
                        onChatClick = { onChatClick(sadhak) },
                        onCallClick = { onCallClick(sadhak) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeV2SadhakCard(
    sadhak: SadhakItem,
    code: String,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val name = if (code == "hi") sadhak.nameHi else sadhak.nameEn
    val initial = if (code == "hi") sadhak.initialHi.ifEmpty { "सा" } else sadhak.initialEn.take(2).ifEmpty { "S" }
    val specialties = (if (code == "hi") sadhak.titleHi else sadhak.titleEn)
        .replace(" एवं ", " • ")
        .replace(" & ", " • ")
    val price = if (code == "hi") sadhak.consultationFee.replace("min", "मिनट") else sadhak.consultationFee

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 1.5.dp,
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onChatClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SadhakItem has no photo field yet, so the avatar is the initial.
            Box {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(HomeV2Colors.SaffronLight)
                        .border(BorderStroke(2.5.dp, HomeV2Colors.Gold), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontFamily = AppFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomeV2Colors.SaffronDark
                    )
                }
                if (sadhak.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(HomeV2Colors.Online)
                            .border(2.5.dp, Color.White, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = name,
                fontFamily = AppFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HomeV2Colors.TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = specialties,
                fontSize = 12.5.sp,
                color = HomeV2Colors.TextSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = HomeV2Colors.Gold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = sadhak.rating,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HomeV2Colors.TextDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = price,
                    fontSize = 13.sp,
                    color = HomeV2Colors.SaffronDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chat Button
                if (sadhak.chatPricePerMin > 0) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onChatClick,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.2.dp, HomeV2Colors.Saffron),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HomeV2Colors.Saffron),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = "Chat",
                            tint = HomeV2Colors.Saffron,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = tr(code, "चैट", "Chat", "Chat"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Call Button
                if (sadhak.callPricePerMin > 0) {
                    Button(
                        onClick = onCallClick,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HomeV2Colors.Saffron,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Call,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = tr(code, "कॉल", "Call", "Call"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 6. "रोज़ की साधना": four light-saffron tool tiles.
 */
@Composable
fun HomeV2DailySadhana(
    onJapaClick: () -> Unit,
    onPanchangClick: () -> Unit,
    onKundliClick: () -> Unit,
    onDreamsClick: () -> Unit,
    language: String,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    val code = homeLangCode(language, isHindi)
    val tools = listOf(
        Triple(R.drawable.ic_home_mala, tr(code, "जप माला", "Jap mala", "Japa mala"), onJapaClick),
        Triple(R.drawable.ic_home_panchang, tr(code, "पंचांग", "Panchang", "Panchang"), onPanchangClick),
        Triple(R.drawable.ic_home_kundli, tr(code, "कुंडली", "Kundli", "Kundli"), onKundliClick),
        Triple(R.drawable.ic_home_dream, tr(code, "सपने", "Sapne", "Dreams"), onDreamsClick)
    )
    Column(modifier = modifier.fillMaxWidth()) {
        HomeV2SectionTitle(title = tr(code, "रोज़ की साधना", "Roz ki saadhna", "Daily sadhana"))
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            tools.chunked(2).forEach { rowTools ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowTools.forEach { (icon, label, onClick) ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(HomeV2Colors.SaffronLight)
                                .clickable { onClick() }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = icon),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = label,
                                fontFamily = AppFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = HomeV2Colors.SaffronDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
