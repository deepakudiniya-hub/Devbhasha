package com.example.ui

import com.example.ui.theme.* 

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ProviderViewModel
import com.example.ui.components.DevSaadhakLogo
import android.util.Log

/**
 * AuthScreen - Real Firebase Phone Authentication screen with OTP dispatch and verification.
 * Persists authenticated user credentials and guards provider dashboard access.
 * 
 * FIXED: Proper state management, null safety, and error handling
 */
@Composable
fun AuthScreen(
    viewModel: ProviderViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val keyboardController = LocalSoftwareKeyboardController.current

    var phoneInput by remember { mutableStateOf(viewModel.currentPhoneNumber) }
    var otpInput by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val isOtpSent by remember { derivedStateOf { viewModel.otpSent } }
    val isLoading by remember { derivedStateOf { viewModel.isAuthLoading } }
    val resendSeconds by remember { derivedStateOf { viewModel.resendTimerSeconds } }
    val serverError by remember { derivedStateOf { viewModel.authErrorMessage } }

    val snackbarHostState = remember { SnackbarHostState() }

    // FIX: Use LaunchedEffect to observe server errors properly
    LaunchedEffect(serverError) {
        if (serverError != null && serverError.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = serverError,
                duration = SnackbarDuration.Long
            )
        }
    }

    // FIX: Ensure activity is available before proceeding
    LaunchedEffect(Unit) {
        if (activity == null) {
            Log.e("AuthScreen", "Activity is null - Firebase Phone Auth will not work!")
            localError = "Critical Error: Activity not available. Please restart the app."
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Logo and Header Branding
                DevSaadhakLogo(size = 64.dp, fontSize = 32.sp, elevation = 4.dp)

                Text(
                    text = "देवसाधक पार्टनर लॉगिन",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "सर्विस पार्टनर पोर्टल (Firebase Phone Auth)",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Step Flow Indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (!isOtpSent) MaterialTheme.colorScheme.primary else BentoGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isOtpSent) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "मोबाइल नंबर",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (!isOtpSent) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Text("➔", color = MaterialTheme.colorScheme.outline)

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isOtpSent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "2",
                                    color = if (isOtpSent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "ओटीपी सत्यापन",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isOtpSent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Error Banner Display (local or Firebase server error)
                val displayError = localError ?: serverError
                if (displayError != null && displayError.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = displayError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Step 1: Phone Number Input Field
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = {
                        val filtered = it.filter { ch -> ch.isDigit() }.take(10)
                        phoneInput = filtered
                        // FIX: Clear errors properly
                        localError = null
                        if (viewModel.authErrorMessage != null) {
                            viewModel.clearAuthError()
                        }
                    },
                    label = { Text("मोबाइल नंबर (10 अंक)") },
                    placeholder = { Text("98765 43210") },
                    prefix = {
                        Text(
                            text = "🇮🇳 +91 ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (isOtpSent) {
                            IconButton(
                                onClick = {
                                    viewModel.resetAuthState()
                                    otpInput = ""
                                    localError = null
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "बदलें", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    enabled = !isOtpSent && !isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            // FIX: Validate before sending OTP
                            if (phoneInput.length == 10 && activity != null) {
                                viewModel.sendPhoneOtp(
                                    activity = activity,
                                    phone = phoneInput,
                                    onSuccess = {
                                        Log.d("AuthScreen", "OTP sent successfully to +91$phoneInput")
                                    },
                                    onError = { error ->
                                        localError = error
                                        Log.e("AuthScreen", "Failed to send OTP: $error")
                                    }
                                )
                            } else {
                                localError = "कृपया 10 अंकों का मान्य मोबाइल नंबर दर्ज करें"
                            }
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_phone_input")
                )

                // Step 2: OTP Verification Field (Visible once OTP is sent)
                AnimatedVisibility(
                    visible = isOtpSent,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEBF2E4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = BentoGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "+91 $phoneInput पर ओटीपी भेजा गया",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BentoGreenDeep,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "बदलें",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        viewModel.resetAuthState()
                                        otpInput = ""
                                        localError = null
                                    }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = {
                                val filtered = it.filter { ch -> ch.isDigit() }.take(6)
                                otpInput = filtered
                                localError = null
                                if (viewModel.authErrorMessage != null) {
                                    viewModel.clearAuthError()
                                }
                            },
                            label = { Text("6 अंकों का ओटीपी (OTP)") },
                            placeholder = { Text("123456") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "OTP",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    if (otpInput.length == 6) {
                                        viewModel.verifyPhoneOtp(
                                            otpCode = otpInput,
                                            onSuccess = {
                                                Log.d("AuthScreen", "OTP verified successfully")
                                                onAuthSuccess()
                                            },
                                            onError = { error ->
                                                localError = error
                                                Log.e("AuthScreen", "OTP verification failed: $error")
                                            }
                                        )
                                    } else {
                                        localError = "कृपया 6 अंकों का ओटीपी कोड दर्ज करें"
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_otp_input")
                        )

                        // Resend Countdown or Trigger Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ओटीपी नहीं मिला?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (resendSeconds > 0) {
                                Text(
                                    text = "पुनः भेजें (${resendSeconds}s)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            } else {
                                TextButton(
                                    onClick = {
                                        // FIX: Proper null check
                                        if (activity != null) {
                                            viewModel.resendOtp(
                                                activity = activity,
                                                onSuccess = {
                                                    otpInput = ""
                                                    localError = null
                                                    Log.d("AuthScreen", "OTP resent successfully")
                                                },
                                                onError = { error ->
                                                    localError = error
                                                    Log.e("AuthScreen", "Failed to resend OTP: $error")
                                                }
                                            )
                                        } else {
                                            localError = "Critical Error: Activity not available"
                                            Log.e("AuthScreen", "Cannot resend OTP - Activity is null")
                                        }
                                    },
                                    enabled = !isLoading
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ओटीपी पुनः भेजें", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Primary Submit Button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        localError = null

                        // FIX: Proper validation flow
                        if (!isOtpSent) {
                            // Step 1: Send OTP
                            if (phoneInput.isEmpty()) {
                                localError = "कृपया मोबाइल नंबर दर्ज करें"
                                return@Button
                            }
                            if (phoneInput.length != 10) {
                                localError = "कृपया 10 अंकों का मान्य मोबाइल नंबर दर्ज करें"
                                return@Button
                            }
                            
                            // FIX: Ensure activity is available before Firebase call
                            if (activity == null) {
                                localError = "Critical Error: Activity not available. Please restart the app."
                                Log.e("AuthScreen", "Cannot send OTP - Activity is null")
                                return@Button
                            }
                            
                            viewModel.sendPhoneOtp(
                                activity = activity,
                                phone = phoneInput,
                                onSuccess = {
                                    Log.d("AuthScreen", "OTP send request successful")
                                },
                                onError = { error ->
                                    localError = error
                                    Log.e("AuthScreen", "OTP send failed: $error")
                                }
                            )
                        } else {
                            // Step 2: Verify OTP
                            if (otpInput.isEmpty()) {
                                localError = "कृपया ओटीपी कोड दर्ज करें"
                                return@Button
                            }
                            if (otpInput.length < 6) {
                                localError = "कृपया 6 अंकों का ओटीपी कोड दर्ज करें"
                                return@Button
                            }
                            
                            viewModel.verifyPhoneOtp(
                                otpCode = otpInput,
                                onSuccess = {
                                    Log.d("AuthScreen", "OTP verification successful - calling onAuthSuccess")
                                    onAuthSuccess()
                                },
                                onError = { error ->
                                    localError = error
                                    Log.e("AuthScreen", "OTP verification failed: $error")
                                }
                            )
                        }
                    },
                    enabled = !isLoading && activity != null,  // FIX: Disable button if activity is null
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800), // Orange brand color
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (!isOtpSent) "ओटीपी भेजा जा रहा है..." else "सत्यापित किया जा रहा है...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = if (!isOtpSent) "ओटीपी प्राप्त करें" else "सत्यापित करें व लॉगिन करें",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Security Note
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "सुरक्षित लॉगिन - Firebase से सुरक्षित",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
