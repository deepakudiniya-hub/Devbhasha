
data class PaymentReceiptItem(
    val paymentId: String = "",
    val orderId: String = "",
    val amountRupees: Double = 0.0,
    val method: String = "UPI / Razorpay",
    val status: String = "SUCCESS",
    val timestamp: Long = 0L
)

@Composable
fun PaymentReceiptDialog(
    receipt: PaymentReceiptItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateStr = remember(receipt.timestamp) {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
        sdf.format(java.util.Date(receipt.timestamp))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = SaffronPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Payment Receipt", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Receipt No:", fontSize = 11.5.sp, color = Color(0xFF64748B))
                            Text(receipt.paymentId.take(18), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order ID:", fontSize = 11.5.sp, color = Color(0xFF64748B))
                            Text(receipt.orderId.take(18), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date & Time:", fontSize = 11.5.sp, color = Color(0xFF64748B))
                            Text(dateStr, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Method:", fontSize = 11.5.sp, color = Color(0xFF64748B))
                            Text(receipt.method, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status:", fontSize = 11.5.sp, color = Color(0xFF64748B))
                            Text(receipt.status.uppercase(), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SaffronSoft,
                    border = BorderStroke(1.dp, SaffronBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Amount Credited:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SaffronDark)
                        Text("₹${String.format(java.util.Locale.getDefault(), "%.2f", receipt.amountRupees)}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = SaffronPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareText = """
                        Devbhasha Payment Receipt
                        --------------------------------
                        Receipt No : ${receipt.paymentId}
                        Order ID   : ${receipt.orderId}
                        Date & Time: $dateStr
                        Amount     : ₹${String.format(java.util.Locale.getDefault(), "%.2f", receipt.amountRupees)}
                        Method     : ${receipt.method}
                        Status     : ${receipt.status.uppercase()}
                        --------------------------------
                        Thank you for your devotion with Devbhasha.
                    """.trimIndent()

                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Devbhasha Payment Receipt")
                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Share Receipt"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.HapticFeedbackHelper
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
    val haptic = LocalHapticFeedback.current
    val userSession = remember { UserSession(context) }
    var walletBalance by remember { mutableStateOf<Double?>(userSession.getCachedWalletBalance()) }
    var userPayments by remember { mutableStateOf<List<PaymentReceiptItem>>(emptyList()) }
    var selectedReceipt by remember { mutableStateOf<PaymentReceiptItem?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            val db = com.example.utils.FirestoreProvider.get(context)
            db.collection("payments")
                .whereEqualTo("uid", userId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            val paymentId = doc.getString("paymentId") ?: doc.id
                            val orderId = doc.getString("orderId") ?: "N/A"
                            val amountPaise = doc.getLong("amountPaise") ?: ((doc.getDouble("amount") ?: 0.0) * 100).toLong()
                            val method = doc.getString("method") ?: doc.getString("paymentMethod") ?: "UPI / Net Banking"
                            val status = doc.getString("status") ?: "SUCCESS"
                            val time = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                            PaymentReceiptItem(paymentId, orderId, amountPaise / 100.0, method, status, time)
                        }.sortedByDescending { it.timestamp }
                        userPayments = list
                    }
                }
        }
    }
    
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

    val presetAmounts = listOf(100.0, 200.0, 500.0, 1000.0)
    val promoOffers = listOf(
        Triple(500.0, 600.0, "Popular (+20% Bonus)"),
        Triple(1000.0, 1300.0, "Best Value (+30% Bonus)")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8F2))
            .verticalScroll(rememberScrollState())
    ) {
        // 1) Rich Deep-Saffron Header Framing Elevated Floating Wallet Balance Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SaffronDark,
                            SaffronDeep,
                            SaffronPrimary
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(bottom = 36.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Top Action Bar inside Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "देवभाषा वॉलेट (Digital Wallet)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable {
                                HapticFeedbackHelper.playClick(haptic)
                                if (userPayments.isNotEmpty()) {
                                    selectedReceipt = userPayments.first()
                                } else if (onOpenHistory != null) {
                                    onOpenHistory()
                                }
                            }
                    ) {
                        Text(
                            text = "इतिहास (Receipts) →",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Elevated Floating Wallet Balance Card with Glowing Golden Border
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color(0xFFFFD700)) // Glowing Golden Border
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "उपलब्ध बैलेंस (Available Balance)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹%.2f".format(walletBalance),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronPrimary
                                )
                            }

                            // Secure Payment Badge
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = SaffronContainer,
                                border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = SaffronPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "100% Secure SSL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "सत्रों, स्वप्न विचार और सात्विक परामर्श के लिए त्वरित उपयोग।",
                                fontSize = 11.5.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Body Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // 2) Redesigned Recharge Amount Buttons and Promotional Offer Cards
            Text(
                text = "रीचार्ज राशि चुनें (Select Recharge Amount)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Preset Amount Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetAmounts.forEach { amount ->
                    val isSelected = selectedAmount == amount
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SaffronPrimary else Color.White,
                        border = BorderStroke(1.5.dp, if (isSelected) SaffronPrimary else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                HapticFeedbackHelper.playClick(haptic)
                                selectedAmount = amount
                                customAmountText = amount.toInt().toString()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "₹${amount.toInt()}",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Promotional Offer Cards (e.g. 'Pay ₹500 & Get ₹600')
            promoOffers.forEach { (payAmt, getAmt, tag) ->
                val isSelected = selectedAmount == payAmt
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) SaffronPrimary else Color.White,
                    border = BorderStroke(1.5.dp, if (isSelected) SaffronPrimary else Color(0xFFD4AF37)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            HapticFeedbackHelper.playClick(haptic)
                            selectedAmount = payAmt
                            customAmountText = payAmt.toInt().toString()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color.White.copy(alpha = 0.2f) else Color(0xFFFFF9E6),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.CardGiftcard,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color(0xFFD4AF37),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Pay ₹${payAmt.toInt()} & Get ₹${getAmt.toInt()}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tag,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color(0xFFE2E8F0) else Color(0xFFD4AF37)
                                )
                            }
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                HapticFeedbackHelper.playClick(haptic)
                                selectedAmount = payAmt
                                customAmountText = payAmt.toInt().toString()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color.White,
                                unselectedColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3) Custom Amount Input Field Styled with High-Contrast Typography & Rounded Corners
            OutlinedTextField(
                value = customAmountText,
                onValueChange = {
                    customAmountText = it
                    it.toDoubleOrNull()?.let { amt -> selectedAmount = amt }
                },
                label = { Text("अन्य राशि दर्ज करें (Custom Amount ₹)", fontWeight = FontWeight.Medium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronPrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Final CTA 'Pay' Button with Rich Saffron-Gold Gradient & Smooth Rounded Corners
            val saffronGoldBrush = Brush.horizontalGradient(
                colors = listOf(
                    SaffronPrimary,
                    SaffronDeep,
                    Color(0xFFD4AF37)
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(saffronGoldBrush)
                    .clickable(enabled = !isLoading) {
                        HapticFeedbackHelper.playClick(haptic)
                        val amt = customAmountText.toDoubleOrNull() ?: selectedAmount
                        if (amt <= 0) {
                            Toast.makeText(context, "कृपया वैध राशि दर्ज करें", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }
                        if (activity == null) {
                            Toast.makeText(context, "Payment activity unavailable", Toast.LENGTH_SHORT).show()
                            return@clickable
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
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "वॉलेट रीचार्ज करें · Pay ₹${customAmountText.ifBlank { selectedAmount.toInt().toString() }}",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4) Frame Bottom Recharge Summary and Benefits Inside a Subtle Warm-Gold Tinted Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFFBEB), // Subtle Warm-Gold Tinted Card background
                border = BorderStroke(1.2.dp, Color(0xFFE5D1A1)), // Warm gold border
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF854D0E)
                        )
                        Text(
                            text = "रीचार्ज सारांश एवं आधिकारिक परामर्श दरें",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF713F12)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• लाइव परामर्श: साधक द्वारा निर्धारित प्रति-मिनट अथवा 30-मिनट सत्र दर\n• टेक्स्ट प्रश्न: मात्र ₹${Pricing.TEXT_QUESTION_PRICE_RUPEES} प्रति प्रश्न\n• स्वप्न विश्लेषण: चैट ₹${Pricing.DREAM_CHAT_PRICE_RUPEES} • कॉल ₹${Pricing.DREAM_CALL_PRICE_RUPEES}\n• सभी रीचार्ज तुरंत वॉलेट में क्रेडिट होते हैं और कभी समाप्त नहीं होते।",
                        fontSize = 12.sp,
                        color = Color(0xFF854D0E),
                        lineHeight = 18.sp
                    )
                }
            }

            if (userPayments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Top-Up History & Receipts (रिचार्ज रसीदें)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(8.dp))
                userPayments.take(10).forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedReceipt = item }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "+₹${String.format(java.util.Locale.getDefault(), "%.2f", item.amountRupees)} Top-Up",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SaffronPrimary
                                )
                                Text(
                                    text = "ID: ${item.paymentId.take(16)}...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SaffronSoft
                            ) {
                                Text(
                                    text = "View Receipt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SaffronDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (selectedReceipt != null) {
        PaymentReceiptDialog(
            receipt = selectedReceipt!!,
            onDismiss = { selectedReceipt = null }
        )
    }
}
