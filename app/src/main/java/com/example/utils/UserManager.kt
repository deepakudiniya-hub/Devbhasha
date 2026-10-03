package com.example.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object UserManager {
    private const val TAG = "UserManager"
    private val db = FirebaseFirestore.getInstance()

    fun initializeOrSyncUser(
        uid: String,
        userName: String,
        phoneNumber: String = "",
        email: String = "",
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val userMap = mapOf(
            "uid" to uid,
            "userName" to userName,
            "phoneNumber" to phoneNumber,
            "email" to email,
            "lastUpdated" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(uid)
            .set(userMap, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "User synchronized with Firestore: uid=$uid")
                onComplete?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync user with Firestore", e)
                onComplete?.invoke(false)
            }
    }

    fun updateLanguagePreference(uid: String, language: String) {
        if (uid.isBlank()) return
        // FIX: update() fails when the user document does not exist yet (e.g. a
        // brand-new / guest user who only changed the language). set(merge)
        // creates-or-updates, so the preference is never silently lost.
        db.collection("users").document(uid)
            .set(
                mapOf("language" to language),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .addOnSuccessListener { Log.d(TAG, "Language updated in Firestore") }
            .addOnFailureListener { e -> Log.e(TAG, "Failed to update language", e) }
    }
}
