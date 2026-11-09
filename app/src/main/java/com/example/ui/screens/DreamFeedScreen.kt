package com.example.ui.screens
import com.example.utils.CommunityModerationManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.models.DreamCommentItem
import com.example.ui.models.PublicDreamItem
import com.example.ui.models.SadhakCommentSnippet
import com.example.viewmodel.CommunityDreamViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom Vedic Brand Colors
private val BrandSaffron = SaffronPrimary
private val BrandSaffronLight = SaffronSurface
private val BrandSaffronBorder = Color(0xFFFDBA74)
private val SurfaceBento = Color(0xFFFFFFFF)
private val BackgroundWarm = Color(0xFFFBF8F3)
private val TextPrimary = Color(0xFF1E293B)
private val TextSecondary = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamFeedScreen(
    userId: String,
    userName: String,
    onBackClick: () -> Unit = {},
    onAskSadhakPrivate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    showTopAppBar: Boolean = true,
    viewModel: CommunityDreamViewModel = viewModel(
        factory = CommunityDreamViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val commentsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var blockedUserIds by remember { mutableStateOf(setOf<String>()) }
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            CommunityModerationManager.observeBlockedUsers(userId).collect {
                blockedUserIds = it
            }
        }
    }
    var reportTargetData by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var reportReason by remember { mutableStateOf("Spam") }

    // SnackBar notification handling
    LaunchedEffect(uiState.postSuccessMessage) {
        uiState.postSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundWarm,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showTopAppBar) {
                Surface(
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.testTag("dream_feed_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = SaffronDeep
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(
                                    text = "Dream Feed",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronDeep
                                    )
                                )
                                Text(
                                    text = "Vedic Community Insights & Sadhak Guidance",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("dream_feed_lazy_column"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header when topAppBar is embedded in HomeScreen
            if (!showTopAppBar) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Community Dreams",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronDeep
                                )
                            )
                            Text(
                                text = "Human-to-human Vedic interpretation & discussion",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // Top Post Dream Card (Bento 24dp)
            item {
                TopCreateDreamCard(
                    isPosting = uiState.isPostingDream,
                    onSharePublic = { text, isAnon ->
                        viewModel.postDream(
                            dreamText = text,
                            isAnonymous = isAnon,
                            onSuccess = {}
                        )
                    },
                    onAskSadhakPrivate = { text ->
                        if (text.isNotBlank()) {
                            onAskSadhakPrivate(text)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please describe your dream first")
                            }
                        }
                    }
                )
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Community Dreams & Sadhak Insights",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                    )
                    Text(
                        text = "${uiState.dreams.size} Dreams",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary
                        )
                    )
                }
            }

            // Loading State
            if (uiState.isLoading && uiState.dreams.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = SaffronPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading dreams...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else if (uiState.dreams.isEmpty()) {
                // Empty state
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🌙",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No community dreams yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronDeep
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Share your dream above to discuss with the community and spiritual guides.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // List of Community Dreams
                items(
                    items = uiState.dreams.filter { it.userId !in blockedUserIds },
                    key = { it.dreamId }
                ) { dream ->
                    DreamCard(
                        dream = dream,
                        onPranamClick = { viewModel.togglePranamLike(dream) },
                        onDiscussClick = { viewModel.openCommentsSheet(dream) },
                        onReportClick = {
                            reportReason = "Spam"
                            reportTargetData = Triple("dream", dream.dreamId, dream.userId)
                        },
                        onBlockClick = {
                            CommunityModerationManager.blockUser(context, dream.userId)
                        }
                    )
                }
            }

            // Bottom space for scroll ergonomics
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Comments Modal Bottom Sheet
    if (uiState.activeCommentsDream != null) {
        val activeDream = uiState.activeCommentsDream!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeCommentsSheet() },
            sheetState = commentsSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.testTag("dream_comments_bottom_sheet")
        ) {
            DreamCommentsSheetContent(
                dream = activeDream,
                comments = uiState.activeComments.filter { it.userId !in blockedUserIds },
                isLoading = uiState.isLoadingComments,
                isPosting = uiState.isPostingComment,
                onClose = {
                    scope.launch {
                        commentsSheetState.hide()
                        viewModel.closeCommentsSheet()
                    }
                },
                onPostComment = { text, role ->
                    viewModel.postComment(
                        text = text,
                        authorRole = role,
                        onSuccess = {}
                    )
                },
                onReportComment = { comment ->
                    reportReason = "Spam"
                    reportTargetData = Triple("comment", comment.commentId, comment.userId)
                },
                onBlockUser = { authorId ->
                    CommunityModerationManager.blockUser(context, authorId)
                }
            )
        }
    }

    if (reportTargetData != null) {
        val (targetType, targetId, ownerId) = reportTargetData!!
        AlertDialog(
            onDismissRequest = { reportTargetData = null },
            title = { Text("Report ${targetType.replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a reason for reporting:", fontSize = 13.sp, color = Color(0xFF475569))
                    CommunityModerationManager.REPORT_REASONS.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = reportReason == reason,
                                onClick = { reportReason = reason }
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
                            targetType = targetType,
                            targetId = targetId,
                            ownerId = ownerId,
                            reason = reportReason
                        )
                        reportTargetData = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportTargetData = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Top Create Card:
 * Bento style card with 24dp corner radius.
 * Formatted in pure English.
 */
@Composable
fun TopCreateDreamCard(
    isPosting: Boolean,
    onSharePublic: (text: String, isAnonymous: Boolean) -> Unit,
    onAskSadhakPrivate: (text: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dreamInput by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var showMicToast by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_dream_input_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBento),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header row with Vedic emblem
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SaffronContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🕉", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Post Dream",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                    )
                    Text(
                        text = "Vedic symbolism & spiritual community dialogue",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dream Text Input
            OutlinedTextField(
                value = dreamInput,
                onValueChange = { dreamInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dream_text_input"),
                placeholder = {
                    Text(
                        text = "What did you see? e.g. holy river, Shiva temple, snake, ancestors...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF94A3B8),
                            fontStyle = FontStyle.Italic
                        )
                    )
                },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFFAFBFB),
                    unfocusedContainerColor = Color(0xFFFAFBFB)
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { showMicToast = true },
                        modifier = Modifier.testTag("mic_input_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (showMicToast) SaffronDeep else SaffronPrimary
                        )
                    }
                }
            )

            // Mic UI Feedback
            AnimatedVisibility(visible = showMicToast) {
                Text(
                    text = "🎙️ Voice input active (Speak your dream - Simulated)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BrandSaffron,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Anonymously Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isAnonymous = !isAnonymous }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAnonymous,
                    onCheckedChange = { isAnonymous = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SaffronPrimary,
                        uncheckedColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("post_anonymous_checkbox")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Post Anonymously",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Your identity remains hidden as 'Anonymous'",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: Ask Sadhak (Private)
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        onAskSadhakPrivate(dreamInput)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("ask_sadhak_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandSaffron
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(BrandSaffron, Color(0xFFEA580C)))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = BrandSaffron
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ask Sadhak (Private)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Button 2: Share in Community (Public)
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSharePublic(dreamInput, isAnonymous)
                        dreamInput = ""
                    },
                    enabled = !isPosting && dreamInput.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("post_dream_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaffronPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = SaffronPrimary.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Post to Community",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dream Card:
 * Bento style card in pure English.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DreamCard(
    dream: PublicDreamItem,
    onPranamClick: () -> Unit,
    onDiscussClick: () -> Unit,
    onReportClick: () -> Unit = {},
    onBlockClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dream_card_${dream.dreamId}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBento),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Author and Relative Time Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (dream.isAnonymous) Color(0xFFF1F5F9) else SaffronContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (dream.isAnonymous) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = dream.authorName.take(1).uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronDeep
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (dream.isAnonymous) "Anonymous Seeker" else dream.authorName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        if (dream.isAnonymous) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🔒",
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = formatRelativeTime(dream.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Report",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.clickable { onReportClick() }
                    )
                    Text(
                        text = "Block",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        modifier = Modifier.clickable { onBlockClick() }
                    )
                    Text(
                        text = "🪷",
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dream Text
            Text(
                text = dream.dreamText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            )

            // Tags (999dp pill shape)
            if (dream.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dream.tags.forEach { tag ->
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.testTag("tag_pill_${tag}")
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaffronDark,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Sadhak Highlight Box if a Sadhak has replied
            if (dream.topSadhakComment != null) {
                Spacer(modifier = Modifier.height(16.dp))
                SadhakHighlightBox(sadhakComment = dream.topSadhakComment)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Pranam (Like) and Discuss (Comment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pranam (Like) Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onPranamClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("pranam_button_${dream.dreamId}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (dream.isLikedByMe) "🙏" else "🤲",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (dream.isLikedByMe) "Pranamed (${dream.likeCount})" else "Pranam (${dream.likeCount})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (dream.isLikedByMe) BrandSaffron else TextSecondary,
                            fontWeight = if (dream.isLikedByMe) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }

                // Discuss (Comment) Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onDiscussClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("discuss_button_${dream.dreamId}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Discuss",
                        tint = SaffronDeep,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Discuss (${dream.commentCount})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = SaffronDeep,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Sadhak Highlight Box:
 * Rendered when a Sadhak has commented on a dream.
 */
@Composable
fun SadhakHighlightBox(
    sadhakComment: SadhakCommentSnippet,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BrandSaffronBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("sadhak_highlight_box"),
        color = BrandSaffronLight,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Sadhak Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = BrandSaffron,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "🕉 Verified Sadhak",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = sadhakComment.authorName.ifBlank { "Spiritual Sadhak" },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9A3412)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Vedic Drishti",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BrandSaffron,
                        fontStyle = FontStyle.Italic,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sadhak's interpretation text
            Text(
                text = sadhakComment.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF7C2D12),
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

/**
 * Comments Modal Bottom Sheet Content:
 * Pure English discussion interface.
 */
@Composable
fun DreamCommentsSheetContent(
    dream: PublicDreamItem,
    comments: List<DreamCommentItem>,
    isLoading: Boolean,
    isPosting: Boolean,
    onClose: () -> Unit,
    onPostComment: (text: String, role: String) -> Unit,
    onReportComment: (DreamCommentItem) -> Unit = {},
    onBlockUser: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var newCommentText by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("user") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(bottom = 12.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Community Discussion",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaffronDeep
                    )
                )
                Text(
                    text = "100% Human-to-Human Vedic Dialogue",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_comments_sheet_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextSecondary
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)

        // Dream snippet preview
        Surface(
            color = BackgroundWarm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp)) {
                Text("💭", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (dream.isAnonymous) "Anonymous Seeker's Dream" else "${dream.authorName}'s Dream",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                    )
                    Text(
                        text = dream.dreamText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Comments List
        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isLoading && comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = SaffronPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            } else if (comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🙏", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No comments yet",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = SaffronDark
                                )
                            )
                            Text(
                                text = "Be the first to share your Vedic perspective on this dream.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = comments,
                    key = { it.commentId }
                ) { comment ->
                    CommentItemCard(
                        comment = comment,
                        onReportClick = { onReportComment(comment) },
                        onBlockClick = { onBlockUser(comment.userId) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)

        // Role selector chip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Comment as:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = if (selectedRole == "user") SaffronContainer else Color.Transparent,
                    shape = RoundedCornerShape(999.dp),
                    border = if (selectedRole == "user") null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { selectedRole = "user" }
                        .testTag("role_user_chip")
                ) {
                    Text(
                        text = "Seeker",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selectedRole == "user") SaffronDeep else TextSecondary,
                            fontWeight = if (selectedRole == "user") FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = if (selectedRole == "sadhak") BrandSaffronLight else Color.Transparent,
                    shape = RoundedCornerShape(999.dp),
                    border = if (selectedRole == "sadhak") BorderStroke(1.dp, BrandSaffronBorder) else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { selectedRole = "sadhak" }
                        .testTag("role_sadhak_chip")
                ) {
                    Text(
                        text = "🕉 Verified Sadhak",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selectedRole == "sadhak") BrandSaffron else TextSecondary,
                            fontWeight = if (selectedRole == "sadhak") FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Bottom Comment Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("comment_input_field"),
                placeholder = {
                    Text(
                        text = if (selectedRole == "sadhak") "Write your Vedic interpretation..." else "Share your thoughts or perspective...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
                    )
                },
                maxLines = 3,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronPrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color(0xFFFAFBFB),
                    unfocusedContainerColor = Color(0xFFFAFBFB)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (newCommentText.isNotBlank() && !isPosting) {
                            focusManager.clearFocus()
                            onPostComment(newCommentText, selectedRole)
                            newCommentText = ""
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Submit Comment Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onPostComment(newCommentText, selectedRole)
                    newCommentText = ""
                },
                enabled = newCommentText.isNotBlank() && !isPosting,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("submit_comment_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaffronPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE2E8F0),
                    disabledContentColor = Color(0xFF94A3B8)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Comment",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Comment Item Card in pure English.
 */
@Composable
fun CommentItemCard(
    comment: DreamCommentItem,
    onReportClick: () -> Unit = {},
    onBlockClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSadhak = comment.authorRole.equals("sadhak", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSadhak) {
                    Modifier.border(
                        width = 1.dp,
                        color = BrandSaffronBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier.border(
                        width = 0.5.dp,
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            )
            .testTag("comment_card_${comment.commentId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSadhak) BrandSaffronLight else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            if (isSadhak) SaffronPrimary else SaffronContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSadhak) "🕉" else comment.authorName.take(1).uppercase(Locale.getDefault()),
                        fontSize = 12.sp,
                        color = if (isSadhak) Color.White else SaffronDeep,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = comment.authorName.ifBlank { if (isSadhak) "Sadhak" else "Seeker" },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSadhak) Color(0xFF9A3412) else TextPrimary
                            )
                        )
                        if (isSadhak) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = BrandSaffron,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = "🕉 Verified Sadhak",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = formatRelativeTime(comment.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Report",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.clickable { onReportClick() }
                    )
                    Text(
                        text = "Block",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        modifier = Modifier.clickable { onBlockClick() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isSadhak) Color(0xFF7C2D12) else TextPrimary,
                    lineHeight = 19.sp
                )
            )
        }
    }
}

/**
 * Format timestamp into pure English relative time.
 */
private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return "Recently"
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000L -> "Just now"
        diff < 3600_000L -> "${diff / 60_000L}m ago"
        diff < 86400_000L -> "${diff / 3600_000L}h ago"
        diff < 604800_000L -> "${diff / 86400_000L}d ago"
        else -> {
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)
            sdf.format(Date(timestamp))
        }
    }
}
