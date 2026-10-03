package com.example.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// =========================================================================
// 1. FAMILY PROBLEMS (पारिवारिक समस्या, गृह क्लेश निवारण व साधक परामर्श)
// =========================================================================
data class FamilyProblemCategory(
    val id: String,
    val titleHi: String,
    val titleEn: String,
    val emoji: String,
    val description: String,
    val remedies: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoFamilyProblemsScreen(
    onBackClick: () -> Unit,
    onStartChat: ((SadhakItem) -> Unit)? = null,
    onStartCall: ((SadhakItem) -> Unit)? = null,
    sadhaks: List<SadhakItem> = emptyList(),
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        else -> "en"
    }

    val categories = remember {
        listOf(
            FamilyProblemCategory(
                id = "griha_klesh",
                titleHi = "गृह क्लेश व अशांति",
                titleEn = "Household Conflict",
                emoji = "🏡",
                description = "घर में अकारण तनाव, झगड़े, अशांति और नकारात्मक माहौल को समाप्त कर शांति स्थापित करें।",
                remedies = listOf(
                    "संध्याकाल में कर्पूर और 2 लौंग जलाकर पूरे घर में घुमाएं।",
                    "प्रत्येक मंगलवार व शनिवार को सुंदरकांड या हनुमान चालीसा का पाठ करें।",
                    "मुख्य द्वार पर प्रतिदिन गंगाजल का छिड़काव करें व स्वास्तिक बनाएं।"
                )
            ),
            FamilyProblemCategory(
                id = "marital_discord",
                titleHi = "पति-पत्नी में मतभेद",
                titleEn = "Marital Discord",
                emoji = "❤️",
                description = "वैवाहिक संबंधों में संवाद हीनता, गलतफहमी, क्रोध या अलगाव की स्थिति में सामंजस्य।",
                remedies = listOf(
                    "शुक्रवार को मां लक्ष्मी व नारायण को सफेद मिठाई का भोग लगाएं।",
                    "शयनकक्ष में राधा-कृष्ण की सौम्य तस्वीर लगाएं, कांटेदार पौधे न रखें।",
                    "पारिवारिक सामंजस्य के लिए 'ॐ नमो भगवते वासुदेवाय' का 108 बार जप करें।"
                )
            ),
            FamilyProblemCategory(
                id = "children_worry",
                titleHi = "संतान चिंता व पढ़ाई",
                titleEn = "Children & Future",
                emoji = "👶",
                description = "संतान के स्वास्थ्य, पढ़ाई में एकाग्रता, संस्कार या भविष्य को लेकर चिंता निवारण।",
                remedies = listOf(
                    "बुधवार को भगवान गणेश को दूर्वा व मोदक अर्पित कर बुद्धि मंत्र जपें।",
                    "संतान के अध्ययन कक्ष में उत्तर-पूर्व दिशा में मां सरस्वती का चित्र लगाएं।",
                    "गुरुवार को पीले फल या चने की दाल का दान करें।"
                )
            ),
            FamilyProblemCategory(
                id = "inlaws_relation",
                titleHi = "सास-बहू व रिश्ते",
                titleEn = "In-laws & Harmony",
                emoji = "🤝",
                description = "पारिवारिक रिश्तों में सम्मान, प्रेम, कड़वाहट दूर करने व संयुक्त परिवार में सौहार्द।",
                remedies = listOf(
                    "पूर्णिमा के दिन घर में खीर बनाकर पूरे परिवार के साथ प्रसाद ग्रहण करें।",
                    "सोमवार को शिवलिंग पर कच्चा दूध व जल चढ़ाकर पारिवारिक शांति मांगें।",
                    "एक-दूसरे के प्रति कटु वचनों से बचें और मधुर संवाद का संकल्प लें।"
                )
            ),
            FamilyProblemCategory(
                id = "property_dispute",
                titleHi = "पैतृक संपत्ति विवाद",
                titleEn = "Property Disputes",
                emoji = "⚖️",
                description = "पारिवारिक भूमि, मकान, जायदाद या बंटवारे से जुड़े तनाव और कानूनी उलझनों का उपाय।",
                remedies = listOf(
                    "शनिवार को शनि मंदिर में सरसों के तेल का दीपक प्रज्वलित करें।",
                    "सत्य व धर्म का मार्ग अपनाते हुए मध्यस्थता से समाधान का प्रयास करें।",
                    "भगवान भैरव या हनुमान जी के समक्ष संकटमोचन अष्टक का पाठ करें।"
                )
            ),
            FamilyProblemCategory(
                id = "nazar_dosh",
                titleHi = "नज़र दोष व नकारात्मकता",
                titleEn = "Evil Eye / Negativity",
                emoji = "🛡️",
                description = "घर में बीमारी, अचानक तरक्की में बाधा, भारीपन या बुरी नज़र का प्रभाव दूर करना।",
                remedies = listOf(
                    "शनिवार शाम को नमक मिले पानी से पूरे घर में पोछा लगाएं।",
                    "मुख्य द्वार के ऊपर पंचमुखी हनुमान जी का चित्र लगाएं।",
                    "प्रतिदिन सुबह गायत्री मंत्र का 11 बार सस्वर उच्चारण करें।"
                )
            )
        )
    }

    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var problemNote by remember { mutableStateOf("") }
    var isMicActive by remember { mutableStateOf(false) }
    var submittedMessage by remember { mutableStateOf<String?>(null) }

    val saffronGradient = Brush.horizontalGradient(
        listOf(SaffronGradientStart, SaffronGradientEnd)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Surface(color = Color.White, modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF000000)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLangCode == "hi") "पारिवारिक समस्या व समाधान" else "Family Problems & Solutions",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF000000)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Header Banner
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text(
                        text = if (currentLangCode == "hi") "गृह शांति व पारिवारिक समाधान" else "Family Peace & Spiritual Guidance",
                        fontFamily = FontFamily.Serif,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLangCode == "hi")
                            "गृह क्लेश, वैवाहिक मतभेद, संतान चिंता या पारिवारिक विवादों का वैदिक उपाय व अनुभवी साधक से परामर्श पाएं।"
                        else
                            "Find remedies and consult verified sadhaks for household discord, relationships, children and peace.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF737373),
                        lineHeight = 17.sp
                    )
                }
            }

            // Category Chips Row
            item {
                Text(
                    text = if (currentLangCode == "hi") "समस्या की श्रेणी चुनें:" else "Select Problem Category:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF000000)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = cat.id == selectedCategory.id
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) Color(0xFF000000) else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF000000) else BorderLight),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    selectedCategory = cat
                                    if (problemNote.isBlank()) {
                                        problemNote = "${cat.titleHi} के निवारण हेतु मार्गदर्शन चाहिए..."
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = cat.emoji, fontSize = 14.sp)
                                Text(
                                    text = if (currentLangCode == "hi") cat.titleHi else cat.titleEn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF737373)
                                )
                            }
                        }
                    }
                }
            }

            // Selected Category Detail & Remedies Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = selectedCategory.emoji, fontSize = 28.sp)
                            Column {
                                Text(
                                    text = if (currentLangCode == "hi") selectedCategory.titleHi else selectedCategory.titleEn,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF000000)
                                )
                                Text(
                                    text = selectedCategory.description,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF737373),
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderLight)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (currentLangCode == "hi") "✨ सरल वैदिक शांति उपाय:" else "✨ Sacred Vedic Remedies:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Saffron
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        selectedCategory.remedies.forEach { remedy ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "•", color = Saffron, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = remedy,
                                    fontSize = 12.sp,
                                    color = Color(0xFF000000),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Write / Speak Your Family Problem Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (currentLangCode == "hi") "अपनी पारिवारिक समस्या का विवरण लिखें:" else "Describe Your Problem:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF000000)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = problemNote,
                            onValueChange = { problemNote = it },
                            placeholder = {
                                Text(
                                    text = if (currentLangCode == "hi")
                                        "उदा. परिवार में शांति नहीं रहती या सदस्यों में अनबन रहती है..."
                                    else
                                        "e.g. Constant arguments, lack of peace at home...",
                                    fontSize = 13.sp,
                                    color = Color(0xFFA8A8A8)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF000000),
                                unfocusedBorderColor = BorderLight,
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (isMicActive) SaffronLight else Color.White,
                                border = BorderStroke(1.dp, if (isMicActive) Saffron else BorderLight),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable {
                                        isMicActive = !isMicActive
                                        if (isMicActive && problemNote.isBlank()) {
                                            problemNote = "घर में बहुत क्लेश रहता है, सदस्यों के बीच तालमेल और शांति के लिए उपाय बताएं..."
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Mic",
                                        tint = if (isMicActive) Saffron else Color(0xFF000000),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (isMicActive) "माइक चालू (बोलें)" else "माइक से बोलें",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isMicActive) Saffron else Color(0xFF000000)
                                    )
                                }
                            }

                            if (problemNote.isNotBlank()) {
                                TextButton(onClick = { problemNote = "" }) {
                                    Text(text = "साफ़ करें", color = Color(0xFF737373), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Consult Sadhak Section
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (currentLangCode == "hi") "साधक से परामर्श लें (Live Consultation)" else "Consult Verified Sadhak",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF000000)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentLangCode == "hi")
                                "पारिवारिक शांति व समाधान हेतु अनुभवी साधक से चैट या सीधे वॉइस कॉल द्वारा बात करें।"
                            else
                                "Connect with experienced spiritual mentors via chat or voice call for direct solutions.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF737373)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dual Action Buttons: Chat & Call
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val activeSadhak = sadhaks.firstOrNull()

                            // Primary Saffron: Chat
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.Transparent,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(saffronGradient)
                                    .clickable {
                                        if (activeSadhak != null && onStartChat != null) {
                                            onStartChat(activeSadhak)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Chat,
                                        contentDescription = "Chat",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLangCode == "hi") "चैट परामर्श" else "Chat Consult",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Secondary White: Voice Call
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White,
                                border = BorderStroke(1.2.dp, Color(0xFF000000)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable {
                                        if (activeSadhak != null && onStartCall != null) {
                                            onStartCall(activeSadhak)
                                        }
                                    }
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLangCode == "hi") "कॉल परामर्श" else "Call Consult",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF000000)
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

// Keep BentoKundliScreen definition as wrapper pointing to Family Problems for backwards compatibility
@Composable
fun BentoKundliScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BentoFamilyProblemsScreen(
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
private fun MetricPill(title: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .background(PaperDeep, RoundedCornerShape(10.dp))
            .border(1.dp, EditorialLine, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 10.sp, color = InkSoft)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun DashaRow(title: String, desc: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Ink)
            Text(text = desc, fontSize = 11.5.sp, color = InkSoft)
        }
    }
}

