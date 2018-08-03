package com.sa.healthtest.services

import com.sa.healthtest.data.model.FitResponse

interface ConnectCallback {
        fun successConnected(service: FitConnection)
        fun disconnected(service: FitConnection)
        fun updateFitData(steps: FitResponse)
        fun error(message: String?)
        fun onPermissionDenied(service: FitConnection?)
}