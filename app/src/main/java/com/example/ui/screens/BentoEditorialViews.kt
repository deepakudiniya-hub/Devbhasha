package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.pager.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.components.pressFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.*
import com.example.ui.theme.*
import com.example.ui.components.DevLogoIcon
import com.example.ui.components.DevWatermarkLogo
import com.example.utils.HapticFeedbackHelper
import com.example.utils.UserSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Bento Editorial Masthead
// English comment placeholder
 */
@Composable
fun BentoEditorialMasthead(
    balanceAmount: Double,
    userName: String = "Devotee",
    avatarId: String = "avatar_01",
    language: String = "en",
    isHindi: Boolean = (language == "hi"),
    onProfileClick: () -> Unit = {},
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
// English comment placeholder
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "देवभाषा",
                style = TextStyle(
                    fontFamily = TiroDevanagariHindi,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right Action Group: Wallet Chip + Profile Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Translucent White Wallet Chip on Teal header
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                modifier = Modifier
                    .clip(CircleShape)
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
                        fontFamily = NotoSansDevanagari,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add money",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
            
            // Profile / Sadhak Avatar Icon Button
            val context = LocalContext.current
            val userSession = remember { UserSession(context) }
            val effectiveAvatarId = avatarId.ifBlank { userSession.getAvatarId() }
            val avatarDrawable = getAvatarDrawableRes(effectiveAvatarId)

            Surface(
                shape = CircleShape,
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.85f)),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() }
            ) {
                Image(
                    painter = painterResource(id = avatarDrawable),
                    contentDescription = "प्रोफ़ाइल अवतार",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
// English comment placeholder
 */
@Composable
fun BentoEditorialPoochhaHeroCard(
    onAskClick: () -> Unit,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 3.dp,
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onAskClick() }
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Icon + Title + Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🕉️", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "पूछें",
                                fontFamily = TiroDevanagariHindi,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "— सीधा मार्गदर्शन",
                                fontFamily = NotoSansDevanagari,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (currentLangCode == "hi") "सत्यापित Sadhak से व्यक्तिगत समाधान पाएं" else "Personal spiritual guidance from verified sadhak",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live Online Pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (currentLangCode == "hi") "लाइव" else "LIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Input Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAskClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (currentLangCode == "hi") "विवाह, करियर, स्वास्थ्य या कोई भी प्रश्न पूछें..." else "Ask about marriage, career, health or any question...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Ask",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Topic Tags Horizontal Scroll (Chips)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val topics = listOf(
                    "💍 विवाह",
                    "💼 करियर",
                    "🪙 व्यापार",
                    "🌿 स्वास्थ्य",
                    "👨‍👩‍👧 परिवार",
                    "💰 धन/वित्त",
                    "🔮 कुंडली"
                )
                items(topics) { topic ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onAskClick() }
                    ) {
                        Text(
                            text = topic,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
// English comment placeholder
 */
@Composable
fun BentoEditorialRecommendationPanel(
    userName: String = "Devotee",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedMuhuratIndex by remember { mutableStateOf(0) }
    val muhuratList = remember {
        listOf(
            Triple(
                "अभिजित मुहूर्त",
                "11:45 AM – 12:35 PM",
                "सर्वोत्तम शुभ समय • नवीन कार्य, गृह प्रवेश एवं महत्वपूर्ण निर्णयों हेतु उत्तम"
            ),
            Triple(
                "गोधूलि मुहूर्त",
                "06:15 PM – 06:40 PM",
                "संध्या काल • संध्या पूजन, दीपदान एवं पारिवारिक शांति हेतु अत्यंत शुभ"
            ),
            Triple(
                "अमृत काल",
                "02:10 AM – 03:45 AM",
                "सिद्धि योग • साधना, मंत्र जप एवं आध्यात्मिक अनुष्ठानों के लिए विशेष फलदायी"
            )
        )
    }

    val currentMuhurat = muhuratList[selectedMuhuratIndex]

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFFF1EDE6)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable {
                selectedMuhuratIndex = (selectedMuhuratIndex + 1) % muhuratList.size
                onClick()
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
// English comment placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "Clock",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Text(
                        text = "आज का शुभ मुहूर्त",
                        fontFamily = AppFontFamily,
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "शुक्र मुहूर्त ⭐️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Centered Muhurat Name & Time
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = currentMuhurat.first,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentMuhurat.second,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Description
            Text(
                text = currentMuhurat.third,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

// English comment placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pagination Dots
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    muhuratList.indices.forEach { index ->
                        val isSelected = index == selectedMuhuratIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 14.dp else 6.dp, 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                                .clickable { selectedMuhuratIndex = index }
                        )
                    }
                }

// English comment placeholder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DevLogoIcon(size = 18.dp, elevation = 0.dp)
                        Text(
                            text = "देव भाषा",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable {
                                selectedMuhuratIndex = (selectedMuhuratIndex + 1) % muhuratList.size
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
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
 * Today's Panchang Headline & DayBlock Calendar Box (Compact & Sleek)
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

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
        // Compact Headline Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> "आज का "
                            "hgl" -> "Aaj ka "
                            else -> "Today's "
                        },
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 19.sp,
                        lineHeight = 22.sp,
                        color = Ink
                    )
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> "पंचांग"
                            "hgl" -> "panchang"
                            else -> "panchang"
                        },
                        fontFamily = AppFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 19.sp,
                        lineHeight = 22.sp,
                        color = Ink
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "सूर्योदय 6:12 · सूर्यास्त 6:32"
                        "hgl" -> "Suryoday 6:12 · Suryast 6:32"
                        else -> "Sunrise 6:12 · Sunset 6:32"
                    },
                    fontSize = 9.5.sp,
                    letterSpacing = 0.3.sp,
                    color = InkSoft,
                    fontWeight = FontWeight.Medium
                )
            }

            // Compact DayBlock Calendar Box Widget
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PaperCard,
                border = BorderStroke(1.dp, EditorialLineStrong),
                shadowElevation = 1.dp,
                modifier = Modifier.width(36.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = Ink,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = monthName.uppercase(),
                            color = Color.White,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(vertical = 1.5.dp)
                        )
                    }

                    Text(
                        text = dayNum,
                        fontFamily = AppFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink,
                        modifier = Modifier.padding(top = 1.dp)
                    )

                    Text(
                        text = dayOfWeek.take(3).uppercase(),
                        fontSize = 6.5.sp,
                        letterSpacing = 0.5.sp,
                        color = InkSoft,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = EditorialLine, thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(4.dp))

        // Compact Day Stats Strip with ✦ separator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (currentLangCode == "hi") "तिथि " else "Tithi ", fontSize = 9.5.sp, color = InkSoft)
                Text(text = if (currentLangCode == "hi") "शुक्ल दशमी" else "Shukla Dashami", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Text(text = "✦", fontSize = 8.sp, color = InkSoft)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (currentLangCode == "hi") "नक्षत्र " else "Nakshatra ", fontSize = 9.5.sp, color = InkSoft)
                Text(text = if (currentLangCode == "hi") "अनुराधा" else "Anuradha", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Text(text = "✦", fontSize = 8.sp, color = InkSoft)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (currentLangCode == "hi") "योग " else "Yoga ", fontSize = 9.5.sp, color = InkSoft)
                Text(text = if (currentLangCode == "hi") "सिद्ध" else "Siddha", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = EditorialLine, thickness = 0.8.dp)
    }
}

