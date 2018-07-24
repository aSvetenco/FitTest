package com.sa.healthtest.model

data class FitResponse(val resourceName: String,
                       var stepCount: Int,
                       val icon: Int,
                       var isConnected: Boolean)