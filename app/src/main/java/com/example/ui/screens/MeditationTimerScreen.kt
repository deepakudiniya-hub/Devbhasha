package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationTimerScreen(onBackClick: () -> Unit) {
    var timeLeft by remember { mutableLongStateOf(10 * 60 * 1000L) } // 10 minutes in ms
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1000
        }
    }

    val minutes = (timeLeft / 1000) / 60
    val seconds = (timeLeft / 1000) % 60

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ध्यान टाइमर (Meditation Timer)") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF083A35)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF083A35), CircleShape)
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = {
                        isRunning = false
                        timeLeft = 10 * 60 * 1000L
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFF6C644), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                }
            }
        }
    }
}
