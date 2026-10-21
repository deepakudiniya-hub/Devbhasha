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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@IgnoreExtraProperties
data class SattvicChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String = "",
    val text: String = "",
    val isFromUser: Boolean = false,
    val isSpecialCard: Boolean = false,
    val cardTitle: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

/**
 * Sattvic Teal and Cream Spiritual Chat & Consultation Screen
 */
@Composable
fun SattvicChatScreen(
    mentorName: String = "आचार्य विवेक शास्त्री",
    isHindi: Boolean = true,
    walletBalance: Double = 250.0,
    onBackClick: () -> Unit = {},
    onAudioCallClick: () -> Unit = {},
    onVideoCallClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    var inputMessage by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                SattvicChatMessage(
                    id = "msg_1",
                    senderName = mentorName,
                    text = "प्रणाम! आपकी जन्म कुंडली और ग्रहों की स्थिति के अनुसार, करियर में आ रही बाधा शीघ्र ही दूर होगी। क्या आप इसके उपाय जानना चाहते हैं?",
                    isFromUser = false
                ),
                SattvicChatMessage(
                    id = "card_1",
                    senderName = mentorName,
                    text = "शुक्ल पक्ष में किए गए अनुष्ठान और जप से करियर एवं व्यापार में अवांछित सफलता और मानसिक शांति प्राप्त होती है।",
                    isSpecialCard = true,
                    cardTitle = "विशेष उपाय: शुक्ल पक्ष एवं नवग्रह शांति"
                ),
                SattvicChatMessage(
                    id = "msg_2",
                    senderName = "आप (Seeker)",
                    text = "गुरुजी, प्रणाम! कृपया मुझे करियर में स्थिरता के लिए कोई सरल सात्विक उपाय बताएं।",
                    isFromUser = true
                )
            )
        )
    }

    fun sendMessage() {
        val trimmed = inputMessage.trim()
        if (trimmed.isBlank()) return

        val userMsg = SattvicChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = "आप",
            text = trimmed,
            isFromUser = true
        )
        messages = messages + userMsg
        inputMessage = ""
        focusManager.clearFocus()

        coroutineScope.launch {
            delay(1200)
            val replyMsg = SattvicChatMessage(
                id = "msg_${System.currentTimeMillis() + 1}",
                senderName = mentorName,
                text = if (isHindi) "निश्चिंत रहें, देव कृपा से सब मंगल होगा। प्रतिदिन सूर्योदय के समय गायत्री मंत्र का 1 ाला जाप करें और सात्विक आहार अपनाएं।"
                else "Be assured, with Divine grace all obstacles will clear. Recite Gayatri Mantra 1 mala daily at sunrise.",
                isFromUser = false
            )
            messages = messages + replyMsg
        }
    }

    Scaffold(
        topBar = {
            // 1. Top App Bar - Deep Teal background (Color(0xFF083A35))
            Surface(
                color = Color(0xFF083A35),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Circular mentor/AI avatar with subtle golden ring
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F5C5C))
                            .border(1.5.dp, Color(0xFFF6C644), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "आ",
                            color = Color(0xFFF6C644),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mentorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF5CC28A))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(0.5.dp, Color.White, CircleShape)
                                )
                            }
                            Text(
                                text = "ऑनलाइन",
                                fontSize = 11.5.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    // Right: Direct audio/video call action icon and menu icon in white
                    IconButton(onClick = onAudioCallClick) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = "Audio Call",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onVideoCallClick) {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = "Video Call",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = {
                        Toast.makeText(context, "Wallet Balance: ₹${walletBalance.toInt()}", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // 4. Bottom Input Bar - Clean white elevated container at the bottom with 16.dp padding
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left: Attachment (+) icon
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "जन्म कुंडली और विवरण संलग्न किए गए", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Attach",
                            tint = Color(0xFF526E6B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Voice Mic Icon
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "सात्विक वॉइस रिकॉर्डिंग शुरू...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "Voice Mic",
                            tint = Color(0xFF526E6B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

// English comment placeholder
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF8FAFC)),
                        placeholder = {
                            Text(
                                text = "अपना संदेश लिखें...",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0F5C5C),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )

                    // Right: A circular Deep Teal send button (Color(0xFF0F5C5C), size 44.dp) with a white paper-plane / arrow icon
                    IconButton(
                        onClick = { sendMessage() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F5C5C))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // 2. Chat Body & Message Bubbles (Cream Background #F9FBF9)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
// English comment placeholder
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE2E8F0).copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "आज, 14 अक्टूबर • सात्विक परामर्श सत्र",
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    if (msg.isSpecialCard) {
                        // 3. Special Astrological Card Component (Inside Chat)
                        // Highlighted soft golden-yellow card (Background Color(0xFFFDF8E2), border Color(0xFFF5C443), 16.dp rounded corners)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFDF8E2),
                            border = BorderStroke(1.2.dp, Color(0xFFF5C443)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFF5C443).copy(alpha = 0.3f),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🪔", fontSize = 14.sp)
                                        }
                                    }
                                    Text(
                                        text = msg.cardTitle ?: "विशेष उपाय",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF78350F)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = msg.text,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF451A03),
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    } else {
                        // Normal Incoming or Outgoing Message Bubble
                        val isUser = msg.isFromUser
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isUser) Color(0xFF0F5C5C) else Color.White,
                                border = if (isUser) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = 1.dp,
                                modifier = Modifier.widthIn(max = 300.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 14.sp,
                                        color = if (isUser) Color.White else Color(0xFF083A35),
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = msg.formattedTime,
                                            fontSize = 9.5.sp,
                                            color = if (isUser) Color.White.copy(alpha = 0.75f) else Color(0xFF94A3B8)
                                        )
                                        if (isUser) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.DoneAll,
                                                contentDescription = "Read",
                                                tint = Color(0xFF93C5FD),
                                                modifier = Modifier.size(13.dp)
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
