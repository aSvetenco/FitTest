package com.sa.healthtest.data

import android.content.SharedPreferences

fun SharedPreferences.isServiceConnected(flag: String) = getBoolean(flag, false)
fun SharedPreferences.setServiceConnected(flag: String, isConnected: Boolean) {
    edit().putBoolean(flag, isConnected).apply()
}

