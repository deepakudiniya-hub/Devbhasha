package com.example.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object UserManager {
    private const val TAG = "UserManager"
    private val db get() = FirestoreProvider.get()

    fun initializeOrSyncUser(
        uid: String,
        userName: String,
        phoneNumber: String = "",
        email: String = "",
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val currentAuthUser = FirebaseAuth.getInstance().currentUser
        if (currentAuthUser == null || currentAuthUser.uid != uid) {
            // Local guest or demo session; skip remote Firestore sync to respect security rules
            Log.d(TAG, "Local/guest session (uid=$uid), skipping Firestore remote sync")
            onComplete?.invoke(true)
            return
        }

        val userMap = mapOf(
            "uid" to uid,
            "userName" to userName,
            "phoneNumber" to phoneNumber,
            "email" to email,
            "lastUpdated" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(uid)
            .set(userMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "User synchronized with Firestore: uid=$uid")
                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to sync user with Firestore: ${e.message}")
                onComplete?.invoke(false)
            }
    }

    fun updateLanguagePreference(uid: String, language: String) {
        val currentAuthUser = FirebaseAuth.getInstance().currentUser
        if (currentAuthUser == null || currentAuthUser.uid != uid) {
            Log.d(TAG, "Local/guest session (uid=$uid), skipping Firestore language update")
            return
        }

        db.collection("users").document(uid)
            .update("language", language)
            .addOnSuccessListener { Log.d(TAG, "Language updated in Firestore") }
            .addOnFailureListener { e -> Log.w(TAG, "Failed to update language: ${e.message}") }
    }

    fun updateAvatarPreference(uid: String, avatarId: String) {
        val currentAuthUser = FirebaseAuth.getInstance().currentUser
        if (currentAuthUser == null) {
            Log.d(TAG, "Local session, skipping Firestore avatar update")
            return
        }

        db.collection("users").document(uid)
            .update("avatarId", avatarId)
            .addOnSuccessListener { Log.d(TAG, "Avatar updated in Firestore: $avatarId") }
            .addOnFailureListener { e ->
                // Fallback to set with merge if document doesn't exist yet
                db.collection("users").document(uid)
                    .set(mapOf("avatarId" to avatarId), SetOptions.merge())
            }
    }

    fun updateFullProfile(uid: String, profileData: Map<String, Any>, onComplete: ((Boolean) -> Unit)? = null) {
        val currentAuthUser = FirebaseAuth.getInstance().currentUser
        if (currentAuthUser == null) {
            Log.d(TAG, "Local session, skipping Firestore full profile update")
            onComplete?.invoke(true)
            return
        }

        db.collection("users").document(uid)
            .set(profileData + ("lastUpdated" to com.google.firebase.Timestamp.now()), SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Full profile synchronized with Firestore")
                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to sync full profile: ${e.message}")
                onComplete?.invoke(false)
            }
    }
}
