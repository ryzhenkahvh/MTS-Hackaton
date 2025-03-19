package ry.tech.mtc.interfaces;

import java.util.List;

import ry.tech.mtc.models.SensorData;

public interface SensorController {
    SensorData getSensorData(String sensorId);
    List<SensorData> getSensorHistory(String sensorId, long startTime, long endTime);
    void subscribeSensorUpdates(String sensorId, SensorUpdateCallback callback);
}