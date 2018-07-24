package com.sa.healthtest.services;

public interface FitConnection {
    void checkPermission();
    void connect();
    void disconnect();
    void retrieveData();
}