/**
// English comment placeholder
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
// English comment placeholder
        Text(
            text = when (currentLangCode) {
                "hi" -> "त्वरित सेवाएं"
                "hgl" -> "Quick Services"
                else -> "Quick Services"
            },
            fontFamily = AppFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B2B)
        )

        // 2x2 Grid
// English comment placeholder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoQuickActionCard(
                title = when (currentLangCode) {
                    "hi" -> "पूछो"
                    "hgl" -> "Poochho"
                    else -> "Ask Expert"
                },
                subtitle = when (currentLangCode) {
                    "hi" -> "विशेषज्ञ से परामर्श"
                    "hgl" -> "Consult Expert"
                    else -> "Consult Expert"
                },
                icon = Icons.AutoMirrored.Outlined.Chat,
                onClick = onPoochhoClick,
                modifier = Modifier.weight(1f)
            )

            BentoQuickActionCard(
                title = when (currentLangCode) {
                    "hi" -> "स्वप्न विचार"
                    "hgl" -> "Dream Insights"
                    else -> "Dream Insights"
                },
                subtitle = when (currentLangCode) {
                    "hi" -> "अचेतन के संकेत"
                    "hgl" -> "Unconscious signs"
                    else -> "Unconscious signs"
                },
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                onClick = onDreamsClick,
                modifier = Modifier.weight(1f)
            )
        }

// English comment placeholder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoQuickActionCard(
                title = when (currentLangCode) {
                    "hi" -> "कुंडली"
                    "hgl" -> "Kundli"
                    else -> "Kundli"
                },
                subtitle = when (currentLangCode) {
                    "hi" -> "दोष व फलादेश"
                    "hgl" -> "Dosha & Guidance"
                    else -> "Dosha & Guidance"
                },
                icon = Icons.Outlined.Stars,
                onClick = onKundliClick,
                modifier = Modifier.weight(1f)
            )

            BentoQuickActionCard(
                title = when (currentLangCode) {
                    "hi" -> "पंचांग"
                    "hgl" -> "Panchang"
                    else -> "Panchang"
                },
                subtitle = when (currentLangCode) {
                    "hi" -> "दैनिक शुभ मुहूर्त"
                    "hgl" -> "Daily Auspicious Time"
                    else -> "Daily Auspicious Time"
                },
                icon = Icons.Outlined.CalendarMonth,
                onClick = onTarotClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Horizontal Scrollable Row for Circular Problem & Category Cards
 */
