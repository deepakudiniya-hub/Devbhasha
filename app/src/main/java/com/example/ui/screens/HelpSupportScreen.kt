package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DevLogoIcon
import com.example.utils.FirestoreProvider
import com.example.utils.UserSession
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
    var emailCopiedNotice by remember { mutableStateOf(false) }

    // Support Form State
    var supportSubject by remember { mutableStateOf("") }
    var supportMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccessMessage by remember { mutableStateOf<String?>(null) }
    var submitErrorMessage by remember { mutableStateOf<String?>(null) }

    val faqs = listOf(
        Pair(
            "1. मेरा व्यक्तिगत डेटा कितना सुरक्षित है? (How secure is my personal data?)",
            "देव भाषा (Dev Bhasha) में आपका डेटा DPDP Act 2023 और उद्योग-मानक सुरक्षा मानकों के अनुरूप एन्क्रिप्टेड रूप में Firebase (Google Cloud) पर सुरक्षित रखा जाता है। डेटा केवल ज्योतिषीय परामर्श और वैध ऐप सेवाओं के लिए उपयोग होता है।"
        ),
        Pair(
            "2. क्या मैं अपनी सहमति (Consent) कभी भी वापस ले सकता हूँ? (Can I withdraw my consent anytime?)",
            "हाँ, आप ऐप की 'प्रोफाइल -> मेरा डेटा / सहमति wapas lein' सेटिंग्स से कभी भी अपनी सहमति वापस ले सकते हैं। सहमति वापस लेने पर आपका खाता और डेटा सुरक्षा नियमों के तहत प्रबंधित किया जाता है।"
        ),
        Pair(
            "3. मेरा खाता और डेटा कैसे डिलीट किया जा सकता है? (How to delete my account and data?)",
            "आप प्रोफाइल स्क्रीन में दिए गए 'Account delete karein' विकल्प से अपना खाता, Firestore डेटा और संबद्ध स्टोरेज फाइलें स्थायी रूप से हटा सकते हैं।"
        ),
        Pair(
            "4. शिकायत निवारण अधिकारी (Grievance Officer) से कैसे संपर्क करें?",
            "डेटा सुरक्षा या गोपनीयता संबंधी किसी भी शिकायत के लिए आप सीधे हमारे ग्रिवांस अधिकारी को support@devbhasha.com पर ईमेल भेज सकते हैं।"
        ),
        Pair(
            "5. डेटा प्रोसेसर कौन हैं? (Who are the data processors?)",
            "हम क्लाउड स्टोरेज और बुनियादी ढांचे के लिए Google Firebase / Google Cloud को अधिकृत डेटा प्रोसेसर के रूप में उपयोग करते हैं जो उच्चतम सुरक्षा का पालन करते हैं।"
        )
    )

    val filteredFaqs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            faqs
        } else {
            faqs.filter {
                it.first.contains(searchQuery, ignoreCase = true) ||
                it.second.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DevLogoIcon(size = 32.dp, showGlow = false)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "सहायता एवं डेटा सुरक्षा FAQ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Support Card & Email Intent
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "देव भाषा सहायता केंद्र (Help & Support)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "सीधे support@devbhasha.com पर संपर्क करें",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:support@devbhasha.com")
                                                putExtra(Intent.EXTRA_SUBJECT, "Dev Bhasha Support / DPDP Query")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("Support Email", "support@devbhasha.com")
                                            clipboard.setPrimaryClip(clip)
                                            emailCopiedNotice = true
                                        }
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "सपोर्ट ईमेल (Support Email)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "support@devbhasha.com",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Text(
                                    text = "ईमेल भेजें ✉️",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (emailCopiedNotice) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ ईमेल पता क्लिपबोर्ड पर कॉपी हो गया: support@devbhasha.com",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Direct Message Form to support@devbhasha.com via Firebase Functions / Firestore Integration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "सपोर्ट फॉर्म (Send to support@devbhasha.com)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Firebase के माध्यम से सीधे हमारी सहायता टीम को संदेश भेजें",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Subject Input
                        OutlinedTextField(
                            value = supportSubject,
                            onValueChange = { supportSubject = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("विषय / Subject (उदा. वॉलेट / डेटा सुरक्षा)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Message Input
                        OutlinedTextField(
                            value = supportMessage,
                            onValueChange = { supportMessage = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            label = { Text("संदेश / Message लिखें...") },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (submitSuccessMessage != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = submitSuccessMessage!!,
                                    modifier = Modifier.padding(10.dp),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (submitErrorMessage != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = submitErrorMessage!!,
                                    modifier = Modifier.padding(10.dp),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Button(
                            onClick = {
                                if (supportSubject.isBlank() || supportMessage.isBlank()) {
                                    submitErrorMessage = "कृपया विषय और संदेश दोनों भरें।"
                                    return@Button
                                }
                                isSubmitting = true
                                submitErrorMessage = null
                                submitSuccessMessage = null

                                coroutineScope.launch {
                                    try {
                                        val db = FirestoreProvider.get(context)
                                        val userId = userSession.getUserId()
                                        val userName = userSession.getUserName()
                                        val ticketData = hashMapOf(
                                            "userId" to userId,
                                            "userName" to userName,
                                            "recipient" to "support@devbhasha.com",
                                            "subject" to supportSubject,
                                            "message" to supportMessage,
                                            "status" to "pending",
                                            "createdAt" to FieldValue.serverTimestamp()
                                        )
                                        db.collection("support_tickets")
                                            .add(ticketData)
                                            .addOnSuccessListener {
                                                isSubmitting = false
                                                val successMsg = "✓ आपका संदेश सफलतापर्वक support@devbhasha.com के लिए दर्ज हो गया है!"
                                                submitSuccessMessage = successMsg
                                                supportSubject = ""
                                                supportMessage = ""
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(successMsg)
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                isSubmitting = false
                                                submitErrorMessage = "संदेश भेजने में Error: ${e.localizedMessage}"
                                            }
                                    } catch (e: Exception) {
                                        isSubmitting = false
                                        submitErrorMessage = "Error: ${e.localizedMessage}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("भेजा जा रहा है...")
                            } else {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("support@devbhasha.com पर भेजें", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // FAQ Section Header & Search Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "डेटा सुरक्षा एवं गोपनीयता FAQ (Data Protection FAQs)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Search Bar for FAQs
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "FAQ में खोजें (जैसे डेटा, सहमति, डिलीट)...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            // FAQs List (Filtered)
            if (filteredFaqs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "कोई संबंधित प्रश्न नहीं मिला (No matching FAQ found)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredFaqs.size) { index ->
                    val (question, answer) = filteredFaqs[index]
                    val isExpanded = expandedFaqIndex == index

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedFaqIndex = if (isExpanded) null else index
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = question,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = answer,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
