package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.Pricing
import com.example.utils.RazorpayPaymentManager
import com.example.utils.UserSession
import com.example.utils.WalletRepository

@Composable
fun WalletScreen(
    userId: String,
    userName: String = "Devotee",
    userPhone: String = "",
    onOpenHistory: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userSession = remember { UserSession(context) }
    var walletBalance by remember { mutableDoubleStateOf(userSession.getCachedWalletBalance()) }
    
    DisposableEffect(userId) {
        val reg = WalletRepository.observeBalance(userId) {
            walletBalance = it
            userSession.setCachedWalletBalance(it)
        }
        onDispose { reg.remove() }
    }

    var selectedAmount by remember { mutableDoubleStateOf(200.0) }
    var customAmountText by remember { mutableStateOf("200") }
    var isLoading by remember { mutableStateOf(false) }

    val presetAmounts = listOf(100.0, 200.0, 500.0, 1000.0, 2000.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8F2))
            .padding(16.dp)
    ) {
        // Wallet Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE6F4F1),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFF0F5C5C),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = "आपका डिजिटल वॉलेट",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    if (onOpenHistory != null) {
                        TextButton(onClick = onOpenHistory) {
                            Text(
                                text = "इतिहास (History) →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F5C5C)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "उपलब्ध राशि: ₹%.2f".format(walletBalance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F5C5C)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "पे-पर-मिनट कॉल और चैट के लिए सुरक्षित रीचार्ज",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recharge Amount Selection
        Text(
            text = "रीचार्ज राशि चुनें (Select Amount)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetAmounts.forEach { amount ->
                val isSelected = selectedAmount == amount
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF0F5C5C) else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F5C5C) else Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            selectedAmount = amount
                            customAmountText = amount.toInt().toString()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "₹${amount.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF1E293B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = customAmountText,
            onValueChange = {
                customAmountText = it
                it.toDoubleOrNull()?.let { amt -> selectedAmount = amt }
            },
            label = { Text("अन्य राशि दर्ज करें (Custom Amount ₹)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0F5C5C),
                unfocusedBorderColor = Color(0xFFCBD5E1)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Razorpay / UPI Recharge Button
        Button(
            onClick = {
                val amt = customAmountText.toDoubleOrNull() ?: selectedAmount
                if (amt <= 0) {
                    Toast.makeText(context, "कृपया वैध राशि दर्ज करें", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (activity == null) {
                    Toast.makeText(context, "Payment activity unavailable", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                val started = RazorpayPaymentManager.startRechargePayment(
                    activity = activity,
                    amount = amt,
                    userId = userId,
                    userName = userName,
                    userPhone = userPhone
                )
                if (!started) {
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "वॉलेट रीचार्ज करें (Pay via Razorpay / UPI)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Official Pricing Guidelines Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "📋 आधिकारिक परामर्श दरें (Devbhasha Pricing)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• वॉइस कॉल परामर्श: ₹${Pricing.CONSULTATION_PRICE_RUPEES} (${Pricing.CONSULTATION_DURATION_MINUTES} मिनट सत्र)\n• टेक्स्ट प्रश्न (चैट): मात्र ₹${Pricing.TEXT_QUESTION_PRICE_RUPEES} प्रति प्रश्न\n• पारदर्शी दक्षिणा: ${Pricing.SADHAK_SHARE_PERCENT}% हिस्सा सीधे साधक को और ${Pricing.PLATFORM_SHARE_PERCENT}% प्लेटफ़ॉर्म को।",
                    fontSize = 11.5.sp,
                    color = Color(0xFF475569),
                    lineHeight = 17.sp
                )
            }
        }
    }
}
