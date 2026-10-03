package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight

@Composable
fun AskAndDreamCards(
    selectedSadhakName: String = "वैदिक साधक",
    userId: String = "",
    userName: String = "साधक",
    onQuestionSubmitted: (() -> Unit)? = null
) {
    var questionText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderLight),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "पंडित जी से सीधा संवाद ('पूछा')",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000)
            )
            
            Text(
                text = "चयनित साधक: $selectedSadhakName",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
            )

            // प्रश्न लिखने के लिए टेक्स्ट बॉक्स
            OutlinedTextField(
                value = questionText,
                onValueChange = { 
                    questionText = it 
                    if (isSuccess) isSuccess = false
                    statusMessage = ""
                },
                label = { Text("अपना प्रश्न या समस्या यहाँ लिखें...") },
                placeholder = { Text("उदा. व्यापार में लाभ, विवाह में आ रही अड़चनें अथवा स्वास्थ्य संबंधी प्रश्न") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // समाधान पाने का बटन
            Button(
                onClick = {
                    val trimmed = questionText.trim()
                    if (trimmed.isNotBlank()) {
                        isLoading = false
                        isSuccess = true
                        statusMessage = "आपका प्रश्न $selectedSadhakName जी के पास सफलतापूर्वक भेज दिया गया है! स्थिति: Pending"
                        questionText = "" // बॉक्स खाली करें
                        onQuestionSubmitted?.invoke()
                    } else {
                        statusMessage = "कृपया पहले अपना प्रश्न लिखें।"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isLoading && questionText.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("पंडित जी को भेजा जा रहा है...")
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("पंडित जी से समाधान पाएं", fontWeight = FontWeight.Bold)
                }
            }

            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSuccess) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = statusMessage,
                        fontSize = 12.5.sp,
                        color = if (isSuccess) Color(0xFF047857) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
