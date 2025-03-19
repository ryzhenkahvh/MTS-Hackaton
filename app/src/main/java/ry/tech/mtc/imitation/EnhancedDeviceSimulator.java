package ry.tech.mtc.imitation;

import android.os.Handler;
import android.os.Looper;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

import ry.tech.mtc.MockDeviceData;
import ry.tech.mtc.models.Device;
import ry.tech.mtc.sensors.SensorDataProcessor;
import ry.tech.mtc.sensors.SensorThresholdManager;
import ry.tech.mtc.sensors.SensorCalibrationService;
import ry.tech.mtc.sensors.SensorNotificationManager;

public class EnhancedDeviceSimulator {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Map<String, DeviceSimulationData> deviceData = new HashMap<>();
    private static final int UPDATE_INTERVAL = 2000;
    private boolean isRunning = false;

    // Сервисы для работы с датчиками
    private final SensorDataProcessor dataProcessor;
    private final SensorThresholdManager thresholdManager;
    private final SensorCalibrationService calibrationService;
    private final SensorNotificationManager notificationManager;

    public class DeviceSimulationData {
        public double temperature;           // Температура
        public double humidity;             // Влажность
        public double waterLevel;           // Уровень воды
        public double powerConsumption;     // Потребление энергии
        public double voltage;              // Напряжение
        public double current;              // Ток
        public double pressure;             // Давление
        public double co2Level;             // Уровень CO2
        public double gasLevel;             // Уровень газа
        public double lightLevel;           // Уровень освещенности
        public double noiseLevel;           // Уровень шума
        public double uvIndex;              // УФ индекс
        public double windSpeed;            // Скорость ветра
        public double rainIntensity;        // Интенсивность осадков
        public boolean isConnected;         // Статус подключения
        public int signalStrength;          // Уровень сигнала
        public long lastUpdateTime;         // Время последнего обновления
        public Map<String, Object> additionalParams;

        public DeviceSimulationData() {
            // Инициализация с учетом мартовских условий в Беларуси
            this.temperature = 2 + random.nextDouble() * 6;
            this.humidity = 65 + random.nextDouble() * 20;
            this.waterLevel = 70 + random.nextDouble() * 30;
            this.powerConsumption = 800 + random.nextDouble() * 400;
            this.voltage = 220 + random.nextDouble() * 5;
            this.current = powerConsumption / voltage;
            this.pressure = 1013 + random.nextDouble() * 20;
            this.co2Level = 400 + random.nextDouble() * 200;
            this.gasLevel = random.nextDouble() * 5;
            this.lightLevel = 1000 + random.nextDouble() * 500;
            this.noiseLevel = 30 + random.nextDouble() * 20;
            this.uvIndex = random.nextDouble() * 3;
            this.windSpeed = 2 + random.nextDouble() * 6;
            this.rainIntensity = random.nextDouble() * 2;
            this.isConnected = true;
            this.signalStrength = 60 + random.nextInt(40);
            this.lastUpdateTime = System.currentTimeMillis();
            this.additionalParams = new HashMap<>();
        }
    }

    public interface SimulationUpdateListener {
        void onDeviceDataUpdated(String deviceId, DeviceSimulationData data);
        void onDeviceStatusChanged(String deviceId, boolean isConnected);
    }

    private SimulationUpdateListener listener;

    public EnhancedDeviceSimulator() {
        this.dataProcessor = SensorDataProcessor.getInstance();
        this.thresholdManager = SensorThresholdManager.getInstance();
        this.calibrationService = SensorCalibrationService.getInstance();
        this.notificationManager = SensorNotificationManager.getInstance();
    }

    public void setUpdateListener(SimulationUpdateListener listener) {
        this.listener = listener;
    }

    private void updateAllSensorValues(DeviceSimulationData data) {
        data.temperature += (random.nextDouble() - 0.5) * 0.2;
        data.humidity += (random.nextDouble() - 0.5) * 0.5;
        data.waterLevel += (random.nextDouble() - 0.5) * 0.3;
        data.pressure += (random.nextDouble() - 0.5) * 0.4;
        data.co2Level += (random.nextDouble() - 0.5) * 5;
        data.gasLevel += (random.nextDouble() - 0.5) * 0.1;
        data.lightLevel += (random.nextDouble() - 0.5) * 20;
        data.noiseLevel += (random.nextDouble() - 0.5) * 1;
        data.uvIndex += (random.nextDouble() - 0.5) * 0.1;
        data.windSpeed += (random.nextDouble() - 0.5) * 0.3;
        data.rainIntensity += (random.nextDouble() - 0.5) * 0.1;

        // Ограничение значений в реалистичных пределах
        data.temperature = clamp(data.temperature, -5, 8);
        data.humidity = clamp(data.humidity, 65, 85);
        data.waterLevel = clamp(data.waterLevel, 0, 100);
        data.pressure = clamp(data.pressure, 990, 1035);
        data.co2Level = clamp(data.co2Level, 350, 2000);
        data.gasLevel = clamp(data.gasLevel, 0, 10);
        data.lightLevel = clamp(data.lightLevel, 0, 2000);
        data.noiseLevel = clamp(data.noiseLevel, 20, 80);
        data.uvIndex = clamp(data.uvIndex, 0, 5);
        data.windSpeed = clamp(data.windSpeed, 0, 15);
        data.rainIntensity = clamp(data.rainIntensity, 0, 5);
    }

