// ISensorReadingService.aidl
package com.example.sensorreading;

// Declare any non-default types here with import statements

interface ISensorReadingService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void readSensorData(int period, int accThreshold);
}