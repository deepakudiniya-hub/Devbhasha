package com.example.utils

import android.content.Context
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreProvider {
    @Volatile
    private var instance: FirebaseFirestore? = null

    fun get(context: Context? = null): FirebaseFirestore {
        return instance ?: synchronized(this) {
            instance ?: run {
                val dbId = try {
                    context?.getString(R.string.firestore_database_id)
                } catch (e: Exception) {
                    null
                }
                val app = FirebaseApp.getInstance()
                val firestore = if (!dbId.isNullOrBlank()) {
                    try {
                        FirebaseFirestore.getInstance(app, dbId)
                    } catch (e: Exception) {
                        FirebaseFirestore.getInstance(app)
                    }
                } else {
                    FirebaseFirestore.getInstance(app)
                }
                instance = firestore
                firestore
            }
        }
    }
}