@Composable
fun BentoProblemCategoryRow(
    onCategoryClick: (String) -> Unit,
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {


        // Horizontal Row for 5 Solution Cards (Compact, Balanced & Fully Visible)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BentoProblemCategoryCard(
                title = if (currentLangCode == "hi") "स्वास्थ्य" else "Health",
                icon = Icons.Outlined.Favorite,
                iconTint = Color(0xFFE5534B), // Coral/red accent
                onClick = { onCategoryClick("health") },
                modifier = Modifier.weight(1f)
            )
            BentoProblemCategoryCard(
                title = if (currentLangCode == "hi") "परिवार" else "Family",
                icon = Icons.Outlined.People,
                iconTint = Color(0xFF0A4D48), // Dark teal
                onClick = { onCategoryClick("family") },
                modifier = Modifier.weight(1f)
            )
            BentoProblemCategoryCard(
                title = if (currentLangCode == "hi") "दोष" else "Negativity",
                icon = Icons.Outlined.Bolt,
                iconTint = Color(0xFF0A4D48), // Dark teal
                onClick = { onCategoryClick("negativity") },
                modifier = Modifier.weight(1f)
            )
            BentoProblemCategoryCard(
                title = if (currentLangCode == "hi") "धन" else "Wealth",
                icon = Icons.Outlined.TrendingUp,
                iconTint = Color(0xFF0A4D48), // Dark teal
                onClick = { onCategoryClick("wealth") },
                modifier = Modifier.weight(1f)
            )
            BentoProblemCategoryCard(
                title = if (currentLangCode == "hi") "पितृ" else "Spirits",
                icon = Icons.Outlined.AutoAwesome,
                iconTint = Color(0xFF0A4D48), // Dark teal
                onClick = { onCategoryClick("spirits") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Standardized Quick Action Card for the 2x2 Grid
 * Enhanced with soft warm container, increased icon size & touch area, subtle border and depth.
 */
@Composable
private fun BentoQuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFFF1EDE6)),
        modifier = modifier
            .height(98.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Soft teal circular wrapped container
            Surface(
                shape = CircleShape,
                color = Color(0xFFE0F2F1), // soft teal background
                border = BorderStroke(1.dp, Color(0xFFCCFBF1)),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFF125157),
                        modifier = Modifier.size(26.dp) // increased icon size
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B2B2B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF6E6E6E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Promotional Coupon Card: Luxury Gold-Saffron Perforated Voucher Banner with 2D Namaste Woman Illustration.
// English comment placeholder
 * Right: Glowing halo 2D Namaste vector illustration with verified rating badge.
 * Includes subtle entrance animation (fade + scale + slide-up + golden light sheen) for an engaging interactive feel.
 */
@Composable
fun BentoEditorialPromoBanner(
    isOfferClaimed: Boolean = false,
    onClaimOffer: () -> Unit = {},
    onTalkNowClick: () -> Unit = onClaimOffer,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val indication = androidx.compose.material3.ripple()

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF083A35), Color(0xFF0F5C5C))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = indication,
                onClick = { onTalkNowClick() }
            )
            .testTag("promo_banner_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: 44.dp circular badge with golden border and dark teal background displaying bold gold text "AI"
            Surface(
                shape = CircleShape,
                color = Color(0xFF083A35),
                border = BorderStroke(1.5.dp, Color(0xFF0F5C5C)),
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F5C5C)
                    )
                }
            }

