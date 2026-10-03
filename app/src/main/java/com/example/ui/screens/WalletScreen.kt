package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.utils.UserSession

@Composable
fun WalletScreen(userId: String) {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    var walletBalance by remember { mutableDoubleStateOf(userSession.getWalletBalance()) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "आपका डिजिटल वॉलेट", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "उपलब्ध राशि: ₹%.2f".format(walletBalance),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // पैसे जोड़ने (Add Money) का बटन
        Button(
            onClick = {
                // SECURITY FIX: free local credit removed - recharge must go
                // through Razorpay and the balance must be server-owned.
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "प्रोसेसिंग..." else "₹100 वॉलेट में जोड़ें (Add Money)")
        }
    }
}
