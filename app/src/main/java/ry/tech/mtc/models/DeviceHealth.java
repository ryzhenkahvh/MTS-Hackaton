package ry.tech.mtc.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceHealth {
    private String deviceId;
    private String deviceName;
    private String type;
    private double health; // 0-100%
    private int lifespan; // в днях
    private long lastUpdateTime;
    private Map<String, Double> healthFactors;
    private List<HealthEvent> events;

    public static class HealthEvent {
        private String type;
        private String description;
        private long timestamp;

        public HealthEvent(String type, String description) {
            this.type = type;
            this.description = description;
            this.timestamp = System.currentTimeMillis();
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
        public long getTimestamp() { return timestamp; }
    }

    public DeviceHealth(String deviceId, String deviceName, String type,
                        double health, int lifespan, long lastUpdateTime,
                        Map<String, Double> healthFactors) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.type = type;
        this.health = health;
        this.lifespan = lifespan;
        this.lastUpdateTime = lastUpdateTime;
        this.healthFactors = healthFactors;
        this.events = new ArrayList<>();
    }

    public void addEvent(String type, String description) {
        events.add(new HealthEvent(type, description));
    }

    // Геттеры и сеттеры
    public String getDeviceId() { return deviceId; }
    public String getDeviceName() { return deviceName; }
    public String getType() { return type; }
    public double getHealth() { return health; }
    public void setHealth(double health) { this.health = health; }
    public int getLifespan() { return lifespan; }
    public long getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(long time) { this.lastUpdateTime = time; }
    public Map<String, Double> getHealthFactors() { return new HashMap<>(healthFactors); }
    public void setHealthFactors(Map<String, Double> factors) {
        this.healthFactors = new HashMap<>(factors);
    }
    public List<HealthEvent> getEvents() { return new ArrayList<>(events); }
}