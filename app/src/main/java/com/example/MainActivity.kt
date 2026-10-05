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
import com.example.ui.screens.ProviderDashboardScreen
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
    USER_HOME,
    PROVIDER_HOME
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
                var currentUserRole by remember { mutableStateOf(userSession.getUserRole()) }

                // App starts at Splash, then routes to appropriate role dashboard if logged in, else LOGIN
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
                                        currentUserRole = userSession.getUserRole()
                                        currentScreen = if (userSession.isProvider()) AppScreen.PROVIDER_HOME else AppScreen.USER_HOME
                                    } else {
                                        currentScreen = AppScreen.LOGIN
                                    }
                                }
                            )
                        }

                        AppScreen.LOGIN -> {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                LoginScreen(
                                    onLoginSuccess = { uid, name, phone, role ->
                                        userSession.saveUserSessionWithRole(
                                            userName = name,
                                            isLoggedIn = true,
                                            userId = uid,
                                            phoneNumber = phone,
                                            role = role
                                        )
                                        currentUserName = name
                                        currentUserId = uid
                                        currentUserRole = role
                                        currentScreen = if (role.equals("provider", ignoreCase = true)) AppScreen.PROVIDER_HOME else AppScreen.USER_HOME
                                    },
                                    onSkip = { role ->
                                        val defaultName = if (role.equals("provider", ignoreCase = true)) "आचार्य देव शर्मा" else "दीपक जी"
                                        val defaultUid = if (role.equals("provider", ignoreCase = true)) "provider_acharya_dev" else "guest_explorer"
                                        userSession.saveUserSessionWithRole(
                                            userName = defaultName,
                                            isLoggedIn = true,
                                            userId = defaultUid,
                                            phoneNumber = "",
                                            role = role
                                        )
                                        currentUserName = defaultName
                                        currentUserId = defaultUid
                                        currentUserRole = role
                                        currentScreen = if (role.equals("provider", ignoreCase = true)) AppScreen.PROVIDER_HOME else AppScreen.USER_HOME
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        AppScreen.USER_HOME -> {
                            HomeScreen(
                                userName = currentUserName,
                                userId = currentUserId,
                                onLogoutClick = {
                                    userSession.clearSession()
                                    currentUserId = ""
                                    currentUserName = "साधक"
                                    currentUserRole = "user"
                                    currentScreen = AppScreen.LOGIN
                                },
                                onSwitchToProvider = {
                                    userSession.setUserRole("provider")
                                    currentUserRole = "provider"
                                    currentScreen = AppScreen.PROVIDER_HOME
                                }
                            )
                        }

                        AppScreen.PROVIDER_HOME -> {
                            ProviderDashboardScreen(
                                currentSadhakName = currentUserName.ifBlank { "आचार्य देव शर्मा" },
                                providerId = currentUserId.ifBlank { "provider_1" },
                                onSwitchToUserDashboard = {
                                    userSession.setUserRole("user")
                                    currentUserRole = "user"
                                    currentScreen = AppScreen.USER_HOME
                                },
                                onLogoutClick = {
                                    userSession.clearSession()
                                    currentUserId = ""
                                    currentUserName = "साधक"
                                    currentUserRole = "user"
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
