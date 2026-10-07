package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.models.WalletTransaction
import com.example.ui.theme.*
import com.example.ui.components.DevLogoIcon
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    transactions: List<WalletTransaction>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperBg)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
            }
            Text(
                text = "Transaction History",
                fontFamily = FontFamily.Serif,
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium,
                color = Ink
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Official Invoice & Receipt Brand Header (40dp logo height)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, EditorialLine),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DevLogoIcon(
                        size = 40.dp,
                        elevation = 2.dp,
                        showGlow = false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "देव भाषा • डिजिटल रसीद",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = "जीएसटी एवं वॉलेट लेन-देन विवरण",
                            fontSize = 11.sp,
                            color = InkSoft
                        )
                    }
                }
                Surface(
                    color = Sage.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "सुरक्षित ✓",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Sage,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Transactions List
        if (transactions.isEmpty()) {
            com.example.ui.components.DevEmptyState(
                title = "कोई लेन-देन उपलब्ध नहीं है",
                description = "आपके वॉलेट से संबंधित सभी जमा व खर्च के लेन-देन यहाँ सुरक्षित रूप से दिखाई देंगे।",
                symbol = "📜"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions.sortedByDescending { it.timestamp }) { tx ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PaperCard,
                        border = BorderStroke(1.dp, EditorialLine),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (tx.status == "success") (if (tx.isCredit) Color(0x1A67784F) else Color(0x1AC0521C)) else Color(0x1A94A3B8),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (tx.isCredit) Icons.Filled.Add else Icons.Filled.Remove,
                                        contentDescription = null,
                                        tint = if (tx.status == "success") (if (tx.isCredit) Sage else Terra) else InkSoft,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = tx.title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 14.sp)
                                Text(
                                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp)),
                                    fontSize = 11.sp,
                                    color = InkSoft
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (tx.isCredit) "+" else "−"}₹${tx.amount.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.status == "success") (if (tx.isCredit) Sage else Terra) else InkSoft
                                )
                                Text(
                                    text = tx.status.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.status == "success") Sage else Terra
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
