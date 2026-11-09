package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.automirrored.filled.Backspace
import com.example.R
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.utils.HapticFeedbackHelper
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
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
import com.example.ui.components.DevLogoIcon
import com.example.utils.UserManager
import com.example.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class LoginViewState {
    WALKTHROUGH,
    PHONE_INPUT,
    OTP_VERIFY,
    LANGUAGE_SELECT,
    ZODIAC_SELECT,
    PERSONAL_DETAILS,
    ALL_SET_SUCCESS
}

@Composable
fun LoginScreen(
    onLoginSuccess: (uid: String, userName: String, phone: String, role: String) -> Unit,
    onSkip: ((role: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val userSession = remember { UserSession(context) }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var enteredUserName by remember { mutableStateOf("Devotee") }
    var walkthroughStep by remember { mutableIntStateOf(0) }
    var selectedLanguage by remember { mutableStateOf("en") }
    var selectedZodiac by remember { mutableStateOf("aries") }
    var userGender by remember { mutableStateOf("male") }
    var userDob by remember { mutableStateOf("15/08/1998") }
    var userTob by remember { mutableStateOf("08:30 AM") }
    var userPob by remember { mutableStateOf("Varanasi, Uttar Pradesh") }
    var viewState by remember { mutableStateOf(LoginViewState.PHONE_INPUT) }
    var isOtpSent by remember { mutableStateOf(false) }
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
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }

    // Persistent login: if Firebase already has a signed-in user, never force re-login.
    LaunchedEffect(Unit) {
        val existing = auth.currentUser
        if (existing != null) {
            val name = existing.displayName?.takeIf { it.isNotBlank() } ?: enteredUserName
            onLoginSuccess(existing.uid, name, existing.phoneNumber ?: "", "user")
        }
    }

    fun authErrorMessage(e: Exception?): String = when (e) {
        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid OTP or number. Please check again."
        is com.google.firebase.FirebaseTooManyRequestsException -> "Too many requests. Please try again later."
        is com.google.firebase.FirebaseNetworkException -> "Network error. Check internet connection."
        else -> "Verification failed: " + (e?.localizedMessage ?: "Unknown error")
    }

    fun signInWithPhoneCredential(credential: com.google.firebase.auth.PhoneAuthCredential) {
        isLoading = true
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful && auth.currentUser != null) {
                    val firebaseUser = auth.currentUser!!
                    val uid = firebaseUser.uid
                    val finalName = enteredUserName.trim().ifBlank { "Devotee" }
                    UserManager.initializeOrSyncUser(
                        uid = uid,
                        userName = finalName,
                        phoneNumber = if (phoneNumber.isNotBlank()) "+91$phoneNumber" else (firebaseUser.phoneNumber ?: ""),
                        email = ""
                    )
                    showToast(if (selectedLanguage == "hi") "लॉगिन सफल!" else "Login successful!")
                    onLoginSuccess(uid, finalName, if (phoneNumber.isNotBlank()) "+91$phoneNumber" else (firebaseUser.phoneNumber ?: ""), "user")
                } else {
                    // Never fall through to success on failure.
                    otpCode = ""
                    showToast(authErrorMessage(task.exception))
                }
            }
    }

    fun triggerVerifyOtp() {
        if (otpCode.length != 6 || !otpCode.all { it.isDigit() }) {
            showToast("Please enter the 6-digit code")
            return
        }
        if (verificationId.isBlank()) {
            showToast("कृपया पहले OTP भेजें")
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        signInWithPhoneCredential(credential)
    }

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                // Instant verification / SMS auto-retrieval via Play Services Phone Auth
                credential.smsCode?.let { otpCode = it }
                signInWithPhoneCredential(credential)
            }
            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                isLoading = false
                verificationId = ""
                showToast(authErrorMessage(e))
            }
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                resendToken = token
                isLoading = false
                isOtpSent = true
                startTimer()
            }
        }
    }

    fun startSmsRetrieverUserConsent(activity: Activity) {
        try {
            com.google.android.gms.auth.api.phone.SmsRetriever.getClient(activity)
                .startSmsUserConsent(null)
                .addOnSuccessListener {
                    // SMS User Consent listener active
                }
                .addOnFailureListener {
                    // Fallback to manual entry if SMS Retriever is unavailable
                }
        } catch (e: Exception) {
            // Fallback gracefully
        }
    }

    fun triggerSendOtp(isResend: Boolean = false) {
        if (phoneNumber.length != 10 || !phoneNumber.all { it.isDigit() }) {
            showToast("Please enter a valid 10-digit mobile number")
            return
        }
        val activity = context as? Activity
        if (activity == null) {
            showToast("OTP नहीं भेजा जा सका। ऐप दोबारा खोलें।")
            return
        }

        isLoading = true
        startSmsRetrieverUserConsent(activity)

        try {
            val builder = com.google.firebase.auth.PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91$phoneNumber")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
            val token = resendToken
            if (isResend && token != null) builder.setForceResendingToken(token)
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        } catch (e: Exception) {
            isLoading = false
            showToast(authErrorMessage(e))
        }
    }

    // SMS Retriever BroadcastReceiver for automatic OTP auto-fill & auto-submit
    DisposableEffect(context, isOtpSent) {
        if (!isOtpSent) return@DisposableEffect onDispose {}

        val smsReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == com.google.android.gms.auth.api.phone.SmsRetriever.SMS_RETRIEVED_ACTION) {
                    val extras = intent.extras
                    val status = extras?.get(com.google.android.gms.auth.api.phone.SmsRetriever.EXTRA_STATUS) as? com.google.android.gms.common.api.Status
                    when (status?.statusCode) {
                        com.google.android.gms.common.api.CommonStatusCodes.SUCCESS -> {
                            val message = extras.get(com.google.android.gms.auth.api.phone.SmsRetriever.EXTRA_SMS_MESSAGE) as? String
                            if (!message.isNullOrEmpty()) {
                                val extractedCode = Regex("\\b\\d{6}\\b").find(message)?.value
                                if (!extractedCode.isNullOrEmpty()) {
                                    otpCode = extractedCode
                                    showToast("OTP स्वतः डिटेक्ट हुआ: $extractedCode")
                                }
                            }
                        }
                    }
                }
            }
        }

        val filter = android.content.IntentFilter(com.google.android.gms.auth.api.phone.SmsRetriever.SMS_RETRIEVED_ACTION)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(smsReceiver, filter, android.content.Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(smsReceiver, filter)
            }
        } catch (e: Exception) {
            // Ignore registration issues if receiver already registered
        }

        onDispose {
            try {
                context.unregisterReceiver(smsReceiver)
            } catch (e: Exception) {
                // Ignore unregister issues
            }
        }
    }

    // Auto-verify when full 6-digit OTP code is populated (manual or SMS Retriever)
    LaunchedEffect(otpCode, isOtpSent) {
        if (isOtpSent && otpCode.length == 6 && otpCode.all { it.isDigit() } && !isLoading && verificationId.isNotBlank()) {
            delay(200L)
            triggerVerifyOtp()
        }
    }

    fun completeFirebaseLogin(email: String, name: String) {
        isLoading = false
        val user = auth.currentUser
        if (user == null) {
            showToast("लॉगिन विफल। कृपया दोबारा कोशिश करें।")
            return
        }
        val finalName = name.ifBlank { user.displayName ?: "Sadhak" }
        UserManager.initializeOrSyncUser(
            uid = user.uid,
            userName = finalName,
            phoneNumber = user.phoneNumber ?: "",
            email = email.ifBlank { user.email ?: "" }
        )
        showToast("लॉगिन सफल: $finalName")
        onLoginSuccess(user.uid, finalName, user.phoneNumber ?: "", "user")
    }

    fun triggerGoogleSignIn() {
        val activity = context as? Activity
        if (activity == null) {
            showToast("Google लॉगिन उपलब्ध नहीं है")
            return
        }

        coroutineScope.launch {
            isLoading = true
            try {
                val credentialManager = androidx.credentials.CredentialManager.create(context)
                val clientId = try {
                    context.getString(com.example.R.string.default_web_client_id)
                } catch (e: Exception) {
                    "1059256538774-idma03hbteibb7k0vq75f9clq8e6qgis.apps.googleusercontent.com"
                }
                val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
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
                    val name = googleIdTokenCredential.displayName ?: ""
                    val idToken = googleIdTokenCredential.idToken
                    if (idToken.isNotBlank()) {
                        val firebaseCredential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnSuccessListener { completeFirebaseLogin(email, name) }
                            .addOnFailureListener { e ->
                                isLoading = false
                                showToast(authErrorMessage(e))
                            }
                    } else {
                        isLoading = false
                        showToast("Google लॉगिन विफल")
                    }
                } else {
                    isLoading = false
                    showToast("Google लॉगिन विफल")
                }
            } catch (e: Exception) {
                isLoading = false
                showToast("Google लॉगिन विफल: " + (e.localizedMessage ?: ""))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
            LanguageToggle(
                selectedLanguage = selectedLanguage,
                onLanguageChange = {
                    selectedLanguage = it
                    userSession.setLanguage(it)
                }
            )
        }

        Crossfade(
            targetState = viewState,
            label = "login_screen_crossfade",
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                LoginViewState.WALKTHROUGH,
                LoginViewState.PHONE_INPUT -> {
                    DevbhashaLoginScreen(
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = {
                            phoneNumber = it
                            if (isOtpSent) {
                                isOtpSent = false
                                otpCode = ""
                                verificationId = ""
                            }
                        },
                        onGetOtpClick = {
                            focusManager.clearFocus()
                            if (phoneNumber.length == 10) {
                                triggerSendOtp()
                                viewState = LoginViewState.OTP_VERIFY
                            } else {
                                showToast(if (selectedLanguage == "hi") "कृपया 10-अंकों का मोबाइल नंबर दर्ज करें" else "Please enter a valid 10-digit mobile number")
                            }
                        },
                        onGoogleSignIn = {
                            triggerGoogleSignIn()
                        },
                        isLoading = isLoading,
                        currentLanguage = selectedLanguage,
                        onLanguageToggle = {
                            val newLang = if (selectedLanguage == "hi") "en" else "hi"
                            selectedLanguage = newLang
                            userSession.setLanguage(newLang)
                            showToast(if (newLang == "hi") "भाषा हिंदी में बदली गई" else "Language switched to English")
                        }
                    )
                }

                LoginViewState.OTP_VERIFY -> {
                    OtpVerificationScreen(
                        phoneNumber = phoneNumber,
                        otpCode = otpCode,
                        onOtpChange = {
                            otpCode = it
                            if (it.length == 6) {
                                focusManager.clearFocus()
                                triggerVerifyOtp()
                            }
                        },
                        otpTimer = otpTimer,
                        isTimerRunning = isTimerRunning,
                        onVerifyOtpClick = {
                            focusManager.clearFocus()
                            triggerVerifyOtp()
                        },
                        onResendOtpClick = {
                            triggerSendOtp(isResend = true)
                            showToast(if (selectedLanguage == "hi") "+91 $phoneNumber पर पुनः OTP भेजा गया" else "Code resent to +91 $phoneNumber")
                        },
                        onBackClick = {
                            isOtpSent = false
                            otpCode = ""
                            verificationId = ""
                            viewState = LoginViewState.PHONE_INPUT
                        },
                        onEditNumberClick = {
                            isOtpSent = false
                            otpCode = ""
                            verificationId = ""
                            viewState = LoginViewState.PHONE_INPUT
                        },
                        isLoading = isLoading,
                        currentLanguage = selectedLanguage
                    )
                }

                LoginViewState.LANGUAGE_SELECT -> {
                    AstrotalkLanguageSelectionScreen(
                        selectedLanguage = selectedLanguage,
                        onLanguageSelected = { selectedLanguage = it },
                        onContinue = { viewState = LoginViewState.PERSONAL_DETAILS },
                        onSkip = { viewState = LoginViewState.PERSONAL_DETAILS }
                    )
                }

                LoginViewState.ZODIAC_SELECT -> {
                    AstrotalkZodiacSelectionScreen(
                        selectedZodiac = selectedZodiac,
                        onZodiacSelected = { selectedZodiac = it },
                        onContinue = { viewState = LoginViewState.PERSONAL_DETAILS },
                        onSkip = { viewState = LoginViewState.PERSONAL_DETAILS }
                    )
                }

                LoginViewState.PERSONAL_DETAILS -> {
                    AstrotalkPersonalDetailsScreen(
                        initialName = enteredUserName,
                        onSaveDetails = { name, gender, dob, tob, pob ->
                            enteredUserName = name
                            userGender = gender
                            userDob = dob
                            userTob = tob
                            userPob = pob
                            viewState = LoginViewState.ALL_SET_SUCCESS
                        },
                        onBackClick = { viewState = LoginViewState.LANGUAGE_SELECT }
                    )
                }

                LoginViewState.ALL_SET_SUCCESS -> {
                    AstrotalkAllSetScreen(
                        onLetsGo = {
                            val firebaseUser = auth.currentUser
                            if (firebaseUser == null) {
                                showToast("सत्र समाप्त। कृपया दोबारा लॉगिन करें।")
                                viewState = LoginViewState.PHONE_INPUT
                                return@AstrotalkAllSetScreen
                            }
                            val uid = firebaseUser.uid
                            val finalName = enteredUserName.trim().ifBlank { "Devotee" }
                            UserManager.initializeOrSyncUser(
                                uid = uid,
                                userName = finalName,
                                phoneNumber = if (phoneNumber.isNotBlank()) "+91$phoneNumber" else "",
                                email = ""
                            )
                            onLoginSuccess(uid, finalName, if (phoneNumber.isNotBlank()) "+91$phoneNumber" else "", "user")
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
    showToast: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Masthead: 72.dp circular container with deep saffron spiritual lotus icon, Hindi title and subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
        ) {
            DevLogoIcon(
                size = 72.dp,
                elevation = 4.dp,
                showGlow = false
            )
            Text(
                text = "देवभाषा",
                fontFamily = TiroDevanagariHindi,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = "Devbhasha",
                fontFamily = NotoSansDevanagari,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SaffronDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        // Greeting
        Text(
            text = "नमस्ते, Sadhak",
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "वैदिक Sadhak, स्वप्न विचार एवं सीधा सात्विक समाधान।",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

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
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Send OTP Button (Solid Saffron)
        val isButtonActive = phoneNumber.length == 10
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isButtonActive) MaterialTheme.colorScheme.primary else InkFaint)
                .clickable(enabled = isButtonActive) { onSendCode() }
        ) {
            Text(
                text = if (isLoading) "Sending..." else "OTP प्राप्त करें (Send Code) →",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // OR Divider
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = EditorialLineStrong)
            Text(text = " OR ", color = InkFaint, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 14.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = EditorialLineStrong)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Google Sign-In Button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, EditorialLineStrong),
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onGoogleSignIn() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue with Google",
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
            }
        }
        

        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Footer
        Text("Secure encrypted verification · Zero Spam Guarantee", style = MaterialTheme.typography.bodySmall, color = InkFaint, textAlign = TextAlign.Center)
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
                    fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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

        Spacer(modifier = Modifier.height(16.dp))

        // Top 72x72 dp Dev logo, center aligned
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            DevLogoIcon(
                size = 72.dp,
                elevation = 6.dp,
                showGlow = false
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hero
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Check your\n",
                fontFamily = com.example.ui.theme.ManropeHindFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                color = Ink
            )
        }
        Text(
            text = "messages.",
            fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
                path.relativeQuadraticTo(waveLength / 4, -waveHeight, waveLength / 2, 0f)
                path.relativeQuadraticTo(waveLength / 4, waveHeight, waveLength / 2, 0f)
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

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SaffronContainer,
            border = BorderStroke(1.dp, SaffronPrimary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = PrimaryPeacock
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SMS न पहुँचे तो 30 सेकंड बाद दोबारा भेजें",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPeacock
                )
            }
        }

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
                                fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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

        // Resend Timer/Button Area
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isTimerRunning) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, InkFaint),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Resend in ",
                            fontSize = 12.sp,
                            color = InkFaint
                        )
                        Text(
                            text = "0:${String.format("%02d", otpTimer)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkSoft
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Terra),
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onResendCode() }
                ) {
                    Text(
                        text = "Resend OTP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Terra,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                    )
                }
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
                    fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
// English comment placeholder
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
        // Top Center-Aligned 72x72 dp Dev Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            DevLogoIcon(
                size = 72.dp,
                elevation = 6.dp,
                showGlow = false
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
                            fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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
            fontFamily = com.example.ui.theme.ManropeHindFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            color = Ink,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (userName.isNotBlank() && userName != "Sadhak") "$userName." else "Sadhak.",
            fontFamily = com.example.ui.theme.ManropeHindFontFamily,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            color = Terra,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Optional Name Input Field
        OutlinedTextField(
            value = if (userName == "Sadhak") "" else userName,
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

// English comment placeholder
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
                    fontFamily = com.example.ui.theme.ManropeHindFontFamily,
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

@Composable
fun DevbhashaLoginScreen(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onGetOtpClick: () -> Unit,
    onGoogleSignIn: () -> Unit = {},
    isLoading: Boolean = false,
    currentLanguage: String = "hi",
    onLanguageToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var termsAccepted by remember { mutableStateOf(true) }
    val isHindi = currentLanguage == "hi"
    val haptic = LocalHapticFeedback.current
    val primarySaffron = Color(0xFFFF9800)
    val lightCardBg = Color(0xFFFFFBF0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Language Toggle Pill Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .clickable {
                            HapticFeedbackHelper.playClick(haptic)
                            onLanguageToggle()
                        }
                        .height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = "Language toggle",
                            tint = primarySaffron,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isHindi) "EN" else "अ",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header: App Logo, Welcome Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DevLogoIcon(
                    size = 72.dp,
                    elevation = 3.dp,
                    showGlow = true
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (isHindi) "देवभाषा में आपका स्वागत है" else "Welcome to Devbhasha",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isHindi) "साधकों से जुड़ें" else "Connect with Sadhaks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Mobile Login Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = lightCardBg,
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = if (isHindi) "मोबाइल नंबर" else "Mobile Number",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Country Code Prefix (+91) & Number Input Field
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "India country code",
                                modifier = Modifier.size(20.dp),
                                tint = primarySaffron
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+91",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(Color(0xFFE2E8F0))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = {
                                    if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                                        onPhoneNumberChange(it)
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                ),
                                placeholder = {
                                    Text(
                                        text = if (isHindi) "10-अंकों का मोबाइल नंबर" else "10-digit mobile number",
                                        fontSize = 14.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Terms & Conditions Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAccepted = !termsAccepted }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = primarySaffron,
                                uncheckedColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val consentText = if (isHindi) {
                            buildAnnotatedString {
                                append("मैं ")
                                withStyle(style = SpanStyle(color = primarySaffron, fontWeight = FontWeight.Bold)) {
                                    append("नियम व शर्तें")
                                }
                                append(" से सहमत हूँ")
                            }
                        } else {
                            buildAnnotatedString {
                                append("I agree to ")
                                withStyle(style = SpanStyle(color = primarySaffron, fontWeight = FontWeight.Bold)) {
                                    append("Terms & Conditions")
                                }
                            }
                        }
                        Text(
                            text = consentText,
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Solid Primary Saffron Button: "Get OTP"
                    val isGetOtpEnabled = !isLoading && termsAccepted && phoneNumber.length == 10
                    Button(
                        onClick = {
                            HapticFeedbackHelper.playClick(haptic)
                            onGetOtpClick()
                        },
                        enabled = isGetOtpEnabled,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primarySaffron,
                            contentColor = Color.White,
                            disabledContainerColor = primarySaffron.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = if (isHindi) "ओटीपी प्राप्त करें" else "Get OTP",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign-In Option
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { onGoogleSignIn() }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Google Sign In",
                        tint = primarySaffron,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isHindi) "Google के साथ लॉगिन करें" else "Continue with Google",
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}



@Composable
private fun LanguageToggle(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.8f))
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val languages = listOf("en" to "English", "hi" to "हिंदी")
        languages.forEach { (code, name) ->
            val isSelected = selectedLanguage == code
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { onLanguageChange(code) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable fun AstrotalkLanguageSelectionScreen(selectedLanguage: String, onLanguageSelected: (String) -> Unit, onContinue: () -> Unit, onSkip: () -> Unit) {}
@Composable fun AstrotalkZodiacSelectionScreen(selectedZodiac: String, onZodiacSelected: (String) -> Unit, onContinue: () -> Unit, onSkip: () -> Unit) {}
@Composable fun AstrotalkPersonalDetailsScreen(initialName: String, onSaveDetails: (String, String, String, String, String) -> Unit, onBackClick: () -> Unit) {}
@Composable fun AstrotalkAllSetScreen(onLetsGo: () -> Unit) { LaunchedEffect(Unit) { onLetsGo() } }