    private void processAndNotify(String deviceId, String sensorType, DeviceSimulationData data) {
        SensorDataProcessor.ProcessedSensorData processedData = new SensorDataProcessor.ProcessedSensorData();
        dataProcessor.processSensorData(deviceId, sensorType, data.temperature);

        if (calibrationService.needsCalibration(deviceId, sensorType)) {
            double calibratedValue = calibrationService.calibrateValue(deviceId, sensorType, data.temperature);
            data.temperature = calibratedValue;
        }

        SensorThresholdManager.ThresholdStatus status = thresholdManager.checkThresholdStatus(
                deviceId, "temperature", data.temperature);

        if (status != SensorThresholdManager.ThresholdStatus.NORMAL) {
            Map<String, Double> thresholds = thresholdManager.getThresholds(deviceId);
            if (thresholds != null) {
                notificationManager.sendAlert(
                        deviceId,
                        sensorType,
                        data.temperature,
                        "°C",
                        thresholds.get("temperature_min"),
                        thresholds.get("temperature_max")
                );
            }
        }
    }

    private void updateSimulation() {
        for (Map.Entry<String, DeviceSimulationData> entry : deviceData.entrySet()) {
            String deviceId = entry.getKey();
            DeviceSimulationData data = entry.getValue();
            Device device = MockDeviceData.getDevice(deviceId);

            if (device != null) {
                if (random.nextDouble() < 0.01) {
                    data.isConnected = !data.isConnected;
                    if (listener != null) {
                        listener.onDeviceStatusChanged(deviceId, data.isConnected);
                    }
                }

                if (data.isConnected) {
                    updateAllSensorValues(data);
                    updateDeviceSpecificParameters(device, data);
                    processAndNotify(deviceId, device.getType(), data);

                    data.signalStrength = (int) clamp(
                            data.signalStrength + (random.nextInt(3) - 1),
                            0, 100);

                    data.lastUpdateTime = System.currentTimeMillis();

                    if (listener != null) {
                        listener.onDeviceDataUpdated(deviceId, data);
                    }
                }
            }
        }
    }

