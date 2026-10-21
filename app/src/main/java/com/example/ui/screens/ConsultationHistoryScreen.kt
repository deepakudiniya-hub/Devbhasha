package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PostConsultationReviewDialog
import com.example.ui.models.SadhakItem
import com.example.utils.DateTimeUtils
import com.example.utils.FirestoreProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

data class ConsultationRecord(
    val id: String = "",
    val providerId: String = "",
    val sadhakName: String = "",
    val sadhakTitle: String = "",
    val type: String = "call",
    val status: String = "COMPLETED",
    val rating: Int = 0,
    val feedback: String = "",
    val tags: List<String> = emptyList(),
    val durationSeconds: Int = 0,
    val createdAt: com.google.firebase.Timestamp? = null
)

@Composable
fun ConsultationHistoryScreen(
    userId: String,
    onBackClick: () -> Unit,
    sadhaks: List<SadhakItem> = emptyList(),
    isHindi: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { FirestoreProvider.get(context) }
    var historyList by remember { mutableStateOf<List<ConsultationRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabFilter by remember { mutableStateOf("all") }

    // Dialog state for reviewing past unrated sessions
    var sessionToReview by remember { mutableStateOf<ConsultationRecord?>(null) }

    fun refreshHistory() {
        val authUid = FirebaseAuth.getInstance().currentUser?.uid
        val effectiveUid = authUid ?: userId.ifBlank { "devotee_user" }

        isLoading = true
        // Fetch from consultation_history (which contains submitted ratings)
        db.collection("consultation_history")
            .whereEqualTo("userId", effectiveUid)
            .get()
            .addOnSuccessListener { historySnap ->
                val historyDocs = historySnap.documents.mapNotNull { doc ->
                    val id = doc.getString("consultationId") ?: doc.id
                    val providerId = doc.getString("providerId") ?: "sadhak"
                    val sadhakName = doc.getString("sadhakName") ?: "Vedic Sadhak"
                    val sadhakTitle = doc.getString("sadhakTitle") ?: "Vedic Astrologer"
                    val type = doc.getString("type") ?: "call"
                    val status = doc.getString("status") ?: "COMPLETED"
                    val rating = doc.getLong("rating")?.toInt() ?: 0
                    val feedback = doc.getString("feedback") ?: ""
                    @Suppress("UNCHECKED_CAST")
                    val tags = (doc.get("tags") as? List<String>) ?: emptyList()
                    val duration = doc.getLong("durationSeconds")?.toInt() ?: 0
                    val createdAt = doc.getTimestamp("createdAt")
                    ConsultationRecord(id, providerId, sadhakName, sadhakTitle, type, status, rating, feedback, tags, duration, createdAt)
                }

                // Also fetch from consultation_requests to catch any sessions not yet reviewed
                db.collection("consultation_requests")
                    .whereEqualTo("userId", effectiveUid)
                    .get()
                    .addOnSuccessListener { reqSnap ->
                        val reqDocs = reqSnap.documents.mapNotNull { doc ->
                            val id = doc.getString("requestId") ?: doc.id
                            // If this id is already in historyDocs, skip
                            if (historyDocs.any { it.id == id }) return@mapNotNull null

                            val providerId = doc.getString("providerId") ?: "sadhak"
                            val matchedSadhak = sadhaks.firstOrNull { it.id == providerId }
                            val sadhakName = matchedSadhak?.let { if (isHindi) it.nameHi else it.nameEn } ?: "Vedic Sadhak"
                            val sadhakTitle = matchedSadhak?.let { if (isHindi) it.titleHi else it.titleEn } ?: "Vedic Guide"
                            val type = doc.getString("type") ?: "call"
                            val status = doc.getString("status") ?: "COMPLETED"
                            val rating = doc.getLong("rating")?.toInt() ?: 0
                            val feedback = doc.getString("feedback") ?: ""
                            @Suppress("UNCHECKED_CAST")
                            val tags = (doc.get("tags") as? List<String>) ?: emptyList()
                            val duration = doc.getLong("durationSeconds")?.toInt() ?: 0
                            val createdAt = doc.getTimestamp("createdAt")
                            ConsultationRecord(id, providerId, sadhakName, sadhakTitle, type, status, rating, feedback, tags, duration, createdAt)
                        }

                        val merged = (historyDocs + reqDocs).sortedByDescending { it.createdAt?.seconds ?: 0L }
                        historyList = merged
                        isLoading = false
                    }
                    .addOnFailureListener {
                        historyList = historyDocs.sortedByDescending { it.createdAt?.seconds ?: 0L }
                        isLoading = false
                    }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    LaunchedEffect(userId) {
        refreshHistory()
    }

    val filteredList = remember(historyList, selectedTabFilter) {
        when (selectedTabFilter) {
            "calls" -> historyList.filter { it.type.lowercase() == "call" }
            "chats" -> historyList.filter { it.type.lowercase() == "chat" }
            "unrated" -> historyList.filter { it.rating == 0 }
            "rated" -> historyList.filter { it.rating > 0 }
            else -> historyList
        }
    }

    val totalCount = historyList.size
    val ratedCount = historyList.count { it.rating > 0 }
    val avgRating = if (ratedCount > 0) {
        String.format(Locale.US, "%.1f", historyList.filter { it.rating > 0 }.map { it.rating }.average())
    } else "5.0"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFBF8F2),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onBackClick() }
                            .testTag("consultation_history_back_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isHindi) "परामर्श इतिहास व समीक्षाएं" else "Consultation History & Reviews",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.size(38.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE8DFC8))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F5C5C)
                        )
                        Text(
                            text = if (isHindi) "कुल परामर्श" else "Total Sessions",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$ratedCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = if (isHindi) "समीक्षा दर्ज" else "Reviews Given",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = avgRating,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (isHindi) "औसत रेटिंग" else "Average Rating",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Filter Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "all" to (if (isHindi) "सभी ($totalCount)" else "All ($totalCount)"),
                    "calls" to (if (isHindi) "कॉल (Calls)" else "Calls"),
                    "chats" to (if (isHindi) "चैट (Chats)" else "Chats"),
                    "unrated" to (if (isHindi) "समीक्षा शेष (${totalCount - ratedCount})" else "Pending Review (${totalCount - ratedCount})"),
                    "rated" to (if (isHindi) "समीक्षित ($ratedCount)" else "Reviewed ($ratedCount)")
                )

                items(filters) { (key, label) ->
                    val isSelected = selectedTabFilter == key
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0F5C5C) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F5C5C) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .clickable { selectedTabFilter = key }
                            .testTag("history_filter_$key")
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main List
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF0F5C5C)
                    )
                } else if (filteredList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isHindi) "कोई परामर्श रिकॉर्ड नहीं मिला" else "No consultation records found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHindi)
                                "जब आप साधक से कॉल या चैट पर बात करेंगे, उसका विवरण व समीक्षा यहाँ दिखेगी।"
                            else
                                "Your calls and chat consultations with spiritual mentors will appear here.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(filteredList) { record ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("history_record_${record.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFE6F4F1),
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (record.type.lowercase() == "chat") Icons.Outlined.Chat else Icons.Outlined.Phone,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0F5C5C),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = record.sadhakName.ifBlank { "Sadhak #${record.providerId.take(6)}" },
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = record.sadhakTitle.ifBlank { record.type.uppercase() },
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = record.type.uppercase(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F5C5C),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color(0xFFF8FAFC))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Rating & Review Section
                                    if (record.rating > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                for (i in 1..5) {
                                                    Icon(
                                                        imageVector = if (i <= record.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                                        contentDescription = null,
                                                        tint = if (i <= record.rating) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${record.rating}.0 / 5",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFB45309)
                                                )
                                            }

                                            if (record.createdAt != null) {
                                                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                                                Text(
                                                    text = sdf.format(record.createdAt.toDate()),
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            }
                                        }

                                        if (record.tags.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                record.tags.take(3).forEach { tag ->
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFFEF3C7)
                                                    ) {
                                                        Text(
                                                            text = "#$tag",
                                                            fontSize = 10.5.sp,
                                                            color = Color(0xFF92400E),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (record.feedback.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF8FAFC),
                                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "\"${record.feedback}\"",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF475569),
                                                    modifier = Modifier.padding(10.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        // Unrated session: Call to action button to rate and review!
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isHindi) "सत्र की समीक्षा अभी बाकी है" else "Session unrated",
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )

                                            Button(
                                                onClick = { sessionToReview = record },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5C5C)),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.testTag("rate_session_btn_${record.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFDE68A),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isHindi) "रेटिंग दें" else "Rate & Review",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
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

    // Modal review dialog when user clicks "Rate & Review" on an unrated session
    if (sessionToReview != null) {
        val rec = sessionToReview!!
        val matchedSadhak = sadhaks.firstOrNull { it.id == rec.providerId } ?: SadhakItem(
            id = rec.providerId,
            nameHi = rec.sadhakName.ifBlank { "साधक" },
            nameEn = rec.sadhakName.ifBlank { "Vedic Sadhak" },
            titleHi = rec.sadhakTitle.ifBlank { "वैदिक ज्योतिषी" },
            titleEn = rec.sadhakTitle.ifBlank { "Vedic Astrologer" }
        )

        PostConsultationReviewDialog(
            sadhak = matchedSadhak,
            consultationId = rec.id,
            sessionType = rec.type,
            durationSeconds = rec.durationSeconds,
            isHindi = isHindi,
            userId = userId,
            userName = "",
            onDismiss = { sessionToReview = null },
            onSubmitSuccess = {
                sessionToReview = null
                refreshHistory()
            }
        )
    }
}
