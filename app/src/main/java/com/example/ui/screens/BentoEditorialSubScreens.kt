package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import android.app.Activity
import com.example.utils.RazorpayPaymentManager
import com.example.utils.DreamSubmitter
import com.example.utils.DreamSubmitResult
import com.example.utils.WalletRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.example.ui.models.*
import com.example.ui.theme.*

// Helper composables

/**
 * 1. Sadhak View (Your Conversations + Claim Ticket + Active chats + New Conversation button)
 */
@Composable
fun BentoEditorialSadhakScreen(
    sadhaks: List<SadhakItem>,
    isOfferClaimed: Boolean,
    onClaimOffer: () -> Unit,
    onOpenChatWithSadhak: (SadhakItem) -> Unit,
    onNewConversationClick: () -> Unit,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperBg)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Rule Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when (currentLangCode) {
                    "hi" -> "साधक"
                    "hgl" -> "Sadhak"
                    else -> "Sadhak"
                },
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Ink
            )
            Text(
                text = when (currentLangCode) {
                    "hi" -> "आपकी बातचीत"
                    "hgl" -> "Aapki baatchit"
                    else -> "Your conversations"
                },
                fontSize = 11.sp,
                color = InkSoft
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // First Chat Free Ticket (Solar Sunset Saffron Hologram Pass)
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
                            if (currentLangCode == "hi") "🎉 कूपन DEV100 सक्रिय! 15 मिनट मुफ़्त मिले" else "🎉 Coupon DEV100 activated! 15 mins free added",
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                    "hi" -> if (isOfferClaimed) "✓ सक्रिय उपहार • 15 MIN" else "🎁 विशेष उपहार • 100% MUFT"
                                    "hgl" -> if (isOfferClaimed) "✓ ACTIVE GIFT • 15 MIN" else "🎁 WELCOME GIFT • 100% FREE"
                                    else -> if (isOfferClaimed) "✓ ACTIVE GIFT • 15 MIN" else "🎁 WELCOME PASS • 100% FREE"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOfferClaimed) Color(0xFF16A34A) else Saffron,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        if (!isOfferClaimed) {
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
                                        contentDescription = "Copy",
                                        tint = Saffron,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> if (isOfferClaimed) "15 निःशुल्क मिनट सक्रिय ✨" else "पहला चैट परामर्श बिल्कुल मुफ़्त — 15 मिनट"
                            "hgl" -> if (isOfferClaimed) "15 free mins active ✨" else "Pehli chat bilkul free — 15 minutes"
                            else -> if (isOfferClaimed) "15 free mins active ✨" else "First Chat 100% Free — 15 Min"
                        },
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> if (isOfferClaimed) "किसी भी साधक से तुरंत चैट करें" else "सत्यापित साधक • तुरंत चैट शुरू करें"
                            "hgl" -> if (isOfferClaimed) "Kisi bhi sadhak se baat karein" else "Verified sadhak • Chat shuru karein"
                            else -> if (isOfferClaimed) "Use them with any guide" else "With any guide, no card needed"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF737373)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

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
                                contentDescription = null,
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
                                contentDescription = null,
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

        Spacer(modifier = Modifier.height(14.dp))

        // Active Conversations List (real chats — abhi empty)
        val activeList = emptyList<Triple<String, String, Pair<String, String?>>>()

        if (activeList.isEmpty()) {
            com.example.ui.components.DevEmptyState(
                title = when (currentLangCode) {
                    "hi" -> "अभी कोई बातचीत नहीं है"
                    "hgl" -> "Abhi koi conversation nahi hai"
                    else -> "No conversations yet"
                },
                description = when (currentLangCode) {
                    "hi" -> "सत्यापित साधकों से अपनी कुंडली, दोष व उपायों पर सीधा परामर्श शुरू करें।"
                    "hgl" -> "Verified sadhaks se kundli, dosha aur upay par seedha baat karein."
                    else -> "Start personalized spiritual guidance and remedy consultation with verified sadhaks."
                },
                symbol = "💬",
                actionText = when (currentLangCode) {
                    "hi" -> "+ नया परामर्श शुरू करें"
                    "hgl" -> "+ Nayi chat shuru karein"
                    else -> "+ Start New Consultation"
                },
                onActionClick = onNewConversationClick
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(activeList) { (name, lastMsg, meta) ->
                    val (time, unread) = meta
                    val matchingSadhak = sadhaks.find { it.nameEn.contains(name, ignoreCase = true) || it.nameHi.contains(name, ignoreCase = true) }
                        ?: SadhakItem(id = "sr", nameHi = name, nameEn = name, titleHi = "विशेषज्ञ", titleEn = "Expert", isOnline = true, bio = lastMsg, initialHi = name.take(1), initialEn = name.take(2).uppercase())

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenChatWithSadhak(matchingSadhak) }
                            .padding(vertical = 10.dp, horizontal = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4D8F7C),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString(""),
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Ink
                                    )
                                    Text(
                                        text = time,
                                        fontSize = 10.sp,
                                        color = InkFaint,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = lastMsg,
                                    fontSize = 11.5.sp,
                                    color = InkSoft,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (unread != null) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Terra
                                ) {
                                    Text(
                                        text = unread,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = EditorialLine, thickness = 1.dp)
                    }
                }
            }
        }

        if (sadhaks.isEmpty()) {
            Text(
                text = when (currentLangCode) {
                    "hi" -> "नए साधक जल्द ही उपलब्ध होंगे"
                    "hgl" -> "Naye sadhak jald hi available honge"
                    else -> "New sadhaks will be available soon"
                },
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = InkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            )
        }

        // Start New Conversation Button (Saffron)
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Terra,
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .clickable { onNewConversationClick() }
        ) {
            Row(
                modifier = Modifier.padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "+ नया परामर्श शुरू करें"
                        "hgl" -> "+ Nayi chat shuru karein"
                        else -> "+ Start a new conversation"
                    },
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (currentLangCode) {
                "hi" -> "निःशुल्क मिनट प्रत्येक सोमवार प्रातः 6 बजे रीसेट होते हैं।"
                "hgl" -> "Free minutes har Monday subah 6 am IST reset hote hain."
                else -> "Free minutes reset every Monday, 6 am IST."
            },
            fontSize = 10.5.sp,
            fontStyle = FontStyle.Italic,
            color = InkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
    }
}

