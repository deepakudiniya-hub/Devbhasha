package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.SadhakItem
import com.example.ui.components.SessionStatusBar
import com.example.ui.components.rememberSessionController
import com.example.ui.components.sessionErrorText
import com.example.utils.PriceLabels
import com.example.utils.SessionBillingException
import com.example.utils.SessionType
import com.example.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val isFromUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

@Composable
fun ChatConsultationScreen(
    sadhaks: List<SadhakItem>,
    isHindi: Boolean,
    userId: String,
    userName: String,
    walletBalance: Double,
    onLowBalance: () -> Unit,
    onAddBalance: () -> Unit,
    onStartCall: (SadhakItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeChatSadhak by remember { mutableStateOf<SadhakItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }

    if (activeChatSadhak != null) {
        LiveChatRoomScreen(
            sadhak = activeChatSadhak!!,
            isHindi = isHindi,
            userId = userId,
            userName = userName,
            walletBalance = walletBalance,
            onCloseChat = { activeChatSadhak = null },
            onStartCall = { onStartCall(activeChatSadhak!!) },
            onLowBalance = onLowBalance,
            onAddBalance = onAddBalance
        )
    } else {
        ChatAstrologerListScreen(
            sadhaks = sadhaks,
            isHindi = isHindi,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            walletBalance = walletBalance,
            onStartChat = { sadhak ->
                // Balance & price are checked by the server in startSession.
                activeChatSadhak = sadhak
            },
            onStartCall = onStartCall,
            modifier = modifier
        )
    }
}

@Composable
private fun ChatAstrologerListScreen(
    sadhaks: List<SadhakItem>,
    isHindi: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    walletBalance: Double,
    onStartChat: (SadhakItem) -> Unit,
    onStartCall: (SadhakItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "all" to (if (isHindi) "सभी साधक" else "All Astrologers"),
        "online" to (if (isHindi) "🟢 ऑनलाइन" else "🟢 Online"),
        "kundli" to (if (isHindi) "कुंडली विशेषज्ञ" else "Kundli"),
        "marriage" to (if (isHindi) "विवाह व प्रेम" else "Love & Marriage"),
        "career" to (if (isHindi) "कैरियर व व्यापार" else "Career & Business"),
        "vastu" to (if (isHindi) "वास्तु शास्त्र" else "Vastu")
    )

    val filteredList = remember(sadhaks, searchQuery, selectedCategory) {
        sadhaks.filter { sadhak ->
            val matchesQuery = searchQuery.isBlank() ||
                sadhak.nameHi.contains(searchQuery, ignoreCase = true) ||
                sadhak.nameEn.contains(searchQuery, ignoreCase = true) ||
                sadhak.titleHi.contains(searchQuery, ignoreCase = true) ||
                sadhak.titleEn.contains(searchQuery, ignoreCase = true)

            val matchesCat = when (selectedCategory) {
                "online" -> sadhak.isOnline
                "kundli" -> sadhak.titleHi.contains("कुंडली", true) || sadhak.titleHi.contains("ज्योतिष", true)
                "marriage" -> sadhak.titleHi.contains("विवाह", true) || sadhak.bio.contains("विवाह", true) || sadhak.titleHi.contains("ज्योतिष", true)
                "career" -> sadhak.titleHi.contains("कैरियर", true) || sadhak.titleHi.contains("व्यापार", true) || sadhak.titleHi.contains("ज्योतिष", true)
                "vastu" -> sadhak.titleHi.contains("वास्तु", true)
                else -> true
            }

            matchesQuery && matchesCat
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Header
        Surface(
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "💬",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "साधक चैट परामर्श" else "Chat with Astrologers",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Text(
                            text = if (isHindi) "सर्वश्रेष्ठ सत्यापित ज्योतिषी से लाइव चैट करें • ${PriceLabels.SESSION}"
                            else "Chat live with verified Vedic experts • ${PriceLabels.SESSION}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Online live count badge
                    val onlineLiveCount = sadhaks.count { it.isOnline }
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isHindi) "$onlineLiveCount लाइव" else "$onlineLiveCount Live",
                                color = Color(0xFF15803D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                    placeholder = {
                        Text(
                            text = if (isHindi) "ज्योतिषी का नाम या विशेषता खोजें..." else "Search by name or skills...",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = SaffronPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(categories) { (key, label) ->
                        val isSelected = selectedCategory == key
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) SaffronPrimary else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) SaffronDeep else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onCategorySelected(key) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Astrologer Chat Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList, key = { it.id }) { sadhak ->
                ChatAstrologerCard(
                    sadhak = sadhak,
                    isHindi = isHindi,
                    onStartChat = { onStartChat(sadhak) },
                    onStartCall = { onStartCall(sadhak) }
                )
            }
        }
    }
}

@Composable
fun ChatAstrologerCard(
    sadhak: SadhakItem,
    isHindi: Boolean,
    onStartChat: () -> Unit,
    onStartCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with online pulse
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0D656C), Color(0xFF0D656C))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isHindi) sadhak.initialHi else sadhak.initialEn,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (sadhak.isOnline) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isHindi) sadhak.nameHi else sadhak.nameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF000000)
                        )
                        Surface(
                            color = Color(0xFFFAFAFA),
                            border = BorderStroke(1.dp, BorderLight),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "⭐ ${sadhak.rating}",
                                color = Saffron,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isHindi) sadhak.titleHi else sadhak.titleEn,
                        color = SaffronDeep,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "💼 ${if (isHindi) sadhak.experienceHi else sadhak.experienceEn}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• 💬 हिन्दी, Eng",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹499",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = SaffronDeep
                        )
                        Text(
                            text = if (isHindi) " / 20 मिनट" else " / 20 min",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Text(
                        text = if (isHindi) "🟢 तुरंत चैट उपलब्ध" else "🟢 Available Now",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF16A34A)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Call Shortcut button
                    IconButton(
                        onClick = onStartCall,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDCFCE7))
                            .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = "Call",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Main Start Chat Button
                    Button(
                        onClick = onStartChat,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "चैट करें" else "Chat Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-screen Interactive Live Chat Conversation Room with Astrologer
 */
@Composable
fun LiveChatRoomScreen(
    sadhak: SadhakItem,
    isHindi: Boolean,
    userId: String,
    userName: String,
    walletBalance: Double,
    onCloseChat: () -> Unit,
    onStartCall: () -> Unit,
    onLowBalance: () -> Unit,
    onAddBalance: () -> Unit,
    sessionType: SessionType = SessionType.SESSION
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Server-billed session: price, duration and debit are decided by Cloud Functions.
    val sessionController = rememberSessionController(
        type = sessionType,
        providerId = sadhak.id,
        onExpired = {
            Toast.makeText(
                context,
                if (isHindi) "सत्र का समय समाप्त हो गया" else "Session time is over",
                Toast.LENGTH_LONG
            ).show()
            onCloseChat()
        }
    )

    LaunchedEffect(sessionController.error, sessionController.session) {
        val err = sessionController.error
        if (err != null && sessionController.session == null) {
            Toast.makeText(context, sessionErrorText(err, isHindi), Toast.LENGTH_LONG).show()
            if (err is SessionBillingException && err.isInsufficientBalance) onLowBalance()
            onCloseChat()
        }
    }

    var inputMessage by remember { mutableStateOf("") }
    var isAstrologerTyping by remember { mutableStateOf(false) }

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    val listState = rememberLazyListState()
    val db = remember { com.example.utils.FirestoreProvider.get(context) }
    val chatRef = db.collection("chats")
        .document(sadhak.id)
        .collection("messages")

    // Fetch messages from Firestore
    LaunchedEffect(sadhak.id) {
        chatRef.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    messages = snapshot.toObjects(ChatMessage::class.java)
                }
            }
    }

    // Scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = listOf(
        if (isHindi) "मेरी कुंडली में सरकारी नौकरी का योग कब है? 💼" else "Government Job prospects in my Kundli? 💼",
        if (isHindi) "विवाह में आ रही बाधा व सटीक समय बताएं 💍" else "Marriage timing & remedies 💍",
        if (isHindi) "व्यापार में वृद्धि हेतु सात्विक उपाय बताएं ✨" else "Remedies for business growth ✨",
        if (isHindi) "स्वास्थ्य एवं मानसिक शांति हेतु मंत्र जप 🌿" else "Mantras for health and peace of mind 🌿",
        if (isHindi) "आज का शुभ मुहूर्त व राहुकाल का प्रभाव 🪔" else "Today's auspicious muhurat & guidance 🪔"
    )

    fun sendMessage(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isBlank()) return
        if (!sessionController.isActive) {
            Toast.makeText(
                context,
                if (isHindi) "सत्र सक्रिय नहीं है" else "Session is not active",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val userMsg = ChatMessage(
            senderId = userId.ifBlank { "user" },
            senderName = userName,
            text = trimmed,
            isFromUser = true
        )
        
        chatRef.add(userMsg)
        inputMessage = ""
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCloseChat) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }

                    // Astrologer Mini Avatar with Online Dot
                    Box {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SaffronPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isHindi) sadhak.initialHi else sadhak.initialEn,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .align(Alignment.BottomEnd)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) sadhak.nameHi else sadhak.nameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isAstrologerTyping) {
                                if (isHindi) "✍️ टाइप कर रहे हैं..." else "✍️ typing..."
                            } else {
                                if (isHindi) "🟢 सक्रिय चैट • ${PriceLabels.forType(sessionType)}" else "🟢 Active • ${PriceLabels.forType(sessionType)}"
                            },
                            fontSize = 11.sp,
                            color = if (isAstrologerTyping) SaffronDeep else Color(0xFF16A34A),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Call Button in Chat Header
                    IconButton(
                        onClick = onStartCall,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = "Switch to Call",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Add Balance Button
                    IconButton(
                        onClick = onAddBalance,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF000000))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Balance",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // End Chat Button
                    TextButton(
                        onClick = {
                            sessionController.end()
                            onCloseChat()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (isHindi) "समाप्त" else "End",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Quick Questions Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        items(quickQuestions) { q ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFAFAFA),
                                border = BorderStroke(1.dp, BorderLight),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { sendMessage(q) }
                            ) {
                                Text(
                                    text = q,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF000000),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Input Field Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Kundli / Attachment Button
                        IconButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    if (isHindi) "आपकी जन्म विवरण और कुंडली साधक के साथ साझा कर दी गई है।" else "Kundli & Birth Details shared with Astrologer.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                sendMessage(if (isHindi) "📜 [मेरी जन्म विवरण व कुंडली साझा की गई]" else "📜 [Birth Chart & Kundli shared]")
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AttachFile,
                                contentDescription = "Attach Kundli",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFFF8FAFC)),
                            placeholder = {
                                Text(
                                    text = if (isHindi) "अपना प्रश्न या जन्म विवरण लिखें..." else "Type your question...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { sendMessage(inputMessage) }),
                            maxLines = 3,
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaffronPrimary,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Button
                        IconButton(
                            onClick = { sendMessage(inputMessage) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (inputMessage.isNotBlank()) SaffronPrimary else Color(0xFFE2E8F0))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputMessage.isNotBlank()) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            // Vedic Trust Header Banner
            Surface(
                color = Color(0xFFFAFAFA),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isHindi) "🔒 100% गोपनीय व सात्विक परामर्श • प्रत्यक्ष वैदिक गणना आधारित"
                        else "🔒 100% Confidential & Authentic Vedic Guidance",
                        fontSize = 11.sp,
                        color = Color(0xFF737373),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            SessionStatusBar(
                controller = sessionController,
                isHindi = isHindi,
                darkBackground = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                onExtendFailed = { e ->
                    Toast.makeText(context, sessionErrorText(e, isHindi), Toast.LENGTH_LONG).show()
                    if (e is SessionBillingException && e.isInsufficientBalance) onLowBalance()
                }
            )

            // Message Bubble List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        isHindi = isHindi
                    )
                }

                if (isAstrologerTyping) {
                    item {
                        TypingIndicatorItem(sadhakName = if (isHindi) sadhak.nameHi else sadhak.nameEn)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    isHindi: Boolean
) {
    val isUser = message.isFromUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) SaffronPrimary else Color.White,
            border = if (isUser) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!isUser) {
                    Text(
                        text = "🪔 ${message.senderName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDeep
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 13.5.sp,
                    color = if (isUser) Color.White else Color(0xFF1E293B),
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.formattedTime,
                    fontSize = 9.5.sp,
                    color = if (isUser) Color.White.copy(alpha = 0.75f) else Color(0xFF94A3B8),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TypingIndicatorItem(sadhakName: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$sadhakName विचार कर रहे हैं... 📿",
                fontSize = 11.5.sp,
                color = SaffronDeep,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun buildSadhakPrompt(
    userQuery: String,
    sadhak: SadhakItem,
    userName: String,
    isHindi: Boolean
): String {
    val persona = if (isHindi) {
        """
        आप ${sadhak.nameHi} हैं — देव (Devbhasha) ऐप के अनुभवी ज्योतिषी साधक।
        आप $userName जी नाम के साधक की मदद कर रहे हैं।
        नियम:
        1. उत्तर सदा सरल हिंदी या Hinglish में दें — भाषा प्रश्न की भाषा से मिलाएं।
        2. उत्तर 90 शब्दों से छोटा रखें (3-4 वाक्य)।
        3. शुरुआत "$userName जी" को सादर प्रणाम के साथ करें।
        4. ज्योतिषीय दृष्टि से संतुलित मार्गदर्शन दें — निर्णायक डरावनी भविष्यवाणी नहीं।
        5. उपयुक्त हो तो एक सरल उपाय (मंत्र / दान / व्रत) सुझाएं।
        6. गंभीर स्वास्थ्य, कानूनी या मानसिक समस्या में योग्य विशेषज्ञ से मिलने की सलाह दें।
        """.trimIndent()
    } else {
        """
        You are ${sadhak.nameEn}, an experienced Vedic astrologer on the Devbhasha app.
        You are guiding a seeker named $userName.
        Rules:
        1. Keep the reply under 90 words (3-4 sentences).
        2. Greet $userName respectfully first.
        3. Give balanced astrological guidance, never fear-driven deterministic predictions.
        4. Suggest one simple remedy (mantra / charity / fast) when appropriate.
        5. For serious health, legal or mental issues, advise consulting a qualified professional.
        """.trimIndent()
    }
    return "$persona\n\nSadhak ka prashn: $userQuery"
}

private fun generateAstrologerReply(
    query: String,
    sadhak: SadhakItem,
    userName: String,
    isHindi: Boolean
): String {
    val q = query.lowercase()
    return if (isHindi) {
        when {
            q.contains("नौकरी") || q.contains("व्यापार") || q.contains("job") || q.contains("career") -> {
                "$userName जी, आपकी कुंडली में दशम भाव का स्वामी बृहस्पति अनुकूल स्थिति में आ रहा है। आगामी 3 से 6 महीनों में कार्यक्षेत्र में पदोन्नति अथवा नए व्यापारिक अनुबंध के प्रबल योग बन रहे हैं। प्रतिदिन सूर्य देव को तांबे के पात्र से ॐ घृणि सूर्याय नमः मंत्र बोलते हुए अर्घ्य दें।"
            }
            q.contains("विवाह") || q.contains("marriage") || q.contains("प्रेम") || q.contains("शादी") -> {
                "$userName जी, सप्तम भाव में शुक्र और गुरु का प्रभाव होने से अगले वर्ष के पूर्वार्ध में शुभ विवाह संबंध निश्चित होने के उत्तम योग हैं। गुरुवार के दिन पीले वस्त्र धारण करें तथा भगवान लक्ष्मीनारायण को बेसन के लड्डू अर्पित करें।"
            }
            q.contains("स्वास्थ्य") || q.contains("शांति") || q.contains("health") || q.contains("peace") -> {
                "मानसिक शांति व उत्तम स्वास्थ्य हेतु चंद्रमा को बलवान करना श्रेयस्कर रहेगा। प्रतिदिन प्रातः 'ॐ नमः शिवाय' की एक माला का जाप करें और पूर्णिमा को चांदी के पात्र से जल अर्पण करें। सभी विघ्न दूर होंगे।"
            }
            q.contains("कुंडली") || q.contains("kundli") || q.contains("दोष") -> {
                "आपकी जन्मपत्री का सूक्ष्म अध्ययन करने पर वर्तमान में महादशा का शुभ फल दृष्टिगोचर हो रहा है। कोई विशेष अनिष्टकारी दोष नहीं है। केवल शनि की ढैय्या/साढ़े साती के हल्के प्रभाव के लिए शनिवार को पीपल के वृक्ष के नीचे सरसों के तेल का दीपक प्रज्वलित करें।"
            }
            else -> {
                "$userName जी, आपके प्रश्न का ज्योतिषीय व वैदिक दृष्टि से समाधान यह है कि समय आपके अनुकूल बन रहा है। धैर्य पूर्वक अपने कर्म पर ध्यान केंद्रित रखें। नित्य 'गायत्री मंत्र' का 11 बार जप आपके आत्मबल और सौभाग्य में वृद्धि करेगा। शुभम भवतु!"
            }
        }
    } else {
        when {
            q.contains("job") || q.contains("career") || q.contains("business") -> {
                "Dear $userName, your 10th house lord Jupiter is entering a favorable transit. Career advancement and favorable opportunities are strongly indicated in the next 3 to 6 months. Offer water to Surya Dev every morning."
            }
            q.contains("marriage") || q.contains("love") || q.contains("relationship") -> {
                "Dear $userName, auspicious combinations are forming for marriage and relationships in the coming months. Offering yellow flowers and fasting on Thursdays will accelerate positive results."
            }
            else -> {
                "Dear $userName, through Vedic astrological calculations, your planetary alignment is turning favorable. Maintain daily spiritual discipline with Gayatri Mantra chants. May divine blessings be upon you!"
            }
        }
    }
}
