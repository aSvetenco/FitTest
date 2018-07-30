package com.sa.healthtest.data.model

data class FitResponse(val tagName: String,
                       val resourceName: String? = "",
                       var stepCount: Int = 0,
                       val icon: Int,
                       var isConnected: Boolean)