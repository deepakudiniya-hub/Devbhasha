package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.HapticFeedbackHelper

@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    otpTimer: Int = 30,
    isTimerRunning: Boolean = false,
    onVerifyOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    onBackClick: () -> Unit,
    onEditNumberClick: () -> Unit,
    isLoading: Boolean = false,
    currentLanguage: String = "hi",
    modifier: Modifier = Modifier
) {
    val isHindi = currentLanguage == "hi"
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Request focus on opening OTP screen
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Focus fallback
        }
    }

    val primarySaffron = Color(0xFFFF9800)
    val saffronDark = Color(0xFFCC6F2E)
    val lightCardBg = Color(0xFFFFFBF0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar: Back Arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        HapticFeedbackHelper.playClick(haptic)
                        onBackClick()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to mobile input",
                        tint = Color(0xFF1E293B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main OTP Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = lightCardBg,
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Title
                    Text(
                        text = if (isHindi) "ओटीपी सत्यापित करें" else "Verify OTP",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subtitle & Edit Mobile Number Row
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "ओटीपी इस नंबर पर भेजा गया:" else "6-digit code sent to:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "+91 $phoneNumber",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            TextButton(
                                onClick = {
                                    HapticFeedbackHelper.playClick(haptic)
                                    onEditNumberClick()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit phone number",
                                        tint = primarySaffron,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isHindi) "बदलें" else "Edit",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primarySaffron
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // 6 Digit Boxes Component
                    Text(
                        text = if (isHindi) "6-अंकों का कोड दर्ज करें" else "Enter 6-digit verification code",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (i in 0 until 6) {
                                val charAtPos = otpCode.getOrNull(i)?.toString() ?: ""
                                val isFocused = otpCode.length == i || (otpCode.length == 6 && i == 5)
                                val isFilled = charAtPos.isNotEmpty()

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isFocused -> Color.White
                                        isFilled -> Color(0xFFFFF7EC)
                                        else -> Color.White
                                    },
                                    border = BorderStroke(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = when {
                                            isFocused -> primarySaffron
                                            isFilled -> saffronDark
                                            else -> Color(0xFFCBD5E1)
                                        }
                                    ),
                                    shadowElevation = if (isFocused) 2.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = charAtPos,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }
                        }

                        BasicTextField(
                            value = otpCode,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                    onOtpChange(it)
                                    if (it.length == 6) {
                                        focusManager.clearFocus()
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (otpCode.length == 6) {
                                        focusManager.clearFocus()
                                        onVerifyOtpClick()
                                    }
                                }
                            ),
                            textStyle = TextStyle(color = Color.Transparent),
                            modifier = Modifier
                                .matchParentSize()
                                .focusRequester(focusRequester)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Countdown Timer & Resend OTP Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isTimerRunning && otpTimer > 0) {
                            Text(
                                text = if (isHindi) "पुनः OTP भेजें: ${otpTimer}s" else "Resend OTP in ${otpTimer}s",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        } else {
                            Text(
                                text = if (isHindi) "ओटीपी नहीं मिला? " else "Didn't receive code? ",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = if (isHindi) "पुनः भेजें" else "Resend",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = primarySaffron,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        HapticFeedbackHelper.playClick(haptic)
                                        onResendOtpClick()
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Solid Primary Saffron Button: "Verify & Continue"
                    Button(
                        onClick = {
                            HapticFeedbackHelper.playClick(haptic)
                            focusManager.clearFocus()
                            onVerifyOtpClick()
                        },
                        enabled = !isLoading && otpCode.length == 6,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primarySaffron,
                            contentColor = Color.White,
                            disabledContainerColor = primarySaffron.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = if (isHindi) "सत्यापित करें और आगे बढ़ें" else "Verify & Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
