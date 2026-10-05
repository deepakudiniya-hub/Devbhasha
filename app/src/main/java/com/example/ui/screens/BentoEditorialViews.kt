package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Bento Editorial Masthead (Logo + DEV title + 3-Segment Language Toggle (HIN/ENG/HGL) + Saffron Balance Button)
 */
@Composable
fun BentoEditorialMasthead(
    balanceAmount: Double,
    language: String = "en",
    isHindi: Boolean = (language == "hi"),
    onToggleLanguage: () -> Unit = {},
    onLanguageSelect: ((String) -> Unit)? = null,
    onSelectLanguage: ((String) -> Unit)? = null,
    onBalanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveSelect = onLanguageSelect ?: onSelectLanguage
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // DEV Logo & Text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Squircle Dev Icon
            Surface(
                modifier = Modifier.size(34.dp),
                shape = RoundedCornerShape(11.dp),
                color = Saffron, // Strict Saffron (#FF6B00) Logo
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "देव",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Text(
                text = when (currentLangCode) {
                    "hi" -> "देव भाषा"
                    "hgl" -> "Dev Bhasha"
                    else -> "D E V B H A S H A"
                },
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = if (currentLangCode == "hi") 16.5.sp else 14.5.sp,
                letterSpacing = when (currentLangCode) {
                    "hi" -> 0.5.sp
                    "hgl" -> 1.sp
                    else -> 1.8.sp
                },
                color = Ink
            )
        }

        // Right Action Group: Saffron Balance Pill Button
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Terra,
            shadowElevation = 2.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable { onBalanceClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "₹${balanceAmount.toInt()}",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default
                )
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add money",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
fun BentoEditorialRecommendationPanel(
    userName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF3E8FF), // Soft Indigo
        border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🔮", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Good Morning $userName!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4C1D95)
                )
                Text(
                    text = "Try Ananya's Top Pick ritual",
                    fontSize = 12.sp,
                    color = Color(0xFF6D28D9)
                )
            }
        }
    }
}

/**
 * Editorial Search / Ask Bar (Hairline line with minimal prompt & round ink Go button)
 */
@Composable
fun BentoEditorialAskBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onGoClick: (String) -> Unit,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Underline Hairline Input Field
        Row(
            modifier = Modifier
                .weight(1f)
                .drawBehind {
                    val strokeWidth = 1.5.dp.toPx()
                    drawLine(
                        color = Ink,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                }
                .padding(bottom = 6.dp, top = 4.dp, start = 2.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = InkSoft,
                modifier = Modifier.size(17.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> "कुछ भी पूछें — पंचांग, उपाय, साधना…"
                            "hgl" -> "Kuch bhi poochein — panchang, upay, sadhna…"
                            else -> "Ask anything — plan, track, book…"
                        },
                        color = InkFaint,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.5.sp
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Ink,
                        fontSize = 13.5.sp,
                        fontFamily = FontFamily.Default
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onGoClick(query) }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Circular Black Go Button (→)
        Surface(
            shape = CircleShape,
            color = Ink,
            shadowElevation = 2.dp,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable { onGoClick(query) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Rotated or forward
                    contentDescription = "Go",
                    tint = Color.White,
                    modifier = Modifier
                        .size(17.dp)
                        .rotate(180f)
                )
            }
        }
    }
}

/**
 * Today's Panchang Headline & DayBlock Calendar Box
 */
