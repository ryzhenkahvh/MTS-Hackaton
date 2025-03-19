package ry.tech.mtc.sensors;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class SensorDataProcessor {
    private static final String TAG = "SensorDataProcessor";
    private static SensorDataProcessor instance;
    private final Map<String, ProcessedSensorData> processedDataMap;
    private final SensorThresholdManager thresholdManager;
    private final SensorNotificationManager notificationManager;

    private SensorDataProcessor() {
        this.processedDataMap = new HashMap<>();
        this.thresholdManager = SensorThresholdManager.getInstance();
        this.notificationManager = SensorNotificationManager.getInstance();
    }

    public static synchronized SensorDataProcessor getInstance() {
        if (instance == null) {
            instance = new SensorDataProcessor();
        }
        return instance;
    }

    public static class ProcessedSensorData {
        public double rawValue;
        public double processedValue;
        public double average;
        public double min;
        public double max;
        public long lastUpdateTime;
        public boolean isValid;
        public String unit;
        public Map<String, Double> additionalMetrics;

        public ProcessedSensorData() {
            this.additionalMetrics = new HashMap<>();
            this.lastUpdateTime = System.currentTimeMillis();
            this.isValid = true;
        }
    }

    public void processSensorData(String deviceId, String sensorType, double rawValue) {
        ProcessedSensorData data = processedDataMap.getOrDefault(deviceId, new ProcessedSensorData());

        // Обработка данных в зависимости от типа датчика
        switch (sensorType) {
            case "temperature_sensor":
                processTemperatureData(data, rawValue);
                break;
            case "humidity_sensor":
                processHumidityData(data, rawValue);
                break;
            case "water_sensor":
                processWaterSensorData(data, rawValue);
                break;
            case "electricity_sensor":
                processElectricitySensorData(data, rawValue);
                break;
            case "air_sensor":
                processAirSensorData(data, rawValue);
                break;
        }

        // Проверка пороговых значений
        checkThresholds(deviceId, sensorType, data);

        // Обновление статистики
        updateStatistics(data, rawValue);

        processedDataMap.put(deviceId, data);
    }

    private void processTemperatureData(ProcessedSensorData data, double rawValue) {
        // Калибровка и фильтрация данных температуры
        data.rawValue = rawValue;
        data.processedValue = calibrateTemperature(rawValue);
        data.unit = "°C";

        // Расчет дополнительных метрик
        data.additionalMetrics.put("heatIndex", calculateHeatIndex(rawValue,
                data.additionalMetrics.getOrDefault("humidity", 50.0)));
        data.additionalMetrics.put("dewPoint", calculateDewPoint(rawValue,
                data.additionalMetrics.getOrDefault("humidity", 50.0)));
    }

    private void processHumidityData(ProcessedSensorData data, double rawValue) {
        data.rawValue = rawValue;
        data.processedValue = calibrateHumidity(rawValue);
        data.unit = "%";

        data.additionalMetrics.put("absoluteHumidity",
                calculateAbsoluteHumidity(rawValue, data.additionalMetrics.getOrDefault("temperature", 20.0)));
    }

    private void processWaterSensorData(ProcessedSensorData data, double rawValue) {
        data.rawValue = rawValue;
        data.processedValue = calibrateWaterLevel(rawValue);
        data.unit = "cm";

        data.additionalMetrics.put("pressure", calculateWaterPressure(rawValue));
        data.additionalMetrics.put("flow", calculateWaterFlow(rawValue));
    }

    private void processElectricitySensorData(ProcessedSensorData data, double rawValue) {
        data.rawValue = rawValue;
        data.processedValue = calibrateElectricity(rawValue);
        data.unit = "kWh";

        data.additionalMetrics.put("current", calculateCurrent(rawValue));
        data.additionalMetrics.put("voltage", calculateVoltage(rawValue));
        data.additionalMetrics.put("powerFactor", calculatePowerFactor(rawValue));
    }

    private void processAirSensorData(ProcessedSensorData data, double rawValue) {
        data.rawValue = rawValue;
        data.processedValue = calibrateAirQuality(rawValue);
        data.unit = "AQI";

        data.additionalMetrics.put("pm25", calculatePM25(rawValue));
        data.additionalMetrics.put("pm10", calculatePM10(rawValue));
        data.additionalMetrics.put("co2", calculateCO2(rawValue));
    }

    private void checkThresholds(String deviceId, String sensorType, ProcessedSensorData data) {
        Map<String, Double> thresholds = thresholdManager.getThresholds(deviceId);

        if (thresholds != null) {
            double minThreshold = thresholds.getOrDefault("min", Double.NEGATIVE_INFINITY);
            double maxThreshold = thresholds.getOrDefault("max", Double.POSITIVE_INFINITY);

            if (data.processedValue < minThreshold || data.processedValue > maxThreshold) {
                notificationManager.sendAlert(deviceId, sensorType, data.processedValue,
                        data.unit, minThreshold, maxThreshold);
            }
        }
    }

    private void updateStatistics(ProcessedSensorData data, double newValue) {
        if (data.min == 0 || newValue < data.min) data.min = newValue;
        if (data.max == 0 || newValue > data.max) data.max = newValue;

        // Обновление скользящего среднего
        data.average = (data.average * 9 + newValue) / 10;
        data.lastUpdateTime = System.currentTimeMillis();
    }

    // Методы калибровки
    private double calibrateTemperature(double rawValue) {
        // Применение калибровочной формулы для температуры
        return rawValue * 1.02 - 0.1; // Пример калибровки
    }

    private double calibrateHumidity(double rawValue) {
        return Math.min(100, Math.max(0, rawValue * 1.05)); // Пример калибровки
    }

    private double calibrateWaterLevel(double rawValue) {
        return rawValue * 1.1; // Пример калибровки
    }

    private double calibrateElectricity(double rawValue) {
        return rawValue * 0.98; // Пример калибровки
    }

    private double calibrateAirQuality(double rawValue) {
        return rawValue * 1.15; // Пример калибровки
    }

    // Вспомогательные расчеты
    private double calculateHeatIndex(double temp, double humidity) {
        // Формула расчета индекса тепла
        return -8.78469475556 + 1.61139411 * temp + 2.33854883889 * humidity +
                -0.14611605 * temp * humidity + -0.012308094 * temp * temp +
                -0.0164248277778 * humidity * humidity + 0.002211732 * temp * temp * humidity +
                0.00072546 * temp * humidity * humidity +
                -0.000003582 * temp * temp * humidity * humidity;
    }

    private double calculateDewPoint(double temp, double humidity) {
        // Формула расчета точки росы
        double a = 17.27;
        double b = 237.7;
        double alpha = ((a * temp) / (b + temp)) + Math.log(humidity/100.0);
        return (b * alpha) / (a - alpha);
    }

    private double calculateAbsoluteHumidity(double humidity, double temperature) {
        // Формула расчета абсолютной влажности
        double e = (humidity / 100) * 6.112 * Math.exp((17.67 * temperature) / (temperature + 243.5));
        return (2.167 * e) / (273.15 + temperature);
    }

    private double calculateWaterPressure(double level) {
        // Формула расчета давления воды
        return level * 0.098; // Примерный расчет
    }

    private double calculateWaterFlow(double level) {
        // Формула расчета потока воды
        return Math.sqrt(2 * 9.81 * level); // Примерный расчет
    }

    private double calculateCurrent(double power) {
        // Расчет тока
        return power / 220.0; // Примерный расчет для 220В
    }

    private double calculateVoltage(double power) {
        // Мониторинг напряжения
        return 220.0 + (Math.random() - 0.5) * 10; // Примерные колебания
    }

    private double calculatePowerFactor(double power) {
        // Расчет коэффициента мощности
        return 0.95 + (Math.random() * 0.05); // Примерный расчет
    }

    private double calculatePM25(double aqi) {
        // Расчет PM2.5
        return aqi * 0.4; // Примерный расчет
    }

    private double calculatePM10(double aqi) {
        // Расчет PM10
        return aqi * 0.6; // Примерный расчет
    }

    private double calculateCO2(double aqi) {
        // Расчет CO2
        return 400 + aqi * 2; // Примерный расчет
    }

    public ProcessedSensorData getProcessedData(String deviceId) {
        return processedDataMap.get(deviceId);
    }

    public void clearData(String deviceId) {
        processedDataMap.remove(deviceId);
    }

    public void clearAllData() {
        processedDataMap.clear();
    }
}