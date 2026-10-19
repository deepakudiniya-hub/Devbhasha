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
                val app = FirebaseApp.getInstance()
                val ctx = context ?: try { app.applicationContext } catch (e: Exception) { null }
                val rawDbId = try {
                    ctx?.getString(R.string.firestore_database_id)
                } catch (e: Exception) {
                    null
                }
                val dbId = rawDbId?.trim()?.takeIf { it.isNotEmpty() && it != "default" && it != "(default)" }
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
