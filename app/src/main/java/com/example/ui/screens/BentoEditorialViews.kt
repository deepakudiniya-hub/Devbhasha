package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.res.painterResource
import com.example.R
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
 * Layout: Left has 36x36 Dev logo + "नमस्ते, दीपक जी" with small tithi/panchang below; Right has language toggle (with globe icon) and wallet chip.
 */
@Composable
fun BentoEditorialMasthead(
    balanceAmount: Double,
    userName: String = "दीपक जी",
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
        // Left Column: App Name Title "Devbhasha"
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Devbhasha",
                fontFamily = AppFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2B2B2B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right Action Group: Saffron Balance Pill + Profile
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Saffron / Maroon Accent Balance Pill Button
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White,
                shadowElevation = 1.5.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onBalanceClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "₹${balanceAmount.toInt()}",
                        color = Color(0xFFB83A0E),
                        fontFamily = AppFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFB83A0E).copy(alpha = 0.12f),
                        modifier = Modifier.size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add money",
                                tint = Color(0xFFB83A0E),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
            
            // Profile / Sadhak Avatar Icon Button
            val context = LocalContext.current
            val userSession = remember { UserSession(context) }
            val currentAvatar = remember(userSession) { getAvatarById(userSession.getAvatarId()) }

            Surface(
                shape = CircleShape,
                color = currentAvatar.bgColor,
                border = BorderStroke(2.dp, Color(0xFFF59E0B)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = currentAvatar.symbol,
                        fontSize = 19.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Prominent Large "पूछा" Hero Card - Direct consultation with verified Vedic Sadhak
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
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, Color(0xFFFED7AA)),
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
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🕉️", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "पूछा"
                                    "hgl" -> "Poochha"
                                    else -> "Poochha"
                                },
                                fontFamily = AppFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "— " + if (currentLangCode == "hi") "सीधा मार्गदर्शन" else "Instant Answer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB83A0E)
                            )
                        }
                        Text(
                            text = if (currentLangCode == "hi") "सत्यापित साधक से व्यक्तिगत समाधान पाएं" else "Personal spiritual guidance from verified sadhak",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Live Online Pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (currentLangCode == "hi") "लाइव" else "LIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Input Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFBF9F5),
                border = BorderStroke(1.dp, Color(0xFFE7DFD5)),
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
                        tint = Color(0xFFB83A0E),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (currentLangCode == "hi") "विवाह, करियर, स्वास्थ्य या कोई भी प्रश्न पूछें..." else "Ask about marriage, career, health or any question...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFB83A0E),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Ask",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Topic Tags Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val topics = listOf(
                    "💍 विवाह",
                    "💼 करियर",
                    "🪙 व्यापार",
                    "🌿 स्वास्थ्य"
                )
                topics.forEach { topic ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onAskClick() }
                    ) {
                        Text(
                            text = topic,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9A3412),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shubh Muhurat Card: "आज का शुभ मुहूर्त" with clock icon, green badge "शुक्र मुहूर्त ⭐️", Abhijit Muhurat times, description, pagination dots, Dev logo and arrow.
 */
@Composable
fun BentoEditorialRecommendationPanel(
    userName: String = "दीपक जी",
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
            // Header Row: Clock Icon with Orange Border + Title + Green Badge "शुक्र मुहूर्त ⭐️"
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
                        color = Color(0xFFFFF2E8),
                        border = BorderStroke(1.2.dp, Color(0xFFF97316)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "Clock",
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Text(
                        text = "आज का शुभ मुहूर्त",
                        fontFamily = AppFontFamily,
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B2B2B)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Text(
                        text = "शुक्र मुहूर्त ⭐️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
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
                    color = Color(0xFFB83A0E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentMuhurat.second,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Description
            Text(
                text = currentMuhurat.third,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Saffron-colored pagination dots (3 dots) + Dev Logo + "देव भाषा" + Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Saffron Pagination Dots (3 dots)
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
                                    if (isSelected) Color(0xFFEA580C) else Color(0xFFE2E8F0)
                                )
                                .clickable { selectedMuhuratIndex = index }
                        )
                    }
                }

                // Small Dev Logo + "देव भाषा" text + Arrow
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
                            color = Color(0xFFB83A0E)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                tint = Color(0xFF475569),
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
 * 2x2 Quick Services Grid: पूछो, स्वप्न विचार, कुंडली, पंचांग
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
        // Section Header: त्वरित सेवाएं
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
        // Row 1: पूछो (विशेषज्ञ से परामर्श) & स्वप्न विचार (अचेतन के संकेत)
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

        // Row 2: कुंडली (दोष व फलादेश) & पंचांग (दैनिक शुभ मुहूर्त)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when (currentLangCode) {
                    "hi" -> "वैदिक समस्या व समाधान"
                    "hgl" -> "Vedic Problem Solutions"
                    else -> "Vedic Solutions"
                },
                fontFamily = AppFontFamily,
                fontSize = 17.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2B2B2B)
            )

            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "5 प्रमुख विषय"
                        "hgl" -> "5 Topics"
                        else -> "5 Topics"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB83A0E),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Row for 5 Solution Cards (Compact, Balanced & Fully Visible)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BentoProblemCategoryCard(
                title = if (currentLangCode == "hi") "परिवार" else "Family",
                subtitle = if (currentLangCode == "hi") "शांति" else "Peace",
                icon = Icons.Outlined.People,
                isSelected = false,
                onClick = { onCategoryClick("family") },
                modifier = Modifier.weight(1f)
            )
            BentoProblemCategoryCard(
                title = if (currentLangCode == "hi") "करियर" else "Career",
                subtitle = if (currentLangCode == "hi") "उन्नति" else "Growth",
                icon = Icons.Outlined.WorkOutline,
                isSelected = false,
                onClick = { onCategoryClick("career") },
                modifier = Modifier.weight(1f)
            )
            BentoProblemCategoryCard(
                title = if (currentLangCode == "hi") "विवाह" else "Marriage",
                subtitle = if (currentLangCode == "hi") "संबंध" else "Match",
                icon = Icons.Outlined.FavoriteBorder,
                isSelected = false,
                onClick = { onCategoryClick("marriage") },
                modifier = Modifier.weight(1f)
            )
            BentoProblemCategoryCard(
                title = if (currentLangCode == "hi") "स्वास्थ्य" else "Health",
                subtitle = if (currentLangCode == "hi") "आरोग्य" else "Cure",
                icon = Icons.Outlined.FitnessCenter,
                isSelected = false,
                onClick = { onCategoryClick("health") },
                modifier = Modifier.weight(1f)
            )
            BentoProblemCategoryCard(
                title = if (currentLangCode == "hi") "मानसिक" else "Peace",
                subtitle = if (currentLangCode == "hi") "ध्यान" else "Zen",
                icon = Icons.Outlined.SelfImprovement,
                isSelected = false,
                onClick = { onCategoryClick("peace") },
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
            // Soft light-orange/light-grey circular wrapped container
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFF2E8), // soft warm light-orange background
                border = BorderStroke(1.dp, Color(0xFFFFD8BF)),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFFB83A0E),
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
 * Left: "🎉 विशेष प्रस्ताव • 100% मुफ्त", "पहली स्वप्न चैट 5 मिनट मुफ्त" (server-decided) and "अभी बात करें" button.
 * Right: Glowing halo 2D Namaste vector illustration with verified rating badge.
 * Includes subtle entrance animation (fade + scale + slide-up + golden light sheen) for an engaging interactive feel.
 */
