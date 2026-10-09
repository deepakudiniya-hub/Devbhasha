package com.example.ui.screens

import com.example.utils.AgoraVoiceManager
import com.example.utils.SessionBilling
import com.example.utils.SessionType
import com.example.utils.PriceLabels
import com.example.ui.components.rememberSessionController
import com.example.ui.components.SessionStatusBar
import com.example.ui.components.sessionErrorText
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.SadhakItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CallConsultationScreen(
    sadhaks: List<SadhakItem>,
    isHindi: Boolean,
    userId: String,
    userName: String,
    walletBalance: Double,
    onLowBalance: () -> Unit,
    onStartChat: (SadhakItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeCallingSadhak by remember { mutableStateOf<SadhakItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }

    if (activeCallingSadhak != null) {
        LiveAudioCallScreen(
            sadhak = activeCallingSadhak!!,
            isHindi = isHindi,
            userId = userId,
            userName = userName,
            walletBalance = walletBalance,
            onEndCall = { activeCallingSadhak = null }
        )
    } else {
        CallAstrologerListScreen(
            sadhaks = sadhaks,
            isHindi = isHindi,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it },
            walletBalance = walletBalance,
            onStartCall = { sadhak ->
                // Balance & price are checked by the server in startSession.
                activeCallingSadhak = sadhak
            },
            onStartChat = onStartChat,
            modifier = modifier
        )
    }
}

@Composable
private fun CallAstrologerListScreen(
    sadhaks: List<SadhakItem>,
    isHindi: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    walletBalance: Double,
    onStartCall: (SadhakItem) -> Unit,
    onStartChat: (SadhakItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "all" to (if (isHindi) "सभी विशेषज्ञ" else "All Astrologers"),
        "online" to (if (isHindi) "🟢 तुरंत कॉल उपलब्ध" else "🟢 Ready to Call"),
        "top" to (if (isHindi) "⭐ शीर्ष रेटिंग (4.9+)" else "⭐ Top Rated"),
        "vedic" to (if (isHindi) "वैदिक ज्योतिषी" else "Vedic Experts"),
        "tarot" to (if (isHindi) "टैरो व हस्तरेखा" else "Tarot & Palmistry")
    )

    val filteredList = remember(sadhaks, searchQuery, selectedFilter) {
        sadhaks.filter { sadhak ->
            val matchesQuery = searchQuery.isBlank() ||
                sadhak.nameHi.contains(searchQuery, ignoreCase = true) ||
                sadhak.nameEn.contains(searchQuery, ignoreCase = true) ||
                sadhak.titleHi.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "online" -> sadhak.isOnline
                "top" -> (sadhak.rating.toDoubleOrNull() ?: 0.0) >= 4.8
                "vedic" -> sadhak.titleHi.contains("वैदिक", true) || sadhak.titleHi.contains("ज्योतिष", true)
                "tarot" -> sadhak.titleHi.contains("टैरो", true) || sadhak.titleHi.contains("हस्तरेखा", true) || sadhak.bio.contains("हस्तरेखा", true)
                else -> true
            }

            matchesQuery && matchesFilter
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
                                text = "📞",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "साधक वॉइस कॉल परामर्श" else "Call Astrologers",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Text(
                            text = if (isHindi) "सीधे फ़ोन पर बात करें • ${PriceLabels.SESSION}"
                            else "Direct phone consultation • ${PriceLabels.SESSION}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Online Available Count Badge
                    val onlineAvailableCount = sadhaks.count { it.isOnline }
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
                                text = if (isHindi) "$onlineAvailableCount उपलब्ध" else "$onlineAvailableCount Available",
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
                            text = if (isHindi) "ज्योतिषी का नाम या अनुभव खोजें..." else "Search astrologers...",
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

                // Filters LazyRow
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(filters) { (key, label) ->
                        val isSelected = selectedFilter == key
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF15803D) else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF166534) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onFilterSelected(key) }
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

        // Astrologer Call Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList, key = { it.id }) { sadhak ->
                CallAstrologerCard(
                    sadhak = sadhak,
                    isHindi = isHindi,
                    onStartCall = { onStartCall(sadhak) },
                    onStartChat = { onStartChat(sadhak) }
                )
            }
        }
    }
}

