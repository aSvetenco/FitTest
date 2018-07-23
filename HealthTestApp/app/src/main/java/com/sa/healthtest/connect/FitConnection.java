package com.sa.healthtest.connect;

public interface FitConnection {
    void checkPermission();
    boolean isConnected();
    void connect();
    void disconnect();
    void recordData();
    void retrieveData();
}
