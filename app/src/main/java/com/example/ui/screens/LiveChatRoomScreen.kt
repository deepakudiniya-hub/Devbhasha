package com.example.ui.screens
import com.example.utils.CommunityModerationManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.SadhakItem
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SaffronContainer
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Calendar
import java.util.UUID

/**
 * Live Chat Room Screen enforcing strict 365-day TTL security rules,
 * server-validated user identity authentication, and active session liveness checks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveChatRoomScreen(
    sadhak: SadhakItem,
    sessionId: String,
    isHindi: Boolean = false,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val firestore = com.example.utils.FirestoreProvider.get()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val authenticatedUserId = currentUser?.uid ?: ""

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputMessage by remember { mutableStateOf("") }
    var isSessionActive by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    var blockedUserIds by remember { mutableStateOf(setOf<String>()) }
    LaunchedEffect(authenticatedUserId) {
        if (authenticatedUserId.isNotBlank()) {
            CommunityModerationManager.observeBlockedUsers(authenticatedUserId).collect {
                blockedUserIds = it
            }
        }
    }

    var reportTargetMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var selectedReason by remember { mutableStateOf("Spam") }

    // Monitor session liveness status
    DisposableEffect(sessionId) {
        val listener = firestore.collection("sessions")
            .document(sessionId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status") ?: "active"
                    isSessionActive = status.lowercase() in listOf("pending", "active", "live", "connected")
                } else {
                    isSessionActive = false
                }
            }
        onDispose { listener.remove() }
    }

    // Observe live messages
    DisposableEffect(sessionId) {
        val registration = firestore.collection("chats")
            .document(sessionId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val text = doc.getString("text") ?: ""
                        val senderId = doc.getString("senderId") ?: ""
                        val senderName = doc.getString("senderName") ?: "Devotee"
                        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val isFromUser = doc.getBoolean("isFromUser") ?: (senderId == authenticatedUserId)
                        ChatMessage(
                            id = doc.id,
                            senderId = senderId,
                            senderName = senderName,
                            text = text,
                            isFromUser = isFromUser,
                            timestamp = timestamp
                        )
                    }
                    messages = list
                }
            }
        onDispose { registration.remove() }
    }

    var pendingCrisisMessage by remember { mutableStateOf<String?>(null) }

    fun executeSend(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isBlank()) return

        // Rule 4: Block stale session writes if session is inactive or ended
        if (!isSessionActive) {
            Toast.makeText(
                context,
                if (isHindi) "सत्र समाप्त या निष्क्रिय हो चुका है" else "Session is inactive or ended. Cannot send message.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Rule 3: Enforce identity verification via FirebaseAuth
        val verifiedSenderId = FirebaseAuth.getInstance().currentUser?.uid
        if (verifiedSenderId.isNullOrBlank()) {
            Toast.makeText(context, "Authentication required. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        val contactCheck = com.example.utils.SafetyAndCrisisManager.sanitizeContactSharing(trimmed)
        if (contactCheck.hasContactInfo) {
            Toast.makeText(context, contactCheck.warningMessage, Toast.LENGTH_LONG).show()
        }
        val finalText = contactCheck.sanitizedText

        val msgId = UUID.randomUUID().toString()
        val executionInstantMillis = System.currentTimeMillis()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = executionInstantMillis
            add(Calendar.DAY_OF_YEAR, 365)
        }
        val expireAtDate = calendar.time
        val expireAtTimestamp = Timestamp(expireAtDate)

        val isFromUserDynamic = (verifiedSenderId == authenticatedUserId)

        val messagePayload = hashMapOf<String, Any>(
            "id" to msgId,
            "senderId" to verifiedSenderId,
            "senderName" to (currentUser?.displayName ?: "Devotee"),
            "text" to finalText,
            "isFromUser" to isFromUserDynamic,
            "fromUser" to isFromUserDynamic,
            "timestamp" to executionInstantMillis,
            "expireAt" to expireAtTimestamp
        )

        // Rule 4: Wrap Firestore message write in a transaction hook that verifies
        // the parent chat session is currently live and connected before executing the document write.
        val sessionRef = firestore.collection("sessions").document(sessionId)
        val chatDocRef = firestore.collection("chats").document(sessionId)
        val messageRef = chatDocRef.collection("messages").document(msgId)

        firestore.runTransaction { transaction ->
            val sessionSnap = transaction.get(sessionRef)
            if (sessionSnap.exists()) {
                val sessionStatus = sessionSnap.getString("status")?.lowercase()
                val isConnected = sessionSnap.getBoolean("connected") ?: true
                val isEnded = (sessionStatus == "ended" || sessionStatus == "completed" || sessionStatus == "expired" || !isConnected)
                if (isEnded) {
                    throw FirebaseFirestoreException(
                        "Parent chat session has ended or is disconnected",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }
            }
            transaction.set(messageRef, messagePayload)
        }.addOnSuccessListener {
            inputMessage = ""
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendMessage(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isBlank()) return
        if (com.example.utils.SafetyAndCrisisManager.containsSelfHarmRisk(trimmed)) {
            pendingCrisisMessage = trimmed
            return
        }
        executeSend(trimmed)
    }


    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) sadhak.nameHi else sadhak.nameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isSessionActive) (if (isHindi) "● लाइव सत्र सक्रिय" else "● Live Session Connected")
                            else (if (isHindi) "○ सत्र समाप्त" else "○ Session Ended"),
                            fontSize = 11.sp,
                            color = if (isSessionActive) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1E293B)
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text(if (isHindi) "संदेश लिखें..." else "Type message...") },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        singleLine = true
                    )
                    IconButton(
                        onClick = { sendMessage(inputMessage) },
                        modifier = Modifier
                            .size(52.dp)
                            .background(SaffronPrimary, RoundedCornerShape(26.dp))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White
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
                .background(Color(0xFFFAF7F2))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages.filter { it.senderId !in blockedUserIds }) { msg ->
                    val alignment = if (msg.isFromUser) Alignment.End else Alignment.Start
                    val bubbleColor = if (msg.isFromUser) SaffronContainer else Color.White
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = alignment
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = bubbleColor,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )
                                if (!msg.isFromUser && msg.senderId.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Report",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFDC2626),
                                            modifier = Modifier.clickable {
                                                selectedReason = "Spam"
                                                reportTargetMessage = msg
                                            }
                                        )
                                        Text(
                                            text = "Block",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.clickable {
                                                CommunityModerationManager.blockUser(context, msg.senderId)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (reportTargetMessage != null) {
                val target = reportTargetMessage!!
                AlertDialog(
                    onDismissRequest = { reportTargetMessage = null },
                    title = { Text("Report Message", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Select a reason for reporting:", fontSize = 13.sp, color = Color(0xFF475569))
                            CommunityModerationManager.REPORT_REASONS.forEach { reason ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedReason = reason }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedReason == reason,
                                        onClick = { selectedReason = reason }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(reason, fontSize = 14.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                CommunityModerationManager.reportContent(
                                    context = context,
                                    targetType = "message",
                                    targetId = target.id,
                                    ownerId = target.senderId,
                                    reason = selectedReason
                                )
                                reportTargetMessage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                        ) {
                            Text("Report")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { reportTargetMessage = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

    if (pendingCrisisMessage != null) {
        val msg = pendingCrisisMessage!!
        AlertDialog(
            onDismissRequest = { pendingCrisisMessage = null },
            title = { Text("You Matter", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SaffronPrimary) },
            text = {
                Text(
                    text = "You matter. If you are in danger or distress, call Tele-MANAS 14416 or 1800-891-4416 (free, 24x7).",
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        com.example.utils.SafetyAndCrisisManager.callTeleManas(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("Call Tele-MANAS")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingCrisisMessage = null
                        executeSend(msg)
                    }
                ) {
                    Text("Send anyway")
                }
            }
        )
    }

}