@Composable
fun BentoEditorialPromoBanner(
    isOfferClaimed: Boolean,
    onClaimOffer: () -> Unit,
    onTalkNowClick: () -> Unit = onClaimOffer,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    var isCopied by remember { mutableStateOf(false) }

    // Entrance animation states: smooth spring scale & fade-in slide
    val entranceAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    val sheenSweep = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.coroutineScope {
            // Trigger entrance animation with pleasant spring
            launch {
                entranceAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.78f,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    )
                )
            }
            // Subtle golden sheen light sweep on entrance
            launch {
                kotlinx.coroutines.delay(200)
                sheenSweep.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 900,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    )
                )
            }
        }
    }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(2500)
            isCopied = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .graphicsLayer {
                val progress = entranceAnim.value
                alpha = progress.coerceIn(0f, 1f)
                scaleX = 0.94f + (0.06f * progress)
                scaleY = 0.94f + (0.06f * progress)
                translationY = (1f - progress) * 28f
            }
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = (4.dp + (2.dp * entranceAnim.value)),
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFEF08A),
                        Color(0xFFF59E0B),
                        Color(0xFFFDE047),
                        Color(0xFFEA580C)
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF9A1C0E), // Deep Vedic Crimson/Saffron
                                Color(0xFFC2410C), // Rich Orange
                                Color(0xFFEA580C), // Saffron Flame
                                Color(0xFFD97706)  // Warm Amber Gold
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
            ) {
                // Background subtle sacred pattern overlay (dots/stars & entrance sheen sweep)
                Canvas(modifier = Modifier.matchParentSize()) {
                    val width = size.width
                    val height = size.height

                    // Decorative light glow on top right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x33FDE047), Color.Transparent),
                            center = Offset(width * 0.85f, height * 0.3f),
                            radius = width * 0.35f
                        )
                    )

                    // Subtle golden sheen beam sweep on entrance
                    val sheenPos = sheenSweep.value
                    if (sheenPos > 0.01f && sheenPos < 0.99f) {
                        val sweepX = width * (sheenPos * 1.4f - 0.2f)
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.22f),
                                    Color(0xFFFEF08A).copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.22f),
                                    Color.Transparent
                                ),
                                startX = sweepX - 80f,
                                endX = sweepX + 80f
                            ),
                            start = Offset(sweepX - 40f, 0f),
                            end = Offset(sweepX + 40f, height),
                            strokeWidth = 90f
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Top row: Header Offer Tag & Urgency Chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, Color(0xFFFEF08A).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🎉",
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "विशेष स्वागत प्रस्ताव"
                                        "hgl" -> "Special Welcome Offer"
                                        else -> "Special Welcome Offer"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFEF08A)
                                )
                            }
                        }

                        // Urgency / Highlight Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF08A),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "⚡ 100% FREE",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF9A1C0E)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Middle Section: Content + 2D Namaste Artwork
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            // Large bold text: पहले 15 मिनट मुफ्त
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "पहली स्वप्न चैट 5 मिनट मुफ्त*"
                                        "hgl" -> "First Dream Chat 5 Min Free*"
                                        else -> "First Dream Chat 5 Min Free*"
                                    },
                                    fontFamily = AppFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.3.sp
                                )
                            }

                            // Subtext: केवल प्रथम परामर्श पर लागू
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "*प्रति फ़ोन नंबर एक बार • पात्रता सर्वर तय करता है"
                                    "hgl" -> "*Once per phone number • eligibility checked by server"
                                    else -> "*Once per phone number • eligibility checked by server"
                                },
                                fontSize = 11.5.sp,
                                color = Color(0xFFFFF7ED),
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive Action Buttons: DEV15 Copy Pill & Talk Now CTA
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // DEV15 Copy Box (Perforated ticket style with copy feedback)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isCopied) Color(0xFF15803D) else Color.White.copy(alpha = 0.20f),
                                    border = BorderStroke(
                                        1.3.dp,
                                        if (isCopied) Color(0xFF86EFAC) else Color.White.copy(alpha = 0.9f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            HapticFeedbackHelper.playSuccess(haptic)
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("DEV15"))
                                            isCopied = true
                                            onClaimOffer()
                                            Toast.makeText(
                                                context,
                                                if (currentLangCode == "hi") "कूपन कोड DEV15 कॉपी हो गया! ✨" else "Coupon code DEV15 copied! ✨",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = if (isCopied) "COPIED" else "DEV15",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            letterSpacing = 0.8.sp
                                        )
                                        Icon(
                                            imageVector = if (isCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                            contentDescription = if (isCopied) "Copied" else "Copy Code",
                                            tint = if (isCopied) Color(0xFF86EFAC) else Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                // CTA Button: अभी बात करें [phone icon]
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    shadowElevation = 4.dp,
                                    border = BorderStroke(1.dp, Color(0xFFFEF08A)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            HapticFeedbackHelper.playClick(haptic)
                                            onTalkNowClick()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Phone,
                                            contentDescription = "Phone",
                                            tint = Color(0xFFC2410C),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = when (currentLangCode) {
                                                "hi" -> "अभी बात करें"
                                                "hgl" -> "Talk Now"
                                                else -> "Talk Now"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFC2410C)
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Color(0xFFC2410C),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right: 2D Vector Illustration inside circular glowing ring + rating badge
                        Box(
                            contentAlignment = Alignment.BottomCenter,
                            modifier = Modifier.size(98.dp)
                        ) {
                            // Glowing halo circle behind the avatar
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                border = BorderStroke(1.5.dp, Color(0xFFFEF08A).copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .size(92.dp)
                                    .align(Alignment.Center)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                listOf(
                                                    Color(0xFFFEF08A).copy(alpha = 0.45f),
                                                    Color(0xFFF59E0B).copy(alpha = 0.15f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            // 2D Vector Illustration: Friendly young Indian woman in maroon kurta with Namaste gesture
                            Image(
                                painter = painterResource(id = R.drawable.ic_woman_namaste_vector),
                                contentDescription = "Devbhasha Welcoming Sadhak",
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.Center)
                            )

                            // Rating Chip overlay at the bottom of the avatar
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1C1917),
                                border = BorderStroke(0.8.dp, Color(0xFFF59E0B)),
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "⭐ 4.9",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDE047)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Guarantee & Security strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when (currentLangCode) {
                                "hi" -> "🔒 100% सुरक्षित एवं गोपनीय"
                                "hgl" -> "🔒 100% Private & Confidential"
                                else -> "🔒 100% Private & Confidential"
                            },
                            fontSize = 10.sp,
                            color = Color(0xFFFEF08A).copy(alpha = 0.95f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when (currentLangCode) {
                                "hi" -> "⚡ तुरंत लाइव कॉल/चैट"
                                "hgl" -> "⚡ Instant Live Call/Chat"
                                else -> "⚡ Instant Live Call/Chat"
                            },
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = if (isPressed) 1.dp else 4.dp,
        border = BorderStroke(1.2.dp, if (isSelected) Color(0xFFB83A0E) else Color(0xFFFFEDD5)),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFFB83A0E)),
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFF7ED),
                border = BorderStroke(0.8.dp, Color(0xFFFFEDD5)),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFFB83A0E),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2B2B2B),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF6E6E6E),
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
                        "hi" -> "सत्यापित साधक"
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
                color = Color(0xFFB83A0E),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSeeAllClick() }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
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
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color(0xFFFED7AA).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .width(155.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onChatClick(sadhak) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar with Pulsing Online Badge
                            Box {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFFF7ED),
                                    border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (currentLangCode == "hi") sadhak.initialHi.ifEmpty { "सा" } else sadhak.initialEn.take(2).ifEmpty { "AS" },
                                            fontFamily = AppFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB83A0E)
                                        )
                                    }
                                }
                                if (sadhak.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A).copy(alpha = pulseAlpha))
                                            .border(2.dp, Color.White, CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(7.dp))

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
                                    tint = Color(0xFFF59E0B),
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
                                    color = Color(0xFFB83A0E),
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

            // Featured Card with the SINGLE PRIMARY FILLED BUTTON ("बातचीत करें") on the entire screen
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
                            color = Color(0xFFFAF7F2),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (currentLangCode == "hi") featuredSadhak.initialHi.ifEmpty { "सा" } else featuredSadhak.initialEn.take(2).ifEmpty { "SR" },
                                    fontFamily = AppFontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2B2B2B)
                                )
                            }
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

                    // The ONLY filled primary button on the screen: "बातचीत करें"
                    Button(
                        onClick = { onChatClick(featuredSadhak) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB83A0E),
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
                    color = Color(0xFFFAF7F2),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (currentLangCode == "hi") sadhak.initialHi.ifEmpty { "सा" } else sadhak.initialEn.take(2).ifEmpty { "EX" },
                            fontFamily = AppFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2B2B2B)
                        )
                    }
                }

                if (sadhak.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (currentLangCode == "hi") sadhak.nameHi else sadhak.nameEn,
                    fontFamily = AppFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2B2B2B)
                )
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
                color = Color(0xFFB83A0E)
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
                    border = BorderStroke(1.dp, Color(0xFFB83A0E)),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onChatClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = "Chat",
                            tint = Color(0xFFB83A0E),
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
 * 4 Navigation Tabs Dock: होम, साधक, स्वप्न (Dream), शॉप
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

    val primaryColor = Saffron
    val inactiveColor = Color(0xFF6E6E6E)

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = Color(0xFFF8FAFC),
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
                icon = {
                    Box(modifier = Modifier.padding(bottom = 2.dp)) {
                        Icon(
                            imageVector = if (isSelected) iconsPair.first else iconsPair.second,
                            contentDescription = label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = primaryColor.copy(alpha = 0.12f),
                    selectedIconColor = primaryColor,
                    selectedTextColor = primaryColor,
                    unselectedIconColor = inactiveColor,
                    unselectedTextColor = inactiveColor
                )
            )
        }
    }
}

