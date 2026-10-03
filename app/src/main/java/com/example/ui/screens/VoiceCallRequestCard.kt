package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VoiceCallRequestCard(
    sadhakName: String,
    callFee: Double,
    userId: String,
    userName: String = "साधक"
) {
    var isLoading by remember { mutableStateOf(false) }
    var callStatus by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "पंडित जी से सीधी वॉइस कॉल", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "विशेषज्ञ: $sadhakName", style = MaterialTheme.typography.bodyMedium)
                Text(text = "शुल्क: ₹%.2f (प्रति सत्र)".format(callFee), style = MaterialTheme.typography.bodySmall)
                
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (userId.isNotEmpty()) {
                            isLoading = true
                            callStatus = "पंडित जी को कॉल मिलाई जा रही है... कृपया प्रतीक्षा करें।"
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "कनेक्ट हो रहा है..." else "पंडित जी को कॉल करें (Voice Call)")
                }

                if (callStatus.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = callStatus, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
