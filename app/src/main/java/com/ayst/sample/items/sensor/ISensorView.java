package com.ayst.sample.items.sensor;

public interface ISensorView {
    void updateProximitySensorData(float value);
    void updateAccSensorData(float x, float y, float z);
    void updateGyroSensorData(float x, float y, float z);
    void updateAdcSensorData(float value);
}
