package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Chat
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.models.SadhakItem
import com.example.utils.FirestoreProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions

/**
 * Polished Post-Consultation Rating and Review Dialog.
 * Allows users to rate spiritual sessions from 1 to 5 stars, select qualitative feedback tags,
 * provide optional written thoughts, and submits data to the 'consultation_history' collection.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostConsultationReviewDialog(
    sadhak: SadhakItem,
    consultationId: String = "",
    sessionType: String = "call", // "call" or "chat"
    durationSeconds: Int = 0,
    isHindi: Boolean = true,
    userId: String = "",
    userName: String = "",
    onDismiss: () -> Unit,
    onSubmitSuccess: () -> Unit = onDismiss
) {
    val context = LocalContext.current
    var selectedRating by remember { mutableIntStateOf(5) }
    var reviewFeedbackText by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    val selectedTags = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }

    val positiveTags = listOf(
        if (isHindi) "सटीक भविष्यवाणी" else "Accurate Predictions",
        if (isHindi) "सरल उपाय" else "Practical Remedies",
        if (isHindi) "धैर्यवान व विनम्र" else "Patient & Polite",
        if (isHindi) "आत्मिक शांति" else "Peaceful & Calming",
        if (isHindi) "गहन वैदिक ज्ञान" else "Deep Knowledge",
        if (isHindi) "संतुष्ट मार्गदर्शन" else "Clear Answers"
    )

    val constructiveTags = listOf(
        if (isHindi) "कम समय मिला" else "Felt Rushed",
        if (isHindi) "आवाज़ में व्यवधान" else "Audio/Network Lag",
        if (isHindi) "सामान्य उत्तर" else "Generic Guidance",
        if (isHindi) "उपाय अस्पष्ट" else "Unclear Remedies"
    )

    val currentTags = if (selectedRating >= 4) positiveTags else constructiveTags

    val ratingLabel = when (selectedRating) {
        5 -> if (isHindi) "⭐⭐⭐⭐⭐ अत्यंत लाभकारी व संतुष्ट" else "⭐⭐⭐⭐⭐ Outstanding & Enlightening"
        4 -> if (isHindi) "⭐⭐⭐⭐ बहुत अच्छा व स्पष्ट" else "⭐⭐⭐⭐ Very Good & Clear"
        3 -> if (isHindi) "⭐⭐⭐ अच्छा रहा" else "⭐⭐⭐ Good & Helpful"
        2 -> if (isHindi) "⭐⭐ सामान्य (औसत)" else "⭐⭐ Fair / Average"
        else -> if (isHindi) "⭐ सुधार की आवश्यकता" else "⭐ Needs Improvement"
    }

    val formattedDuration = if (durationSeconds > 0) {
        val m = durationSeconds / 60
        val s = durationSeconds % 60
        String.format(java.util.Locale.US, "%02d:%02d min", m, s)
    } else ""

    Dialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isSubmitting,
            dismissOnClickOutside = !isSubmitting,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("post_consultation_review_dialog"),
            color = Color.White,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isHindi) "परामर्श की समीक्षा दें" else "Rate Consultation",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isHindi) "आपका अनुभव हमारे लिए मूल्यवान है" else "Your feedback helps the spiritual community",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_review_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sadhak Info Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFBF8F2),
                    border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F5C5C))
                                .border(1.5.dp, Color(0xFFD4AF37), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isHindi) sadhak.initialHi.ifBlank { "सा" } else sadhak.initialEn.ifBlank { "S" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) sadhak.nameHi else sadhak.nameEn,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isHindi) sadhak.titleHi.ifBlank { "वैदिक ज्योतिषी" } else sadhak.titleEn.ifBlank { "Vedic Astrologer" },
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE6F4F1)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (sessionType.lowercase() == "chat") Icons.Outlined.Chat else Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF0F5C5C),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (formattedDuration.isNotBlank()) formattedDuration else sessionType.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F5C5C)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive 5-Star Rating
                Text(
                    text = if (isHindi) "आपका समग्र अनुभव कैसा रहा?" else "How was your overall session?",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("star_rating_selector"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        val isSelected = i <= selectedRating
                        IconButton(
                            onClick = {
                                selectedRating = i
                                selectedTags.clear()
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .testTag("star_rating_$i")
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "$i Star",
                                tint = if (isSelected) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                Text(
                    text = ratingLabel,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB45309),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tag Chips
                Text(
                    text = if (isHindi) "महत्वपूर्ण बिंदु चुनें (Tap to select):" else "Select highlights (optional):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentTags.forEach { tag ->
                        val isTagSelected = selectedTags.contains(tag)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isTagSelected) Color(0xFF0F5C5C) else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                1.dp,
                                if (isTagSelected) Color(0xFF0F5C5C) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .clickable {
                                    if (isTagSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                }
                                .testTag("feedback_tag_$tag")
                        ) {
                            Text(
                                text = (if (isTagSelected) "✓ " else "+ ") + tag,
                                fontSize = 11.5.sp,
                                fontWeight = if (isTagSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isTagSelected) Color.White else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Review Text Input
                OutlinedTextField(
                    value = reviewFeedbackText,
                    onValueChange = { if (it.length <= 500) reviewFeedbackText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_text_input"),
                    placeholder = {
                        Text(
                            text = if (isHindi) "अपने अनुभव या साधक द्वारा दिए गए मार्गदर्शन के बारे में लिखें (वैकल्पिक)..."
                            else "Write about your experience or how the session guided you (optional)...",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    maxLines = 4,
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    supportingText = {
                        Text(
                            text = "${reviewFeedbackText.length}/500",
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                )

                // Anonymous checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAnonymous = !isAnonymous }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0F5C5C)),
                        modifier = Modifier.testTag("anonymous_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (isHindi) "समीक्षा में नाम गुप्त रखें (Post anonymously)" else "Post review anonymously",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isHindi) "आपका नाम अन्य उपयोगकर्ताओं को नहीं दिखेगा" else "Your profile name won't be shown publicly",
                            fontSize = 10.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("skip_review_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        enabled = !isSubmitting
                    ) {
                        Text(
                            text = if (isHindi) "बाद में दें (Skip)" else "Skip for now",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            isSubmitting = true
                            val authUid = FirebaseAuth.getInstance().currentUser?.uid
                            val effectiveUid = authUid ?: userId.ifBlank { "devotee_user" }
                            val effectiveAuthor = if (isAnonymous) {
                                if (isHindi) "गुमनाम साधक" else "Anonymous Devotee"
                            } else {
                                userName.ifBlank { "Devotee" }
                            }

                            val payload = hashMapOf<String, Any>(
                                "userId" to effectiveUid,
                                "userName" to effectiveAuthor,
                                "providerId" to sadhak.id,
                                "sadhakName" to (if (isHindi) sadhak.nameHi else sadhak.nameEn),
                                "sadhakTitle" to (if (isHindi) sadhak.titleHi else sadhak.titleEn),
                                "rating" to selectedRating,
                                "feedback" to reviewFeedbackText.trim(),
                                "tags" to selectedTags.toList(),
                                "type" to sessionType,
                                "consultationId" to consultationId,
                                "isAnonymous" to isAnonymous,
                                "durationSeconds" to durationSeconds,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "status" to "COMPLETED"
                            )

                            val db = FirestoreProvider.get(context)
                            db.collection("consultation_history")
                                .add(payload)
                                .addOnSuccessListener {
                                    // Also update session/request doc if consultationId is present
                                    if (consultationId.isNotBlank()) {
                                        val updatePayload = mapOf(
                                            "rating" to selectedRating,
                                            "feedback" to reviewFeedbackText.trim(),
                                            "tags" to selectedTags.toList(),
                                            "reviewedAt" to FieldValue.serverTimestamp(),
                                            "hasReviewed" to true
                                        )
                                        db.collection("consultation_requests")
                                            .document(consultationId)
                                            .set(updatePayload, SetOptions.merge())
                                    }

                                    isSubmitting = false
                                    Toast.makeText(
                                        context,
                                        if (isHindi) "समीक्षा और रेटिंग दर्ज की गई • धन्यवाद! 🙏"
                                        else "Rating & review submitted successfully • Thank you! 🙏",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onSubmitSuccess()
                                }
                                .addOnFailureListener { err ->
                                    isSubmitting = false
                                    Toast.makeText(
                                        context,
                                        if (isHindi) "सहेजने में त्रुटि: ${err.localizedMessage}"
                                        else "Failed to submit: ${err.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onSubmitSuccess()
                                }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("submit_review_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5C5C)),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (isHindi) "समीक्षा जमा करें" else "Submit Review",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
