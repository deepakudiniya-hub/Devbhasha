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
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DevbhashaTheme
import com.example.utils.RazorpayPaymentManager
import com.example.utils.UserSession
import com.google.firebase.FirebaseApp
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

enum class AppScreen {
    SPLASH,
    LOGIN,
    HOME
}

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        // Preload Razorpay Checkout
        RazorpayPaymentManager.init(this)

        setContent {
            DevbhashaTheme {
                val context = LocalContext.current
                val userSession = remember { UserSession(context) }

                var currentUserName by remember { mutableStateOf(userSession.getUserName()) }
                var currentUserId by remember { mutableStateOf(userSession.getUserId()) }

                // App starts at Splash, then routes to HOME if logged in, else LOGIN
                var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

                Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    when (screen) {
                        AppScreen.SPLASH -> {
                            SplashScreen(
                                onTimeout = {
                                    val loggedIn = userSession.isLoggedIn()
                                    val uid = userSession.getUserId()
                                    if (loggedIn && uid.isNotBlank()) {
                                        currentUserName = userSession.getUserName()
                                        currentUserId = uid
                                        currentScreen = AppScreen.HOME
                                    } else {
                                        currentScreen = AppScreen.LOGIN
                                    }
                                }
                            )
                        }

                        AppScreen.LOGIN -> {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                LoginScreen(
                                    onLoginSuccess = { uid, name, phone ->
                                        userSession.saveUserSession(
                                            userName = name,
                                            isLoggedIn = true,
                                            userId = uid,
                                            phoneNumber = phone
                                        )
                                        currentUserName = name
                                        currentUserId = uid
                                        currentScreen = AppScreen.HOME
                                    },
                                    onSkip = {
                                        val generatedUid = userSession.getUserId()
                                        userSession.saveUserSession(
                                            userName = "Guest Sadhak",
                                            isLoggedIn = true,
                                            userId = generatedUid,
                                            phoneNumber = ""
                                        )
                                        currentUserName = "Guest Sadhak"
                                        currentUserId = generatedUid
                                        currentScreen = AppScreen.HOME
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        AppScreen.HOME -> {
                            HomeScreen(
                                userName = currentUserName,
                                userId = currentUserId,
                                onLogoutClick = {
                                    userSession.clearSession()
                                    currentUserId = ""
                                    currentUserName = "साधक"
                                    currentScreen = AppScreen.LOGIN
                                }
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
