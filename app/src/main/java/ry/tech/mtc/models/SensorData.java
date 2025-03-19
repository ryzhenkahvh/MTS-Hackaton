package ry.tech.mtc.models;

public class SensorData {
    private String sensorId;
    private String type;
    private double value;
    private String unit;
    private long timestamp;

    public SensorData(String sensorId, String type, double value, String unit) {
        this.sensorId = sensorId;
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.timestamp = System.currentTimeMillis();
    }

    // get/set?
}