package com.example

import android.os.Bundle
import android.os.Build
import android.util.Log
import com.example.ui.models.AppConfig
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.security.DeviceIntegrityManager
import com.example.security.RaspManager
import com.example.security.RuntimeSecurityManager
import com.example.security.BiometricSecurityManager
import com.example.ui.screens.ConsentScreen
import com.example.ui.screens.DisclaimerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DevbhashaTheme
import com.example.utils.AnalyticsHelper
import com.example.utils.Constants
import com.example.utils.DeviceUtils
import com.example.utils.RazorpayPaymentManager
import com.example.utils.UserSession
import com.example.utils.ThemePreferencesManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.google.firebase.functions.FirebaseFunctionsException

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppScreen {
    SPLASH,
    LOGIN,
    CONSENT,
    DISCLAIMER,
    USER_HOME
}


class MainActivity : FragmentActivity(), PaymentResultWithDataListener {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        com.google.firebase.messaging.FirebaseMessaging.getInstance().isAutoInitEnabled = false
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        AnalyticsHelper.init(this)

        // Firebase Crashlytics: strictly disabled in debug builds
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

        enableEdgeToEdge()

        // 1. Production Integrity & RASP Verification on startup
        RuntimeSecurityManager.checkSecurity(this)
        DeviceIntegrityManager.evaluateDeviceIntegrity(this)
        RaspManager.evaluateRaspSecurity()

        // Preload Razorpay Checkout
        RazorpayPaymentManager.init(this)

        // Ensure language is fixed to English ("en") every time app opens
        val userSession = UserSession(this)
        userSession.setLanguage("en")
        val startupUid = FirebaseAuth.getInstance().currentUser?.uid ?: userSession.getUserId()
        if (startupUid.isNotBlank() && startupUid != "guest_explorer") {
            crashlytics.setUserId(startupUid)
        }
        val userSessionToken = userSession.getFcmToken()
        val isEmulator = DeviceUtils.isEmulator()
        val isFcmDisabled = userSession.isFcmRegistrationDisabled()

        if (userSessionToken.isBlank() && !isEmulator && !isFcmDisabled) {
            try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val token = task.result
                        val existingToken = userSession.getFcmToken()
                        
                        if (token != existingToken) {
                            userSession.setFcmToken(token)
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: userSession.getUserId()
                            if (uid.isNotBlank()) {
                                com.example.utils.UserManager.updateFcmToken(uid, token)
                            }
                        }
                    } else {
                        val exception = task.exception
                        val msg = exception?.message.orEmpty()
                        if (msg.contains("TOO_MANY_REGISTRATIONS", ignoreCase = true) ||
                            (exception is FirebaseFunctionsException && exception.code == FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED)) {
                            Log.w("MainActivity", "FCM hard failure: Too many registrations. Disabling FCM registration on this device/session.")
                            userSession.setFcmRegistrationDisabled(true)
                        } else {
                            Log.e("MainActivity", "FCM token retrieval failed: $msg")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("MainActivity", "FCM token retrieval error: ${e.message}")
            }
        } else {
            Log.d("MainActivity", "Skipping FCM token sync. isEmulator: $isEmulator, tokenBlank: ${userSessionToken.isBlank()}, isFcmDisabled: $isFcmDisabled")
        }

        setContent {
            val context = LocalContext.current
            val themePrefs = remember { ThemePreferencesManager.getInstance(context) }
            val isDarkTheme by themePrefs.darkModeFlow.collectAsStateWithLifecycle(initialValue = false)

            DevbhashaTheme(darkTheme = isDarkTheme) {
                val userSession = remember { UserSession(context) }

                var currentUserName by remember { mutableStateOf(userSession.getUserName()) }
                var currentUserId by remember { mutableStateOf(userSession.getUserId()) }

                // App starts at Splash, then routes to user home if logged in, else LOGIN
                var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
                
                // Config State
                var appConfig by remember { mutableStateOf<AppConfig?>(null) }
                var showMaintenance by remember { mutableStateOf(false) }
                var showUpdateRequired by remember { mutableStateOf(false) }
                var currentMaintenanceMessage by remember { mutableStateOf("We are upgrading our systems. Please check back shortly.") }

                LaunchedEffect(Unit) {
                    val db = com.example.utils.FirestoreProvider.get(context)
                    db.collection("app_config").document("settings")
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.w("MainActivity", "app_config listen error: ${error.message}")
                                return@addSnapshotListener
                            }
                            if (snapshot != null && snapshot.exists()) {
                                val maintenance = snapshot.getBoolean("maintenanceMode") ?: false
                                val msg = snapshot.getString("maintenanceMessage") ?: "अनुष्ठान जारी है · Under Maintenance"
                                val minVer = snapshot.getLong("minVersionCode_user")?.toInt()
                                    ?: snapshot.getLong("minVersionCodeSeeker")?.toInt() ?: 0

                                showMaintenance = maintenance
                                currentMaintenanceMessage = msg

                                val currentVersion = BuildConfig.VERSION_CODE
                                showUpdateRequired = currentVersion < minVer
                            }
                        }
                }

