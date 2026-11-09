package com.example.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun formatTimestamp(timestamp: Long): String {
        return formatter.format(Date(timestamp))
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
}
