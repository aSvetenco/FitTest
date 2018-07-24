package com.sa.healthtest.services;

import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;

import java.util.List;

public interface ConnectCallback {
    void checkPermissions(FitnessOptions options);
    void successConnected(FitConnection service);
    void setFitData(int steps);
    void error(String message);
}
