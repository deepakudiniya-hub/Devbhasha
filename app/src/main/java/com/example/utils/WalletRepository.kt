package com.example.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions

/**
 * Server-authoritative wallet.
 *
 * The balance lives in Firestore at `users/{uid}.walletBalance` (integer
 * **paise**) and is written ONLY by the Cloud Functions (see functions/index.js).
 * This client never sets a balance — it reads it for display and asks the
 * server to credit, debit or refund.
 */
object WalletRepository {
    private const val TAG = "WalletRepository"

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    private fun paiseToRupees(paise: Long): Double = paise / 100.0
    private fun rupeesToPaise(rupees: Double): Long = Math.round(rupees * 100.0)

    /**
     * Observe the balance (in rupees) for display only. The value is owned by
     * the server; this listener just mirrors it.
     */
    fun observeBalance(uid: String, onChange: (Double) -> Unit): ListenerRegistration {
        return userDoc(uid).addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(TAG, "balance listener error", err)
                return@addSnapshotListener
            }
            onChange(paiseToRupees(snap?.getLong("walletBalance") ?: 0L))
        }
    }

    /**
     * Claim the one-time signup bonus. Idempotent on the server — safe to call
     * on every login; the user is credited at most once.
     */
    fun claimSignupBonus(onResult: (Result<Double>) -> Unit = {}) {
        functions.getHttpsCallable("claimSignupBonus")
            .call()
            .addOnSuccessListener { res ->
                @Suppress("UNCHECKED_CAST")
                val data = res.data as? Map<String, Any>
                val bal = (data?.get("balancePaise") as? Number)?.toLong()
                if (bal != null) onResult(Result.success(paiseToRupees(bal)))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    /** Create a server-side Razorpay order. Returns (orderId, publicKeyId). */
    fun createOrder(
        amountRupees: Double,
        onResult: (Result<Pair<String, String>>) -> Unit
    ) {
        val payload = mapOf("amountPaise" to rupeesToPaise(amountRupees))
        functions.getHttpsCallable("createRazorpayOrder")
            .call(payload)
            .addOnSuccessListener { res ->
                @Suppress("UNCHECKED_CAST")
                val data = res.data as? Map<String, Any>
                val orderId = data?.get("orderId") as? String
                val keyId = data?.get("keyId") as? String
                if (orderId.isNullOrBlank() || keyId.isNullOrBlank()) {
                    onResult(Result.failure(IllegalStateException("Malformed order response")))
                } else {
                    onResult(Result.success(orderId to keyId))
                }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    /** Verify the payment server-side and credit the wallet exactly once. */
    fun verifyAndCredit(
        orderId: String,
        paymentId: String,
        signature: String,
        onResult: (Result<Double>) -> Unit
    ) {
        val payload = mapOf(
            "orderId" to orderId,
            "paymentId" to paymentId,
            "signature" to signature
        )
        functions.getHttpsCallable("verifyRazorpayAndCredit")
            .call(payload)
            .addOnSuccessListener { res ->
                @Suppress("UNCHECKED_CAST")
                val data = res.data as? Map<String, Any>
                val bal = (data?.get("balancePaise") as? Number)?.toLong()
                if (bal == null) {
                    onResult(Result.failure(IllegalStateException("Malformed verify response")))
                } else {
                    onResult(Result.success(paiseToRupees(bal)))
                }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    /**
     * Debit the wallet server-side. `ref` makes the debit idempotent — reuse the
     * same ref to retry safely without double-charging.
     */
    fun spend(
        amountRupees: Double,
        purpose: String,
        ref: String,
        onResult: (Result<Double>) -> Unit
    ) {
        callBalanceFn("walletSpend", amountRupees, purpose, ref, onResult)
    }

    /** Refund / credit the wallet server-side (idempotent by `ref`). */
    fun refund(
        amountRupees: Double,
        purpose: String,
        ref: String,
        onResult: (Result<Double>) -> Unit
    ) {
        callBalanceFn("walletRefund", amountRupees, purpose, ref, onResult)
    }

    private fun callBalanceFn(
        fnName: String,
        amountRupees: Double,
        purpose: String,
        ref: String,
        onResult: (Result<Double>) -> Unit
    ) {
        val payload = mapOf(
            "amountPaise" to rupeesToPaise(amountRupees),
            "purpose" to purpose,
            "ref" to ref
        )
        functions.getHttpsCallable(fnName)
            .call(payload)
            .addOnSuccessListener { res ->
                @Suppress("UNCHECKED_CAST")
                val data = res.data as? Map<String, Any>
                val bal = (data?.get("balancePaise") as? Number)?.toLong()
                if (bal == null) {
                    onResult(Result.failure(IllegalStateException("Malformed $fnName response")))
                } else {
                    onResult(Result.success(paiseToRupees(bal)))
                }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
}