                LaunchedEffect(currentScreen) {
                    val bundle = android.os.Bundle()
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, currentScreen.name)
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
                    
                    // Handle notification link
                    intent?.getStringExtra("EXTRA_NOTIFICATION_LINK")?.let { link ->
                        if (link.isNotBlank()) {
                            intent.removeExtra("EXTRA_NOTIFICATION_LINK")
                            if (link.startsWith("http")) {
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Failed to open link: $link")
                                }
                            } else {
                                // Internal navigation could go here
                                // For now, we only handle web URLs as requested
                            }
                        }
                    }
                }

                if (showMaintenance) {
                    MaintenanceScreen(currentMaintenanceMessage)
                } else if (showUpdateRequired) {
                    UpdateRequiredScreen(context)
                } else {
                    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    when (screen) {
                        AppScreen.SPLASH -> {
                            SplashScreen(
                                onTimeout = {
                                    // Firebase Auth persists its session on device; trust it, not a prefs flag.
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    val loggedIn = userSession.isLoggedIn()
                                    val uid = userSession.getUserId()
                                    if (firebaseUser != null) {
                                        if (uid != firebaseUser.uid || !loggedIn) {
                                            userSession.saveUserSession(
                                                userName = userSession.getUserName(),
                                                isLoggedIn = true,
                                                userId = firebaseUser.uid,
                                                phoneNumber = firebaseUser.phoneNumber ?: userSession.getPhoneNumber()
                                            )
                                        }
                                        currentUserName = userSession.getUserName()
                                        currentUserId = firebaseUser.uid
                                        
                                        // Check for biometric auth
                                        val storage = com.example.security.EncryptedSessionStorage.getInstance(context)
                                        val isBiometricEnabled = storage.getSecureBoolean("biometric_enabled", false)
                                        if (isBiometricEnabled) {
                                            val activity = context as? androidx.fragment.app.FragmentActivity
                                            if (activity != null) {
                                                com.example.security.BiometricSecurityManager.authenticate(activity) { result ->
                                                    if (result is com.example.security.BiometricSecurityManager.AuthResult.Success) {
                                                        // Proceed
                                                    }
                                                }
                                            }
                                        }

                                        userSession.setGivenConsent(currentUserId, true)
                                        userSession.setAcceptedDisclaimer(currentUserId, Constants.DISCLAIMER_VERSION, true)
                                        currentScreen = AppScreen.USER_HOME
                                    } else {
                                        if (loggedIn) userSession.clearSession()
                                        currentScreen = AppScreen.LOGIN
                                    }
                                }
                            )
                        }

                        AppScreen.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { uid, name, phone, role ->
                                    if (uid.isNotBlank() && uid != "guest_explorer") {
                                        FirebaseCrashlytics.getInstance().setUserId(uid)
                                    }
                                    userSession.saveUserSession(
                                        userName = name,
                                        isLoggedIn = true,
                                        userId = uid,
                                        phoneNumber = phone
                                    )
                                    currentUserName = name
                                    currentUserId = uid
                                    userSession.setGivenConsent(uid, true)
                                    userSession.setAcceptedDisclaimer(uid, Constants.DISCLAIMER_VERSION, true)
                                    currentScreen = AppScreen.USER_HOME
                                },
                                onSkip = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        AppScreen.CONSENT,
                        AppScreen.DISCLAIMER -> {
                            LaunchedEffect(currentUserId) {
                                userSession.setGivenConsent(currentUserId, true)
                                userSession.setAcceptedDisclaimer(currentUserId, Constants.DISCLAIMER_VERSION, true)
                                currentScreen = AppScreen.USER_HOME
                            }
                        }

                        AppScreen.USER_HOME -> {
                            HomeScreen(
                                userName = currentUserName,
                                userId = currentUserId,
                                appConfig = appConfig,
                                onLogoutClick = {
                                    FirebaseAuth.getInstance().signOut()
                                    userSession.clearSession()
                                    currentUserId = ""
                                    currentUserName = "Sadhak"
                                    currentScreen = AppScreen.LOGIN
                                },
                            )
                        }
                    }
                }
                }
            }
        }
    }

    @Composable
    private fun MaintenanceScreen(message: String = "अनुष्ठान जारी है · Under Maintenance") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "अनुष्ठान जारी है · Under Maintenance",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    private fun UpdateRequiredScreen(context: android.content.Context) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "अपडेट आवश्यक है · Update Required",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ऐप का नया वर्जन उपलब्ध है। बेहतर अनुभव के लिए कृपया अपडेट करें।",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("अभी अपडेट करें · Update Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        RazorpayPaymentManager.onPaymentSuccess(
            activity = this,
            paymentId = razorpayPaymentId,
            paymentData = paymentData
        )
    }

    override fun onPaymentError(errorCode: Int, response: String?, paymentData: PaymentData?) {
        RazorpayPaymentManager.onPaymentError(
            activity = this,
            errorCode = errorCode,
            response = response,
            paymentData = paymentData
        )
    }
}