// English comment placeholder
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "AI साधना",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F5C5C)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "नवरात्रि विशेष - दैनिक साधना में जुड़ें और पुण्य लाभ पाएं।",
                    fontSize = 12.sp,
                    color = Color.White,
                    lineHeight = 16.sp
                )
            }

// English comment placeholder
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF5C443),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onTalkNowClick() }
                    .testTag("participate_promo_button")
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "भाग लें",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF083A35)
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoProblemCategoryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = Color(0xFF0A4D48),
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = if (isPressed) 1.dp else 2.dp,
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF0A4D48) else Color(0xFFE2E8F0)),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFF0A4D48)),
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFDDF3EF), // Soft mint-teal background
                modifier = Modifier.size(58.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        // Section Header: Verified Astrologers + Online Badge + See All
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "सत्यापित Sadhak"
                        "hgl" -> "Verified Sadhak"
                        else -> "Verified Sadhak"
                    },
                    fontFamily = AppFontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B2B2B)
                )
                val liveCount = sadhaks.count { it.isOnline }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = if (liveCount > 0) "🟢 $liveCount ऑनलाइन" else "🟢 ऑनलाइन",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }
            }

            Text(
                text = when (currentLangCode) {
                    "hi" -> "सभी देखें →"
                    "hgl" -> "Sabhi dekhein →"
                    else -> "See all →"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF125157),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSeeAllClick() }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        if (sadhaks.isEmpty()) {
            com.example.ui.components.DevEmptyState(
                title = when (currentLangCode) {
                    "hi" -> "कोई Sadhak ऑनलाइन उपलब्ध नहीं है"
                    "hgl" -> "Koi sadhak online nahi hai"
                    else -> "No verified sadhaks online currently."
                },
                description = when (currentLangCode) {
                    "hi" -> "Sadhak शीघ्र ही ऑनलाइन उपलब्ध होंगे। कृपया कुछ समय बाद पुनः देखें।"
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
            // Horizontal list of Astrologers with photo, rating, and online badge (Trust Factor)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sadhaks) { sadhak ->
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_alpha"
                    )

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        border = BorderStroke(1.dp, Color(0xFFDCCFB4).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .width(155.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .pressFeedback(
                                onClick = { onChatClick(sadhak) }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar with Pulsing Online Badge
                            Box {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = sadhak.getAvatarRes()),
                                        contentDescription = sadhak.nameHi,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (sadhak.isOnline) Color(0xFF16A34A).copy(alpha = pulseAlpha) else Color(0xFF94A3B8))
                                        .border(2.dp, Color.White, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Online/Offline badge
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (sadhak.isOnline) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (sadhak.isOnline) Color(0xFF16A34A) else Color(0xFF94A3B8))
                                    )
                                    Text(
                                        text = if (sadhak.isOnline) "Online" else "Offline",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sadhak.isOnline) Color(0xFF15803D) else Color(0xFF64748B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (currentLangCode == "hi") sadhak.nameHi else sadhak.nameEn,
                                fontFamily = AppFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2B2B2B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = if (currentLangCode == "hi") sadhak.titleHi else sadhak.titleEn,
                                fontSize = 11.sp,
                                color = Color(0xFF6E6E6E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            // Rating & Price
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFD0BF75),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${sadhak.rating} ★",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2B2B2B)
                                )
                                Text(
                                    text = "• " + com.example.utils.PriceLabels.SESSION,
                                    fontSize = 11.sp,
                                    color = Color(0xFF125157),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action Button: Call & Chat
                            Button(
                                onClick = { onCallClick(sadhak) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "बातचीत करें",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

// English comment placeholder
            val featuredSadhak = sadhaks.first()

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Image(
                                painter = painterResource(id = featuredSadhak.getAvatarRes()),
                                contentDescription = featuredSadhak.nameHi,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        if (featuredSadhak.isOnline) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentLangCode == "hi") featuredSadhak.nameHi else featuredSadhak.nameEn,
                            fontFamily = AppFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2B2B2B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${if (currentLangCode == "hi") featuredSadhak.titleHi else featuredSadhak.titleEn} • ${featuredSadhak.rating} ★",
                            fontSize = 12.5.sp,
                            color = Color(0xFF6E6E6E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

// English comment placeholder
                    Button(
                        onClick = { onChatClick(featuredSadhak) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF125157),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "बातचीत करें",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Secondary Outlined Call Button
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onCallClick(featuredSadhak) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = "Call",
                                tint = Color(0xFF2B2B2B),
                                modifier = Modifier.size(16.dp)
                            )
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
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
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
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.size(46.dp)
                ) {
                    Image(
                        painter = painterResource(id = sadhak.getAvatarRes()),
                        contentDescription = sadhak.nameHi,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (sadhak.isOnline) Color(0xFF16A34A) else Color(0xFF94A3B8))
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (currentLangCode == "hi") sadhak.nameHi else sadhak.nameEn,
                        fontFamily = AppFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2B2B2B)
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (sadhak.isOnline) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (sadhak.isOnline) Color(0xFF16A34A) else Color(0xFF94A3B8))
                            )
                            Text(
                                text = if (sadhak.isOnline) "Online" else "Offline",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sadhak.isOnline) Color(0xFF15803D) else Color(0xFF64748B)
                            )
                        }
                    }
                }
                Text(
                    text = if (currentLangCode == "hi") sadhak.titleHi else sadhak.titleEn,
                    fontSize = 11.sp,
                    color = Color(0xFF6E6E6E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = sadhak.rating,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B2B2B)
                )
            }

            // Price
            Text(
                text = "₹${when(sadhak.id) { "am" -> 12; "ri" -> 19; else -> 25 }}/${if (currentLangCode == "hi") "मिनट" else "min"}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF125157)
            )

            // Outline Actions (No filled orange button)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onCallClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = "Call",
                            tint = Color(0xFF2B2B2B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color(0xFF125157)),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onChatClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = "Chat",
                            tint = Color(0xFF125157),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
    }
}

