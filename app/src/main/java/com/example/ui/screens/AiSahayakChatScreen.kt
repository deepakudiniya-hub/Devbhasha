package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AiChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSahayakChatScreen(
    onBackClick: () -> Unit,
    isHindi: Boolean,
    language: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val welcomeMessage = if (isHindi || language.lowercase() in listOf("hi", "hindi")) {
        "नमस्ते! मैं आपका AI सहायक (AI Sahayak) हूँ। आप मुझसे वेदों, ज्योतिष, सपनों या किसी भी आध्यात्मिक विषय पर प्रश्न पूछ सकते हैं। मैं आपकी कैसे सहायता कर सकता हूँ?"
    } else {
        "Namaste! I am your AI Sahayak. You can ask me about Vedic astrology, dreams, or any spiritual guidance. How can I help you today?"
    }

    val messages = remember {
        mutableStateListOf(
            AiChatMessage(role = "assistant", content = welcomeMessage)
        )
    }

    val currentLangCode = when {
        language.lowercase() in listOf("hgl", "hinglish") -> "hgl"
        language.lowercase() in listOf("hi", "hindi") || isHindi -> "hi"
        else -> "en"
    }

    fun sendMessage() {
        val text = inputMessage.trim()
        if (text.isBlank() || isLoading) return

        messages.add(AiChatMessage(role = "user", content = text))
        inputMessage = ""
        focusManager.clearFocus()
        isLoading = true

        coroutineScope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        val functions = FirebaseFunctions.getInstance()
        val payloadMessages = messages.map { mapOf("role" to it.role, "content" to it.content) }
        val data = hashMapOf("messages" to payloadMessages)

        functions.getHttpsCallable("aiAgent")
            .call(data)
            .addOnSuccessListener { result ->
                isLoading = false
                val resMap = result.data as? Map<*, *>
                val reply = resMap?.get("reply") as? String ?: "नमस्ते! मैं आपकी सेवा में हूँ।"
                messages.add(AiChatMessage(role = "assistant", content = reply))
                coroutineScope.launch {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                val errText = e.localizedMessage ?: "Connection error"
                messages.add(AiChatMessage(role = "assistant", content = "क्षमा करें, उत्तर प्राप्त करने में असफलता हुई: $errText"))
                Toast.makeText(context, "Error: $errText", Toast.LENGTH_SHORT).show()
                coroutineScope.launch {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PaperBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = Saffron.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✨", fontSize = 16.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "AI सहायक · AI Sahayak",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Ink
                            )
                            Text(
                                text = "वैदिक एवं आध्यात्मिक मार्गदर्शक",
                                fontSize = 11.sp,
                                color = InkSoft
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperBg)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = {
                            Text(
                                text = when (currentLangCode) {
                                    "hi" -> "यहाँ प्रश्न या सपना लिखें..."
                                    "hgl" -> "Yahan message type karein..."
                                    else -> "Ask a question or share dream..."
                                },
                                fontSize = 13.5.sp,
                                color = InkFaint
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 120.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Terra,
                            unfocusedBorderColor = EditorialLineStrong,
                            focusedContainerColor = PaperCard,
                            unfocusedContainerColor = PaperCard
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() })
                    )

                    FloatingActionButton(
                        onClick = { sendMessage() },
                        containerColor = Terra,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GoldStampSoft,
                        border = BorderStroke(1.dp, Saffron.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🪔", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "AI Sadhak मार्गदर्शक",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Ink
                                )
                                Text(
                                    text = "आप Sadhakों की सूची देखने, उनकी उपलब्धता जाँचने या चैट अनुरोध भेजने के लिए भी कह सकते हैं।",
                                    fontSize = 11.5.sp,
                                    color = InkSoft
                                )
                            }
                        }
                    }
                }

                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = if (isUser) Terra else Color.White,
                            border = if (isUser) null else BorderStroke(1.dp, EditorialLine),
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = msg.content,
                                    color = if (isUser) Color.White else Ink,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.formattedTime,
                                    color = if (isUser) Color.White.copy(alpha = 0.7f) else InkFaint,
                                    fontSize = 9.5.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, EditorialLine),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Terra
                                    )
                                    Text(
                                        text = "AI सोच रहा है...",
                                        fontSize = 12.sp,
                                        color = InkSoft,
                                        fontWeight = FontWeight.Medium
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
