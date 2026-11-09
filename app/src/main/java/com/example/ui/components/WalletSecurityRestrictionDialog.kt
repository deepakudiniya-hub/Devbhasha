package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig

/**
 * High-end security restriction dialog that locks access to wallet features
 * when running on rooted devices or untrusted emulator environments.
 */
@Composable
fun WalletSecurityRestrictionDialog(
    isHindi: Boolean = false,
    indicators: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onToggleSimulation: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFFFFFFF),
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("wallet_security_restriction_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Shield Icon Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFEE2E2), Color(0xFFFECACA))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GppBad,
                        contentDescription = "Security Alert",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = if (isHindi) "सुरक्षा चेतावनी: प्रतिबंधित उपकरण" else "Security Alert: Untrusted Device",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Explanatory Subtitle
                Text(
                    text = if (isHindi)
                        "वित्तीय धोखाधड़ी और अनधिकृत छेड़छाड़ से सुरक्षा हेतु, रूटेड (Rooted) या असुरक्षित एमुलेटर उपकरणों पर Devbhasha वॉलेट व भुगतान सेवाएं प्रतिबंधित हैं।"
                    else
                        "To protect your financial transactions from fraud or tampering, Devbhasha wallet operations and recharge services are restricted on rooted devices or untrusted environments.",
                    fontSize = 13.5.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Threat Indicators Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "पहचाने गए सुरक्षा जोखिम:" else "Detected Security Flags:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9F1239)
                            )
                        }

                        val displayFlags = if (indicators.isNotEmpty()) {
                            indicators
                        } else {
                            listOf(
                                if (isHindi) "सुपरयूज़र (SU) या अनधिकृत बाइनरी उपस्थिति" else "Superuser (SU) or unauthorized binary detected",
                                if (isHindi) "Play Integrity / SafetyNet सुरक्षा सत्यापन विफल" else "Play Integrity verification standard unfulfilled"
                            )
                        }

                        displayFlags.forEach { flag ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE11D48),
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(
                                    text = flag,
                                    fontSize = 12.sp,
                                    color = Color(0xFF881337)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary CTA: Go Back / Return to Safety
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_security_dialog_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "मुख्य पृष्ठ पर वापस जाएं" else "Return to Home",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Debug preview toggle for development testing
                if (BuildConfig.DEBUG && onToggleSimulation != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onToggleSimulation,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("toggle_simulation_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "सुरक्षा सिमुलेशन टॉगल करें (Debug)" else "Toggle Security Simulation (Debug)",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }
    }
}