/**
// English comment placeholder
 * Active tab has filled icon and primary deep saffron color.
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

    val primaryColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        val tabs = listOf(
            Triple(
                "today",
                when (currentLangCode) {
                    "hi" -> "होम"
                    "hgl" -> "Home"
                    else -> "Home"
                },
                Pair(Icons.Filled.Home, Icons.Outlined.Home)
            ),
            Triple(
                "sadhak",
                when (currentLangCode) {
                    "hi" -> "साधक"
                    "hgl" -> "Sadhak"
                    else -> "Sadhak"
                },
                Pair(Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement)
            ),
            Triple(
                "dreams",
                when (currentLangCode) {
                    "hi" -> "स्वप्न"
                    "hgl" -> "Dream"
                    else -> "Dream"
                },
                Pair(Icons.Filled.Bedtime, Icons.Outlined.Bedtime)
            ),
            Triple(
                "remedy",
                when (currentLangCode) {
                    "hi" -> "उपाय"
                    "hgl" -> "Remedy"
                    else -> "Remedy"
                },
                Pair(Icons.Filled.AutoFixHigh, Icons.Outlined.AutoFixHigh)
            )
        )

        tabs.forEach { (tab, label, iconsPair) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(tab) },
                alwaysShowLabel = true,
                icon = {
                    Box(modifier = Modifier.padding(bottom = 2.dp)) {
                        Icon(
                            imageVector = if (isSelected) iconsPair.first else iconsPair.second,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontFamily = NotoSansDevanagari,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Auto-scrolling Carousel for Home Screen Banners (Navratri Vishesh & AI Sahayak)
 */