@Composable
fun BentoEditorialPanchangHeadline(
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val calendar = Calendar.getInstance()
    val monthName = SimpleDateFormat("MMM", if (currentLangCode == "hi") Locale("hi", "IN") else Locale.ENGLISH).format(calendar.time)
    val dayNum = SimpleDateFormat("dd", Locale.ENGLISH).format(calendar.time)
    val dayOfWeek = SimpleDateFormat("EEEE", if (currentLangCode == "hi") Locale("hi", "IN") else Locale.ENGLISH).format(calendar.time)

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        // Headline Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> "आज का "
                            "hgl" -> "Aaj ka "
                            else -> "Today's "
                        },
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 25.sp,
                        lineHeight = 28.sp,
                        color = Ink
                    )
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> "पंचांग।"
                            "hgl" -> "panchang."
                            else -> "panchang."
                        },
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 25.sp,
                        lineHeight = 28.sp,
                        color = Ink
                    )
                }

                // Decorative Subtle Squiggle Line
                Canvas(modifier = Modifier.size(width = 90.dp, height = 8.dp)) {
                    val path = Path()
                    path.moveTo(0f, size.height / 2)
                    var x = 0f
                    val waveLength = 16f
                    val waveHeight = 3.5f
                    while (x < size.width) {
                        path.relativeQuadraticTo(waveLength / 4, -waveHeight, waveLength / 2, 0f)
                        path.relativeQuadraticTo(waveLength / 4, waveHeight, waveLength / 2, 0f)
                        x += waveLength
                    }
                    drawPath(
                        path = path,
                        color = BorderMedium,
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "सूर्योदय 6:12 · सूर्यास्त 6:32"
                        "hgl" -> "Suryoday 6:12 · Suryast 6:32"
                        else -> "Sunrise 6:12 · Sunset 6:32"
                    },
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = InkSoft,
                    fontWeight = FontWeight.Medium
                )
            }

            // DayBlock Calendar Box Widget
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PaperCard,
                border = BorderStroke(1.dp, EditorialLineStrong),
                shadowElevation = 2.dp,
                modifier = Modifier.width(44.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Minimal Ink Month header
                    Surface(
                        color = Ink,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = monthName.uppercase(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Text(
                        text = dayNum,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Text(
                        text = dayOfWeek.take(3).uppercase(),
                        fontSize = 7.sp,
                        letterSpacing = 1.sp,
                        color = InkSoft,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = EditorialLine, thickness = 1.dp)
        Spacer(modifier = Modifier.height(6.dp))

        // Day Stats Strip with ✦ separator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (currentLangCode == "hi") "तिथि " else "Tithi ", fontSize = 10.sp, color = InkSoft)
                Text(text = if (currentLangCode == "hi") "शुक्ल दशमी" else "Shukla Dashami", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Text(text = "✦", fontSize = 9.sp, color = InkSoft)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (currentLangCode == "hi") "नक्षत्र " else "Nakshatra ", fontSize = 10.sp, color = InkSoft)
                Text(text = if (currentLangCode == "hi") "अनुराधा" else "Anuradha", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Text(text = "✦", fontSize = 9.sp, color = InkSoft)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (currentLangCode == "hi") "योग " else "Yoga ", fontSize = 10.sp, color = InkSoft)
                Text(text = if (currentLangCode == "hi") "सिद्ध" else "Siddha", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = EditorialLine, thickness = 1.dp)
    }
}

/**
 * Grouped Tools Grid (Poochho, Dreams, and Circular Quick Tools Row)
 */
@Composable
fun BentoEditorialToolsGrid(
    currentView: String,
    onPoochhoClick: () -> Unit,
    onDreamsClick: () -> Unit,
    onKundliClick: () -> Unit,
    onTarotClick: () -> Unit,
    onMatchClick: () -> Unit,
    onHabitsClick: () -> Unit,
    onTimerClick: () -> Unit,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Poochho Tile (01) & Dreams Tile (02)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tile 1: Poochho (01)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderLight),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(118.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onPoochhoClick() }
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Text(
                        text = "01",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = Saffron,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "पूछा (Poocha)"
                                    "hgl" -> "Poocha"
                                    else -> "Poocha"
                                },
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF000000)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "चैट व कॉल द्वारा\nपरामर्श प्राप्त करें"
                                    "hgl" -> "Consult through\nchat and call"
                                    else -> "Get consultation via\nchat and call"
                                },
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                color = Color(0xFF737373)
                            )
                        }
                        Text(text = "💬", fontSize = 26.sp)
                    }
                }
            }

            // Tile 2: Dreams (02)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderLight),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(118.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onDreamsClick() }
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Text(
                        text = "02",
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontSize = 13.sp,
                        color = Saffron,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dreams",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF000000)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Decode your subconscious\n& align with divine wisdom",
                                fontSize = 10.sp,
                                lineHeight = 13.sp,
                                color = Color(0xFF737373)
                            )
                        }
                        Text(text = "🌙✨", fontSize = 24.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Section Header for Problem & Vedic Remedies Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 3.5.dp, height = 15.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Saffron)
                )
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "वैदिक समस्या व समाधान"
                        "hgl" -> "Vedic Problem Remedies"
                        else -> "Vedic Problem Remedies"
                    },
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Surface(
                color = Color(0xFFFAFAFA),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "5 प्रमुख विषय"
                        "hgl" -> "5 Topics"
                        else -> "5 Topics"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Saffron,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontal Row for Problem Cards (All 5 visible)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item {
                BentoProblemCategoryCard(
                    title = if (currentLangCode == "hi") "पारिवारिक समस्या" else "Family",
                    subtitle = if (currentLangCode == "hi") "शांति उपाय" else "Harmony",
                    emoji = "👨‍👩‍👧‍👦",
                    badge = if (currentLangCode == "hi") "समाधान" else "Remedies",
                    isSelected = currentView == "kundli",
                    onClick = onKundliClick,
                    modifier = Modifier.width(120.dp)
                )
            }
            item {
                BentoProblemCategoryCard(
                    title = if (currentLangCode == "hi") "स्वास्थ्य समस्या" else "Health",
                    subtitle = if (currentLangCode == "hi") "रोग निवारण" else "Healing",
                    emoji = "🩺",
                    badge = if (currentLangCode == "hi") "आरोग्य" else "Healing",
                    isSelected = currentView == "tarot",
                    onClick = onTarotClick,
                    modifier = Modifier.width(120.dp)
                )
            }
            item {
                BentoProblemCategoryCard(
                    title = if (currentLangCode == "hi") "धन समस्या" else "Money",
                    subtitle = if (currentLangCode == "hi") "कर्ज मुक्ति" else "Wealth",
                    emoji = "💰",
                    badge = if (currentLangCode == "hi") "समृद्धि" else "Wealth",
                    isSelected = currentView == "match",
                    onClick = onMatchClick,
                    modifier = Modifier.width(120.dp)
                )
            }
            item {
                BentoProblemCategoryCard(
                    title = if (currentLangCode == "hi") "नकारात्मकता" else "Negativity",
                    subtitle = if (currentLangCode == "hi") "बुरी नज़र" else "Shield",
                    emoji = "🧿",
                    badge = if (currentLangCode == "hi") "सुरक्षा" else "Shield",
                    isSelected = currentView == "habits",
                    onClick = onHabitsClick,
                    modifier = Modifier.width(120.dp)
                )
            }
            item {
                BentoProblemCategoryCard(
                    title = if (currentLangCode == "hi") "पितृ दोष" else "Pitr Dosh",
                    subtitle = if (currentLangCode == "hi") "पूर्वज शांति" else "Blessings",
                    emoji = "🪔",
                    badge = if (currentLangCode == "hi") "तर्पण" else "Tarpan",
                    isSelected = currentView == "timer",
                    onClick = onTimerClick,
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}

/**
 * Promo Banner positioned right above Experts on Call
 * Redesigned as a modern, high-contrast voucher coupon card (Pure White, Black, Saffron accent)
 */
@Composable
fun BentoEditorialPromoBanner(
    isOfferClaimed: Boolean,
    onClaimOffer: () -> Unit,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(
                1.2.dp,
                if (isOfferClaimed) Color(0xFF16A34A).copy(alpha = 0.5f) else Saffron.copy(alpha = 0.5f)
            ),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable {
                    onClaimOffer()
                    if (!isOfferClaimed) {
                        Toast.makeText(
                            context,
                            if (currentLangCode == "hi") "🎉 कूपन DEV100 सक्रिय! 15 मिनट मुफ़्त परामर्श मिला" else "🎉 Coupon DEV100 applied! 15 mins free consultation activated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Details & Coupon Code Tag
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Badge Tag
                        Surface(
                            color = if (isOfferClaimed) Color(0xFFDCFCE7) else Color(0xFFFFF7ED),
                            border = BorderStroke(
                                1.dp,
                                if (isOfferClaimed) Color(0xFF86EFAC) else Saffron.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> if (isOfferClaimed) "✓ सक्रिय कूपन • 15 MIN" else "🎁 स्वागत कूपन • 100% मुफ़्त"
                                    "hgl" -> if (isOfferClaimed) "✓ ACTIVE COUPON • 15 MIN" else "🎁 WELCOME OFFER • 100% FREE"
                                    else -> if (isOfferClaimed) "✓ ACTIVE COUPON • 15 MIN" else "🎁 WELCOME PASS • 100% FREE"
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOfferClaimed) Color(0xFF16A34A) else Saffron,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        // Code Tag
                        Surface(
                            color = Color(0xFFFAFAFA),
                            border = BorderStroke(1.dp, BorderLight),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "DEV100",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = Saffron,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    // Title
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> if (isOfferClaimed) "15 निःशुल्क मिनट सक्रिय हैं ✨" else "प्रथम 15 मिनट परामर्श 100% मुफ़्त"
                            "hgl" -> if (isOfferClaimed) "15 Free Mins Active ✨" else "First 15 Mins 100% Free Consult"
                            else -> if (isOfferClaimed) "15 Free Mins Active ✨" else "First 15 Mins 100% Free Consult"
                        },
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Subtitle
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> if (isOfferClaimed) "किसी भी साधक से तुरंत बात करें • बैलेंस में जुड़ा" else "सत्यापित वैदिक साधक से बात करें • कार्ड की ज़रूरत नहीं"
                            "hgl" -> if (isOfferClaimed) "Kisi bhi sadhak se baat karein • Balance added" else "Verified sadhaks se connect karein • No card needed"
                            else -> if (isOfferClaimed) "Connect with any verified sadhak • Added to balance" else "Connect with verified sadhaks • No card needed"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF737373)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Dashed Vertical Divider
                Canvas(
                    modifier = Modifier
                        .height(48.dp)
                        .width(1.dp)
                ) {
                    drawLine(
                        color = Color(0xFFE5E5E5),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right CTA Button / Claimed Pill
                if (isOfferClaimed) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Active",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (currentLangCode == "hi") "सक्रिय" else "Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Saffron,
                        shadowElevation = 2.dp,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = "Claim",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (currentLangCode == "hi") "क्लेम करें" else "Claim",
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
}

@Composable
private fun BentoProblemCategoryCard(
    title: String,
    subtitle: String,
    emoji: String,
    badge: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) Color(0xFF4C1D95) else Color.White, // Deep Purple for selected
        border = if (isSelected) null else BorderStroke(1.dp, BorderLight),
        shadowElevation = if (isSelected) 4.dp else 1.5.dp,
        modifier = modifier
            .size(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) Color(0xFF6D28D9) else Color(0xFFFAFAFA),
                border = if (isSelected) null else BorderStroke(1.dp, BorderLight),
                modifier = Modifier.size(40.dp).drawBehind {
                    // Subtle Golden Shimmer Effect
                    val strokeWidth = 2.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent),
                            radius = size.width
                        )
                    )
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp, // Reduced font size
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF1F2937), // Better contrast
                maxLines = 1, // Single line
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp, 
                color = if (isSelected) Color(0xFFDDD6FE) else Color(0xFF6B7280), // Better contrast
                maxLines = 1, // Single line
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Experts on Call Section (Double hairline rule + Featured Expert Card + Expert List rows)
 */
@Composable
fun BentoEditorialExpertsSection(
    sadhaks: List<SadhakItem>,
    onChatClick: (SadhakItem) -> Unit,
    onCallClick: (SadhakItem) -> Unit,
    onSeeAllClick: () -> Unit,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        // Section Rule Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "ऑनलाइन साधक"
                        "hgl" -> "Online Sadhak"
                        else -> "Experts on call"
                    },
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink
                )
                val liveCount = sadhaks.count { it.isOnline }
                Text(
                    text = when (currentLangCode) {
                        "hi" -> if (liveCount > 0) "$liveCount लाइव" else "सत्यापित साधक"
                        "hgl" -> if (liveCount > 0) "$liveCount live" else "Verified sadhak"
                        else -> if (liveCount > 0) "$liveCount online" else "Verified guides"
                    },
                    fontSize = 11.sp,
                    color = InkSoft
                )
            }

            Text(
                text = when (currentLangCode) {
                    "hi" -> "सभी देखें →"
                    "hgl" -> "Sabhi dekhein →"
                    else -> "See all →"
                },
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Terra,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        if (sadhaks.isEmpty()) {
            com.example.ui.components.DevEmptyState(
                title = when (currentLangCode) {
                    "hi" -> "कोई साधक ऑनलाइन उपलब्ध नहीं है"
                    "hgl" -> "Koi sadhak online nahi hai"
                    else -> "No verified sadhaks online currently."
                },
                description = when (currentLangCode) {
                    "hi" -> "साधक शीघ्र ही ऑनलाइन उपलब्ध होंगे। कृपया कुछ समय बाद पुनः देखें।"
                    "hgl" -> "Sadhak jald hi online available honge. Kripya thodi der baad try karein."
                    else -> "Verified guides will be available shortly. Please check back soon."
                },
                symbol = "🧘",
                actionText = when (currentLangCode) {
                    "hi" -> "पुनः देखें (Refresh)"
                    "hgl" -> "Refresh karein"
                    else -> "Refresh"
                },
                onActionClick = onSeeAllClick
            )
        } else {
            // Featured Expert Card (Elevated Style)
            val featuredSadhak = sadhaks.first()

            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChatClick(featuredSadhak) }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Avatar with 2px saffron ring only if online
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFAFAFA),
                            border = if (featuredSadhak.isOnline) BorderStroke(2.dp, Saffron) else BorderStroke(1.dp, BorderLight),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (currentLangCode == "hi") featuredSadhak.initialHi.ifEmpty { "सा" } else featuredSadhak.initialEn.take(2).ifEmpty { "SR" },
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF000000)
                                )
                            }
                        }
                        if (featuredSadhak.isOnline) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Saffron)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        // Stamp "TODAY'S PICK"
                        Surface(
                            color = SaffronLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "विशेष साधक"
                                    "hgl" -> "FEATURED EXPERT"
                                    else -> "FEATURED EXPERT"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = Saffron,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentLangCode == "hi") featuredSadhak.nameHi else featuredSadhak.nameEn,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF000000)
                        )
                        Text(
                            text = when (currentLangCode) {
                                "hi" -> "${featuredSadhak.titleHi} · ₹19/मिनट"
                                "hgl" -> "${featuredSadhak.titleHi} · ₹19/min"
                                else -> "${featuredSadhak.titleEn} · ₹19/min"
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFF737373),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Action Buttons: Saffron Chat + White Call
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val saffronBrush = Brush.horizontalGradient(
                            listOf(SaffronGradientStart, SaffronGradientEnd)
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(saffronBrush)
                                .clickable { onChatClick(featuredSadhak) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = "Chat",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFF000000)),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onCallClick(featuredSadhak) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = "Call",
                                    tint = Color(0xFF000000),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expert List Rows
            val displayExperts = sadhaks.filter { it.id != featuredSadhak.id }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF1F1F1), RoundedCornerShape(20.dp))
            ) {
                displayExperts.take(3).forEach { expert ->
                    BentoEditorialExpertRow(
                        sadhak = expert,
                        isHindi = isHindi,
                        language = language,
                        onChatClick = { onChatClick(expert) },
                        onCallClick = { onCallClick(expert) }
                    )
                }
            }
        }
    }
}

