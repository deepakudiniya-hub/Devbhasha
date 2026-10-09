package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*
import com.example.utils.CardContentRepository
import com.example.utils.FirestoreCardContent
import com.example.utils.CommentRepository
import com.example.utils.CardComment
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirestoreCardDetailScreen(
    cardId: String,
    onBackClick: () -> Unit,
    onStartChat: (SadhakItem) -> Unit = {},
    onStartCall: (SadhakItem) -> Unit = {},
    sadhaks: List<SadhakItem> = emptyList(),
    isHindi: Boolean = true,
    language: String = "hi",
    modifier: Modifier = Modifier
) {
    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var cardContent by remember { mutableStateOf<FirestoreCardContent?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()
    var submittedMessage by remember { mutableStateOf<String?>(null) }
    var commentsList by remember { mutableStateOf<List<CardComment>>(emptyList()) }
    var newCommentText by remember { mutableStateOf("") }
    var isPostingComment by remember { mutableStateOf(false) }

    // State-based fetching from Firestore using the card's unique ID with fallback empty state handling
    LaunchedEffect(cardId) {
        isLoading = true
        errorMessage = null
        try {
            val remoteContent = CardContentRepository.fetchCardContent(cardId)
            if (remoteContent.existsInCloud) {
                cardContent = remoteContent
                errorMessage = null
            } else {
                // Empty state fallback when no document exists in Firestore collection
                cardContent = getDefaultCardContent(cardId, isHindi)
                errorMessage = if (isHindi) 
                    "क्लाउड डेटाबेस पर कार्ड ID '$cardId' का कोई दस्तावेज़ नहीं मिला। (Empty State Fallback सक्रिय)" 
                else 
                    "No corresponding document found in Firestore collection for card ID '$cardId'. (Showing fallback state)"
            }
            commentsList = CommentRepository.fetchComments(cardId)
        } catch (e: Exception) {
            cardContent = getDefaultCardContent(cardId, isHindi)
            errorMessage = if (isHindi) "क्लाउड फेच त्रुटि: ${e.localizedMessage}" else "Cloud fetch error: ${e.localizedMessage}"
            commentsList = CommentRepository.fetchComments(cardId)
        } finally {
            isLoading = false
        }
    }

    val displayContent = cardContent ?: getDefaultCardContent(cardId, isHindi)
    val title = if (currentLangCode == "hi") displayContent.titleHi.ifBlank { displayContent.titleEn } else displayContent.titleEn.ifBlank { displayContent.titleHi }
    val description = if (currentLangCode == "hi") displayContent.descriptionHi.ifBlank { displayContent.descriptionEn } else displayContent.descriptionEn.ifBlank { displayContent.descriptionHi }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAF7F2),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = displayContent.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Ink
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
                    }
                },
                actions = {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                appendLine("${displayContent.emoji} $title")
                                appendLine()
                                appendLine(description)
                                appendLine()
                                appendLine("✨ वैदिक उपाय / Remedies:")
                                displayContent.remedies.forEach { remedy ->
                                    appendLine("• $remedy")
                                }
                                appendLine()
                                appendLine("— Sanatan Vedic Guide App")
                            }
                            val intent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                        }
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF7F2))
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = pullRefreshState,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    try {
                        val remoteContent = CardContentRepository.fetchCardContent(cardId)
                        if (remoteContent.existsInCloud) {
                            cardContent = remoteContent
                            errorMessage = null
                        } else {
                            cardContent = getDefaultCardContent(cardId, isHindi)
                            errorMessage = if (isHindi) "क्लाउड डेटाबेस पर कार्ड ID '$cardId' का कोई दस्तावेज़ नहीं मिला।" else "No corresponding document found in Firestore."
                        }
                    } catch (e: Exception) {
                        errorMessage = e.localizedMessage
                    } finally {
                        isRefreshing = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Loading Indicator state
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Terra)
                    }
                }
            }

            // Fallback / Empty State Notification Banner when document is missing in Firestore
            if (errorMessage != null && !isLoading) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.2.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "⚠️", fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "क्लाउड दस्तावेज़ अनुपलब्ध (Empty State)" else "Cloud Document Not Found",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = errorMessage!!,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF7F1D1D),
                                    lineHeight = 16.sp
                                )
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    val retryContent = CardContentRepository.fetchCardContent(cardId)
                                    if (retryContent.existsInCloud) {
                                        cardContent = retryContent
                                        errorMessage = null
                                    } else {
                                        errorMessage = if (isHindi) "पुनः प्रयास: अभी भी क्लाउड पर दस्तावेज़ नहीं मिला।" else "Retry: Document still not found on cloud."
                                    }
                                    isLoading = false
                                }
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry Cloud Fetch", tint = Color(0xFF991B1B))
                            }
                        }
                    }
                }
            }

            // Hero Card Section
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.2.dp, Saffron.copy(alpha = 0.3f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SaffronLight,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = displayContent.emoji, fontSize = 28.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = title,
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Ink,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = description,
                            fontSize = 13.5.sp,
                            color = InkSoft,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (displayContent.existsInCloud) Color(0xFFFEF3C7) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = if (displayContent.existsInCloud) "☁️ Cloud Synced ID: ${displayContent.cardId}" else "📌 Local Fallback ID: ${displayContent.cardId}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (displayContent.existsInCloud) Color(0xFFC24E1B) else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Remedies Section
            item {
                Text(
                    text = if (currentLangCode == "hi") "✨ वैदिक उपाय एवं मार्गदर्शन" else "✨ Vedic Remedies & Guidance",
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Ink
                )
            }

            items(displayContent.remedies) { remedy ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SaffronLight,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🕉️", fontSize = 12.sp)
                            }
                        }
                        Text(
                            text = remedy,
                            fontSize = 13.5.sp,
                            color = Ink,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Consult Verified Sadhak Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🕉️", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = if (currentLangCode == "hi") "पंडित जी से सीधा परामर्श" else "Direct Consultation",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF000000)
                                )
                                Text(
                                    text = if (currentLangCode == "hi") "व्यक्तिगत समाधान हेतु चैट या कॉल करें" else "Private chat or voice call guidance",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF737373)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val targetSadhak = sadhaks.firstOrNull() ?: SadhakItem(
                            id = "default_acharya",
                            nameHi = "आचार्य देव शर्मा",
                            nameEn = "Acharya Dev Sharma",
                            titleHi = "वरिष्ठ वैदिक ज्योतिष विशेषज्ञ",
                            titleEn = "Vedic Problem Specialist",
                            experienceHi = "18 वर्ष अनुभव",
                            experienceEn = "18 Years Exp",
                            rating = "4.9",
                            isOnline = true,
                            bio = "पारिवारिक शांति, आरोग्य, धन लाभ व दोष निवारण में सिद्धहस्त।",
                            phone = "",
                            initialHi = "आ",
                            initialEn = "AD"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF000000),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        submittedMessage = "पंडित जी से चैट प्रारंभ की जा रही है..."
                                        onStartChat(targetSadhak)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLangCode == "hi") "चैट (₹499)" else "Chat (₹499)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Saffron,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        submittedMessage = "पंडित जी से कॉल जोड़ी जा रही है..."
                                        onStartCall(targetSadhak)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLangCode == "hi") "कॉल करें" else "Call Now",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        if (submittedMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Sage, modifier = Modifier.size(16.dp))
                                Text(
                                    text = submittedMessage!!,
                                    color = Sage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Comments & Feedback Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "💬", fontSize = 20.sp)
                            Text(
                                text = if (currentLangCode == "hi") "साधक अनुभव एवं टिप्पणियाँ" else "User Comments & Feedback",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Ink
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Comment Input Field
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = {
                                Text(
                                    text = if (currentLangCode == "hi") "अपनी टिप्पणी या अनुभव यहाँ लिखें..." else "Write your feedback or comment here...",
                                    fontSize = 12.5.sp,
                                    color = InkFaint
                                )
                            },
                            singleLine = false,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Saffron,
                                unfocusedBorderColor = EditorialLineStrong,
                                focusedContainerColor = PaperCard,
                                unfocusedContainerColor = PaperCard
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    coroutineScope.launch {
                                        isPostingComment = true
                                        val success = CommentRepository.postComment(cardId, "साधक", newCommentText.trim())
                                        if (success) {
                                            newCommentText = ""
                                            commentsList = CommentRepository.fetchComments(cardId)
                                        }
                                        isPostingComment = false
                                    }
                                }
                            },
                            enabled = newCommentText.isNotBlank() && !isPostingComment,
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            if (isPostingComment) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = if (currentLangCode == "hi") "टिप्पणी भेजें" else "Post Comment",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderLight)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (commentsList.isEmpty()) {
                            Text(
                                text = if (currentLangCode == "hi") "अभी तक कोई टिप्पणी नहीं है। पहली टिप्पणी आप लिखें!" else "No comments yet. Be the first to share your feedback!",
                                fontSize = 12.sp,
                                color = InkSoft,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                commentsList.forEach { comment ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFAF7F2),
                                        border = BorderStroke(1.dp, Color(0xFFF1EDE6)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = comment.userName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    color = Ink
                                                )
                                                val dateStr = java.text.SimpleDateFormat("d MMM, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(comment.timestamp))
                                                Text(
                                                    text = dateStr,
                                                    fontSize = 10.5.sp,
                                                    color = InkFaint
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = comment.commentText,
                                                fontSize = 12.5.sp,
                                                color = InkSoft,
                                                lineHeight = 17.sp
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
}
}

private fun getDefaultCardContent(cardId: String, isHindi: Boolean): FirestoreCardContent {
    return when (cardId) {
        "family" -> FirestoreCardContent(
            cardId = cardId,
            titleHi = "पारिवारिक शांति एवं क्लेश निवारण",
            titleEn = "Family Peace & Harmony",
            emoji = "🏡",
            descriptionHi = "परिवार में आपसी मनमुटाव, गृह क्लेश, आपसी प्रेम की कमी और वास्तु दोष का वैदिक समाधान।",
            descriptionEn = "Vedic solutions for family disputes, lack of harmony, and domestic peace.",
            remedies = listOf(
                "प्रतिदिन घर के मुख्य द्वार पर शुद्ध घी का दीपक जलाएं।",
                "गुरुवार को पीले वस्त्र धारण कर बृहस्पति स्तोत्र का पाठ करें।",
                "परिवार के सभी सदस्य मिलकर सामूहिक रूप से संध्या आरती करें।"
            ),
            existsInCloud = false
        )
        "career" -> FirestoreCardContent(
            cardId = cardId,
            titleHi = "करियर, व्यापार एवं सफलता",
            titleEn = "Career & Business Success",
            emoji = "💼",
            descriptionHi = "नौकरी में उन्नति, व्यवसाय में वृद्धि, आर्थिक बाधाओं और साक्षात्कार में सफलता के उपाय।",
            descriptionEn = "Solutions for career advancement, business growth, and overcoming obstacles.",
            remedies = listOf(
                "प्रत्येक बुधवार को गणेश जी को दूर्वा व मोदक अर्पित करें।",
                "कार्यक्षेत्र में उत्तर दिशा की ओर मुख करके कार्य करें।",
                "शनिवार को पीपल के वृक्ष के नीचे सरसों के तेल का दीपक जलाएं।"
            ),
            existsInCloud = false
        )
        "marriage" -> FirestoreCardContent(
            cardId = cardId,
            titleHi = "विवाह, प्रेम एवं संबंध",
            titleEn = "Marriage & Relationships",
            emoji = "❤️",
            descriptionHi = "विवाह में आ रही बाधाएं, मांगलिक दोष, वैवाहिक जीवन में मधुरता और प्रेम संबंध के उपाय।",
            descriptionEn = "Remedies for marriage delays, manglik dosha, and relationship harmony.",
            remedies = listOf(
                "शीघ्र विवाह हेतु गुरुवार को केले के वृक्ष का पूजन करें।",
                "माता गौरी और भगवान शिव की नियमित पूजा अर्चना करें।",
                "शुक्रवार को जरूरतमंदों को सफेद वस्तु या चावल का दान करें।"
            ),
            existsInCloud = false
        )
        "health" -> FirestoreCardContent(
            cardId = cardId,
            titleHi = "स्वास्थ्य, आरोग्य एवं शांति",
            titleEn = "Health & Well-being",
            emoji = "🩺",
            descriptionHi = "दीर्घकालिक स्वास्थ्य समस्याएं, मानसिक तनाव, अनिद्रा और शारीरिक ऊर्जा में वृद्धि के उपाय।",
            descriptionEn = "Vedic health remedies for chronic issues, stress, and vitality.",
            remedies = listOf(
                "महामृत्युंजय मंत्र का 108 बार नियमित जप करें।",
                "प्रातःकाल सूर्योदय के समय तांबे के लोटे से सूर्य को अर्घ्य दें।",
                "सोमवार को शिवलिंग पर कच्चा दूध व बेलपत्र अर्पित करें।"
            ),
            existsInCloud = false
        )
        else -> FirestoreCardContent(
            cardId = cardId,
            titleHi = "वैदिक समाधान एवं मार्गदर्शन",
            titleEn = "Vedic Guidance & Remedies",
            emoji = "✨",
            descriptionHi = "आपके जीवन की विशिष्ट समस्याओं के लिए पराशर पद्धति एवं वैदिक ज्योतिष आधारित समाधान।",
            descriptionEn = "Personalized Vedic solutions based on your unique card selection.",
            remedies = listOf(
                "नित्य प्रातः अपने इष्टदेव का स्मरण कर दिन की शुरुआत करें।",
                "सात्विक आहार अपनाएं और नियमित ध्यान (Meditation) करें।",
                "किसी अनुभवी वैदिक आचार्य से व्यक्तिगत परामर्श लें।"
            ),
            existsInCloud = false
        )
    }
}
