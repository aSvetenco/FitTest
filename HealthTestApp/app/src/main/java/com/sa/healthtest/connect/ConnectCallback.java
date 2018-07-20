package com.sa.healthtest.connect;

import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;

import java.util.List;

public interface ConnectCallback {
    void checkPermissions(FitnessOptions options);
    void successConnected();
    void setFitData(List<DataSet> data);
    void error();
}
