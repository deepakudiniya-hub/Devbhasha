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

    fun recordConsent(
        uid: String,
        consentVersion: String = Constants.CONSENT_VERSION,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val currentAuthUser = FirebaseAuth.getInstance().currentUser
        if (currentAuthUser == null || currentAuthUser.uid != uid) {
            Log.d(TAG, "Local/guest session (uid=$uid), skipping Firestore consent record")
            onComplete?.invoke(true)
            return
        }

        val consentData = mapOf(
            "consentVersion" to consentVersion,
            "consentAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(uid)
            .set(consentData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Consent recorded in Firestore for uid=$uid")
                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to record consent in Firestore: ${e.message}")
                onComplete?.invoke(false)
            }
    }

    fun withdrawConsent(uid: String, onComplete: ((Boolean) -> Unit)? = null) {
        val currentAuthUser = FirebaseAuth.getInstance().currentUser
        if (currentAuthUser == null || currentAuthUser.uid != uid) {
            Log.d(TAG, "Local/guest session, skipping Firestore consent withdrawal")
            onComplete?.invoke(true)
            return
        }

        val withdrawData = mapOf(
            "consentWithdrawnAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(uid)
            .set(withdrawData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Consent withdrawal recorded in Firestore")
                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to record consent withdrawal: ${e.message}")
                onComplete?.invoke(false)
            }
    }

    fun deleteAccountAndData(uid: String, onResult: (Boolean, String?, Boolean) -> Unit) {
        // onResult(success, errorMessage, requiresReauth)
        val currentAuthUser = FirebaseAuth.getInstance().currentUser
        if (currentAuthUser == null || currentAuthUser.uid != uid) {
            Log.d(TAG, "Guest or local account deletion")
            onResult(true, null, false)
            return
        }

        // 1. Delete Firestore user document
        db.collection("users").document(uid)
            .delete()
            .addOnCompleteListener { firestoreTask ->
                if (!firestoreTask.isSuccessful) {
                    Log.w(TAG, "Firestore user doc deletion warning: ${firestoreTask.exception?.message}")
                }

                // 2. Try deleting Storage files if present
                try {
                    com.google.firebase.storage.FirebaseStorage.getInstance()
                        .reference.child("users/$uid")
                        .delete()
                } catch (e: Exception) {
                    Log.w(TAG, "Storage files deletion ignored/skipped: ${e.message}")
                }

                // 3. Delete Firebase Auth user account
                currentAuthUser.delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Firebase Auth account deleted successfully")
                        onResult(true, null, false)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to delete Auth user: ${e.message}")
                        val isReauthNeeded = e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ||
                                e.message?.contains("CREDENTIAL_TOO_OLD", ignoreCase = true) == true ||
                                e.message?.contains("recent login", ignoreCase = true) == true
                        onResult(false, e.message ?: "Account deletion failed", isReauthNeeded)
                    }
            }
    }
}
