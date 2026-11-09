package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.models.ALL_AVATAR_OPTIONS
import com.example.ui.theme.*
import com.example.utils.HapticFeedbackHelper

/**
 * Devbhasha Profile Avatar Picker Bottom Sheet
 * 3-column grid of the 15 avatars (circular, 72dp, selected one gets a 3dp primary ring and check mark),
 * plus a 'save' primary button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevbhashaAvatarPickerSheet(
    currentAvatarId: String,
    onDismiss: () -> Unit,
    onAvatarSelected: (String) -> Unit
) {
    var selectedId by remember { mutableStateOf(currentAvatarId.ifBlank { "avatar_01" }) }
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 38.dp, height = 4.dp)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Title & Close Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.avatar_picker_title),
                    fontFamily = TiroDevanagariHindi,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "बंद करें",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(R.string.avatar_picker_subtitle),
                fontFamily = NotoSansDevanagari,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Column Grid of 15 Avatars (circular, 72dp)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(ALL_AVATAR_OPTIONS) { avatar ->
                    val isSelected = avatar.id == selectedId

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    }
                                )
                                .clickable {
                                    HapticFeedbackHelper.playClick(haptic)
                                    selectedId = avatar.id
                                }
                        ) {
                            Image(
                                painter = painterResource(id = avatar.drawableRes),
                                contentDescription = avatar.nameHi,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )

                            // Checkmark overlay badge on selected avatar
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    border = BorderStroke(1.5.dp, Color.White),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "चयनित",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Save Button
            Button(
                onClick = {
                    HapticFeedbackHelper.playSuccess(haptic)
                    onAvatarSelected(selectedId)
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.avatar_picker_save_button),
                    fontFamily = NotoSansDevanagari,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