@Composable
fun CallAstrologerCard(
    sadhak: SadhakItem,
    isHindi: Boolean,
    onStartCall: () -> Unit,
    onStartChat: () -> Unit,
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
                // Avatar with green call status badge
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF16A34A), Color(0xFF0D9488))
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
                            color = Color(0xFF1E293B)
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
                        color = Color(0xFF15803D),
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
                            text = "• ⏱️ ${if (isHindi) "प्रतीक्षा 0 मिनट" else "0 min wait"}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Medium
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
                            color = Color(0xFF15803D)
                        )
                        Text(
                            text = if (isHindi) " / 20 मिनट" else " / 20 min",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Text(
                        text = if (isHindi) "📞 सीधी वॉइस कॉल" else "📞 Direct Audio Call",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF15803D)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Chat Shortcut button
                    IconButton(
                        onClick = onStartChat,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFAFAFA))
                            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = "Chat",
                            tint = Saffron,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Main Start Call Button
                    Button(
                        onClick = onStartCall,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "कॉल लगाएं" else "Call Now",
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
 * Full-screen Interactive Audio Call Screen with Ringing, Live Timer, Mute, Speaker, and End Call
 */
@Composable
fun LiveAudioCallScreen(
    sadhak: SadhakItem,
    isHindi: Boolean,
    userId: String,
    userName: String,
    walletBalance: Double,
    onEndCall: () -> Unit,
    sessionType: SessionType = SessionType.SESSION
) {
    val context = LocalContext.current

    var isCallConnected by remember { mutableStateOf(false) }
    var callDurationSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var callError by remember { mutableStateOf<String?>(null) }

    // Server-billed session: price, duration and debit are decided by Cloud Functions.
    val sessionController = rememberSessionController(
        type = sessionType,
        providerId = sadhak.id,
        onExpired = {
            AgoraVoiceManager.leave()
            showReviewDialog = true
        }
    )

    // Once the server has started the session, fetch an RTC token and join.
    LaunchedEffect(sessionController.sessionId) {
        val sessionId = sessionController.sessionId
        if (sessionId.isBlank()) return@LaunchedEffect
        SessionBilling.getAgoraToken(sessionId)
            .onSuccess { info ->
                val ready = AgoraVoiceManager.initEngine(context, info.appId)
                val joined = ready && AgoraVoiceManager.join(info.channel, info.token, info.uid)
                if (!joined) {
                    callError = if (isHindi) "कॉल कनेक्ट नहीं हो सकी" else "Could not connect the call"
                }
            }
            .onFailure {
                callError = if (isHindi) "कॉल टोकन प्राप्त नहीं हुआ" else "Could not get call token"
            }
    }

    // Session failed to start (e.g. low balance): leave the screen.
    LaunchedEffect(sessionController.error, sessionController.session) {
        val err = sessionController.error
        if (err != null && sessionController.session == null) {
            Toast.makeText(context, sessionErrorText(err, isHindi), Toast.LENGTH_LONG).show()
            onEndCall()
        }
    }

    LaunchedEffect(callError) {
        callError?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    LaunchedEffect(Unit) {
        AgoraVoiceManager.onRemoteJoined = { isCallConnected = true }
        AgoraVoiceManager.onRemoteLeft = { showReviewDialog = true }
    }

    DisposableEffect(Unit) {
        onDispose {
            AgoraVoiceManager.leave()
            AgoraVoiceManager.onRemoteJoined = null
            AgoraVoiceManager.onRemoteLeft = null
        }
    }

    // Call Duration Timer (display only; billing is server-side)
    LaunchedEffect(isCallConnected) {
        if (isCallConnected) {
            while (true) {
                delay(1000)
                callDurationSeconds++
            }
        }
    }

    val formattedDuration = remember(callDurationSeconds) {
        val minutes = callDurationSeconds / 60
        val seconds = callDurationSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = {
                showReviewDialog = false
                onEndCall()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🕉️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "परामर्श संपन्न हुआ" else "Consultation Completed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isHindi)
                            "साधक ${sadhak.nameHi} के साथ आपकी $formattedDuration की वार्ता सफल रही।"
                        else
                            "Your consultation of $formattedDuration with ${sadhak.nameEn} completed.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF334155),
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isHindi) "रेटिंग दें: ⭐⭐⭐⭐⭐" else "Rating: ⭐⭐⭐⭐⭐",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDeep
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReviewDialog = false
                        onEndCall()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text(if (isHindi) "धन्यवाद व संपन्न" else "Done")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0F172A) // Rich Deep Vedic Navy
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isCallConnected) Color(0xFF10B981) else Saffron)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCallConnected) {
                                if (isHindi) "सक्रिय ऑडियो परामर्श • $formattedDuration" else "Active Call • $formattedDuration"
                            } else {
                                if (isHindi) "पंडित जी से संपर्क हो रहा है..." else "Connecting call..."
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔒 100% End-to-End Encrypted Vedic Audio",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(10.dp))

                SessionStatusBar(
                    controller = sessionController,
                    isHindi = isHindi,
                    darkBackground = true,
                    onExtendFailed = { e ->
                        Toast.makeText(context, sessionErrorText(e, isHindi), Toast.LENGTH_LONG).show()
                    }
                )
            }

            // Center Astrologer Avatar & Aura
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Outer Glowing Ring
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Saffron.copy(alpha = 0.35f),
                                    Saffron.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Middle Ring
                    Box(
                        modifier = Modifier
                            .size(118.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(3.dp, Saffron, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isHindi) sadhak.initialHi else sadhak.initialEn,
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isHindi) sadhak.nameHi else sadhak.nameEn,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isHindi) sadhak.titleHi else sadhak.titleEn,
                    fontSize = 13.5.sp,
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "🎙️ सात्विक वैदिक परामर्श चालू है" else "🎙️ Sacred Vedic Session Live",
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            // Bottom In-Call Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                AgoraVoiceManager.setMuted(isMuted)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) Color(0xFFEF4444) else Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                                contentDescription = "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isMuted) (if (isHindi) "म्यूट" else "Muted") else (if (isHindi) "माइक" else "Mic"),
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }

                    // End Call Big Red Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                AgoraVoiceManager.leave()
                                sessionController.end()
                                showReviewDialog = true
                            },
                            modifier = Modifier
                                .size(68.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isHindi) "समाप्त करें" else "End Call",
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Speaker Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                isSpeakerOn = !isSpeakerOn
                                AgoraVoiceManager.setSpeaker(isSpeakerOn)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) SaffronPrimary else Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Speaker",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSpeakerOn) (if (isHindi) "स्पीकर ON" else "Speaker ON") else (if (isHindi) "स्पीकर" else "Speaker"),
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