@Composable
fun BentoEditorialExpertRow(
    sadhak: SadhakItem,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChatClick() }
                .padding(vertical = 12.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar with 2px saffron ring only if online
            Box {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFAFAFA),
                    border = if (sadhak.isOnline) BorderStroke(2.dp, Saffron) else BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (currentLangCode == "hi") sadhak.initialHi.ifEmpty { "सा" } else sadhak.initialEn.take(2).ifEmpty { "EX" },
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                    }
                }

                if (sadhak.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Saffron)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (currentLangCode == "hi") sadhak.nameHi else sadhak.nameEn,
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink
                )
                Text(
                    text = if (currentLangCode == "hi") sadhak.titleHi else (if (currentLangCode == "hgl") sadhak.titleHi else sadhak.titleEn),
                    fontSize = 10.5.sp,
                    color = InkSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B), // Material 3 Amber
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = sadhak.rating,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            }

            // Price
            Text(
                text = "₹${when(sadhak.id) { "am" -> 12; "ri" -> 19; else -> 25 }}/${if (currentLangCode == "hi") "मिनट" else "min"}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            // Mini Actions: Call & Accent Chat Button
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, EditorialLineStrong),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onCallClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = "Call",
                            tint = Ink,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Terra,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onChatClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = "Chat",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = EditorialLine, thickness = 1.dp)
    }
}

