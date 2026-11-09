package com.example.utils

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreProvider {
    const val DATABASE_ID = "ai-studio-android-devbhash-c2b59e88-80f5-4209-81ce-859c3cb917d9"
    private val instance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance(FirebaseApp.getInstance(), DATABASE_ID) }
    fun get(context: Context? = null): FirebaseFirestore = instance
}