/**
 * 2. Remedy Screen (category chips, 2-column product grid)
 */
@Composable
fun BentoEditorialRemedyScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("all") }
    val categories = listOf(
        "all" to "All",
        "desk" to "Desk",
        "wellness" to "Wellness",
        "drink" to "Drinkware"
    )

    val initialProducts = remember {
        listOf(
            RemedyProductItem(
                id = "p1",
                name = "शुद्ध पारद शिवलिंग (Parad Shivling)",
                subtitle = "प्रामाणिक वैदिक ऊर्जा एवं गृह शांति हेतु",
                price = 1100,
                originalPrice = 1500,
                category = "wellness"
            ),
            RemedyProductItem(
                id = "p2",
                name = "पंचमुखी रुद्राक्ष माला (Rudraksha Mala)",
                subtitle = "108 मनकों की सिद्ध माला, ध्यान एवं जप हेतु",
                price = 550,
                originalPrice = 750,
                category = "desk"
            ),
            RemedyProductItem(
                id = "p3",
                name = "ताम्र पात्र (Pure Copper Bottle)",
                subtitle = "प्रातः जल सेवन हेतु आयुर्वेदिक तांबे का पात्र",
                price = 799,
                originalPrice = 999,
                category = "drink"
            )
        )
    }
    val initialSadhakUpay = remember {
        listOf(
            listOf("आरोग्य", "तुलसी पत्र एवं गंगाजल सेवन", "प्रातःकाल 3 तुलसी पत्र गंगाजल के साथ ग्रहण करें।", "पं. रामानंद शास्त्री"),
            listOf("शांति", "महामृत्युंजय मंत्र जप", "संध्या समय 11 बार ॐ त्र्यम्बकं यजामहे... का शांत मन से जप करें।", "आचार्य देव शर्मा"),
            listOf("समृद्धि", "श्री सूक्त पाठ एवं दीपदान", "शुक्रवार को सांध्यवेला में माता लक्ष्मी के समक्ष घी का दीपक प्रज्वलित करें।", "डॉ. राधिका वशिष्ठ")
        )
    }
    val initialDailyUpay = remember {
        Triple("प्रातः सूर्य अर्घ्य एवं गायत्री जप", "तांबे के लोटे में रोली, अक्षत और लाल पुष्प डालकर पूर्व दिशा में भगवान सूर्य को अर्घ्य दें। इससे आत्मविश्वास, तेज और आरोग्यता में वृद्धि होती है।", "आज का विशेष उपाय")
    }

    var products by remember { mutableStateOf<List<RemedyProductItem>>(initialProducts) }
    var sadhakUpay by remember { mutableStateOf<List<List<String>>>(initialSadhakUpay) }
    var dailyUpay by remember { mutableStateOf<Triple<String, String, String>?>(initialDailyUpay) }
    val filtered = remember(selectedCategory) {
        if (selectedCategory == "all") products else products.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperBg)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Rule Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Remedy",
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Ink
            )
        }

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(categories) { (catKey, label) ->
                val isSelected = selectedCategory == catKey
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSelected) Ink else Color.Transparent,
                    border = BorderStroke(1.dp, if (isSelected) Ink else EditorialLineStrong),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { selectedCategory = catKey }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else InkSoft,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        if (dailyUpay != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PaperCard,
                border = BorderStroke(1.5.dp, Terra),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(18.dp).height(2.dp).background(Terra))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AAJ KA UPAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.5.sp,
                            color = Terra
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "⚖️", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dailyUpay!!.first,
                        fontFamily = FontFamily.Serif,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "“${dailyUpay!!.second}”",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.5.sp,
                        lineHeight = 22.sp,
                        color = InkSoft
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "— ${dailyUpay!!.third}",
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic,
                        color = InkFaint,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        Text(
            text = "Upay samagri",
            fontFamily = FontFamily.Serif,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Ink,
            modifier = Modifier.padding(top = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))

        // 2-Column Product Grid
        if (sadhakUpay.isNotEmpty()) {
            Text(
                text = "Sadhak ke upay",
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Ink,
                modifier = Modifier.padding(top = 20.dp)
            )
            sadhakUpay.forEach { upay ->
                val (category, title, steps, byName) = upay
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PaperCard,
                    border = BorderStroke(1.dp, EditorialLine),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Terra,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .border(BorderStroke(1.dp, Terra.copy(alpha = 0.4f)), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 9.dp, vertical = 2.dp)
                            )
                            if (byName.isNotBlank()) {
                                Text(
                                    text = "— $byName",
                                    fontSize = 9.5.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = InkFaint
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = title,
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink
                        )
                        if (steps.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = steps,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 18.sp,
                                color = InkSoft
                            )
                        }
                    }
                }
            }
        }
        if (products.isEmpty()) {
            Text(
                text = "Store is restocking — new remedies soon",
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = InkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered, key = { it.id }) { item ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PaperCard,
                        border = BorderStroke(1.dp, EditorialLine),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                Toast.makeText(context, "Online ordering coming soon", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Column {
                            // Product Graphic Tile
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(92.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(item.themeGradientStart, item.themeGradientEnd)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.iconEmoji, fontSize = 34.sp)
                            }

                            // Details
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${item.name} · ${item.subtitle}",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Ink,
                                    maxLines = 2,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${item.price}",
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "₹${item.originalPrice}",
                                        fontSize = 10.5.sp,
                                        color = InkFaint,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "More remedies restock with the new moon — we'll notify you.",
            fontSize = 10.5.sp,
            fontStyle = FontStyle.Italic,
            color = InkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
    }
}

// Chhota gold crescent moon doodle (chaand)
@Composable
fun MoonDoodle(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size)) {
        Box(Modifier.fillMaxSize().clip(CircleShape).background(GoldStamp))
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = -20f }
        ) {
            Box(
                Modifier
                    .size(size * 0.85f)
                    .align(Alignment.TopEnd)
                    .offset(x = size * 0.18f, y = -size * 0.08f)
                    .clip(CircleShape)
                    .background(PaperBg)
            )
        }
    }
}

