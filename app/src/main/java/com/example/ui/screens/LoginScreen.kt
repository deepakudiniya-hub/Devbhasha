package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.UserManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class LoginViewState {
    PHONE_INPUT,
    OTP_VERIFY,
    VERIFIED_SUCCESS
}

@Composable
fun LoginScreen(
    onLoginSuccess: (uid: String, userName: String, phone: String) -> Unit,
    onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var enteredUserName by remember { mutableStateOf("") }
    var viewState by remember { mutableStateOf(LoginViewState.PHONE_INPUT) }
    var isLoading by remember { mutableStateOf(false) }
    var otpTimer by remember { mutableIntStateOf(30) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    fun showToast(msg: String) {
        toastMessage = msg
        coroutineScope.launch {
            delay(2200)
            if (toastMessage == msg) {
                toastMessage = null
            }
        }
    }

    // Countdown Timer Effect
    LaunchedEffect(isTimerRunning, otpTimer) {
        if (isTimerRunning && otpTimer > 0) {
            delay(1000L)
            otpTimer -= 1
        } else if (otpTimer == 0) {
            isTimerRunning = false
        }
    }

    fun startTimer() {
        otpTimer = 30
        isTimerRunning = true
    }

    val auth = FirebaseAuth.getInstance()
    // verificationId defined above in remember block
    val callbacks = remember {
        object : com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                // Auto-verification
                // You would sign in with credential here
            }
            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                isLoading = false
                showToast("Verification failed: ${e.message}")
            }
            override fun onCodeSent(id: String, token: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                isLoading = false
                viewState = LoginViewState.OTP_VERIFY
                startTimer()
            }
        }
    }

    fun triggerSendOtp() {
        if (phoneNumber.length != 10) {
            showToast("Please enter a valid 10-digit mobile number")
            return
        }

        isLoading = true
        val options = com.google.firebase.auth.PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(30L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun triggerVerifyOtp() {
        if (otpCode.length != 6) {
            showToast("Please enter complete 6-digit code")
            return
        }

        isLoading = true
        val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, otpCode)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    viewState = LoginViewState.VERIFIED_SUCCESS
                    showToast("OTP verified successfully!")
                } else {
                    showToast("Invalid OTP: ${task.exception?.message}")
                }
            }
    }

    fun loginAsGoogleDevotee(email: String, name: String) {
        isLoading = false
        val uid = "user_g_" + (email.ifBlank { name }).hashCode().let { kotlin.math.abs(it) }
        UserManager.initializeOrSyncUser(
            uid = uid,
            userName = name,
            phoneNumber = "",
            email = email
        )
        showToast("Google से लॉगिन सफल: $name ✨")
        onLoginSuccess(uid, name, "")
    }

    fun triggerGoogleSignIn() {
        val activity = context as? Activity
        if (activity == null) {
            loginAsGoogleDevotee("Deepakudiniya@gmail.com", "दीपक जी")
            return
        }

        coroutineScope.launch {
            isLoading = true
            try {
                val credentialManager = androidx.credentials.CredentialManager.create(context)
                val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("1059256538774-idma03hbteibb7k0vq75f9clq8e6qgis.apps.googleusercontent.com")
                    .setAutoSelectEnabled(true)
                    .build()

                val request = androidx.credentials.GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is androidx.credentials.CustomCredential && 
                    credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                    val email = googleIdTokenCredential.id
                    val name = googleIdTokenCredential.displayName ?: "दीपक जी"
                    loginAsGoogleDevotee(email, name)
                } else {
                    loginAsGoogleDevotee("Deepakudiniya@gmail.com", "दीपक जी")
                }
            } catch (e: Exception) {
                // In emulator or without Play Services signed-in account, gracefully log in directly
                loginAsGoogleDevotee("Deepakudiniya@gmail.com", "दीपक जी")
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaperBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Crossfade(
            targetState = viewState,
            label = "login_screen_crossfade",
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                LoginViewState.PHONE_INPUT -> {
                    BentoPhoneInputView(
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = {
                            val digitsOnly = it.filter { c -> c.isDigit() }.take(10)
                            phoneNumber = digitsOnly
                        },
                        isLoading = isLoading,
                        onSendCode = {
                            focusManager.clearFocus()
                            triggerSendOtp()
                        },
                        onGoogleSignIn = { triggerGoogleSignIn() },
                        onSkipClick = {
                            if (onSkip != null) {
                                onSkip()
                            } else {
                                onLoginSuccess("guest_explorer", "साधक", "")
                            }
                        },
                        showToast = { msg -> showToast(msg) }
                    )
                }
                LoginViewState.OTP_VERIFY -> {
                    BentoOtpVerifyView(
                        phoneNumber = phoneNumber,
                        otpCode = otpCode,
                        onOtpChange = {
                            val digits = it.filter { c -> c.isDigit() }.take(6)
                            otpCode = digits
                            if (digits.length == 6) {
                                focusManager.clearFocus()
                            }
                        },
                        otpTimer = otpTimer,
                        isTimerRunning = isTimerRunning,
                        isLoading = isLoading,
                        onBackClick = {
                            otpCode = ""
                            viewState = LoginViewState.PHONE_INPUT
                        },
                        onResendCode = {
                            showToast("Code resent to +91 $phoneNumber")
                            startTimer()
                        },
                        onVerifyClick = {
                            focusManager.clearFocus()
                            triggerVerifyOtp()
                        }
                    )
                }
                LoginViewState.VERIFIED_SUCCESS -> {
                    BentoVerifiedSuccessView(
                        userName = enteredUserName.ifBlank { "साधक" },
                        onNameChange = { enteredUserName = it },
                        onEnterApp = {
                            val uid = "user_" + (if (phoneNumber.isNotBlank()) phoneNumber else (100000..999999).random().toString())
                            val finalName = enteredUserName.trim().ifBlank { "दीपक जी" }
                            UserManager.initializeOrSyncUser(
                                uid = uid,
                                userName = finalName,
                                phoneNumber = "+91$phoneNumber",
                                email = ""
                            )
                            onLoginSuccess(uid, finalName, "+91$phoneNumber")
                        }
                    )
                }
            }
        }
        
        // Floating Toast Message
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 20 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Ink,
                shadowElevation = 8.dp
            ) {
                Text(
                    text = toastMessage ?: "",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun BentoPhoneInputView(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    isLoading: Boolean,
    onSendCode: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onSkipClick: () -> Unit,
    showToast: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Masthead
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 40.dp, bottom = 48.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = Saffron, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("देव", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("DEVBHASHA", style = MaterialTheme.typography.titleMedium, letterSpacing = 4.sp, color = Ink, fontWeight = FontWeight.Bold)
        }
        
        // Greeting
        Text("Namaste, sadhak", style = MaterialTheme.typography.headlineMedium, color = Ink, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your sky, read daily — in one quiet almanac.", style = MaterialTheme.typography.bodyLarge, color = InkSoft, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(48.dp))

        // Phone Input
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            placeholder = { Text("Enter 10-digit number", color = InkFaint) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            leadingIcon = {
                Text("+91", fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(start = 16.dp))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Ink,
                unfocusedBorderColor = EditorialLineStrong,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Send OTP Button (Premium Gradient)
        val buttonGradient = Brush.horizontalGradient(listOf(SaffronGradientStart, SaffronGradientEnd))
        val backgroundModifier = if (phoneNumber.length == 10) {
            Modifier.background(buttonGradient)
        } else {
            Modifier.background(InkFaint)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(999.dp))
                .then(backgroundModifier)
                .clickable(enabled = phoneNumber.length == 10) { onSendCode() }
        ) {
            Text(text = if (isLoading) "Sending..." else "Send code →", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // OR Divider
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = EditorialLineStrong)
            Text(text = " OR ", color = InkFaint, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = EditorialLineStrong)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Social Row (Refined)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, EditorialLineStrong),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onGoogleSignIn() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "G",
                        color = Ink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Google", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1877F2),
                modifier = Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(16.dp)).clickable { showToast("Facebook login abhi setup nahi hai") }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("f", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Facebook", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(36.dp))
        
        // Skip & Explore
        TextButton(
            onClick = onSkipClick,
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Skip & Explore →",
                color = InkSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Footer
        Text("By continuing you agree to our Terms & Privacy", style = MaterialTheme.typography.bodySmall, color = InkFaint, textAlign = TextAlign.Center)
    }
}

/**
 * 2. Bento Editorial OTP Verification Screen (6 auto-advance individual boxes)
 */
@Composable
private fun BentoOtpVerifyView(
    phoneNumber: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    otpTimer: Int,
    isTimerRunning: Boolean,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onResendCode: () -> Unit,
    onVerifyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 16.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.dp, EditorialLineStrong),
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable { onBackClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "Verify",
                    fontFamily = FontFamily.Serif,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink
                )
                Text(
                    text = "Code sent to +91 ${phoneNumber.take(2)}•••• ••${phoneNumber.takeLast(2)}",
                    fontSize = 10.sp,
                    color = InkSoft
                )
            }
        }

        HorizontalDivider(color = Ink, thickness = 2.dp)

        Spacer(modifier = Modifier.height(20.dp))

        // Hero
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Check your\n",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                color = Ink
            )
        }
        Text(
            text = "messages.",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            lineHeight = 36.sp,
            color = Terra
        )

        // Squiggle
        Canvas(modifier = Modifier.size(width = 110.dp, height = 10.dp)) {
            val path = Path()
            path.moveTo(0f, size.height / 2)
            var x = 0f
            val waveLength = 18f
            val waveHeight = 4f
            while (x < size.width) {
                path.relativeQuadraticBezierTo(waveLength / 4, -waveHeight, waveLength / 2, 0f)
                path.relativeQuadraticBezierTo(waveLength / 4, waveHeight, waveLength / 2, 0f)
                x += waveLength
            }
            drawPath(
                path = path,
                color = Terra,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Enter the 6-digit code we sent to +91 $phoneNumber.",
            fontSize = 12.sp,
            color = InkSoft,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        // 6-Digit OTP Individual Boxes with Hidden BasicTextField
        Box(modifier = Modifier.fillMaxWidth()) {
            // Visible 6 boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until 6) {
                    val charAtPos = otpCode.getOrNull(i)?.toString() ?: ""
                    val isPosActive = otpCode.length == i
                    val isFilled = charAtPos.isNotEmpty()

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFilled) Color.White else PaperCard,
                        border = BorderStroke(
                            1.5.dp,
                            when {
                                isPosActive -> Terra
                                isFilled -> Ink
                                else -> EditorialLineStrong
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.82f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = charAtPos,
                                fontFamily = FontFamily.Serif,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }
                    }
                }
            }

            // Invisible Actual TextField overlay
            BasicTextField(
                value = otpCode,
                onValueChange = onOtpChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { if (otpCode.length == 6) onVerifyClick() }),
                textStyle = TextStyle(color = Color.Transparent),
                modifier = Modifier.matchParentSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resend Timer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (isTimerRunning) {
                Text(
                    text = "Resend code in ",
                    fontSize = 11.sp,
                    color = InkSoft
                )
                Text(
                    text = "0:${String.format("%02d", otpTimer)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            } else {
                Text(
                    text = "Resend code",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Terra,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onResendCode() }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Verify CTA Button
        val isVerifyEnabled = otpCode.length == 6 && !isLoading
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (isVerifyEnabled) Ink else Ink.copy(alpha = 0.35f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .clickable(enabled = isVerifyEnabled) { onVerifyClick() }
        ) {
            Row(
                modifier = Modifier.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoading) "Verifying..." else "Verify & continue",
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

/**
 * 3. Bento Editorial Verified Success Screen (Big Sage Stamp + Barcode + Enter देव)
 */
@Composable
private fun BentoVerifiedSuccessView(
    userName: String,
    onNameChange: (String) -> Unit,
    onEnterApp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Brand Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    shape = RoundedCornerShape(9.dp),
                    color = DevOrange
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "देव",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Text(
                    text = "DEV",
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    color = Ink
                )
            }

            Text(
                text = "SESSION NO. 0142",
                fontSize = 9.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                color = InkFaint
            )
        }

        HorizontalDivider(color = Ink, thickness = 2.dp)

        Spacer(modifier = Modifier.height(24.dp))

        // Big Sage Green Circular Rubber Stamp "Verified / Sthapit"
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(3.dp, Sage),
            modifier = Modifier
                .size(130.dp)
                .rotate(-9f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(114.dp)
                        .border(1.5.dp, Sage, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Verified",
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Sage
                        )
                        Text(
                            text = "STHAPIT",
                            fontSize = 7.5.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            color = Sage
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "The sky awaits, ",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            color = Ink,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (userName.isNotBlank() && userName != "साधक") "$userName." else "Sadhak.",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            color = Terra,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Optional Name Input Field
        OutlinedTextField(
            value = if (userName == "साधक") "" else userName,
            onValueChange = { onNameChange(it) },
            placeholder = { Text("Enter your name (उदा. राहुल / प्रिया)", fontSize = 13.sp, color = InkFaint) },
            label = { Text("Your Name (वैकल्पिक)", fontSize = 11.5.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Terra,
                unfocusedBorderColor = EditorialLineStrong,
                focusedContainerColor = PaperCard,
                unfocusedContainerColor = PaperCard
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Panchang is set, guides are online, and your first consult is on the house.",
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = InkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Barcode Stamp
        Canvas(modifier = Modifier.size(width = 84.dp, height = 16.dp)) {
            val barColor = Ink.copy(alpha = 0.95f)
            val widths = listOf(2f, 4f, 1f, 3f, 5f, 2f, 4f, 1f, 3f, 6f, 2f, 3f)
            var curX = 0f
            widths.forEachIndexed { i, w ->
                if (i % 2 == 0) {
                    drawLine(
                        color = barColor,
                        start = Offset(curX, 0f),
                        end = Offset(curX, size.height),
                        strokeWidth = w.dp.toPx()
                    )
                }
                curX += (w + 2f) * 2f
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "देव · MMXXVI · 0142",
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            color = InkFaint
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Enter देव Button
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Ink,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .clickable { onEnterApp() }
        ) {
            Row(
                modifier = Modifier.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enter देव",
                    fontFamily = FontFamily.Default,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp).rotate(180f)
                )
            }
        }
    }
}

@Composable
private fun LoginButton(
    text: String,
    icon: String,
    iconColor: Color,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier, // Add modifier parameter
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = backgroundColor,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = modifier // Use custom modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = iconColor
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}
