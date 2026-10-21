package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.ConsentScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DevbhashaTheme
import com.example.utils.RazorpayPaymentManager
import com.example.utils.UserSession
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

enum class AppScreen {
    SPLASH,
    LOGIN,
    CONSENT,
    USER_HOME
}

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        enableEdgeToEdge()
        // Preload Razorpay Checkout
        RazorpayPaymentManager.init(this)

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            DevbhashaTheme(darkTheme = isDarkTheme) {
                val context = LocalContext.current
                val userSession = remember { UserSession(context) }

                var currentUserName by remember { mutableStateOf(userSession.getUserName()) }
                var currentUserId by remember { mutableStateOf(userSession.getUserId()) }

                // App starts at Splash, then routes to user home if logged in, else LOGIN
                var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

                LaunchedEffect(currentScreen) {
                    val bundle = android.os.Bundle()
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, currentScreen.name)
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
                }

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
                                        if (!userSession.hasGivenConsent(currentUserId)) {
                                            currentScreen = AppScreen.CONSENT
                                        } else {
                                            currentScreen = AppScreen.USER_HOME
                                        }
                                    } else if (loggedIn && uid == "guest_explorer") {
                                        // Guest browse mode (no paid features; server rejects unauthenticated calls)
                                        currentUserName = userSession.getUserName()
                                        currentUserId = uid
                                        if (!userSession.hasGivenConsent(currentUserId)) {
                                            currentScreen = AppScreen.CONSENT
                                        } else {
                                            currentScreen = AppScreen.USER_HOME
                                        }
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
                                    userSession.saveUserSession(
                                        userName = name,
                                        isLoggedIn = true,
                                        userId = uid,
                                        phoneNumber = phone
                                    )
                                    currentUserName = name
                                    currentUserId = uid
                                    if (!userSession.hasGivenConsent(uid)) {
                                        currentScreen = AppScreen.CONSENT
                                    } else {
                                        currentScreen = AppScreen.USER_HOME
                                    }
                                },
                                onSkip = { role ->
                                    val defaultName = "Devotee"
                                    val defaultUid = "guest_explorer"
                                    userSession.saveUserSession(
                                        userName = defaultName,
                                        isLoggedIn = true,
                                        userId = defaultUid,
                                        phoneNumber = ""
                                    )
                                    currentUserName = defaultName
                                    currentUserId = defaultUid
                                    if (!userSession.hasGivenConsent(defaultUid)) {
                                        currentScreen = AppScreen.CONSENT
                                    } else {
                                        currentScreen = AppScreen.USER_HOME
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        AppScreen.CONSENT -> {
                            ConsentScreen(
                                userId = currentUserId,
                                userSession = userSession,
                                onConsentAccepted = {
                                    currentScreen = AppScreen.USER_HOME
                                }
                            )
                        }

                        AppScreen.USER_HOME -> {
                            HomeScreen(
                                userName = currentUserName,
                                userId = currentUserId,
                                onLogoutClick = {
                                    FirebaseAuth.getInstance().signOut()
                                    userSession.clearSession()
                                    currentUserId = ""
                                    currentUserName = "Sadhak"
                                    currentScreen = AppScreen.LOGIN
                                },
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { isDarkTheme = it }
                            )
                        }
                    }
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
