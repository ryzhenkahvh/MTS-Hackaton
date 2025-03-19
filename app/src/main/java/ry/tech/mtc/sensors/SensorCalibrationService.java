package ry.tech.mtc.sensors;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

public class SensorCalibrationService {
    private static final String TAG = "SensorCalibrationService";
    private static SensorCalibrationService instance;
    private final Map<String, CalibrationData> calibrationDataMap;
    private final Map<String, List<Double>> calibrationHistory;
    private final Map<String, CalibrationConfig> defaultConfigs;

    public static class CalibrationData {
        public double offset;
        public double multiplier;
        public double referenceValue;
        public long lastCalibrationTime;
        public int calibrationCount;
        public boolean isCalibrated;
        public Map<String, Double> additionalParams;

        public CalibrationData() {
            this.offset = 0.0;
            this.multiplier = 1.0;
            this.referenceValue = 0.0;
            this.lastCalibrationTime = System.currentTimeMillis();
            this.calibrationCount = 0;
            this.isCalibrated = false;
            this.additionalParams = new HashMap<>();
        }
    }

    public static class CalibrationConfig {
        public double defaultOffset;
        public double defaultMultiplier;
        public double minValue;
        public double maxValue;
        public double accuracy;
        public int requiredSamples;
        public long calibrationInterval;
        public String unit;

        public CalibrationConfig(double minValue, double maxValue, double accuracy, String unit) {
            this.defaultOffset = 0.0;
            this.defaultMultiplier = 1.0;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.accuracy = accuracy;
            this.requiredSamples = 10;
            this.calibrationInterval = 24 * 60 * 60 * 1000; // 24 часа
            this.unit = unit;
        }
    }

    private SensorCalibrationService() {
        calibrationDataMap = new ConcurrentHashMap<>();
        calibrationHistory = new ConcurrentHashMap<>();
        defaultConfigs = new HashMap<>();
        initializeDefaultConfigs();
    }

    public static synchronized SensorCalibrationService getInstance() {
        if (instance == null) {
            instance = new SensorCalibrationService();
        }
        return instance;
    }

    private void initializeDefaultConfigs() {
        // Конфигурация для температурных датчиков
        defaultConfigs.put("temperature_sensor",
                new CalibrationConfig(-5, 8, 0.1, "°C"));

        // Конфигурация для датчиков влажности
        defaultConfigs.put("humidity_sensor",
                new CalibrationConfig(65, 85, 1.0, "%"));

        // Конфигурация для датчиков воды
        defaultConfigs.put("water_sensor",
                new CalibrationConfig(0, 100, 0.5, "cm"));

        // Конфигурация для датчиков электричества
        defaultConfigs.put("electricity_sensor",
                new CalibrationConfig(0, 3500, 1.0, "W"));

        // Конфигурация для датчиков воздуха
        defaultConfigs.put("air_sensor",
                new CalibrationConfig(0, 150, 1.0, "AQI"));
    }

    public double calibrateValue(String deviceId, String sensorType, double rawValue) {
        CalibrationData calibData = getCalibrationData(deviceId);
        if (calibData == null || !calibData.isCalibrated) {
            return rawValue;
        }

        // Применение калибровочных коэффициентов
        double calibratedValue = (rawValue + calibData.offset) * calibData.multiplier;

        // Проверка на выход за пределы допустимых значений
        CalibrationConfig config = defaultConfigs.get(sensorType);
        if (config != null) {
            calibratedValue = Math.max(config.minValue, Math.min(config.maxValue, calibratedValue));
        }

        return calibratedValue;
    }

    public void performCalibration(String deviceId, String sensorType, double referenceValue) {
        CalibrationData calibData = calibrationDataMap.computeIfAbsent(
                deviceId, k -> new CalibrationData());
        List<Double> history = calibrationHistory.computeIfAbsent(
                deviceId, k -> new ArrayList<>());

        // Добавление значения в историю калибровки
        history.add(referenceValue);

        // Проверка достаточного количества измерений
        if (history.size() >= getRequiredSamples(sensorType)) {
            calculateCalibrationParameters(deviceId, sensorType, referenceValue);
        }
    }

