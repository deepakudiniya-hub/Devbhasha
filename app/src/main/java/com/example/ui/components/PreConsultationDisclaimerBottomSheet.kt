package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.DisclaimerContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreConsultationDisclaimerBottomSheet(
    isHindi: Boolean = true,
    onConfirm: (dontShowAgain: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var dontShowAgain by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ) {}
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 6.dp)
                .testTag("pre_consultation_disclaimer_sheet")
        ) {
            // Header with Icon and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isHindi) "महत्वपूर्ण परामर्श सूचना" else "Important Consultation Notice",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isHindi) "कृपया आगे बढ़ने से पहले पढ़ें" else "Please read before proceeding",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notice Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            // Hindi Notice
                            Text(
                                text = DisclaimerContent.PRE_CONSULTATION_WARNING_HI,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(8.dp))
                            // English Notice
                            Text(
                                text = DisclaimerContent.PRE_CONSULTATION_WARNING_EN,
                                fontSize = 12.sp,
                                lineHeight = 17.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // "Dobara na dikhayein" Checkbox
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("dont_show_again_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHindi) "Dobara na dikhayein (दोबारा न दिखाएं)" else "Don't show again",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: Confirm & Cancel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("cancel_consultation_button")
                ) {
                    Text(
                        text = if (isHindi) "रद्द करें (Cancel)" else "Cancel",
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onConfirm(dontShowAgain) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("confirm_consultation_button")
                ) {
                    Text(
                        text = if (isHindi) "Samajh gaya, aage badhein" else "Understood, Proceed",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