    public void startSimulation() {
        if (!isRunning) {
            isRunning = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        updateSimulation();
                        handler.postDelayed(this, UPDATE_INTERVAL);
                    }
                }
            }, UPDATE_INTERVAL);
        }
    }

    public void stopSimulation() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    public DeviceSimulationData getDeviceData(String deviceId) {
        return deviceData.get(deviceId);
    }

    public void addDevice(String deviceId, String type) {
        DeviceSimulationData data = new DeviceSimulationData();
        deviceData.put(deviceId, data);
        thresholdManager.setDefaultThresholds(deviceId, type);
        SensorNotificationManager.NotificationConfig notificationConfig =
                new SensorNotificationManager.NotificationConfig();
        notificationManager.setNotificationConfig(deviceId, notificationConfig);
    }

    public void removeDevice(String deviceId) {
        deviceData.remove(deviceId);
        calibrationService.resetCalibration(deviceId);
        thresholdManager.resetThresholds(deviceId);
    }

    private double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    private void updateDeviceSpecificParameters(Device device, DeviceSimulationData data) {
        switch (device.getType()) {
            case Device.TYPE_TEMPERATURE_SENSOR:
                updateTemperatureSensorParams(device, data);
                break;
            case Device.TYPE_HUMIDITY_SENSOR:
                updateHumiditySensorParams(device, data);
                break;
            case Device.TYPE_WATER_SENSOR:
                updateWaterSensorParams(device, data);
                break;
            case Device.TYPE_ELECTRICITY_SENSOR:
                updateElectricitySensorParams(device, data);
                break;
            case Device.TYPE_AIR_SENSOR:
                updateAirSensorParams(device, data);
                break;
            case Device.TYPE_LIGHT:
                updateLightParams(device, data);
                break;
            case Device.TYPE_AC:
                updateACParams(device, data);
                break;
        }
        processDeviceData(device, data);
    }

    private void processDeviceData(Device device, DeviceSimulationData data) {
        String deviceId = device.getId();
        String deviceType = device.getType();

        if (calibrationService.needsCalibration(deviceId, deviceType)) {
            switch (deviceType) {
                case Device.TYPE_TEMPERATURE_SENSOR:
                    data.temperature = calibrationService.calibrateValue(deviceId, deviceType, data.temperature);
                    break;
                case Device.TYPE_HUMIDITY_SENSOR:
                    data.humidity = calibrationService.calibrateValue(deviceId, deviceType, data.humidity);
                    break;
                case Device.TYPE_WATER_SENSOR:
                    data.waterLevel = calibrationService.calibrateValue(deviceId, deviceType, data.waterLevel);
                    break;
                case Device.TYPE_ELECTRICITY_SENSOR:
                    data.powerConsumption = calibrationService.calibrateValue(deviceId, deviceType, data.powerConsumption);
                    break;
                case Device.TYPE_AIR_SENSOR:
                    data.co2Level = calibrationService.calibrateValue(deviceId, deviceType, data.co2Level);
                    break;
            }
        }
        checkThresholdsAndNotify(device, data);
    }

    private void checkThresholdsAndNotify(Device device, DeviceSimulationData data) {
        String deviceId = device.getId();
        Map<String, Double> thresholds = thresholdManager.getThresholds(deviceId);

        if (thresholds != null) {
            switch (device.getType()) {
                case Device.TYPE_TEMPERATURE_SENSOR:
                    checkSensorThreshold(deviceId, "temperature", data.temperature, "°C", thresholds);
                    break;
                case Device.TYPE_HUMIDITY_SENSOR:
                    checkSensorThreshold(deviceId, "humidity", data.humidity, "%", thresholds);
                    break;
                case Device.TYPE_WATER_SENSOR:
                    checkSensorThreshold(deviceId, "water_level", data.waterLevel, "cm", thresholds);
                    break;
                case Device.TYPE_ELECTRICITY_SENSOR:
                    checkSensorThreshold(deviceId, "power", data.powerConsumption, "W", thresholds);
                    checkSensorThreshold(deviceId, "voltage", data.voltage, "V", thresholds);
                    break;
                case Device.TYPE_AIR_SENSOR:
                    checkSensorThreshold(deviceId, "co2", data.co2Level, "ppm", thresholds);
                    checkSensorThreshold(deviceId, "gas", data.gasLevel, "ppm", thresholds);
                    break;
            }
        }
    }

    private void checkSensorThreshold(String deviceId, String parameter, double value,String unit, Map<String, Double> thresholds) {
        Double minThreshold = thresholds.get(parameter + "_min");
        Double maxThreshold = thresholds.get(parameter + "_max");

        if (minThreshold != null && maxThreshold != null) {
            if (value < minThreshold || value > maxThreshold) {
                notificationManager.sendAlert(deviceId, parameter, value, unit, minThreshold, maxThreshold);
            }
        }
    }

    private void updateTemperatureSensorParams(Device device, DeviceSimulationData data) {
        data.additionalParams.put("sensorAccuracy", 0.1 + random.nextDouble() * 0.1);
        double batteryLevel = (double) data.additionalParams.getOrDefault("batteryLevel", 100.0);
        batteryLevel -= random.nextDouble() * 0.1;
        data.additionalParams.put("batteryLevel", clamp(batteryLevel, 0, 100));

        double dewPoint = calculateDewPoint(data.temperature, data.humidity);
        double heatIndex = calculateHeatIndex(data.temperature, data.humidity);

        data.additionalParams.put("dewPoint", dewPoint);
        data.additionalParams.put("heatIndex", heatIndex);

        dataProcessor.processSensorData(device.getId(), "temperature", data.temperature);
    }

    private void updateHumiditySensorParams(Device device, DeviceSimulationData data) {
        data.additionalParams.put("absoluteHumidity",
                calculateAbsoluteHumidity(data.temperature, data.humidity));
        data.additionalParams.put("vaporPressure",
                calculateVaporPressure(data.temperature, data.humidity));

        double batteryLevel = (double) data.additionalParams.getOrDefault("batteryLevel", 100.0);
        batteryLevel -= random.nextDouble() * 0.1;
        data.additionalParams.put("batteryLevel", clamp(batteryLevel, 0, 100));

        dataProcessor.processSensorData(device.getId(), "humidity", data.humidity);
    }

    private void updateWaterSensorParams(Device device, DeviceSimulationData data) {
        data.additionalParams.put("flowRate", 2 + random.nextDouble());
        data.additionalParams.put("waterPressure", 1.5 + random.nextDouble() * 0.5);
        data.additionalParams.put("waterTemperature", 8 + random.nextDouble());
        data.additionalParams.put("tds", 150 + random.nextDouble() * 50);
        data.additionalParams.put("turbidity", 0.5 + random.nextDouble());

        dataProcessor.processSensorData(device.getId(), "water", data.waterLevel);
    }

    private void updateElectricitySensorParams(Device device, DeviceSimulationData data) {
        data.additionalParams.put("frequency", 49.9 + random.nextDouble() * 0.2);
        data.additionalParams.put("powerFactor", 0.95 + random.nextDouble() * 0.05);

        double totalEnergy = (double) data.additionalParams.getOrDefault("totalEnergy", 0.0);
        totalEnergy += data.powerConsumption * UPDATE_INTERVAL / 3600000.0;
        data.additionalParams.put("totalEnergy", totalEnergy);

        data.additionalParams.put("harmonicDistortion", 1.5 + random.nextDouble());

        dataProcessor.processSensorData(device.getId(), "electricity", data.powerConsumption);
    }

    private void updateAirSensorParams(Device device, DeviceSimulationData data) {
        data.additionalParams.put("pm1", 5 + random.nextDouble() * 3);
        data.additionalParams.put("pm25", 10 + random.nextDouble() * 5);
        data.additionalParams.put("pm10", 20 + random.nextDouble() * 10);
        data.additionalParams.put("vocLevel", 100 + random.nextDouble() * 50);
        data.additionalParams.put("o3Level", 20 + random.nextDouble() * 10);

        double aqi = calculateAirQualityIndex(data);
        data.additionalParams.put("airQualityIndex", aqi);

        dataProcessor.processSensorData(device.getId(), "air", data.co2Level);
    }

    private void updateLightParams(Device device, DeviceSimulationData data) {
        if ((boolean) data.additionalParams.getOrDefault("isOn", false)) {
            double colorTemp = (double) data.additionalParams.getOrDefault("colorTemperature", 4000.0);
            colorTemp += (random.nextDouble() - 0.5) * 10;
            data.additionalParams.put("colorTemperature", clamp(colorTemp, 2700, 6500));

            data.additionalParams.put("flickerRate", random.nextDouble() * 0.5);
            data.additionalParams.put("lumens", calculateLumens(data.powerConsumption));
        }
    }

    private void updateACParams(Device device, DeviceSimulationData data) {
        if (device.isOn()) {
            double fanSpeed = (double) data.additionalParams.getOrDefault("fanSpeed", 1.0);
            fanSpeed += (random.nextDouble() - 0.5) * 0.1;
            data.additionalParams.put("fanSpeed", clamp(fanSpeed, 0.5, 5.0));

            double filterStatus = (double) data.additionalParams.getOrDefault("filterStatus", 100.0);
            filterStatus -= random.nextDouble() * 0.01;
            data.additionalParams.put("filterStatus", clamp(filterStatus, 0, 100));

            double compressorTemp = (double) data.additionalParams.getOrDefault("compressorTemp", 40.0);
            compressorTemp += (random.nextDouble() - 0.5);
            data.additionalParams.put("compressorTemp", clamp(compressorTemp, 30, 60));

            data.additionalParams.put("efficiency", calculateACEfficiency(data));
        }
    }

    // Вспомогательные методы для расчетов
    private double calculateDewPoint(double temperature, double humidity) {
        double a = 17.27;
        double b = 237.7;
        double alpha = ((a * temperature) / (b + temperature)) + Math.log(humidity / 100.0);
        return (b * alpha) / (a - alpha);
    }

    private double calculateHeatIndex(double temperature, double humidity) {
        double t = temperature * 9/5 + 32; // конвертация в Фаренгейты
        double rh = humidity;
        double heatIndex = 0.5 * (t + 61.0 + ((t - 68.0) * 1.2) + (rh * 0.094));
        return (heatIndex - 32) * 5/9; // конвертация обратно в Цельсии
    }

    private double calculateAbsoluteHumidity(double temperature, double humidity) {
        double es = 6.112 * Math.exp((17.67 * temperature) / (temperature + 243.5));
        double e = (humidity / 100.0) * es;
        return (2.16679 * e) / (273.15 + temperature);
    }

    private double calculateVaporPressure(double temperature, double humidity) {
        return 6.112 * Math.exp((17.67 * temperature) / (temperature + 243.5)) * (humidity / 100.0);
    }

    private double calculateAirQualityIndex(DeviceSimulationData data) {
        double pm25 = (double) data.additionalParams.get("pm25");
        double pm10 = (double) data.additionalParams.get("pm10");
        double co2 = data.co2Level;

        // Упрощенный расчет AQI
        return (pm25 * 0.3 + pm10 * 0.2 + (co2 - 400) * 0.1);
    }

    private double calculateLumens(double powerConsumption) {
        // Примерный расчет для LED ламп
        return powerConsumption * 100;
    }

    private double calculateACEfficiency(DeviceSimulationData data) {
        double compressorTemp = (double) data.additionalParams.get("compressorTemp");
        double filterStatus = (double) data.additionalParams.get("filterStatus");
        return (100 - (compressorTemp - 30) * 2) * (filterStatus / 100);
    }
}