    private void calculateCalibrationParameters(String deviceId, String sensorType, double referenceValue) {
        List<Double> history = calibrationHistory.get(deviceId);
        if (history == null || history.isEmpty()) return;

        // Расчет среднего значения измерений
        double average = history.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        CalibrationData calibData = calibrationDataMap.get(deviceId);
        if (calibData != null) {
            // Расчет калибровочных коэффициентов
            calibData.offset = referenceValue - average;
            calibData.multiplier = referenceValue / (average + calibData.offset);
            calibData.referenceValue = referenceValue;
            calibData.lastCalibrationTime = System.currentTimeMillis();
            calibData.calibrationCount++;
            calibData.isCalibrated = true;

            // Добавление дополнительных параметров в зависимости от типа датчика
            updateAdditionalParameters(calibData, sensorType);
        }

        // Очистка истории после калибровки
        history.clear();
    }

    private void updateAdditionalParameters(CalibrationData calibData, String sensorType) {
        switch (sensorType) {
            case "temperature_sensor":
                calibData.additionalParams.put("temperatureOffset", calibData.offset);
                calibData.additionalParams.put("temperatureMultiplier", calibData.multiplier);
                break;
            case "humidity_sensor":
                calibData.additionalParams.put("humidityOffset", calibData.offset);
                calibData.additionalParams.put("humidityCompensation",
                        calculateHumidityCompensation(calibData));
                break;
            case "water_sensor":
                calibData.additionalParams.put("pressureCompensation",
                        calculatePressureCompensation(calibData));
                break;
            case "electricity_sensor":
                calibData.additionalParams.put("powerFactor",
                        calculatePowerFactor(calibData));
                break;
            case "air_sensor":
                calibData.additionalParams.put("particleOffset",
                        calculateParticleOffset(calibData));
                break;
        }
    }

    private double calculateHumidityCompensation(CalibrationData calibData) {
        return 1.0 + (calibData.offset / 100.0);
    }

    private double calculatePressureCompensation(CalibrationData calibData) {
        return 1.0 + (calibData.offset / 1000.0);
    }

    private double calculatePowerFactor(CalibrationData calibData) {
        return 0.95 + (calibData.offset / 1000.0);
    }

    private double calculateParticleOffset(CalibrationData calibData) {
        return calibData.offset * 0.1;
    }

    public CalibrationData getCalibrationData(String deviceId) {
        return calibrationDataMap.get(deviceId);
    }

    public void resetCalibration(String deviceId) {
        calibrationDataMap.remove(deviceId);
        calibrationHistory.remove(deviceId);
    }

    public boolean needsCalibration(String deviceId, String sensorType) {
        CalibrationData calibData = calibrationDataMap.get(deviceId);
        if (calibData == null) return true;

        CalibrationConfig config = defaultConfigs.get(sensorType);
        if (config == null) return false;
        long timeSinceLastCalibration = System.currentTimeMillis() - calibData.lastCalibrationTime;
        return timeSinceLastCalibration > config.calibrationInterval;
    }

    public int getRequiredSamples(String sensorType) {
        CalibrationConfig config = defaultConfigs.get(sensorType);
        return config != null ? config.requiredSamples : 10;
    }

    public double getAccuracy(String sensorType) {
        CalibrationConfig config = defaultConfigs.get(sensorType);
        return config != null ? config.accuracy : 1.0;
    }

    public void updateCalibrationConfig(String sensorType, CalibrationConfig config) {
        defaultConfigs.put(sensorType, config);
    }

    public CalibrationConfig getCalibrationConfig(String sensorType) {
        return defaultConfigs.get(sensorType);
    }

    public List<Double> getCalibrationHistory(String deviceId) {
        return calibrationHistory.getOrDefault(deviceId, new ArrayList<>());
    }
}