package ry.tech.mtc;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import ry.tech.mtc.models.Device;
import ry.tech.mtc.sensors.SensorDataProcessor;
import ry.tech.mtc.sensors.SensorThresholdManager;
import ry.tech.mtc.sensors.SensorCalibrationService;
import ry.tech.mtc.sensors.SensorNotificationManager;

public class MockDeviceData {
    private static final List<Device> devices = new ArrayList<>();
    private static final Map<String, DeviceState> deviceStates = new HashMap<>();
    private static final SensorDataProcessor dataProcessor = SensorDataProcessor.getInstance();
    private static final SensorThresholdManager thresholdManager = SensorThresholdManager.getInstance();
    private static final SensorCalibrationService calibrationService = SensorCalibrationService.getInstance();
    private static final SensorNotificationManager notificationManager = SensorNotificationManager.getInstance();

    public static class DeviceState {
        public boolean isOnline;
        public boolean isOn;
        public Map<String, Object> lastReadings;
        public long lastUpdateTime;

        public DeviceState() {
            this.isOnline = true;
            this.isOn = false;
            this.lastReadings = new HashMap<>();
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }

    static {
        // Инициализация устройств с настройкой их параметров и пороговых значений
        initializeDevice("1", "Умная лампа", Device.TYPE_LIGHT);
        initializeDevice("2", "Кондиционер", Device.TYPE_AC);
        initializeDevice("3", "Датчик температуры", Device.TYPE_TEMPERATURE_SENSOR);
        initializeDevice("4", "Датчик влажности", Device.TYPE_HUMIDITY_SENSOR);
        initializeDevice("5", "Датчик воды", Device.TYPE_WATER_SENSOR);
        initializeDevice("6", "Датчик электричества", Device.TYPE_ELECTRICITY_SENSOR);
        initializeDevice("7", "Датчик воздуха", Device.TYPE_AIR_SENSOR);
    }

    private static void initializeDevice(String id, String name, String type) {
        Device device = new Device(id, name, type);
        devices.add(device);
        deviceStates.put(id, new DeviceState());

        // Настройка пороговых значений для датчиков
        setupDeviceThresholds(device);

        // Инициализация конфигурации уведомлений
        setupNotificationConfig(device);

        // Инициализация калибровочных данных
        setupCalibrationData(device);
    }

    private static void setupDeviceThresholds(Device device) {
        Map<String, Double> thresholds = new HashMap<>();
        switch (device.getType()) {
            case Device.TYPE_TEMPERATURE_SENSOR:
                thresholds.put("temperature_min", -5.0);
                thresholds.put("temperature_max", 40.0);
                break;
            case Device.TYPE_HUMIDITY_SENSOR:
                thresholds.put("humidity_min", 30.0);
                thresholds.put("humidity_max", 80.0);
                break;
            case Device.TYPE_WATER_SENSOR:
                thresholds.put("water_level_min", 0.0);
                thresholds.put("water_level_max", 100.0);
                break;
            case Device.TYPE_ELECTRICITY_SENSOR:
                thresholds.put("power_min", 0.0);
                thresholds.put("power_max", 3500.0);
                thresholds.put("voltage_min", 210.0);
                thresholds.put("voltage_max", 240.0);
                break;
            case Device.TYPE_AIR_SENSOR:
                thresholds.put("co2_min", 400.0);
                thresholds.put("co2_max", 1500.0);
                thresholds.put("gas_min", 0.0);
                thresholds.put("gas_max", 10.0);
                break;
        }
        thresholdManager.setThresholds(device.getId(), thresholds);
    }

    private static void setupNotificationConfig(Device device) {
        SensorNotificationManager.NotificationConfig config =
                new SensorNotificationManager.NotificationConfig();

        // Настройка специфических параметров уведомлений для разных типов устройств
        switch (device.getType()) {
            case Device.TYPE_TEMPERATURE_SENSOR:
            case Device.TYPE_HUMIDITY_SENSOR:
                config.priority = 2;
                config.cooldownPeriod = 5 * 60 * 1000; // 5 минут
                break;
            case Device.TYPE_WATER_SENSOR:
            case Device.TYPE_ELECTRICITY_SENSOR:
                config.priority = 1;
                config.cooldownPeriod = 1 * 60 * 1000; // 1 минута
                break;
            case Device.TYPE_AIR_SENSOR:
                config.priority = 2;
                config.cooldownPeriod = 10 * 60 * 1000; // 10 минут
                break;
        }

        notificationManager.setNotificationConfig(device.getId(), config);
    }

