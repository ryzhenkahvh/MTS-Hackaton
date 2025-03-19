package ry.tech.mtc.interfaces;

import ry.tech.mtc.models.SensorData;

public interface SensorUpdateCallback {
    void onSensorUpdate(SensorData data);
}