// =========================================================================
// 2. VEDIC TAROT & SACRED ORACLE SPREAD
// =========================================================================
data class TarotCard(
    val title: String,
    val hindiTitle: String,
    val deity: String,
    val position: String,
    val meaning: String,
    val advice: String,
    val isRevealed: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoTarotScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allCards = remember {
        listOf(
            TarotCard(
                title = "The Chariot (सूर्य रथ)",
                hindiTitle = "संकल्प व विजय",
                deity = "Lord Surya",
                position = "1. Past Influences",
                meaning = "Unshakable will, overcome hurdles, and relentless spiritual discipline leading to past victories.",
                advice = "Remember the persistence that brought you here; stay rooted in dharma."
            ),
            TarotCard(
                title = "Goddess Lakshmi (श्री)",
                hindiTitle = "समृद्धि व अनुग्रह",
                deity = "Maa Lakshmi",
                position = "2. Present Energy",
                meaning = "Abundance flowing into creative endeavors, harmonious relationships, and spiritual auspiciousness.",
                advice = "Practice gratitude and daan (charity) to maintain cosmic balance and divine grace."
            ),
            TarotCard(
                title = "The Cosmic Guru (बृहस्पति)",
                hindiTitle = "ज्ञान व मार्गदर्शन",
                deity = "Lord Brihaspati",
                position = "3. Future Guidance",
                meaning = "Higher wisdom dawn, right mentorship appearing, and mental clarity resolving lingering doubts.",
                advice = "Seek satsang with elders or verified sadhaks before taking big financial or life leaps."
            )
        )
    }

    var cards by remember { mutableStateOf(allCards) }
    var revealedCount by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PaperBg,
        topBar = {
            Surface(color = PaperBg, modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vedic Oracle & Tarot Spread",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Ink
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Text(
                    text = "3-Card Sacred Oracle",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "Tap on each sacred card to reveal Past, Present, and Divine Guidance for your current journey.",
                    fontSize = 13.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            itemsIndexed(cards) { index, card ->
                val rotation by animateFloatAsState(
                    targetValue = if (card.isRevealed) 360f else 0f,
                    animationSpec = tween(durationMillis = 600),
                    label = "card_flip"
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (card.isRevealed) PaperCard else PaperDeep
                    ),
                    border = BorderStroke(
                        1.2.dp,
                        if (card.isRevealed) Terra else EditorialLine
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { rotationY = rotation }
                        .clickable {
                            if (!card.isRevealed) {
                                cards = cards.toMutableList().also {
                                    it[index] = card.copy(isRevealed = true)
                                }
                                revealedCount++
                            }
                        }
                ) {
                    if (!card.isRevealed) {
                        // Card Back Design
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🪔", fontSize = 34.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = card.position,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Ink
                            )
                            Text(
                                text = "Tap to uncover divine card",
                                fontSize = 12.sp,
                                color = Terra
                            )
                        }
                    } else {
                        // Card Front Revealed
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = card.position, fontSize = 11.5.sp, color = Terra, fontWeight = FontWeight.Bold)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SageDeep.copy(alpha = 0.15f)
                                ) {
                                    Text(text = card.deity, fontSize = 10.5.sp, color = SageDeep, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = card.title,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Ink
                            )
                            Text(
                                text = card.hindiTitle,
                                fontSize = 13.sp,
                                color = InkSoft
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = card.meaning,
                                fontSize = 13.sp,
                                color = Ink,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GoldStampSoft,
                                border = BorderStroke(1.dp, GoldStamp.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("✨", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = card.advice, fontSize = 11.5.sp, color = InkSoft)
                                }
                            }
                        }
                    }
                }
            }

            if (revealedCount == 3) {
                item {
                    Button(
                        onClick = {
                            cards = allCards.map { it.copy(isRevealed = false) }
                            revealedCount = 0
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Ink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Shuffle & Draw Again 🪔", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. KUNDLI MILAN (36 GUNA MATCHMAKING CALCULATOR)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoKundliMatchScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var boyName by remember { mutableStateOf("Amit Kumar") }
    var boyRashi by remember { mutableStateOf("Leo (सिंह)") }
    var boyNakshatra by remember { mutableStateOf("Magha (मघा)") }

    var girlName by remember { mutableStateOf("Priya Sharma") }
    var girlRashi by remember { mutableStateOf("Sagittarius (धनु)") }
    var girlNakshatra by remember { mutableStateOf("Moola (मूल)") }

    var isMatchCalculated by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PaperBg,
        topBar = {
            Surface(color = PaperBg, modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vedic Kundli Milan (36 Gunas)",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Ink
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Text(
                    text = "Ashta Kuta Compatibility",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "Check Vedic marriage compatibility across all 8 Kutas (Varna, Vashya, Tara, Yoni, Maitri, Gana, Bhakoot & Nadi).",
                    fontSize = 13.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperCard),
                    border = BorderStroke(1.dp, EditorialLine)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Boy's Details (वर)", fontWeight = FontWeight.Bold, color = Terra, fontSize = 15.sp)
                        OutlinedTextField(
                            value = boyName,
                            onValueChange = { boyName = it },
                            label = { Text("Boy's Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = boyRashi,
                                onValueChange = { boyRashi = it },
                                label = { Text("Rashi") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = boyNakshatra,
                                onValueChange = { boyNakshatra = it },
                                label = { Text("Nakshatra") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        HorizontalDivider(color = EditorialLine, modifier = Modifier.padding(vertical = 4.dp))

                        Text("Girl's Details (कन्या)", fontWeight = FontWeight.Bold, color = SageDeep, fontSize = 15.sp)
                        OutlinedTextField(
                            value = girlName,
                            onValueChange = { girlName = it },
                            label = { Text("Girl's Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = girlRashi,
                                onValueChange = { girlRashi = it },
                                label = { Text("Rashi") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = girlNakshatra,
                                onValueChange = { girlNakshatra = it },
                                label = { Text("Nakshatra") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { isMatchCalculated = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Ink),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Calculate Gun Milan →", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            if (isMatchCalculated) {
                item {
                    // Match Score Verdict Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PaperDeep),
                        border = BorderStroke(1.2.dp, SageDeep)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Compatibility Result", fontSize = 12.sp, color = InkSoft, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "31",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 46.sp,
                                    color = SageDeep
                                )
                                Text(
                                    text = " / 36 Gunas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Ink,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SageDeep
                            ) {
                                Text(
                                    text = "⭐ Utkrishta Milan (Excellent Match)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(text = "Ashta Kuta Detailed Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                }

                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PaperCard),
                        border = BorderStroke(1.dp, EditorialLine)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            KutaRow("1. Varna (आध्यात्मिक अनुकूलता)", "1 / 1", "Full compatibility in temperament")
                            KutaRow("2. Vashya (परस्पर आकर्षण व नियंत्रण)", "2 / 2", "Harmonious mutual respect")
                            KutaRow("3. Tara (भाग्य व दीर्घायु)", "3 / 3", "Auspicious prosperity & health")
                            KutaRow("4. Yoni (जैविक व अंतरंग सामंजस्य)", "4 / 4", "Deep intimacy & affection")
                            KutaRow("5. Graha Maitri (मित्रता व सोच)", "5 / 5", "Both planetary lords are friends")
                            KutaRow("6. Gana (स्वभाव व आचरण)", "5 / 6", "Deva & Manushya Gana match")
                            KutaRow("7. Bhakoot (पारिवारिक समृद्धि व सुख)", "7 / 7", "9-5 Auspicious Navapanchama")
                            KutaRow("8. Nadi (संतान व अनुवांशिकता)", "4 / 8", "Madhya & Antya Nadi (No Dosha)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KutaRow(title: String, score: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
            Text(text = desc, fontSize = 11.sp, color = InkSoft)
        }
        Text(text = score, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SageDeep)
    }
}

// =========================================================================
// 4. DAILY POOJA & SACRED RITUALS GUIDE
// =========================================================================
data class PoojaStep(
    val title: String,
    val hindiTitle: String,
    val mantra: String,
    val meaning: String,
    val durationMin: Int,
    var isDone: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoDailyPoojaScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialSteps = remember {
        listOf(
            PoojaStep(
                title = "1. Pratah Smaran & Karadarshan",
                hindiTitle = "हथेलियों का दर्शन व भूमि वंदना",
                mantra = "कराग्रे वसते लक्ष्मीः करमध्ये सरस्वती ।\nकरमूले तु गोविन्दः प्रभाते करदर्शनम् ॥",
                meaning = "Look at your palms in the morning meditating on Lakshmi, Saraswati and Govinda for fruitful karma.",
                durationMin = 2
            ),
            PoojaStep(
                title = "2. Surya Arghya (Solar Offering)",
                hindiTitle = "भगवान सूर्य को तांबे के पात्र से जल अर्पण",
                mantra = "ॐ सूर्याय नमः । ॐ घृणिः सूर्य आदित्यः ॥",
                meaning = "Offer fresh water to the rising sun facing East to energize the soul and intellect.",
                durationMin = 3
            ),
            PoojaStep(
                title = "3. Gayatri Mantra Japa",
                hindiTitle = "गायत्री मंत्र का 11 या 108 बार जप",
                mantra = "ॐ भूर्भुवः स्वः तत्सवितुर्वरेण्यं भर्गो देवस्य धीमहि धियो यो नः प्रचोदयात् ॥",
                meaning = "Meditate on the supreme light that illuminates all realms to enlighten our minds.",
                durationMin = 5
            ),
            PoojaStep(
                title = "4. Diya Lighting & Sandhya Aarti",
                hindiTitle = "घी का दीपक व सात्विक आरती",
                mantra = "शुभं करोति कल्याणमारोग्यं धनसंपदा ।\nशत्रुबुद्धिविनाशाय दीपज्योतिर्नमोऽस्तुते ॥",
                meaning = "Light a cow ghee diya to dispel darkness, negativity and invite divine auspiciousness into home.",
                durationMin = 5
            )
        )
    }

    var steps by remember { mutableStateOf(initialSteps) }
    val completedCount = steps.count { it.isDone }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PaperBg,
        topBar = {
            Surface(color = PaperBg, modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nitya Pooja & Daily Rituals",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Ink
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Text(
                    text = "Daily Spiritual Rituals",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "Ancient Vedic sequence to start and end your day with peace, purity and positive vibration.",
                    fontSize = 13.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Progress Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperDeep),
                    border = BorderStroke(1.dp, EditorialLine)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "$completedCount of ${steps.size} Rituals Done", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink)
                            Text(text = if (completedCount == steps.size) "Daily Nitya Pooja Completed ✨" else "Complete all 4 steps today", fontSize = 12.sp, color = InkSoft)
                        }
                        CircularProgressIndicator(
                            progress = { completedCount.toFloat() / steps.size.toFloat() },
                            color = Terra,
                            trackColor = EditorialLine,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            itemsIndexed(steps) { index, step ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (step.isDone) Color(0xFFF2F6ED) else PaperCard
                    ),
                    border = BorderStroke(
                        1.2.dp,
                        if (step.isDone) SageDeep else EditorialLine
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = step.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink)
                            Checkbox(
                                checked = step.isDone,
                                onCheckedChange = { checked ->
                                    steps = steps.toMutableList().also {
                                        it[index] = step.copy(isDone = checked)
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SageDeep)
                            )
                        }

                        Text(text = step.hindiTitle, fontSize = 12.sp, color = InkSoft)

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PaperDeep,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = step.mantra,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = TerraDeep,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = step.meaning,
                                    fontSize = 11.5.sp,
                                    color = InkSoft,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. SACRED HABITS TRACKER (NITYA KARMA CHECKLIST)
// =========================================================================
data class SacredHabit(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val icon: String,
    var isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoSacredHabitsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialHabits = remember {
        listOf(
            SacredHabit("1", "Surya Namaskar & Yoga", "प्रातः सूर्य नमस्कार (12 चक्र)", "☀️", true),
            SacredHabit("2", "Mantra Japa Meditation", "108 गायत्री / महामृत्युंजय जप", "📿", true),
            SacredHabit("3", "Satvik Food & Hydration", "सात्विक आहार व पर्याप्त जल", "🌿", false),
            SacredHabit("4", "Gita / Sacred Reading", "श्रीमद्भगवद्गीता के 2 श्लोक", "📖", false),
            SacredHabit("5", "Sandhya Diya & Gratitude", "संध्या दीप प्रज्वलन व धन्यवाद", "🪔", false)
        )
    }

    var habits by remember { mutableStateOf(initialHabits) }
    val doneCount = habits.count { it.isCompleted }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PaperBg,
        topBar = {
            Surface(color = PaperBg, modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sacred Habits (Nitya Karma)",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Ink
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Text(
                    text = "Daily Spiritual Streak",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "Consistently cultivate spiritual energy, mindfulness, and inner peace throughout the day.",
                    fontSize = 13.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Streak Metric Box
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldStampSoft),
                    border = BorderStroke(1.2.dp, GoldStamp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "🔥 7 Days Streak Active", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                            Text(text = "Completed $doneCount of ${habits.size} sacred tasks today", fontSize = 12.sp, color = InkSoft)
                        }
                        Text("🎯", fontSize = 28.sp)
                    }
                }
            }

            itemsIndexed(habits) { index, habit ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (habit.isCompleted) Color(0xFFF4F7EF) else PaperCard
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (habit.isCompleted) SageDeep else EditorialLine
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            habits = habits.toMutableList().also {
                                it[index] = habit.copy(isCompleted = !habit.isCompleted)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = habit.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = habit.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Ink
                            )
                            Text(
                                text = habit.hindiTitle,
                                fontSize = 11.5.sp,
                                color = InkSoft
                            )
                        }
                        Icon(
                            imageVector = if (habit.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = "Status",
                            tint = if (habit.isCompleted) SageDeep else InkFaint
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. JAPA MALA & MEDITATION TIMER (DIGITAL 108 BEAD COUNTER)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoJapaTimerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var japaCount by remember { mutableIntStateOf(0) }
    var totalRounds by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(Vibrator::class.java)
        } else {
            null
        }
    }

    fun handleBeadCount() {
        if (japaCount + 1 >= 108) {
            japaCount = 0
            totalRounds++
            // Long vibration on completing 108 round
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (_: Exception) {}
        } else {
            japaCount++
            // Short bead tactile click
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PaperBg,
        topBar = {
            Surface(color = PaperBg, modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "108 Japa Mala & Chanting Counter",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Ink
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Digital Rudraksha Mala",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "Tap the sacred counter bead for each mantra recitation. Haptic vibration confirms every bead.",
                    fontSize = 12.5.sp,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(999.dp), color = PaperDeep) {
                        Text(
                            text = "Rounds (माला): $totalRounds",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Terra,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Big Circular Interactive Japa Bead
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Terra, TerraDeep, Ink)
                        )
                    )
                    .border(4.dp, GoldStamp, CircleShape)
                    .clickable { handleBeadCount() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$japaCount",
                        fontFamily = FontFamily.Serif,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        color = PaperCard
                    )
                    Text(
                        text = "/ 108",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoldStamp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TAP BEAD (जपें)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        japaCount = 0
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset Bead", color = Ink)
                }

                Button(
                    onClick = {
                        japaCount = 0
                        totalRounds = 0
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset All", color = Color.White)
                }
            }
        }
    }
}