/**
 * Floating Pill Bottom Dock (Clean Pure White container with soft shadow & Saffron highlights)
 */
@Composable
fun BentoEditorialDock(
    currentTab: String,
    onTabSelect: (String) -> Unit,
    hasUnreadSadhak: Boolean = true,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val tabs = listOf(
            Triple("today", if (currentLangCode == "hi") "आज" else "Today", Icons.Outlined.Home),
            Triple("sadhak", if (currentLangCode == "hi") "साधक" else "Sadhak", Icons.AutoMirrored.Outlined.Chat),
            Triple("remedy", if (currentLangCode == "hi") "उपाय" else "Remedy", Icons.Outlined.ShoppingBag),
            Triple("profile", if (currentLangCode == "hi") "प्रोफ़ाइल" else "Profile", Icons.Outlined.Person)
        )

        tabs.forEach { (tab, label, icon) ->
            val selectedIcon = when(tab) {
                "today" -> Icons.Filled.Home
                "sadhak" -> Icons.AutoMirrored.Filled.Chat
                "remedy" -> Icons.Filled.ShoppingBag
                else -> Icons.Filled.Person
            }
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelect(tab) },
                icon = {
                    Icon(
                        imageVector = if (currentTab == tab) selectedIcon else icon,
                        contentDescription = label
                    )
                },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GoogleBlue.copy(alpha = 0.2f),
                    selectedIconColor = GoogleBlue,
                    selectedTextColor = GoogleBlue
                )
            )
        }
    }
}

