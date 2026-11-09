package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import android.app.Activity
import com.example.utils.RazorpayPaymentManager
import com.example.utils.Pricing
import com.example.utils.DreamSubmitter
import com.example.utils.DreamSubmitResult
import com.example.utils.PriceLabels
import com.example.utils.DisclaimerContent
import com.example.utils.SessionType
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.platform.testTag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.fragment.app.FragmentActivity
import com.example.BuildConfig
import com.example.security.SecureScreenEffect
import com.example.security.BiometricSecurityManager
import com.example.security.RootDetection
import com.example.security.RaspManager
import com.example.security.DeviceIntegrityManager
import com.example.ui.components.WalletSecurityRestrictionDialog
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
                    "hi" -> "Sadhak"
                    "hgl" -> "Sadhak"
                    else -> "Sadhak"
                },
                fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                if (isOfferClaimed) OnlineGreen else BorderBeige
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
                            if (currentLangCode == "hi") "First dream chat free 5 min — पात्रता सत्र शुरू होने पर सर्वर तय करेगा" else "First dream chat free for 5 min — eligibility is confirmed by the server",
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
                            color = if (isOfferClaimed) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                1.dp,
                                if (isOfferClaimed) OnlineGreen else BorderBeige
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = if (isOfferClaimed) Color(0xFF16A34A) else Saffron,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "पहली स्वप्न चैट • 5 MIN"
                                        "hgl" -> "FIRST DREAM CHAT • 5 MIN"
                                        else -> "FIRST DREAM CHAT • 5 MIN"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfferClaimed) Color(0xFF16A34A) else Saffron
                                )
                            }
                        }

                        if (false) { // coupon codes removed: free trial is server-decided
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
                                        fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
// English comment placeholder
                            "hgl" -> "Pehli dream chat 5 minute free*"
                            else -> "First dream chat free — 5 min*"
                        },
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (currentLangCode) {
                            "hi" -> if (isOfferClaimed) "किसी भी Sadhak से तुरंत चैट करें" else "सत्यापित Sadhak • तुरंत चैट शुरू करें"
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
                    "hi" -> "सत्यापित Sadhakों से अपनी कुंडली, दोष व उपायों पर सीधा परामर्श शुरू करें।"
                    "hgl" -> "Verified sadhaks se kundli, dosha aur upay par seedha baat karein."
                    else -> "Start personalized spiritual guidance and remedy consultation with verified sadhaks."
                },
                icon = Icons.Outlined.Chat,
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
                                        fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                                        fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                    "hi" -> "नए Sadhak जल्द ही उपलब्ध होंगे"
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
                    fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    modifier: Modifier = Modifier
) {
    val currentLangCode = "en"
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("all") }

    val categories = remember(currentLangCode) {
        listOf(
            "all" to "All",
            "desk" to "Jap & Sadhana",
            "wellness" to "Wellness",
            "drink" to "Drinkware"
        )
    }

    val screenTitle = "Remedy"
    val aajKaUpayLabel = "TODAY'S REMEDY"
    val sadhakUpayTitle = "Sadhak Remedies"
    val upaySamagriTitle = "Remedy Items"
    val onlineOrderingToast = "Online ordering coming soon"
    val restockingText = "Store is restocking — new remedies soon"
    val footerNotice = "More remedies restock with the new moon — we'll notify you."

    val initialProducts = remember {
        listOf(
            RemedyProductItem(
                id = "p1",
                name = "Pure Parad Shivling",
                subtitle = "Authentic Vedic energy & home peace",
                price = 1100,
                originalPrice = 1500,
                category = "wellness"
            ),
            RemedyProductItem(
                id = "p2",
                name = "Panchmukhi Rudraksha Mala",
                subtitle = "108 energized beads for meditation & japa",
                price = 550,
                originalPrice = 750,
                category = "desk"
            ),
            RemedyProductItem(
                id = "p3",
                name = "Pure Copper Water Bottle",
                subtitle = "Ayurvedic copper vessel for morning water",
                price = 799,
                originalPrice = 999,
                category = "drink"
            )
        )
    }
    val initialSadhakUpay = remember {
        listOf(
            listOf("Health", "Holy Tulsi & Ganga Jal Intake", "Take 3 holy basil leaves with holy Ganges water in the morning for cellular purity.", "Pt. Ramanand Shastri"),
            listOf("Peace", "Mahamrityunjaya Mantra Chanting", "Chant Mahamrityunjaya Mantra 11 times in the evening with a calm mind.", "Pt. Devrat Shastri"),
            listOf("Prosperity", "Shri Sukta Recitation & Deepam", "Light a pure cow ghee lamp before Goddess Lakshmi on Friday evening.", "Dr. Radhika Vashishtha")
        )
    }
    val initialDailyUpay = remember {
        Triple("Morning Surya Arghya & Gayatri Japa", "Offer fresh water in a copper vessel facing East towards Surya Deva with sincere devotion. Enhances vitality, clarity, and inner radiance.", "Special Remedy of the Day")
    }

    var products by remember { mutableStateOf<List<RemedyProductItem>>(initialProducts) }
    var sadhakUpay by remember { mutableStateOf<List<List<String>>>(initialSadhakUpay) }
    var dailyUpay by remember { mutableStateOf<Triple<String, String, String>?>(initialDailyUpay) }
    val filtered = remember(selectedCategory, products) {
        if (selectedCategory == "all") products else products.filter { it.category == selectedCategory }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxSize()
            .background(PaperBg)
            .padding(horizontal = 20.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Rule Header
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = screenTitle,
                    fontFamily = com.example.ui.theme.ManropeHindFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink
                )
            }
        }

        // Category Filter Chips
        item(span = { GridItemSpan(2) }) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
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
        }

        // Daily Upay Card
        if (dailyUpay != null) {
            item(span = { GridItemSpan(2) }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PaperCard,
                    border = BorderStroke(1.5.dp, Terra),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(18.dp).height(2.dp).background(Terra))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = aajKaUpayLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = Terra
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.Balance,
                                contentDescription = null,
                                tint = Terra,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dailyUpay!!.first,
                            fontFamily = com.example.ui.theme.ManropeHindFontFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "“${dailyUpay!!.second}”",
                            fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = EditorialLine.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = DisclaimerContent.REMEDY_DISCLAIMER_TEXT,
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 14.5.sp,
                            color = InkFaint
                        )
                    }
                }
            }
        }

        // Sadhak Ke Upay
        if (sadhakUpay.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = sadhakUpayTitle,
                    fontFamily = com.example.ui.theme.ManropeHindFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            items(sadhakUpay.size, span = { GridItemSpan(2) }) { index ->
                val (category, title, steps, byName) = sadhakUpay[index]
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PaperCard,
                    border = BorderStroke(1.dp, EditorialLine),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
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
                                    .border(BorderStroke(1.dp, BorderBeige), RoundedCornerShape(999.dp))
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
                            fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = EditorialLine.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = DisclaimerContent.REMEDY_DISCLAIMER_TEXT,
                            fontSize = 9.5.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 14.sp,
                            color = InkFaint
                        )
                    }
                }
            }
        }

        // Upay Samagri Header
        item(span = { GridItemSpan(2) }) {
            Text(
                text = upaySamagriTitle,
                fontFamily = com.example.ui.theme.ManropeHindFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Ink,
                modifier = Modifier.padding(top = 16.dp, bottom = 2.dp)
            )
        }

        if (filtered.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = restockingText,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PaperCard,
                    border = BorderStroke(1.dp, EditorialLine),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            Toast.makeText(context, onlineOrderingToast, Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Column {
                        // Product Graphic Tile
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(92.dp)
                                .background(Color(0xFFFAFAFA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item.iconEmoji, fontSize = 34.sp)
                        }

                        // Details
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${item.name} · ${item.subtitle}",
                                fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                                    fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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

        // Footer restock notice
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = footerNotice,
                    fontSize = 10.5.sp,
                    fontStyle = FontStyle.Italic,
                    color = InkFaint,
                    textAlign = TextAlign.Center
                )
            }
        }
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
    val saffronColor = MaterialTheme.colorScheme.primary

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
                    fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = saffronColor
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "आज रात क्या देखा?",
                                    fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                                        text = "सुन रहा है...",
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
                                    coroutineScope.launch {
                                        // Server decides price / free trial and debits the wallet.
                                        val res = DreamSubmitter.submitDream(
                                            userName = userName,
                                            dreamText = text,
                                            type = SessionType.DREAM_CHAT
                                        )
                                        when (res) {
                                            is DreamSubmitResult.Success -> {
                                                dreamText = ""
                                                isListeningVoice = false
                                                val freeNote = if (res.isFreeTrial) " (${PriceLabels.FREE_TRIAL_BADGE})" else ""
                                                Toast.makeText(context, "सपना दर्ज हुआ — Sadhak '${res.sadhakName}' को सौंपा गया$freeNote", Toast.LENGTH_LONG).show()
                                            }
                                            is DreamSubmitResult.InsufficientBalance -> {
                                                val activity = context as? Activity
                                                if (activity != null) {
                                                    val started = RazorpayPaymentManager.startRechargePayment(
                                                        activity = activity,
                                                        amount = Pricing.DREAM_CHAT_PRICE_RUPEES.toDouble(),
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
                                                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                            is DreamSubmitResult.NoVerifiedSadhak -> {
                                                Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                            }
                                            is DreamSubmitResult.Error -> {
                                                Toast.makeText(context, "Error: ${res.message}", Toast.LENGTH_LONG).show()
                                            }
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
                                text = "सपना दर्ज करें · ${PriceLabels.DREAM_CHAT}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "वैदिक Sadhakों द्वारा 100% व्यक्तिगत स्वप्न फल मीमांसा",
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
                        fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = saffronColor
                                )
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
                                fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                        entry.description.contains("सांप", ignoreCase = true) || entry.description.contains("शिव", ignoreCase = true) || entry.description.contains("मंदिर", ignoreCase = true) -> "शुभ (Auspicious)"
                        entry.description.contains("गिरना", ignoreCase = true) || entry.description.contains("डर", ignoreCase = true) || entry.description.contains("अंधेरा", ignoreCase = true) -> "चेतावनी (Alert)"
                        entry.description.contains("उड़ना", ignoreCase = true) || entry.description.contains("पक्षी", ignoreCase = true) || entry.description.contains("आकाश", ignoreCase = true) -> "मुक्ति (Freedom)"
                        else -> "रहस्यमय (Mystic)"
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
                                fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                                            color = SaffronSoftBg,
                                            border = BorderStroke(0.8.dp, saffronColor.copy(alpha = 0.25f))
                                        ) {
                                            Text(
                                                text = t,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = saffronColor,
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
                                                Toast.makeText(context, "सपना कॉपी किया गया", Toast.LENGTH_SHORT).show()
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
// English comment placeholder
                                    Button(
                                        onClick = {
                                            selectedMeaningEntryId = entry.id
                                            Toast.makeText(context, "वैदिक स्वप्न फल लोड हो गया", Toast.LENGTH_SHORT).show()
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
 * Data model for recharge packs
 */
data class WalletRechargePack(
    val amount: Int,
    val bonusCoins: Int = 0,
    val tag: String? = null,
    val isBestValue: Boolean = false
) {
    val totalCoins: Int get() = amount + bonusCoins
}

private fun formatWalletTimestamp(timestamp: Long, isHindi: Boolean): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val date = Date(timestamp)
    return when {
        diff in 0..(24 * 60 * 60 * 1000L) -> {
            val prefix = if (isHindi) "आज" else "Today"
            "$prefix, ${timeFormat.format(date)}"
        }
        diff in (24 * 60 * 60 * 1000L)..(48 * 60 * 60 * 1000L) -> {
            val prefix = if (isHindi) "कल" else "Yesterday"
            "$prefix, ${timeFormat.format(date)}"
        }
        else -> dateFormat.format(date)
    }
}

private fun getDefaultWalletTransactions(isHindi: Boolean): List<WalletTransaction> {
    val now = System.currentTimeMillis()
    val hour = 3600_000L
    val day = 86400_000L
    return listOf(
        WalletTransaction(
            id = "TXN-8942",
            title = if (isHindi) "वॉलेट टॉप-अप (UPI)" else "Wallet Top-up (UPI)",
            subtitle = if (isHindi) "HDFC Bank • रेज़रपे आईडी #TXN-8942" else "HDFC Bank • Razorpay ID #TXN-8942",
            amount = 500.0,
            isCredit = true,
            timestamp = now - (2 * hour),
            status = "success"
        ),
        WalletTransaction(
            id = "TXN-8819",
            title = if (isHindi) "आचार्य से वैदिक परामर्श" else "Vedic Consultation with Acharya",
            subtitle = if (isHindi) "लाइव ऑडियो परामर्श (15 मिनट)" else "Live Audio Consultation (15 min)",
            amount = 249.0,
            isCredit = false,
            timestamp = now - (1 * day),
            status = "success"
        ),
        WalletTransaction(
            id = "TXN-8740",
            title = if (isHindi) "वॉलेट टॉप-अप (NetBanking)" else "Wallet Top-up (NetBanking)",
            subtitle = if (isHindi) "ICICI Bank • संदर्भ #TXN-8740" else "ICICI Bank • Ref #TXN-8740",
            amount = 1000.0,
            isCredit = true,
            timestamp = now - (3 * day),
            status = "success"
        ),
        WalletTransaction(
            id = "TXN-8652",
            title = if (isHindi) "शनि शांति अनुष्ठान संकल्प" else "Shani Shanti Remedy Booking",
            subtitle = if (isHindi) "पुरोहित आवंटन एवं संकल्प प्रगति पर" else "Priest allocation & sankalp in progress",
            amount = 351.0,
            isCredit = false,
            timestamp = now - (4 * day),
            status = "pending"
        ),
        WalletTransaction(
            id = "TXN-8510",
            title = if (isHindi) "साधक दक्षिणा भेंट" else "Gift Dakshina to Sadhak",
            subtitle = if (isHindi) "पं. राधेश्याम शास्त्री को दक्षिणा" else "Dakshina to Pt. Radheshyam Shastri",
            amount = 101.0,
            isCredit = false,
            timestamp = now - (6 * day),
            status = "success"
        )
    )
}

/**
 * 4. Wallet Screen — Modern High-End Fintech Redesign
 * 1. Hero Balance Card: Premium glowing gradient card with clear typography, secure shield badge, and smooth quick-add coin breakdown.
 * 2. Recharge Packs & Grid: Interactive tactile elevation cards (₹100, ₹250, ₹500, ₹1000, ₹2000, ₹5000) with clear bonus indicators and active selection states.
 * 3. Transaction History Section: Structured row items with distinct debit/credit avatars, timestamps, and status chips (Success/Pending/Failed).
 * 4. Secure Checkout Footer: Prominent secure payment button, localized text support, and proper padding to prevent overflow.
 */
@Composable
fun BentoEditorialWalletScreen(
    balanceAmount: Double?,
    onBackClick: () -> Unit,
    onAddMoneyClick: () -> Unit,
    onGiftClick: () -> Unit,
    isHindi: Boolean = false,
    language: String = if (isHindi) "hi" else "en",
    transactions: List<WalletTransaction>? = null,
    onRechargeWithAmount: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val packs = remember {
        listOf(
            WalletRechargePack(amount = 100, bonusCoins = 0, tag = null),
            WalletRechargePack(amount = 250, bonusCoins = 25, tag = "+10% Extra"),
            WalletRechargePack(amount = 500, bonusCoins = 75, tag = "BEST VALUE", isBestValue = true),
            WalletRechargePack(amount = 1000, bonusCoins = 200, tag = "+20% Extra"),
            WalletRechargePack(amount = 2000, bonusCoins = 500, tag = "+25% Extra"),
            WalletRechargePack(amount = 5000, bonusCoins = 1500, tag = "+30% VIP")
        )
    }

    var selectedPack by remember { mutableStateOf(packs[2]) } // Default to ₹500 Best Value pack
    var activeFilter by remember { mutableStateOf("all") } // "all", "credits", "debits"

    val resolvedTransactions = remember(transactions, isHindi) {
        transactions ?: getDefaultWalletTransactions(isHindi)
    }

    val filteredTransactions = remember(resolvedTransactions, activeFilter) {
        when (activeFilter) {
            "credits" -> resolvedTransactions.filter { it.isCredit }
            "debits" -> resolvedTransactions.filter { !it.isCredit }
            else -> resolvedTransactions
        }
    }

    // 3. Screen Protector: Apply FLAG_SECURE on sensitive wallet & balance screens
    SecureScreenEffect(enabled = true)

    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity

    // Dual-layered local integrity scan via RaspManager and DeviceIntegrityManager
    var isDeviceRestricted by remember { mutableStateOf(false) }
    var detectedFlags by remember { mutableStateOf<List<String>>(emptyList()) }
    var showRestrictionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val raspStatus = RaspManager.evaluateRaspSecurity()
        val deviceEval = DeviceIntegrityManager.evaluateDeviceIntegrity(context)
        val rootResult = RootDetection.checkDeviceIntegrity(context)

        val flags = mutableListOf<String>()
        flags.addAll(rootResult.detectedIndicators)
        flags.addAll(deviceEval.rootIndicators)
        if (raspStatus.isFridaDetected) flags.add("Frida dynamic instrumentation hook detected")
        if (raspStatus.isXposedDetected) flags.add("Xposed framework signature detected")
        if (raspStatus.isDebuggerConnected && !BuildConfig.DEBUG) flags.add("Active debugger attached")
        if (raspStatus.isPtraceAttached && !BuildConfig.DEBUG) flags.add("Process ptrace attachment detected")

        val maliciousRoot = deviceEval.isRooted && (deviceEval.rootIndicators.any { it.contains("su") || it.contains("test-keys") } || !BuildConfig.DEBUG)
        val instrumentationFlagged = raspStatus.isFridaDetected || raspStatus.isXposedDetected || (!BuildConfig.DEBUG && (raspStatus.isDebuggerConnected || raspStatus.isPtraceAttached))

        if (maliciousRoot || instrumentationFlagged || rootResult.isCompromised || flags.isNotEmpty() && !BuildConfig.DEBUG) {
            isDeviceRestricted = true
            detectedFlags = flags.distinct()
            showRestrictionDialog = true
        }
    }

    if (showRestrictionDialog) {
        WalletSecurityRestrictionDialog(
            isHindi = isHindi,
            indicators = detectedFlags,
            onDismiss = {
                showRestrictionDialog = false
                onBackClick() // restricts access and returns user to safety
            },
            onToggleSimulation = if (BuildConfig.DEBUG) {
                {
                    RootDetection.simulateCompromisedForTesting = !RootDetection.simulateCompromisedForTesting
                    isDeviceRestricted = RootDetection.isWalletRestricted(context)
                    showRestrictionDialog = isDeviceRestricted
                }
            } else null
        )
    }

    // Tactile spring scale for checkout CTA button
    val checkoutInteraction = remember { MutableInteractionSource() }
    val isCheckoutPressed by checkoutInteraction.collectIsPressedAsState()
    val checkoutScale by animateFloatAsState(
        targetValue = if (isCheckoutPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkout_button_scale"
    )

    // Secure checkout handler with Biometric Authentication and Root Restriction Guard
    val handleCheckout: () -> Unit = {
        if (isDeviceRestricted || RootDetection.isWalletRestricted(context)) {
            showRestrictionDialog = true
        } else if (fragmentActivity != null &&
            BiometricSecurityManager.checkBiometricAvailability(context) == BiometricSecurityManager.BiometricStatus.AVAILABLE
        ) {
            BiometricSecurityManager.authenticate(
                activity = fragmentActivity,
                title = if (currentLangCode == "hi") "वॉलेट भुगतान सत्यापन" else "Confirm Wallet Recharge",
                subtitle = if (currentLangCode == "hi") "₹${selectedPack.amount} के सुरक्षित रिचार्ज हेतु बायोमेट्रिक सत्यापन करें" else "Authenticate biometrics to proceed with ₹${selectedPack.amount} recharge"
            ) { result ->
                if (result is BiometricSecurityManager.AuthResult.Success) {
                    if (onRechargeWithAmount != null) {
                        onRechargeWithAmount(selectedPack.amount)
                    } else {
                        onAddMoneyClick()
                    }
                }
            }
        } else {
            if (onRechargeWithAmount != null) {
                onRechargeWithAmount(selectedPack.amount)
            } else {
                onAddMoneyClick()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PaperBg,
        bottomBar = {
            // 4. Secure Checkout Footer: Pinned elevated bottom CTA bar with safe area padding
            Surface(
                color = SurfaceWhite,
                shadowElevation = 14.dp,
                border = BorderStroke(1.dp, OutlineGray.copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Summary row: Amount & Coins breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "देय कुल राशि"
                                    "hgl" -> "Payable Amount"
                                    else -> "Payable Amount"
                                },
                                fontSize = 11.sp,
                                color = SecondaryText,
                                fontWeight = FontWeight.Medium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronPrimary
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${selectedPack.amount}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MainText
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SaffronContainer,
                            border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🪙", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "${selectedPack.totalCoins} सिक्के प्राप्त होंगे"
                                        "hgl" -> "Get ${selectedPack.totalCoins} Coins"
                                        else -> "Get ${selectedPack.totalCoins} Dev Coins"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Trust security line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLangCode) {
                                "hi" -> "256-बिट सुरक्षित भुगतान • रेज़रपे (UPI / कार्ड्स / नेटबैंकिंग)"
                                "hgl" -> "256-Bit Secure Payment • Razorpay (UPI, Cards, NetBanking)"
                                else -> "256-Bit SSL Secured • Razorpay (UPI, Cards, NetBanking)"
                            },
                            fontSize = 10.5.sp,
                            color = SecondaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Prominent Checkout Button with tactile spring feedback, biometric auth & RASP shield
                    Button(
                        onClick = {
                            val liveRasp = RaspManager.evaluateRaspSecurity()
                            val liveDevice = DeviceIntegrityManager.evaluateDeviceIntegrity(context)

                            // Synchronously parse the state arrays and indicators
                            val tamperingFlags = mutableListOf<String>()
                            if (liveRasp.isDebuggerConnected && !BuildConfig.DEBUG) {
                                tamperingFlags.add("Active debugger attached")
                            }
                            if (liveRasp.isPtraceAttached && !BuildConfig.DEBUG) {
                                tamperingFlags.add("Process ptrace attachment detected")
                            }
                            if (liveRasp.isFridaDetected) {
                                tamperingFlags.add("Frida dynamic instrumentation hook detected")
                            }
                            if (liveRasp.isXposedDetected) {
                                tamperingFlags.add("Xposed framework signature detected")
                            }

                            // Check actual malicious root indicators (presence of su binaries or test-keys build tags)
                            val maliciousRootIndicators = liveDevice.rootIndicators.filter { 
                                it.contains("su", ignoreCase = true) || it.contains("test-keys", ignoreCase = true)
                            }
                            tamperingFlags.addAll(maliciousRootIndicators)

                            // Browser-based debug streaming allowed only if BuildConfig.DEBUG is true and no malicious root indicators are found
                            val isDebugStreamingAllowed = BuildConfig.DEBUG && maliciousRootIndicators.isEmpty() && !liveRasp.isFridaDetected && !liveRasp.isXposedDetected
                            val isTampered = tamperingFlags.isNotEmpty() || (!isDebugStreamingAllowed && (liveDevice.isRooted || liveRasp.isTampered))

                            if (isTampered || isDeviceRestricted || RootDetection.isWalletRestricted(context)) {
                                // Freeze payment button immediately
                                isDeviceRestricted = true
                                detectedFlags = (tamperingFlags + detectedFlags).distinct()
                                showRestrictionDialog = true
                            } else {
                                handleCheckout()
                            }
                        },
                        enabled = !isDeviceRestricted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .graphicsLayer {
                                scaleX = checkoutScale
                                scaleY = checkoutScale
                            }
                            .testTag("wallet_checkout_button"),
                        shape = RoundedCornerShape(14.dp),
                        interactionSource = checkoutInteraction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDeviceRestricted) Color(0xFFDC2626) else SaffronPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE2E8F0),
                            disabledContentColor = Color(0xFF94A3B8)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isDeviceRestricted) Icons.Default.GppBad else Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDeviceRestricted) {
                                    if (currentLangCode == "hi") "सुरक्षा कारणों से वॉलेट लॉक है" else "Wallet Restricted (Untrusted Device)"
                                } else {
                                    when (currentLangCode) {
                                        "hi" -> "₹${selectedPack.amount} का सुरक्षित भुगतान करें"
                                        "hgl" -> "Proceed to Pay ₹${selectedPack.amount}"
                                        else -> "Proceed to Pay ₹${selectedPack.amount}"
                                    }
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!isDeviceRestricted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Header with Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, EditorialLineStrong),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onBackClick() }
                            .testTag("wallet_back_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Ink,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (currentLangCode) {
                                "hi" -> "देव वॉलेट"
                                "hgl" -> "Dev Wallet"
                                else -> "Dev Wallet"
                            },
                            fontFamily = com.example.ui.theme.ManropeHindFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = when (currentLangCode) {
                                "hi" -> "सिक्के, शेष राशि व लेन-देन का विवरण"
                                "hgl" -> "Coins, Balance & Transaction Logs"
                                else -> "Coins, Balance & Transaction Logs"
                            },
                            fontSize = 11.5.sp,
                            color = SecondaryText
                        )
                    }
                }

                if (BuildConfig.DEBUG) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDeviceRestricted) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isDeviceRestricted) Color(0xFFFCA5A5) else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                RootDetection.simulateCompromisedForTesting = !RootDetection.simulateCompromisedForTesting
                                isDeviceRestricted = RootDetection.isWalletRestricted(context)
                                showRestrictionDialog = isDeviceRestricted
                            }
                            .testTag("debug_integrity_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDeviceRestricted) Icons.Default.GppBad else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isDeviceRestricted) Color(0xFFDC2626) else SaffronDeep,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isDeviceRestricted)
                                    "Play Integrity: RESTRICTED (Tap to reset)"
                                else
                                    "Play Integrity / Root Detection Active (Tap to test restriction)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDeviceRestricted) Color(0xFF991B1B) else Color(0xFF334155)
                            )
                        }
                    }
                }
            }

            // Warning Banner if Device is Rooted / Restricted
            if (isDeviceRestricted) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRestrictionDialog = true }
                            .testTag("wallet_restriction_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GppBad,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "सुरक्षा चेतावनी: वॉलेट प्रतिबंधित है" else "Security Restriction: Wallet Restricted",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = if (isHindi) "रूटेड या असुरक्षित डिवाइस पर वित्तीय लेन-देन अवरुद्ध हैं। विवरण देखें >" else "Financial transactions blocked on rooted/untrusted device. Tap for details >",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                }
            }

            // 1. Hero Balance Card: Premium glowing gradient card with subtle tactile spring animation
            item {
                val heroInteraction = remember { MutableInteractionSource() }
                val isHeroPressed by heroInteraction.collectIsPressedAsState()
                val heroScale by animateFloatAsState(
                    targetValue = if (isHeroPressed) 0.98f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "hero_balance_card_spring_scale"
                )

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 10.dp,
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                SaffronSoft.copy(alpha = 0.55f),
                                SaffronPrimary.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.10f)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = heroScale
                            scaleY = heroScale
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = heroInteraction,
                            indication = null
                        ) { onAddMoneyClick() }
                        .testTag("wallet_hero_balance_card")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        SaffronDark,
                                        SaffronDeep,
                                        SaffronPrimary
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Column {
                            // Top Row: Label & Subtle Secure Shield Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = SaffronSoft,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLangCode) {
                                            "hi" -> "उपलब्ध देव बैलेंस"
                                            "hgl" -> "AVAILABLE WALLET BALANCE"
                                            else -> "AVAILABLE WALLET BALANCE"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.3.sp,
                                        color = SaffronSoft
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF34D399))
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = SaffronSoft,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when (currentLangCode) {
                                                "hi" -> "100% सुरक्षित"
                                                "hgl" -> "256-BIT SECURE"
                                                else -> "256-BIT SECURE"
                                            },
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            color = SaffronSoft
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Large Balance Display
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SaffronSoft.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%,d", balanceAmount?.toInt() ?: 0),
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "सक्रिय खाता • तत्काल उपयोग हेतु उपलब्ध"
                                    "hgl" -> "Active Account • Instant 1:1 Coins"
                                    else -> "Active Account • Instant 1:1 Coins"
                                },
                                fontSize = 11.sp,
                                color = SaffronSoft.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Smooth Quick-Add Coin Breakdown Capsule
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🪙", fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(7.dp))
                                        Text(
                                            text = when (currentLangCode) {
                                                "hi" -> "${balanceAmount?.toInt() ?: 0} देव सिक्के उपलब्ध"
                                                "hgl" -> "${balanceAmount?.toInt() ?: 0} Dev Coins Available"
                                                else -> "${balanceAmount?.toInt() ?: 0} Dev Coins Available"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = "1 Coin = ₹1.00",
                                            fontSize = 10.sp,
                                            color = SaffronSoft,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Quick Top-up Shortcuts row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "त्वरित चयन (Quick Add):"
                                        "hgl" -> "Quick Select:"
                                        else -> "Quick Select:"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SaffronSoft.copy(alpha = 0.9f)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(100, 250, 500, 1000).forEach { amt ->
                                        val isChosen = selectedPack.amount == amt
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isChosen) SaffronPrimary else Color.White.copy(alpha = 0.14f),
                                            border = BorderStroke(1.dp, if (isChosen) Color.White else Color.White.copy(alpha = 0.25f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedPack = packs.firstOrNull { it.amount == amt }
                                                        ?: WalletRechargePack(amount = amt, bonusCoins = 0)
                                                }
                                        ) {
                                            Text(
                                                text = "+₹$amt",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Dual Action Buttons with subtle spring animations: Add Money & Gift a Sadhak
                            val addMoneyInteraction = remember { MutableInteractionSource() }
                            val isAddMoneyPressed by addMoneyInteraction.collectIsPressedAsState()
                            val addMoneyScale by animateFloatAsState(
                                targetValue = if (isAddMoneyPressed) 0.94f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "add_money_scale"
                            )

                            val giftInteraction = remember { MutableInteractionSource() }
                            val isGiftPressed by giftInteraction.collectIsPressedAsState()
                            val giftScale by animateFloatAsState(
                                targetValue = if (isGiftPressed) 0.94f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "gift_scale"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onAddMoneyClick,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .graphicsLayer {
                                            scaleX = addMoneyScale
                                            scaleY = addMoneyScale
                                        }
                                        .testTag("wallet_add_money_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    interactionSource = addMoneyInteraction,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SaffronPrimary,
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLangCode) {
                                            "hi" -> "पैसे जोड़ें"
                                            "hgl" -> "Paise Jodein"
                                            else -> "Add Money"
                                        },
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = onGiftClick,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .graphicsLayer {
                                            scaleX = giftScale
                                            scaleY = giftScale
                                        }
                                        .testTag("wallet_gift_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    interactionSource = giftInteraction,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CardGiftcard,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLangCode) {
                                            "hi" -> "दक्षिणा भेजें"
                                            "hgl" -> "Dakshina Bhejein"
                                            else -> "Gift Dakshina"
                                        },
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Recharge Packs & Grid: Interactive tactile elevation cards with clear bonus indicators and active selection states
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "रिचार्ज पैक चुनें"
                                    "hgl" -> "RECHARGE PACKS"
                                    else -> "RECHARGE PACKS"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = SecondaryText
                            )
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "अतिरिक्त बोनस कॉइन्स व तत्काल क्रेडिट"
                                    "hgl" -> "Instant coins credit with bonus benefits"
                                    else -> "Instant coins credit with bonus benefits"
                                },
                                fontSize = 11.5.sp,
                                color = InkSoft
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = SaffronContainer,
                            border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "बोनस उपलब्ध"
                                    "hgl" -> "Bonus Active"
                                    else -> "Bonus Active"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3-Column Grid for Recharge Packs with tactile spring-based scale feedback
                    val rows = packs.chunked(3)
                    rows.forEach { rowPacks ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowPacks.forEach { pack ->
                                val isSelected = selectedPack.amount == pack.amount
                                val packInteraction = remember { MutableInteractionSource() }
                                val isPackPressed by packInteraction.collectIsPressedAsState()
                                val packScale by animateFloatAsState(
                                    targetValue = when {
                                        isPackPressed -> 0.94f
                                        isSelected -> 1.02f
                                        else -> 1.0f
                                    },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "pack_scale_${pack.amount}"
                                )
                                val packElevation by animateDpAsState(
                                    targetValue = when {
                                        isPackPressed -> 1.dp
                                        isSelected -> 6.dp
                                        else -> 1.dp
                                    },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "pack_elevation_${pack.amount}"
                                )

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) SaffronSurface else SurfaceWhite,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) SaffronPrimary else OutlineGray
                                    ),
                                    shadowElevation = packElevation,
                                    modifier = Modifier
                                        .weight(1f)
                                        .graphicsLayer {
                                            scaleX = packScale
                                            scaleY = packScale
                                        }
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(
                                            interactionSource = packInteraction,
                                            indication = null
                                        ) { selectedPack = pack }
                                        .testTag("wallet_pack_card_${pack.amount}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 11.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Bonus or Best Value Tag
                                        if (pack.isBestValue) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = SaffronPrimary
                                            ) {
                                                Text(
                                                    text = when (currentLangCode) {
                                                        "hi" -> "★ श्रेष्ठ"
                                                        "hgl" -> "★ BEST"
                                                        else -> "★ BEST"
                                                    },
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else if (pack.tag != null) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = SaffronContainer
                                            ) {
                                                Text(
                                                    text = pack.tag,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SaffronPrimary,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(14.dp))
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Amount
                                        Text(
                                            text = "₹${pack.amount}",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SaffronPrimary else MainText
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Total Coins
                                        Text(
                                            text = "${pack.totalCoins} Coins",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) VerifiedGreen else SecondaryText
                                        )

                                        if (pack.bonusCoins > 0) {
                                            Text(
                                                text = "+${pack.bonusCoins} Bonus",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SaffronPrimary
                                            )
                                        } else {
                                            Text(
                                                text = "Standard",
                                                fontSize = 9.sp,
                                                color = SecondaryText.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Active Selection Summary Strip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SaffronContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (currentLangCode) {
                                        "hi" -> "चयनित: ₹${selectedPack.amount} पैक"
                                        "hgl" -> "Selected: ₹${selectedPack.amount} Pack"
                                        else -> "Selected: ₹${selectedPack.amount} Pack"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                            }

                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "कुल ${selectedPack.totalCoins} देव सिक्के प्राप्त होंगे"
                                    "hgl" -> "Receive ${selectedPack.totalCoins} Dev Coins"
                                    else -> "Total ${selectedPack.totalCoins} Dev Coins"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SaffronPrimary
                            )
                        }
                    }
                }
            }

            // 3. Transaction History Section: Cleaned up transaction logs list
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "लेन-देन का इतिहास"
                                    "hgl" -> "TRANSACTION HISTORY"
                                    else -> "TRANSACTION HISTORY"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = SecondaryText
                            )
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "हाल की सभी गतिविधियां एवं रसीदें"
                                    "hgl" -> "Recent activities and invoices"
                                    else -> "Recent activities & invoices"
                                },
                                fontSize = 11.5.sp,
                                color = InkSoft
                            )
                        }

                        // Filter Chips Row
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "all" to if (currentLangCode == "hi") "सभी" else "All",
                                "credits" to if (currentLangCode == "hi") "जमा (+)" else "Credits (+)",
                                "debits" to if (currentLangCode == "hi") "खर्च (-)" else "Debits (-)"
                            ).forEach { (key, label) ->
                                val isActive = activeFilter == key
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isActive) SaffronPrimary else SurfaceWhite,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isActive) SaffronPrimary else OutlineGray
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .clickable { activeFilter = key }
                                        .testTag("wallet_filter_$key")
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else SecondaryText,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Transaction items or Empty State
            if (filteredTransactions.isEmpty()) {
                item {
                    com.example.ui.components.DevEmptyState(
                        title = when (currentLangCode) {
                            "hi" -> "इस श्रेणी में कोई लेन-देन नहीं है"
                            "hgl" -> "Is category mein koi transaction nahi mila"
                            else -> "No transactions in this category"
                        },
                        description = when (currentLangCode) {
                            "hi" -> "अपने वॉलेट में राशि जोड़ें और परामर्श व उपायों का इतिहास यहाँ देखें।"
                            "hgl" -> "Apne wallet mein balance add karein aur history dekhein."
                            else -> "Recharge your dev wallet to start consultations and track receipts."
                        },
                        icon = Icons.Outlined.ReceiptLong,
                        actionText = when (currentLangCode) {
                            "hi" -> "राशि जोड़ें (Add Money)"
                            "hgl" -> "Paise jodein (Add Money)"
                            else -> "Add Money"
                        },
                        onActionClick = onAddMoneyClick
                    )
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, OutlineGray.copy(alpha = 0.7f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wallet_transaction_item_${tx.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Distinct Avatar for Credit vs Debit
                            Surface(
                                shape = CircleShape,
                                color = if (tx.isCredit) Color(0xFFDCFCE7) else Color(0xFFFEF2F2),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (tx.isCredit) Icons.Filled.Add else Icons.Filled.Remove,
                                        contentDescription = if (tx.isCredit) "Credit" else "Debit",
                                        tint = if (tx.isCredit) VerifiedGreen else ErrorRed,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            // Title, Subtitle, and Formatted Timestamp
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MainText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = tx.subtitle,
                                    fontSize = 11.sp,
                                    color = SecondaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatWalletTimestamp(tx.timestamp, isHindi),
                                    fontSize = 10.sp,
                                    color = SecondaryText.copy(alpha = 0.8f)
                                )
                            }

                            // Amount & Status Chip
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (tx.isCredit) "+" else "−"}₹${tx.amount.toInt()}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.isCredit) VerifiedGreen else MainText
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                // Status Chip: Success / Pending / Failed
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = when (tx.status.lowercase()) {
                                        "success" -> Color(0xFFDCFCE7)
                                        "pending" -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFFEE2E2)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (tx.status.lowercase() == "success") {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF166534),
                                                modifier = Modifier.size(9.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                        } else if (tx.status.lowercase() == "pending") {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = Color(0xFF92400E),
                                                modifier = Modifier.size(9.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                        }
                                        Text(
                                            text = when (tx.status.lowercase()) {
                                                "success" -> if (isHindi) "सफल" else "Success"
                                                "pending" -> if (isHindi) "प्रगति पर" else "Pending"
                                                else -> if (isHindi) "असफल" else "Failed"
                                            },
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (tx.status.lowercase()) {
                                                "success" -> Color(0xFF166534)
                                                "pending" -> Color(0xFF92400E)
                                                else -> Color(0xFF991B1B)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Spacer to prevent cutoff
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

