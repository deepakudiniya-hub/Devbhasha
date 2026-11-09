package com.example.ui.models

import com.google.firebase.Timestamp

data class AppConfig(
    val maintenanceMode: Boolean = false,
    val minVersionCodeSeeker: Int = 0,
    val announcement: String = "",
    val supportCall: String = "",
    val supportWhatsapp: String = "",
    val supportEmail: String = ""
)

data class Banner(
    val id: String = "",
    val imageUrl: String = "",
    val link: String = "",
    val order: Int = 0,
    val active: Boolean = true,
    val startAt: Timestamp? = null,
    val endAt: Timestamp? = null
)
