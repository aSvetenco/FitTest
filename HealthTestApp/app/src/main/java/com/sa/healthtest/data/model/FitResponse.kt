package com.sa.healthtest.data.model

data class FitResponse(val resourceName: String,
                       var stepCount: Int,
                       val icon: Int,
                       var isConnected: Boolean)