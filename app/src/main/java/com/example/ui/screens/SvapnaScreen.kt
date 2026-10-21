package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.models.DreamJournalEntry
import com.example.ui.theme.*
import com.example.utils.DreamSubmitter
import com.example.utils.DreamSubmitResult
import com.example.utils.PriceLabels
import com.example.utils.FirestoreProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


/**
 * Devbhasha Svapna (Dream Journal) Tab Screen
 * Shared top app bar is hoisted in HomeScreen, no back button on root tab.
 */
@Composable
fun SvapnaScreen(
    userId: String = "",
    userName: String = "Sadhak",
    onChatWithSadhak: () -> Unit = {},
    dreamHistory: List<DreamJournalEntry> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var dreamInput by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("🌿 शुभ (Shubh)") }
    var isSaving by remember { mutableStateOf(false) }

    val tags = listOf("🌿 शुभ (Shubh)", "⚠️ चेतावनी (Chetavani)", "🕊️ पूर्वज (Ancestral)", "💫 सामान्य (Samanya)")

    var savedDreams by remember { mutableStateOf(dreamHistory) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            try {
                val db = FirestoreProvider.get(context)
                val snapshot = db.collection("users").document(userId).collection("dreams")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                savedDreams = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        DreamJournalEntry(
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            datePhase = data["datePhase"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            tag = data["tag"] as? String ?: "",
                            timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis(),
                            meaning = data["meaning"] as? String,
                            answeredBy = data["answeredBy"] as? String
                        )
                    } else null
                }
            } catch (e: Exception) {
                // Fallback to dreamHistory if error or empty
                if (savedDreams.isEmpty()) {
                    savedDreams = dreamHistory
                }
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = DevSpacing.space16),
        contentPadding = PaddingValues(top = DevSpacing.space12, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(DevSpacing.space12)
    ) {
        // Tab Header: Title & Subtitle (No back arrow!)
        item {
            Column {
                Text(
                    text = stringResource(R.string.svapna_screen_title),
                    fontFamily = TiroDevanagariHindi,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.svapna_screen_subtitle),
                    fontFamily = NotoSansDevanagari,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dream Input Card (20dp radius, 1dp outline, no shadow)
        item {
            Surface(
                shape = DevbhashaCardShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(DevSpacing.space16)) {
                    Text(
                        text = "नया स्वप्न दर्ज करें",
                        fontFamily = TiroDevanagariHindi,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(DevSpacing.space8))

                    OutlinedTextField(
                        value = dreamInput,
                        onValueChange = { dreamInput = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.svapna_input_hint),
                                fontFamily = NotoSansDevanagari,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        shape = DevbhashaTextFieldShape,
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(DevSpacing.space8))

                    // Tags Selector
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(DevSpacing.space8),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(tags) { tag ->
                            val isSelected = selectedTag == tag
                            Surface(
                                shape = DevbhashaChipShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .clip(DevbhashaChipShape)
                                    .clickable { selectedTag = tag }
                            ) {
                                Text(
                                    text = tag,
                                    fontFamily = NotoSansDevanagari,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(DevSpacing.space12))

                    // Primary Button: Teal filled, 52dp tall
                    Button(
                        onClick = {
                            if (dreamInput.isBlank()) {
                                Toast.makeText(context, "कृपया सपना विस्तार से लिखें", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSaving = true
                            coroutineScope.launch {
                                val result = DreamSubmitter.submitDream(
                                    userName = userName,
                                    dreamText = dreamInput
                                )
                                isSaving = false
                                when (result) {
                                    is DreamSubmitResult.Success -> {
                                        Toast.makeText(context, "सपना सफलतापूर्वक दर्ज हुआ ✓", Toast.LENGTH_SHORT).show()
                                        savedDreams = listOf(
                                            DreamJournalEntry(
                                                id = "dream_${System.currentTimeMillis()}",
                                                title = dreamInput.take(28) + "...",
                                                datePhase = "आज",
                                                description = dreamInput,
                                                tag = selectedTag,
                                                timestamp = System.currentTimeMillis(),
                                                meaning = "Sadhak द्वारा आपके स्वप्न का विश्लेषण किया जा रहा है...",
                                                answeredBy = result.sadhakName
                                            )
                                        ) + savedDreams
                                        dreamInput = ""
                                    }
                                    is DreamSubmitResult.Error -> {
                                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                    }
                                    is DreamSubmitResult.NoVerifiedSadhak -> {
                                        Toast.makeText(context, "वर्तमान में कोई Sadhak उपलब्ध नहीं हैं", Toast.LENGTH_SHORT).show()
                                    }
                                    is DreamSubmitResult.InsufficientBalance -> {
                                        Toast.makeText(context, "पर्याप्त वॉलेट शेष नहीं है", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = !isSaving,
                        shape = DevbhashaButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DevSpacing.buttonHeight)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(DevSpacing.space8))
                                Text(
                                    text = "${stringResource(R.string.svapna_submit_button)} · ${PriceLabels.DREAM_CHAT}",
                                    fontFamily = NotoSansDevanagari,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Saved Dreams Section Heading
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.svapna_history_title),
                    fontFamily = TiroDevanagariHindi,
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    shape = DevbhashaChipShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${savedDreams.size} सपने",
                        fontFamily = NotoSansDevanagari,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Saved Dreams List or Empty State
        if (savedDreams.isEmpty()) {
            item {
                Surface(
                    shape = DevbhashaCardShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(DevSpacing.space24),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🌙", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(DevSpacing.space8))
                        Text(
                            text = stringResource(R.string.svapna_empty_title),
                            fontFamily = NotoSansDevanagari,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(DevSpacing.space4))
                        Text(
                            text = stringResource(R.string.svapna_empty_desc),
                            fontFamily = NotoSansDevanagari,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(savedDreams) { entry ->
                Surface(
                    shape = DevbhashaCardShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(DevSpacing.space16)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = DevbhashaChipShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = entry.tag.ifBlank { "🌿 शुभ" },
                                    fontFamily = NotoSansDevanagari,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = entry.datePhase.ifBlank { "आज" },
                                fontFamily = NotoSansDevanagari,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(DevSpacing.space8))

                        Text(
                            text = entry.title,
                            fontFamily = TiroDevanagariHindi,
                            fontSize = 16.5.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(DevSpacing.space4))

                        Text(
                            text = entry.description,
                            fontFamily = NotoSansDevanagari,
                            fontSize = 13.5.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!entry.meaning.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(DevSpacing.space12))
                            Surface(
                                shape = DevbhashaButtonShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(DevSpacing.space12)) {
                                    Text(
                                        text = "वैदिक अर्थ:",
                                        fontFamily = NotoSansDevanagari,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(DevSpacing.space2))
                                    Text(
                                        text = entry.meaning,
                                        fontFamily = NotoSansDevanagari,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!entry.answeredBy.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(DevSpacing.space4))
                                        Text(
                                            text = "— ${entry.answeredBy}",
                                            fontFamily = NotoSansDevanagari,
                                            fontSize = 11.5.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(DevSpacing.space12))

                        // Actions Row: Ask Sadhak button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onChatWithSadhak,
                                shape = DevbhashaButtonShape,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.secondary
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(DevSpacing.space4))
                                Text(
                                    text = "Sadhak से पूछें",
                                    fontFamily = NotoSansDevanagari,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
