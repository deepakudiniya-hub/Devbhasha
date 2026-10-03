package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.models.UserProfile
import com.example.utils.UserSession

@Composable
fun ProfileVerificationScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var statusMessage by remember { mutableStateOf("No data loaded") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Profile Verification", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (profile != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Name: ${profile?.displayName}")
                    Text(text = "Email: ${profile?.email}")
                    Text(text = "Role: ${profile?.role}")
                }
            }
        } else {
            Text(text = statusMessage)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            onClick = {
                isLoading = true
                statusMessage = "Fetching..."
                val name = userSession.getUserName()
                val uid = userSession.getUserId()
                profile = UserProfile(
                    authUid = uid,
                    displayName = name,
                    email = "",
                    role = "साधक"
                )
                statusMessage = "Profile verified locally!"
                isLoading = false
            }
        ) {
            Text(if (isLoading) "Loading..." else "Verify Profile")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(onClick = onSignOut) {
            Text("Sign Out")
        }
    }
}
