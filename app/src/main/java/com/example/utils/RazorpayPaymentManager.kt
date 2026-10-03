package com.example.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import android.widget.Toast
import com.razorpay.Checkout
import com.razorpay.PaymentData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

sealed class PaymentResultEvent {
    data class Success(
        val amount: Double,
        val transactionId: String,
        val message: String
    ) : PaymentResultEvent()

    data class Error(
        val amount: Double,
        val errorCode: Int,
        val errorMessage: String
    ) : PaymentResultEvent()
}

object RazorpayPaymentManager {
    private const val TAG = "RazorpayPaymentManager"

    // Razorpay API key comes from config (.env / BuildConfig), not hardcoded.
    private val RAZORPAY_KEY = BuildConfig.RAZORPAY_KEY
    private val DEFAULT_RAZORPAY_TEST_KEY = RAZORPAY_KEY

    // Session states for ongoing checkout
    var pendingAmount: Double = 0.0
    var pendingUserId: String = ""
    var pendingUserName: String = ""
    var pendingUserPhone: String = ""
    var pendingPurpose: String = "wallet_recharge"
    var pendingDreamText: String = ""

    private val _paymentEvents = MutableSharedFlow<PaymentResultEvent>(extraBufferCapacity = 5)
    val paymentEvents = _paymentEvents.asSharedFlow()

    fun init(context: Context) {
        // Razorpay automatically loads WebView on-demand when startRechargePayment() is called.
    }

    /**
     * Launch Razorpay Checkout for wallet recharge
     */
    fun startRechargePayment(
        activity: Activity,
        amount: Double,
        userId: String,
        userName: String,
        userPhone: String = "",
        userEmail: String = "user@bhasha.app",
        apiKey: String = DEFAULT_RAZORPAY_TEST_KEY,
        purpose: String = "wallet_recharge",
        dreamText: String = ""
    ): Boolean {
        if (amount <= 0 || userId.isBlank()) {
            Toast.makeText(activity, "अमान्य राशि या यूज़र", Toast.LENGTH_SHORT).show()
            return false
        }

        pendingAmount = amount
        pendingUserId = userId
        pendingUserName = userName
        pendingUserPhone = userPhone
        pendingPurpose = purpose
        pendingDreamText = dreamText

        val checkout = Checkout()
        checkout.setKeyID(apiKey)

        try {
            val options = JSONObject()
            options.put("name", "भाषा (Bhasha)")
            options.put("description", "वॉलेट दक्षिणा रिचार्ज (+₹${amount.toInt()})")
            options.put("currency", "INR")
            // Amount in smallest currency unit (paise)
            options.put("amount", (amount * 100).toLong())
            options.put("theme.color", "#E65100")

            val prefill = JSONObject()
            if (userPhone.isNotBlank()) {
                prefill.put("contact", userPhone)
            }
            if (userEmail.isNotBlank()) {
                prefill.put("email", userEmail)
            }
            options.put("prefill", prefill)

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            checkout.open(activity, options)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Razorpay checkout: ${e.message}", e)
            Toast.makeText(activity, "पेमेंट गेटवे प्रारंभ करने में त्रुटि: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    /**
     * Handle payment success callback from Razorpay
     */
    fun onPaymentSuccess(
        activity: Context,
        paymentId: String?,
        paymentData: PaymentData?
    ) {
        val amount = pendingAmount
        val userId = pendingUserId
        val txnId = paymentId ?: paymentData?.paymentId ?: "RZP_${System.currentTimeMillis()}"

        Log.d(TAG, "Payment Success: id=$txnId, amount=$amount, user=$userId")

        // Update local wallet balance in UserSession
        val userSession = UserSession(activity)
        val currentBalance = userSession.getWalletBalance()
        userSession.setWalletBalance(currentBalance + amount)

        val successMsg = "₹${amount.toInt()} का भुगतान सफल रहा! वॉलेट बैलेंस अपडेट हो गया।"
        Toast.makeText(activity, successMsg, Toast.LENGTH_LONG).show()

        _paymentEvents.tryEmit(
            PaymentResultEvent.Success(
                amount = amount,
                transactionId = txnId,
                message = successMsg
            )
        )
    }

    /**
     * Handle payment error/cancellation callback from Razorpay
     */
    fun onPaymentError(
        activity: Context,
        errorCode: Int,
        response: String?,
        paymentData: PaymentData?
    ) {
        val amount = pendingAmount
        val errorMsg = response ?: "उपयोगकर्ता द्वारा भुगतान रद्द किया गया या नेटवर्क समस्या"

        val userFriendlyMsg = "भुगतान विफल रहा (Payment Failed): $errorMsg"
        Toast.makeText(activity, userFriendlyMsg, Toast.LENGTH_LONG).show()

        _paymentEvents.tryEmit(
            PaymentResultEvent.Error(
                amount = amount,
                errorCode = errorCode,
                errorMessage = userFriendlyMsg
            )
        )
    }
}
