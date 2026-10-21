package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SpiritualOrange

@Composable
fun ProfessionalAdvisorCard(
    name: String,
    specialization: String,
    rating: Float,
    fee: String,
    isOnline: Boolean,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box {
                Image(
                    painter = painterResource(id = R.drawable.avatar_01), // Placeholder
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                            .align(Alignment.BottomEnd)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2A2A)
                )
                Text(
                    text = specialization,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFF0F5C5C),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = rating.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹$fee/min",
                        fontSize = 14.sp,
                        color = SpiritualOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // CTA Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onChatClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SpiritualOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Chat", color = Color.White)
                }
                Button(
                    onClick = onCallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SpiritualOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Call", color = Color.White)
                }
            }
        }
    }
}