    private static void setupCalibrationData(Device device) {
        SensorCalibrationService.CalibrationConfig config = null;
        switch (device.getType()) {
            case Device.TYPE_TEMPERATURE_SENSOR:
                config = new SensorCalibrationService.CalibrationConfig(-5, 40, 0.1, "°C");
                break;
            case Device.TYPE_HUMIDITY_SENSOR:
                config = new SensorCalibrationService.CalibrationConfig(30, 80, 1.0, "%");
                break;
            case Device.TYPE_WATER_SENSOR:
                config = new SensorCalibrationService.CalibrationConfig(0, 100, 0.5, "cm");
                break;
            case Device.TYPE_ELECTRICITY_SENSOR:
                config = new SensorCalibrationService.CalibrationConfig(0, 3500, 1.0, "W");
                break;
            case Device.TYPE_AIR_SENSOR:
                config = new SensorCalibrationService.CalibrationConfig(0, 150, 1.0, "AQI");
                break;
        }
        if (config != null) {
            calibrationService.updateCalibrationConfig(device.getType(), config);
        }
    }

    public static List<Device> getAllDevices() {
        return new ArrayList<>(devices);
    }

    public static Device getDevice(String id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                return device;
            }
        }
        return null;
    }

    public static DeviceState getDeviceState(String id) {
        return deviceStates.get(id);
    }

    public static void updateDeviceState(Device device, boolean isOn) {
        device.setOn(isOn);
        DeviceState state = deviceStates.get(device.getId());
        if (state != null) {
            state.isOn = isOn;
            state.lastUpdateTime = System.currentTimeMillis();
        }
        processDeviceStateChange(device);
    }

    public static void updateDeviceParameter(Device device, String parameter, Object value) {
        device.setParameter(parameter, value);
        DeviceState state = deviceStates.get(device.getId());
        if (state != null) {
            state.lastReadings.put(parameter, value);
            state.lastUpdateTime = System.currentTimeMillis();
        }
        processParameterUpdate(device, parameter, value);
    }

    private static void processDeviceStateChange(Device device) {
        // Обработка изменения состояния устройства
        switch (device.getType()) {
            case Device.TYPE_LIGHT:
            case Device.TYPE_AC:
                processPowerStateChange(device);
                break;
            default:
                processSensorStateChange(device);
                break;
        }
    }

    private static void processPowerStateChange(Device device) {
        if (device.isOn()) {

            // Обработка включения устройства
            switch (device.getType()) {
                case Device.TYPE_LIGHT:
                    updateDeviceParameter(device, "brightness",
                            device.getParameter("brightness"));
                    break;
                case Device.TYPE_AC:
                    updateDeviceParameter(device, "temperature",
                            device.getParameter("temperature"));
                    break;
            }
        }
    }

    private static void processSensorStateChange(Device device) {
        if (device.isOn()) {
            // Обработка активации датчика
            DeviceState state = deviceStates.get(device.getId());
            if (state != null) {
                state.isOnline = true;
                dataProcessor.processSensorData(device.getId(),
                        device.getType(),
                        getLastSensorReading(device));
            }
        }
    }

    private static void processParameterUpdate(Device device, String parameter, Object value) {
        if (value instanceof Number) {
            double numericValue = ((Number) value).doubleValue();

            // Обработка через процессор данных
            dataProcessor.processSensorData(device.getId(), device.getType(), numericValue);

            // Проверка пороговых значений
            SensorThresholdManager.ThresholdStatus status =
                    thresholdManager.checkThresholdStatus(device.getId(), parameter, numericValue);

            // Отправка уведомлений при необходимости
            if (status != SensorThresholdManager.ThresholdStatus.NORMAL) {
                Map<String, Double> thresholds = thresholdManager.getThresholds(device.getId());
                if (thresholds != null) {
                    notificationManager.sendAlert(
                            device.getId(),
                            parameter,
                            numericValue,
                            getParameterUnit(device.getType(), parameter),
                            thresholds.get(parameter + "_min"),
                            thresholds.get(parameter + "_max")
                    );
                }
            }
        }
    }

    private static double getLastSensorReading(Device device) {
        Object value = device.getParameter("current_value");
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    private static String getParameterUnit(String deviceType, String parameter) {
        switch (deviceType) {
            case Device.TYPE_TEMPERATURE_SENSOR:
                return "°C";
            case Device.TYPE_HUMIDITY_SENSOR:
                return "%";
            case Device.TYPE_WATER_SENSOR:
                return "cm";
            case Device.TYPE_ELECTRICITY_SENSOR:
                return parameter.equals("voltage") ? "V" : "W";
            case Device.TYPE_AIR_SENSOR:
                return parameter.equals("co2") ? "ppm" : "AQI";
            default:
                return "";
        }
    }
}