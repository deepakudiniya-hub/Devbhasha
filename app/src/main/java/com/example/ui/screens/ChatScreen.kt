package com.example.ui.screens

import com.example.utils.DateTimeUtils


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SaffronPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Theme Colors
val SaffronDeep = Color(0xFFE65100)
val CreamBackground = Color(0xFFFFFFFF)
val IncomingCardBg = Color(0xFFFFFFFF)
val OutgoingBubbleBg = SaffronPrimary
val RemedyCardBg = Color(0xFFFFF3E0)
val BorderColor = Color(0xFFE2E8F0)

enum class MessageStatus {
    SENT, DELIVERED, READ
}

data class ChatScreenMessage(
    val text: String,
    val isIncoming: Boolean,
    val timestamp: Long,
    val isRemedy: Boolean = false,
    val status: MessageStatus = MessageStatus.READ
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onBackClick: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val messages = remember {
        mutableStateListOf(
            ChatScreenMessage("प्रणाम! आज आपकी जिज्ञासा किस विषय पर है?", true, DateTimeUtils.getCurrentTimestamp()),
            ChatScreenMessage("गुरुवार के दिन पीली वस्तुओं का दान करें और विष्णु सहस्रनाम का पाठ करें। इससे मानसिक शांति और आर्थिक उन्नति होगी।", true, DateTimeUtils.getCurrentTimestamp(), true),
            ChatScreenMessage("गुरु जी, मेरी कुंडली में धन योग के बारे में बताएं।", false, DateTimeUtils.getCurrentTimestamp(), status = MessageStatus.READ)
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(2.dp, SaffronPrimary, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("आचार्य जी (Acharya Ji)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isTyping) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(SaffronPrimary, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Typing...", color = SaffronPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color.Green, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Online", color = Color(0xFFB4D8D3), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Audio Call */ }) {
                            Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = Color.White)
                        }
                        IconButton(onClick = { /* Video Call */ }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                        }
                        IconButton(onClick = { /* More Menu */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SaffronPrimary)
                )

                // Permanent slim disclaimer banner
                Surface(
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI dwara banaye gaye jawab. Galat ho sakte hain. · AI-generated responses. May be inaccurate.",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                // Quick Suggestion Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            val msgIndex = messages.size
                            messages.add(ChatScreenMessage("आज का राशिफल", false, DateTimeUtils.getCurrentTimestamp(), status = MessageStatus.SENT))
                            isTyping = true
                            coroutineScope.launch {
                                delay(600)
                                if (msgIndex < messages.size) {
                                    messages[msgIndex] = messages[msgIndex].copy(status = MessageStatus.DELIVERED)
                                }
                                delay(800)
                                if (msgIndex < messages.size) {
                                    messages[msgIndex] = messages[msgIndex].copy(status = MessageStatus.READ)
                                }
                                delay(800)
                                isTyping = false
                                messages.add(ChatScreenMessage("आज मेष राशि वालों के लिए चंद्रमा पंचम भाव में है। शुभ समाचार मिलने के योग हैं।", true, DateTimeUtils.getCurrentTimestamp()))
                            }
                        },
                        label = { Text("आज का राशिफल") }
                    )
                    SuggestionChip(
                        onClick = {
                            val msgIndex = messages.size
                            messages.add(ChatScreenMessage("गृह शांति उपाय", false, DateTimeUtils.getCurrentTimestamp(), status = MessageStatus.SENT))
                            isTyping = true
                            coroutineScope.launch {
                                delay(600)
                                if (msgIndex < messages.size) {
                                    messages[msgIndex] = messages[msgIndex].copy(status = MessageStatus.DELIVERED)
                                }
                                delay(800)
                                if (msgIndex < messages.size) {
                                    messages[msgIndex] = messages[msgIndex].copy(status = MessageStatus.READ)
                                }
                                delay(800)
                                isTyping = false
                                messages.add(ChatScreenMessage("प्रतिदिन शाम को मुख्य द्वार पर घी का दीपक जलाएं और कपूर की आरती करें।", true, DateTimeUtils.getCurrentTimestamp(), true))
                            }
                        },
                        label = { Text("गृह शांति उपाय") }
                    )
                    SuggestionChip(
                        onClick = {
                            val msgIndex = messages.size
                            messages.add(ChatScreenMessage("कुंडली मिलान", false, DateTimeUtils.getCurrentTimestamp(), status = MessageStatus.SENT))
                            isTyping = true
                            coroutineScope.launch {
                                delay(600)
                                if (msgIndex < messages.size) {
                                    messages[msgIndex] = messages[msgIndex].copy(status = MessageStatus.DELIVERED)
                                }
                                delay(800)
                                if (msgIndex < messages.size) {
                                    messages[msgIndex] = messages[msgIndex].copy(status = MessageStatus.READ)
                                }
                                delay(800)
                                isTyping = false
                                messages.add(ChatScreenMessage("कुंडली मिलान में 36 गुणों में से 28 गुण मिले हैं जो कि अति उत्तम श्रेणी में आता है।", true, DateTimeUtils.getCurrentTimestamp()))
                            }
                        },
                        label = { Text("कुंडली मिलान") }
                    )
                }

                // Input Bar Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Add, contentDescription = "Attachment", tint = SaffronPrimary)
                    }
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("अपना प्रश्न यहाँ लिखें...") },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = SaffronPrimary)
                    }
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                val userQuery = messageText
                                val msgIndex = messages.size
                                messages.add(ChatScreenMessage(userQuery, false, DateTimeUtils.getCurrentTimestamp(), status = MessageStatus.SENT))
                                messageText = ""
                                isTyping = true
                                coroutineScope.launch {
                                    delay(700)
                                    if (msgIndex < messages.size) {
                                        messages[msgIndex] = messages[msgIndex].copy(status = MessageStatus.DELIVERED)
                                    }
                                    delay(900)
                                    if (msgIndex < messages.size) {
                                        messages[msgIndex] = messages[msgIndex].copy(status = MessageStatus.READ)
                                    }
                                    delay(900)
                                    isTyping = false
                                    messages.add(ChatScreenMessage("तथास्तु! आपके प्रश्न '$userQuery' के संदर्भ में, ग्रह अनुकूल हैं। सकारात्मक रहें।", true, DateTimeUtils.getCurrentTimestamp()))
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(SaffronPrimary, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    // Date Separator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE2E8F0)
                        ) {
                            Text(
                                text = "आज, 14 अक्टूबर",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                items(messages) { msg ->
                    if (msg.isRemedy) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = RemedyCardBg,
                            border = BorderStroke(1.dp, Color(0xFFF5C443)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "विशेष ज्योतिषीय उपाय (Astrological Remedy)",
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronPrimary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = msg.text,
                                    color = Color.DarkGray,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(DateTimeUtils.formatTimestamp(msg.timestamp), color = Color.Gray, fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
                            }
                        }
                    } else if (msg.isIncoming) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = IncomingCardBg,
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(msg.text, color = SaffronPrimary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(DateTimeUtils.formatTimestamp(msg.timestamp), color = Color.Gray, fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = OutgoingBubbleBg,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(msg.text, color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.align(Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(DateTimeUtils.formatTimestamp(msg.timestamp), color = Color(0xFFB4D8D3), fontSize = 10.sp)
                                        when (msg.status) {
                                            MessageStatus.SENT -> {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Sent",
                                                    tint = Color(0xFFB4D8D3),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            MessageStatus.DELIVERED -> {
                                                Icon(
                                                    imageVector = Icons.Default.DoneAll,
                                                    contentDescription = "Delivered",
                                                    tint = Color(0xFFB4D8D3),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            MessageStatus.READ -> {
                                                Icon(
                                                    imageVector = Icons.Default.DoneAll,
                                                    contentDescription = "Read",
                                                    tint = SaffronPrimary,
                                                    modifier = Modifier.size(14.dp)
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
