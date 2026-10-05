package com.example.utils

import android.app.Activity
import android.content.Context
import android.util.Log
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

data class PaymentRequest(
    val amount: Double,
    val userId: String,
    val userName: String,
    val userPhone: String = "",
    val userEmail: String = "user@bhasha.app",
    val purpose: String = "wallet_recharge",
    val dreamText: String = ""
)

/**
 * Razorpay checkout, tied to a **server-created order**.
 *
 * SECURITY: the wallet balance is never touched here. On success we ask the
 * server to verify the payment signature and credit the wallet
 * ([WalletRepository.verifyAndCredit]); the balance is only updated once the
 * server confirms it. The Razorpay key SECRET never reaches the app.
 *
 * Payment state is now encapsulated per-request to avoid stale state bugs.
 */
object RazorpayPaymentManager {
    private const val TAG = "RazorpayPaymentManager"

    // Current payment request state (holds one active payment at a time)
    private var currentPaymentRequest: PaymentRequest? = null

    private val _paymentEvents = MutableSharedFlow<PaymentResultEvent>(extraBufferCapacity = 5)
    val paymentEvents = _paymentEvents.asSharedFlow()

    fun init(context: Context) {
        // Razorpay loads its WebView on demand when checkout opens.
    }

    /**
     * Launch Razorpay Checkout for a wallet recharge.
     *
     * Asks the server for an order id first, then opens checkout against it.
     * Returns true if checkout was started (order created and sheet opened).
     */
    fun startRechargePayment(
        activity: Activity,
        amount: Double,
        userId: String,
        userName: String,
        userPhone: String = "",
        userEmail: String = "user@bhasha.app",
        purpose: String = "wallet_recharge",
        dreamText: String = ""
    ): Boolean {
        if (amount <= 0 || userId.isBlank()) {
            Toast.makeText(activity, "अमान्य राशि या यूज़र", Toast.LENGTH_SHORT).show()
            return false
        }

        val request = PaymentRequest(
            amount = amount,
            userId = userId,
            userName = userName,
            userPhone = userPhone,
            userEmail = userEmail,
            purpose = purpose,
            dreamText = dreamText
        )

        currentPaymentRequest = request

        // Create the order server-side; open checkout only once we have an id.
        WalletRepository.createOrder(amount) { result ->
            result.onSuccess { (orderId, keyId) ->
                activity.runOnUiThread {
                    openCheckout(activity, orderId, keyId, request, userEmail)
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed to create Razorpay order", e)
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "पेमेंट शुरू नहीं हो सका: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    _paymentEvents.tryEmit(
                        PaymentResultEvent.Error(amount, -1, e.localizedMessage ?: "Order failed")
                    )
                }
            }
        }
        return true
    }

    private fun openCheckout(
        activity: Activity,
        orderId: String,
        keyId: String,
        request: PaymentRequest,
        userEmail: String
    ) {
        val checkout = Checkout()
        checkout.setKeyID(keyId)
        try {
            val options = JSONObject()
            options.put("name", "भाषा (Bhasha)")
            options.put("description", "वॉलेट दक्षिणा रिचार्ज (+₹${request.amount.toInt()})")
            options.put("currency", "INR")
            // Amount is defined by the server order; do not send it from here.
            options.put("order_id", orderId)
            options.put("theme.color", "#E65100")

            val prefill = JSONObject()
            if (request.userPhone.isNotBlank()) prefill.put("contact", request.userPhone)
            if (userEmail.isNotBlank()) prefill.put("email", userEmail)
            options.put("prefill", prefill)

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            checkout.open(activity, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Razorpay checkout: ${e.message}", e)
            Toast.makeText(
                activity,
                "पेमेंट गेटवे प्रारंभ करने में त्रुटि: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Handle the payment success callback from Razorpay.
     * Credits the wallet ONLY after server-side verification.
     */
    fun onPaymentSuccess(
        activity: Context,
        paymentId: String?,
        paymentData: PaymentData?
    ) {
        val request = currentPaymentRequest
        if (request == null) {
            Log.e(TAG, "No active payment request on success callback")
            _paymentEvents.tryEmit(
                PaymentResultEvent.Error(0.0, -5, "Payment state lost")
            )
            return
        }

        val amount = request.amount
        val orderId = paymentData?.orderId
        val pid = paymentId ?: paymentData?.paymentId
        val signature = paymentData?.signature

        if (orderId.isNullOrBlank() || pid.isNullOrBlank() || signature.isNullOrBlank()) {
            // Never credit without a verifiable payment.
            Log.e(TAG, "Missing order/payment/signature — cannot verify")
            _paymentEvents.tryEmit(
                PaymentResultEvent.Error(amount, -2, "Payment could not be verified")
            )
            return
        }

        WalletRepository.verifyAndCredit(orderId, pid, signature) { result ->
            result.onSuccess { newBalance ->
                Log.d(TAG, "Payment verified. txn=$pid newBalance=$newBalance")
                val msg = "₹${amount.toInt()} का भुगतान सफल रहा! वॉलेट बैलेंस अपडेट हो गया।"
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                _paymentEvents.tryEmit(
                    PaymentResultEvent.Success(amount, pid, msg)
                )
                currentPaymentRequest = null // Clear after success
            }.onFailure { e ->
                Log.e(TAG, "Payment verification failed", e)
                val msg = "भुगतान सत्यापन विफल: ${e.localizedMessage}"
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                _paymentEvents.tryEmit(
                    PaymentResultEvent.Error(amount, -3, msg)
                )
            }
        }
    }

    /**
     * Handle payment error/cancellation callback from Razorpay.
     */
    fun onPaymentError(
        activity: Context,
        errorCode: Int,
        response: String?,
        paymentData: PaymentData?
    ) {
        val request = currentPaymentRequest
        val amount = request?.amount ?: 0.0
        val errorMsg = response ?: "उपयोगकर्ता द्वारा भुगतान रद्द किया गया या नेटवर्क समस्या"
        val userFriendlyMsg = "भुगतान विफल रहा (Payment Failed): $errorMsg"
        Toast.makeText(activity, userFriendlyMsg, Toast.LENGTH_LONG).show()
        _paymentEvents.tryEmit(
            PaymentResultEvent.Error(amount, errorCode, userFriendlyMsg)
        )
        currentPaymentRequest = null // Clear on error
    }
}
