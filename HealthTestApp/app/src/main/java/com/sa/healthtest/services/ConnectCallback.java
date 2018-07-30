package com.sa.healthtest.services;

import com.sa.healthtest.data.model.FitResponse;

public interface ConnectCallback {
    void successConnected(FitConnection service);
    void disconnected(FitConnection service);
    void updateFitData(FitResponse steps);
    void error(String message);
    void onPermissionDenied(FitConnection service);
}
