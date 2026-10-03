package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class QuestionItem(
    val id: String = "",
    val userName: String = "",
    val questionText: String = "",
    val sadhakName: String = "",
    val status: String = "",
    val providerAnswer: String = ""
)

@Composable
fun ProviderDashboardScreen(currentSadhakName: String) {
    val initialQuestions = remember {
        listOf(
            QuestionItem(
                id = "q_1",
                userName = "राजेश शर्मा",
                questionText = "व्यवसाय में लगातार विघ्न आ रहे हैं, कोई उपयुक्त वैदिक उपाय सुझाएं।",
                sadhakName = currentSadhakName.ifBlank { "आचार्य देव शर्मा" },
                status = "Pending"
            ),
            QuestionItem(
                id = "q_2",
                userName = "सुनीता वर्मा",
                questionText = "सपने में बहता हुआ गंगाजल देखना किस फल का संकेत है?",
                sadhakName = currentSadhakName.ifBlank { "योगी आनंद नाथ" },
                status = "Pending"
            )
        )
    }

    var pendingQuestions by remember { mutableStateOf<List<QuestionItem>>(initialQuestions) }
    var answerInputs by remember { mutableStateOf(mapOf<String, String>()) }

    val activePending = remember(pendingQuestions) {
        pendingQuestions.filter { it.status == "Pending" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "पंडित जी डैशबोर्ड: $currentSadhakName",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (activePending.isEmpty()) {
            Text(text = "अभी कोई नया पेंडिंग प्रश्न नहीं है।", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(activePending) { question ->
                    val currentAnswer = answerInputs[question.id] ?: ""

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "साधक (यूज़र): ${question.userName}", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "प्रश्न: ${question.questionText}", style = MaterialTheme.typography.bodyMedium)
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = currentAnswer,
                                onValueChange = { newValue ->
                                    answerInputs = answerInputs.toMutableMap().apply { put(question.id, newValue) }
                                },
                                label = { Text("समाधान / उत्तर यहाँ लिखें...") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (currentAnswer.isNotBlank()) {
                                        pendingQuestions = pendingQuestions.map {
                                            if (it.id == question.id) it.copy(status = "Answered", providerAnswer = currentAnswer) else it
                                        }
                                        answerInputs = answerInputs.toMutableMap().apply { remove(question.id) }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("उत्तर भेजें (Send Answer)")
                            }
                        }
                    }
                }
            }
        }
    }
}
