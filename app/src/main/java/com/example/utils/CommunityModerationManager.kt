package com.example.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object CommunityModerationManager {

    private const val PREFS_NAME = "community_moderation_prefs"
    private const val KEY_REPORTED_ITEMS = "reported_items"

    val REPORT_REASONS = listOf("Spam", "Abusive", "Self-harm", "Other")

    fun isItemReported(context: Context, targetId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val reported = prefs.getStringSet(KEY_REPORTED_ITEMS, emptySet()) ?: emptySet()
        return reported.contains(targetId)
    }

    private fun markItemReported(context: Context, targetId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_REPORTED_ITEMS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(targetId)
        prefs.edit().putStringSet(KEY_REPORTED_ITEMS, current).apply()
    }

    fun reportContent(
        context: Context,
        targetType: String,
        targetId: String,
        ownerId: String,
        reason: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            Toast.makeText(context, "Please log in to report content", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        if (isItemReported(context, targetId)) {
            Toast.makeText(context, "You have already reported this item", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        val report = hashMapOf(
            "reporterId" to currentUid,
            "targetType" to targetType,
            "targetId" to targetId,
            "ownerId" to ownerId,
            "reason" to reason.take(200),
            "createdAt" to FieldValue.serverTimestamp()
        )

        com.example.utils.FirestoreProvider.get().collection("content_reports")
            .add(report)
            .addOnSuccessListener {
                markItemReported(context, targetId)
                Toast.makeText(context, "Thank you for reporting. Our moderation team will review this.", Toast.LENGTH_LONG).show()
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Could not submit report: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
    }

    fun blockUser(
        context: Context,
        blockedId: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            Toast.makeText(context, "Please log in to block a user", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }
        if (blockedId.isBlank() || blockedId == currentUid) {
            onComplete(false)
            return
        }

        val data = hashMapOf(
            "blockedId" to blockedId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        com.example.utils.FirestoreProvider.get().collection("users")
            .document(currentUid)
            .collection("blocked")
            .document(blockedId)
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(context, "User blocked. Content from this user is now hidden.", Toast.LENGTH_SHORT).show()
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to block user: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
    }

    fun observeBlockedUsers(currentUid: String): Flow<Set<String>> = callbackFlow {
        if (currentUid.isBlank()) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }

        val listener = com.example.utils.FirestoreProvider.get().collection("users")
            .document(currentUid)
            .collection("blocked")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptySet())
                    return@addSnapshotListener
                }
                val set = snapshot?.documents?.mapNotNull { it.getString("blockedId") ?: it.id }?.toSet() ?: emptySet()
                trySend(set)
            }

        awaitClose { listener.remove() }
    }
}