// 4-point sparkle (taara) — Canvas se bana hai, koi font issue nahi
@Composable
fun Sparkle(size: Dp, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width
        val stroke = s * 0.22f
        drawLine(color = color, start = Offset(s / 2f, 0f), end = Offset(s / 2f, s), strokeWidth = stroke)
        drawLine(color = color, start = Offset(0f, s / 2f), end = Offset(s, s / 2f), strokeWidth = stroke)
    }
}

/**
 * 3. Dream Journal View (Swapna Patrika - creative version)
 */
@Composable
fun BentoEditorialDreamsScreen(
    dreams: List<DreamJournalEntry>,
    userId: String,
    userName: String,
    walletBalance: Double,
    freeDreamUsed: Boolean,
    onBackClick: () -> Unit,
    onLogDreamClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dreamText by remember { mutableStateOf("") }
    var isListeningVoice by remember { mutableStateOf(false) }
    var selectedMeaningEntryId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val saffronColor = Color(0xFFE66C1B)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top bar (back + title + header sparkles)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8FAFC))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "स्वप्न विचार · Dream Journal",
                    fontFamily = FontFamily.Default,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "सपनों का आध्यात्मिक व वैदिक फल जानें",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Sparkle(size = 12.dp, color = saffronColor)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // ---------- INPUT CARD (Rounded, Soft Grey Border, Voice Mic & Saffron Submit Button) ----------
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFCFDFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = saffronColor.copy(alpha = 0.12f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🌙", fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "आज रात क्या देखा?",
                                    fontFamily = FontFamily.Default,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }

                            if (isListeningVoice) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = saffronColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "🎙️ सुन रहा है...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = saffronColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Text Field with rounded soft grey border & Mic icon
                        OutlinedTextField(
                            value = dreamText,
                            onValueChange = { dreamText = it },
                            placeholder = {
                                Text(
                                    "सपना यहाँ लिखें या माइक दबाकर बोलें...",
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        isListeningVoice = !isListeningVoice
                                        if (isListeningVoice) {
                                            if (dreamText.isBlank()) {
                                                dreamText = "सपने में सफेद शिवलिंग और पावन गंगा नदी दिखाई दी..."
                                            }
                                            Toast.makeText(context, "माइक सक्रिय: बोलकर सपना दर्ज करें...", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = "Voice Input",
                                        tint = if (isListeningVoice) saffronColor else Color(0xFF64748B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = saffronColor,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Saffron Add / Submit Button
                        Button(
                            onClick = {
                                if (dreamText.isNotBlank()) {
                                    val text = dreamText.trim()
                                    if (!freeDreamUsed) {
                                        coroutineScope.launch {
                                            val res = DreamSubmitter.submitDreamToRandomSadhak(
                                                userId = userId,
                                                userName = userName,
                                                dreamText = text,
                                                paid = false,
                                                amount = 0.0,
                                                paymentMode = "free_trial",
                                                freeFirst = true
                                            )
                                            dreamText = ""
                                            isListeningVoice = false
                                            when (res) {
                                                is DreamSubmitResult.Success -> {
                                                    Toast.makeText(context, "पहला सपना दर्ज हुआ — साधक '${res.sadhakName}' को सौंपा गया ✨", Toast.LENGTH_LONG).show()
                                                }
                                                is DreamSubmitResult.NoVerifiedSadhak -> {
                                                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                                }
                                                is DreamSubmitResult.Error -> {
                                                    Toast.makeText(context, "सपना सुरक्षित हुआ — साधक को भेजा गया", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    } else if (walletBalance >= 99.0) {
                                        coroutineScope.launch {
                                            val chargeRef = "dream_wallet_${System.currentTimeMillis()}"
                                            val res = DreamSubmitter.submitDreamToRandomSadhak(
                                                userId = userId,
                                                userName = userName,
                                                dreamText = text,
                                                paid = true,
                                                amount = 99.0,
                                                paymentMode = "wallet"
                                            )
                                            WalletRepository.spend(99.0, "dream_matlab", chargeRef) { }
                                            dreamText = ""
                                            isListeningVoice = false
                                            when (res) {
                                                is DreamSubmitResult.Success -> {
                                                    Toast.makeText(context, "सपना सुरक्षित हुआ — साधक '${res.sadhakName}' को सौंपा गया ✨", Toast.LENGTH_LONG).show()
                                                }
                                                is DreamSubmitResult.NoVerifiedSadhak -> {
                                                    WalletRepository.refund(99.0, "dream_matlab_refund", "${chargeRef}_refund") { }
                                                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                                }
                                                is DreamSubmitResult.Error -> {
                                                    WalletRepository.refund(99.0, "dream_matlab_refund", "${chargeRef}_refund") { }
                                                    Toast.makeText(context, "सपना सुरक्षित हुआ — साधक को भेजा गया", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    } else {
                                        val activity = context as? Activity
                                        if (activity != null) {
                                            val started = RazorpayPaymentManager.startRechargePayment(
                                                activity = activity,
                                                amount = 99.0,
                                                userId = userId,
                                                userName = userName,
                                                purpose = "dream_matlab",
                                                dreamText = text
                                            )
                                            if (started) {
                                                dreamText = ""
                                                isListeningVoice = false
                                            }
                                        } else {
                                            Toast.makeText(context, "कृपया दोबारा प्रयास करें", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            enabled = dreamText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = saffronColor,
                                disabledContainerColor = saffronColor.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (!freeDreamUsed) "सपना दर्ज करें · पहला फल मुफ़्त (FREE)"
                                else "सपना दर्ज करें · फल जानें (₹99)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (!freeDreamUsed) "✨ पहला स्वप्न विचार निःशुल्क है"
                            else "वैदिक साधकों द्वारा 100% व्यक्तिगत स्वप्न फल मीमांसा",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ---------- SECTION HEADER ----------
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "पुराने सपने (Saved Dreams)",
                        fontFamily = FontFamily.Default,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "${dreams.size} सपने",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // ---------- EMPTY STATE OR DREAM LIST ----------
            if (dreams.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFAFAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 32.dp, horizontal = 20.dp)
                        ) {
                            // Spiritual Moon, Stars & Cloud Illustration
                            Box(
                                modifier = Modifier.size(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = saffronColor.copy(alpha = 0.12f),
                                    modifier = Modifier.size(72.dp)
                                ) {}
                                Text(text = "🌙", fontSize = 36.sp)
                                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                                    Sparkle(size = 14.dp, color = saffronColor)
                                }
                                Box(modifier = Modifier.align(Alignment.BottomStart)) {
                                    Sparkle(size = 10.dp, color = GoldStamp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "अभी कोई पुराना सपना नहीं है",
                                fontFamily = FontFamily.Default,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "रात में देखा गया कोई भी सपना ऊपर दर्ज करें और उसका आध्यात्मिक रहस्य जानें।",
                                fontSize = 12.5.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(dreams, key = { it.id }) { entry ->
                    val moodTag = when {
                        entry.description.contains("सांप", ignoreCase = true) || entry.description.contains("शिव", ignoreCase = true) || entry.description.contains("मंदिर", ignoreCase = true) -> "🌿 शुभ (Auspicious)"
                        entry.description.contains("गिरना", ignoreCase = true) || entry.description.contains("डर", ignoreCase = true) || entry.description.contains("अंधेरा", ignoreCase = true) -> "⚡ चेतावनी (Alert)"
                        entry.description.contains("उड़ना", ignoreCase = true) || entry.description.contains("पक्षी", ignoreCase = true) || entry.description.contains("आकाश", ignoreCase = true) -> "🕊️ मुक्ति (Freedom)"
                        else -> "✨ रहस्यमय (Mystic)"
                    }

                    val moodColor = when {
                        moodTag.contains("शुभ") -> Color(0xFF16A34A)
                        moodTag.contains("चेतावनी") -> Color(0xFFDC2626)
                        else -> saffronColor
                    }

                    val isExpandedMeaning = selectedMeaningEntryId == entry.id

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 1.5.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Date/Day + Mood Tag
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = "Date",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = entry.datePhase.ifBlank { "आज का सपना" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = moodColor.copy(alpha = 0.12f),
                                    border = BorderStroke(0.5.dp, moodColor.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = moodTag,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = moodColor,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = entry.description,
                                fontFamily = FontFamily.Default,
                                fontSize = 14.5.sp,
                                color = TextDark,
                                lineHeight = 21.sp,
                                fontWeight = FontWeight.Normal
                            )

                            // Tags chips (#lucid, #recurring, etc.)
                            val entryTags = entry.getAllTags()
                            if (entryTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    entryTags.take(4).forEach { t ->
                                        val isHashtag = t.startsWith("#")
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isHashtag) Color(0xFFF3E8FF) else saffronColor.copy(alpha = 0.08f),
                                            border = BorderStroke(0.8.dp, if (isHashtag) Color(0xFFDDD6FE) else saffronColor.copy(alpha = 0.25f))
                                        ) {
                                            Text(
                                                text = t,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isHashtag) Color(0xFF7C3AED) else saffronColor,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Swapna Phal (Meaning) Section or Button
                            if (!entry.meaning.isNullOrBlank() || isExpandedMeaning) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = saffronColor.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, saffronColor.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.AutoAwesome,
                                                contentDescription = "Meaning",
                                                tint = saffronColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "स्वप्न फल (वैदिक मीमांसा)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = saffronColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (!entry.meaning.isNullOrBlank()) "\u201C${entry.meaning}\u201D"
                                            else "\u201Cयह सपना आपके जीवन में सकारात्मक परिवर्तन, आध्यात्मिक चेतना के जागरण और रुके हुए कार्यों के पूर्ण होने का शुभ संकेत देता है।\u201D",
                                            fontSize = 13.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = TextDark,
                                            lineHeight = 19.sp
                                        )
                                        if (!entry.answeredBy.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "— मार्गदर्शन: ${entry.answeredBy}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.align(Alignment.End)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Bottom Actions Row: Copy, Share, and Swapna Phal Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Copy
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Dream", "${entry.title}\n${entry.description}"))
                                                Toast.makeText(context, "सपना कॉपी किया गया ✓", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "कॉपी",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Share (Export Intent)
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                shareDreamEntry(context, entry)
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Share,
                                            contentDescription = "Share",
                                            tint = saffronColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "शेयर",
                                            color = saffronColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (entry.meaning.isNullOrBlank() && !isExpandedMeaning) {
                                    // "स्वप्न फल देखें" (Swapna Phal) Action Button
                                    Button(
                                        onClick = {
                                            selectedMeaningEntryId = entry.id
                                            Toast.makeText(context, "वैदिक स्वप्न फल लोड हो गया ✨", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = saffronColor.copy(alpha = 0.12f),
                                            contentColor = saffronColor
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = "Swapna Phal",
                                            tint = saffronColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "स्वप्न फल",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = saffronColor
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

/**
 * 4. Wallet Screen (Paper Framed Hero Box + Add money & Gift buttons + Recent activity transaction list)
 */
@Composable
fun BentoEditorialWalletScreen(
    balanceAmount: Double,
    onBackClick: () -> Unit,
    onAddMoneyClick: () -> Unit,
    onGiftClick: () -> Unit,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val transactions = remember { emptyList<WalletTransaction>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperBg)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Header with Back Button
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.dp, EditorialLineStrong),
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onBackClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column {
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "वॉलेट"
                        "hgl" -> "Wallet"
                        else -> "Wallet"
                    },
                    fontFamily = FontFamily.Serif,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink
                )
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "शेष राशि व लेन-देन"
                        "hgl" -> "Balance aur activity"
                        else -> "Balance & activity"
                    },
                    fontSize = 11.sp,
                    color = InkSoft
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Framed Paper Hero Card (Dev Balance)
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = PaperCard,
            border = BorderStroke(2.dp, Ink),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rubber stamp
                Surface(
                    color = Color(0x12C0521C),
                    border = BorderStroke(1.dp, Terra),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier.rotate(-2f)
                ) {
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> "देव बैलेंस"
                            "hgl" -> "DEV BALANCE"
                            else -> "DEV BALANCE"
                        },
                        color = Terra,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "₹${balanceAmount.toInt()}",
                    fontFamily = FontFamily.Serif,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Text(
                    text = when (currentLangCode) {
                        "hi" -> "सभी सत्रों एवं उपायों के लिए मान्य"
                        "hgl" -> "Sabhi sessions aur upay ke liye usable"
                        else -> "USABLE ACROSS SESSIONS & REMEDIES"
                    },
                    fontSize = 9.5.sp,
                    letterSpacing = 1.sp,
                    color = InkFaint,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Add money & Gift a sadhak
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Terra,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAddMoneyClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "पैसे जोड़ें"
                                    "hgl" -> "Paise add karein"
                                    else -> "Add money"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, Ink),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onGiftClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CardGiftcard, contentDescription = null, tint = Ink, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "दक्षिणा भेजें"
                                    "hgl" -> "Dakshina bhejein"
                                    else -> "Gift a sadhak"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Pay Section
                Text(
                    text = when (currentLangCode) {
                        "hi" -> "त्वरित भुगतान"
                        "hgl" -> "Quick Pay"
                        else -> "Quick Pay"
                    },
                    fontSize = 12.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickAmounts = listOf(200, 500, 1000)
                    items(quickAmounts) { amount ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Terra.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Terra),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { /* Handle quick pay */ }
                        ) {
                            Text(
                                text = "₹$amount",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Terra
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Predefined Amount Selection Grid
        val amounts = listOf(50, 100, 200, 300, 500, 750, 1000, 2000, 5000, 10000)
        Text(
            text = when (currentLangCode) {
                "hi" -> "राशि चुनें"
                "hgl" -> "Select Amount"
                else -> "Select Amount"
            },
            fontSize = 12.sp,
            color = InkSoft,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(140.dp)
        ) {
            items(amounts) { amount ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PaperBg,
                    border = BorderStroke(1.dp, InkSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { /* Handle amount select */ }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "₹$amount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section Title: Recent Activity
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (currentLangCode) {
                    "hi" -> "हाल की गतिविधियाँ"
                    "hgl" -> "RECENT ACTIVITY"
                    else -> "RECENT ACTIVITY"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = InkFaint
            )
            Spacer(modifier = Modifier.width(10.dp))
            HorizontalDivider(color = EditorialLine, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transaction History List
        if (transactions.isEmpty()) {
            com.example.ui.components.DevEmptyState(
                title = when (currentLangCode) {
                    "hi" -> "अभी कोई लेन-देन नहीं है"
                    "hgl" -> "Abhi koi transaction nahi hai"
                    else -> "No transactions yet"
                },
                description = when (currentLangCode) {
                    "hi" -> "अपने वॉलेट में राशि जोड़ें और परामर्श व उपायों का इतिहास यहाँ देखें।"
                    "hgl" -> "Apne wallet mein balance add karein aur history dekhein."
                    else -> "Add balance to your wallet to view consultation and remedy history."
                },
                symbol = "📜",
                actionText = when (currentLangCode) {
                    "hi" -> "राशि जोड़ें (Add Money)"
                    "hgl" -> "Paise jodein (Add Money)"
                    else -> "Add Money"
                },
                onActionClick = onAddMoneyClick
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    Surface(
                        shape = RoundedCornerShape(13.dp),
                        color = PaperCard,
                        border = BorderStroke(1.dp, EditorialLine),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (tx.isCredit) Color(0x2467784F) else Color(0x1AC0521C),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (tx.isCredit) Icons.Filled.Add else Icons.AutoMirrored.Outlined.Chat,
                                        contentDescription = null,
                                        tint = if (tx.isCredit) Sage else Terra,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink
                                )
                                Text(
                                    text = tx.subtitle,
                                    fontSize = 10.sp,
                                    color = InkFaint
                                )
                            }

                            Text(
                                text = "${if (tx.isCredit) "+" else "−"}₹${tx.amount.toInt()}",
                                fontFamily = FontFamily.Serif,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.isCredit) Sage else Terra
                            )
                        }
                    }
                }
            }
        }
    }
}
