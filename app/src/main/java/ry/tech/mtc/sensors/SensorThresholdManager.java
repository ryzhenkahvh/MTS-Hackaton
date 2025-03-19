package ry.tech.mtc.sensors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorThresholdManager {
    private static SensorThresholdManager instance;
    private final Map<String, Map<String, Double>> deviceThresholds;
    private final Map<String, Map<String, Double>> defaultThresholds;

    public enum ThresholdStatus {
        NORMAL,
        WARNING,
        CRITICAL
    }

    private SensorThresholdManager() {
        deviceThresholds = new ConcurrentHashMap<>();
        defaultThresholds = new HashMap<>();
        initializeDefaultThresholds();
    }

    public static synchronized SensorThresholdManager getInstance() {
        if (instance == null) {
            instance = new SensorThresholdManager();
        }
        return instance;
    }

    private void initializeDefaultThresholds() {
        // Температурные датчики
        Map<String, Double> temperatureThresholds = new HashMap<>();
        temperatureThresholds.put("temperature_min", -5.0);
        temperatureThresholds.put("temperature_max", 40.0);
        defaultThresholds.put("temperature_sensor", temperatureThresholds);

        // Датчики влажности
        Map<String, Double> humidityThresholds = new HashMap<>();
        humidityThresholds.put("humidity_min", 30.0);
        humidityThresholds.put("humidity_max", 80.0);
        defaultThresholds.put("humidity_sensor", humidityThresholds);

        // Водяные датчики
        Map<String, Double> waterThresholds = new HashMap<>();
        waterThresholds.put("water_level_min", 0.0);
        waterThresholds.put("water_level_max", 100.0);
        defaultThresholds.put("water_sensor", waterThresholds);

        // Электрические датчики
        Map<String, Double> electricityThresholds = new HashMap<>();
        electricityThresholds.put("power_min", 0.0);
        electricityThresholds.put("power_max", 3500.0);
        electricityThresholds.put("voltage_min", 210.0);
        electricityThresholds.put("voltage_max", 240.0);
        defaultThresholds.put("electricity_sensor", electricityThresholds);

        // Датчики воздуха
        Map<String, Double> airThresholds = new HashMap<>();
        airThresholds.put("co2_min", 400.0);
        airThresholds.put("co2_max", 1500.0);
        airThresholds.put("gas_min", 0.0);
        airThresholds.put("gas_max", 10.0);
        defaultThresholds.put("air_sensor", airThresholds);
    }

    public void setDefaultThresholds(String deviceId, String deviceType) {
        Map<String, Double> defaults = defaultThresholds.get(deviceType);
        if (defaults != null) {
            deviceThresholds.put(deviceId, new HashMap<>(defaults));
        }
    }

    public void setThresholds(String deviceId, Map<String, Double> thresholds) {
        deviceThresholds.put(deviceId, new HashMap<>(thresholds));
    }

    public Map<String, Double> getThresholds(String deviceId) {
        return deviceThresholds.get(deviceId);
    }

    public void updateThreshold(String deviceId, String parameter, double minValue, double maxValue) {
        Map<String, Double> thresholds = deviceThresholds.computeIfAbsent(deviceId, k -> new HashMap<>());
        thresholds.put(parameter + "_min", minValue);
        thresholds.put(parameter + "_max", maxValue);
    }

    public ThresholdStatus checkThresholdStatus(String deviceId, String parameter, double value) {
        Map<String, Double> thresholds = deviceThresholds.get(deviceId);
        if (thresholds == null) return ThresholdStatus.NORMAL;

        Double minThreshold = thresholds.get(parameter + "_min");
        Double maxThreshold = thresholds.get(parameter + "_max");

        if (minThreshold == null || maxThreshold == null) return ThresholdStatus.NORMAL;

        // Вычисление диапазона предупреждения (10% от полного диапазона)
        double range = maxThreshold - minThreshold;
        double warningMargin = range * 0.1;

        if (value < minThreshold || value > maxThreshold) {
            return ThresholdStatus.CRITICAL;
        } else if (value < minThreshold + warningMargin || value > maxThreshold - warningMargin) {
            return ThresholdStatus.WARNING;
        }

        return ThresholdStatus.NORMAL;
    }

    public void resetThresholds(String deviceId) {
        deviceThresholds.remove(deviceId);
    }

    public void clearAllThresholds() {
        deviceThresholds.clear();
    }

    public boolean hasThresholds(String deviceId) {
        return deviceThresholds.containsKey(deviceId);
    }

    public Map<String, Map<String, Double>> getAllDeviceThresholds() {
        return new HashMap<>(deviceThresholds);
    }
}