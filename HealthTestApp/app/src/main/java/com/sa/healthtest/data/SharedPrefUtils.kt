package com.sa.healthtest.data

import android.content.SharedPreferences

fun SharedPreferences.isServiceConnected(flag: String): Boolean = getBoolean(flag, false)
fun SharedPreferences.setServiceConnected(flag: String, isConnected: Boolean) {
    edit().putBoolean(flag, isConnected).apply()
}

