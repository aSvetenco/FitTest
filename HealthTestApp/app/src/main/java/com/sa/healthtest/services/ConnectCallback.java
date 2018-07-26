package com.sa.healthtest.services;

import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.sa.healthtest.data.model.FitResponse;

import java.util.List;

public interface ConnectCallback {
    void successConnected(FitConnection service);
    void disconnected(FitConnection service);
    void updateFitData(FitResponse steps);
    void error(String message);
}