@Composable
fun HomeBannerCarousel(
    onNavratriClick: () -> Unit,
    onAiSahayakClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BentoEditorialPromoBanner(
        onTalkNowClick = onAiSahayakClick,
        modifier = modifier
    )
}


@Composable
fun BentoEditorialAskCard(
    onAiAskClick: () -> Unit,
    onSadhakAskClick: () -> Unit,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf("") }
    val topics = listOf("विवाह", "करियर", "स्वास्थ्य", "धन", "परिवार")

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F5C5C), // Teal
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFF0F5C5C).copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "अपना प्रश्न पूछें",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "विवाह, करियर, स्वास्थ्य — कुछ भी",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                placeholder = { Text("यहाँ लिखें…", fontSize = 14.sp, color = Color(0xFF5E6B68)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF1E2A2A),
                    unfocusedTextColor = Color(0xFF1E2A2A),
                    focusedBorderColor = Color(0xFF0F5C5C),
                    unfocusedBorderColor = Color(0xFFDCCFB4)
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Topic Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topics.forEach { topic ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { queryText = topic }
                    ) {
                        Text(
                            text = topic,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onAiAskClick() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5C5C)) // Gold
                ) {
                    Text(
                        text = "✨ AI से तुरंत उत्तर",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E2A2A)
                    )
                }
                OutlinedButton(
                    onClick = { onSadhakAskClick() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = "🙏 Sadhak से पूछें",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Top Online Experts Horizontal Carousel (Quick Services ke theek niche)
 */
@Composable
fun BentoEditorialOnlineSadhaksCarousel(
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

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        // Section Header: Top Online Experts + See All
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "शीर्ष ऑनलाइन साधक (Top Online Experts)"
                        "hgl" -> "Top Online Experts"
                        else -> "Top Online Experts"
                    },
                    fontFamily = AppFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B2B2B)
                )
                val liveCount = sadhaks.count { it.isOnline }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = "🟢 $liveCount ऑनलाइन",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }
            }

            Text(
                text = when (currentLangCode) {
                    "hi" -> "सभी देखें →"
                    "hgl" -> "Sabhi dekhein →"
                    else -> "See all →"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF125157),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSeeAllClick() }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        if (sadhaks.isEmpty()) {
            com.example.ui.components.DevEmptyState(
                title = "कोई Sadhak ऑनलाइन उपलब्ध नहीं है",
                description = "शीघ्र ही साधक ऑनलाइन उपलब्ध होंगे।",
                symbol = "🧘",
                actionText = "सभी साधक देखें",
                onActionClick = onSeeAllClick
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sadhaks) { sadhak ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        border = BorderStroke(1.dp, Color(0xFFDCCFB4).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .width(160.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = sadhak.getAvatarRes()),
                                        contentDescription = sadhak.nameHi,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(13.dp)
                                        .clip(CircleShape)
                                        .background(if (sadhak.isOnline) Color(0xFF16A34A) else Color(0xFF94A3B8))
                                        .border(2.dp, Color.White, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (currentLangCode == "hi") sadhak.nameHi else sadhak.nameEn,
                                fontFamily = AppFontFamily,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2B2B2B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = if (currentLangCode == "hi") sadhak.titleHi else sadhak.titleEn,
                                fontSize = 10.5.sp,
                                color = Color(0xFF6E6E6E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFD0BF75),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "${sadhak.rating}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2B2B2B)
                                )
                                Text(
                                    text = "• ₹25/min",
                                    fontSize = 10.sp,
                                    color = Color(0xFF125157),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onChatClick(sadhak) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.dp, Color(0xFF0F5C5C))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Chat,
                                        contentDescription = "Chat",
                                        tint = Color(0xFF0F5C5C),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("चैट", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F5C5C))
                                }

                                Button(
                                    onClick = { onCallClick(sadhak) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5C5C))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = "Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("कॉल", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
