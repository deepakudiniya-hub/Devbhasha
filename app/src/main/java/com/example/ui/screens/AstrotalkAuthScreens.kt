package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.DevLogoIcon
import com.example.ui.theme.*

// =============================================================================
// ASTROTALK LOGIN SCREEN (Screen 5 in reference image)
// =============================================================================

@Composable
fun AstrotalkLoginScreen(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onGetOtpClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    onAppleLoginClick: () -> Unit,
    onFacebookLoginClick: () -> Unit,
    onSkip: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFF0D656C)
    val textColor = Color(0xFF2B2B2B)
    val textSub = Color(0xFF6E6E6E)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF6EA))
            .verticalScroll(rememberScrollState())
    ) {
        // Curved Wave Top Header matching Astrotalk
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.72f)
                    quadraticTo(
                        size.width * 0.5f, size.height * 1.05f,
                        0f, size.height * 0.72f
                    )
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D656C),
                            Color(0xFF0D656C)
                        )
                    )
                )
            }

            // Top Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Brand Logo in center of curved wave
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DevLogoIcon(
                    size = 64.dp,
                    elevation = 6.dp,
                    showGlow = false
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login to Devbhasha",
                fontFamily = AppFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "सत्यापित वैदिक साधकों से सीधा संवाद और स्वप्न विचार हेतु अपना मोबाइल नंबर दर्ज करें",
                fontFamily = AppFontFamily,
                fontSize = 13.sp,
                color = textSub,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Phone Input Card (Astrotalk style)
            Text(
                text = "Enter your mobile number",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSub,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🇮🇳 +91",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color(0xFFCBD5E1))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) onPhoneNumberChange(it) },
                        placeholder = { Text("Phone number", color = Color(0xFF94A3B8), fontSize = 14.5.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Get OTP Button
            Button(
                onClick = onGetOtpClick,
                enabled = !isLoading && phoneNumber.length == 10,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = "Get OTP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Disclaimer
            Text(
                text = "By continuing, you agree to our Terms of Use & Privacy Policy",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Divider: Or login with
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                Text(
                    text = "  Or login with  ",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Buttons (Google, Apple, Facebook)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onGoogleLoginClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Apple
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onAppleLoginClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("", fontSize = 22.sp, color = Color.Black)
                    }
                }

                // Facebook
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onFacebookLoginClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_facebook_logo),
                            contentDescription = "Facebook",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// =============================================================================
// ASTROTALK OTP VERIFICATION SCREEN (Screen 6 in reference image)
// With separate digit boxes and custom numeric keypad
// =============================================================================

@Composable
fun AstrotalkOtpScreen(
    phoneNumber: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit,
    onBackClick: () -> Unit,
    otpTimer: Int,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFF0D656C)
    val textColor = Color(0xFF2B2B2B)
    val textSub = Color(0xFF6E6E6E)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF6EA))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Top Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "OTP Code Verification",
            fontFamily = AppFontFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enter the 6-digit code sent to your mobile number +91 ${phoneNumber.take(2)}•••• ••${phoneNumber.takeLast(2)}",
            fontFamily = AppFontFamily,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = textSub
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 6 Separate Square OTP Digit Boxes (matching Astrotalk design)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            for (i in 0 until 6) {
                val digit = if (i < otpCode.length) otpCode[i].toString() else ""
                val isCurrent = i == otpCode.length

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = if (isCurrent) 3.dp else 1.dp,
                    border = BorderStroke(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) primaryColor else if (digit.isNotBlank()) primaryColor.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = digit,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Resend Timer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Didn't receive code? ",
                fontSize = 13.sp,
                color = textSub
            )
            if (otpTimer > 0) {
                Text(
                    text = "You can resend code in ${otpTimer}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            } else {
                Text(
                    text = "Resend OTP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.clickable { onResendClick() }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Custom Numeric Keypad (Tactile mobile dialpad as in Astrotalk)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val keypadRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            keypadRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                shadowElevation = 1.dp,
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        when (key) {
                                            "DEL" -> {
                                                if (otpCode.isNotEmpty()) onOtpChange(otpCode.dropLast(1))
                                            }
                                            else -> {
                                                if (otpCode.length < 6) onOtpChange(otpCode + key)
                                            }
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (key == "DEL") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Delete",
                                            tint = textColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Verify Button
        Button(
            onClick = onVerifyClick,
            enabled = !isLoading && otpCode.length == 6,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                disabledContainerColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = "Verify Code",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}
