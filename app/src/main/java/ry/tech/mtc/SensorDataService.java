package ry.tech.mtc;

import java.util.HashMap;
import java.util.Map;

import ry.tech.mtc.models.Device;

public class SensorDataService {
    private static SensorDataService instance;
    private Map<String, SensorData> sensorData;

    private SensorDataService() {
        sensorData = new HashMap<>();
        initializeSensorData();
    }

    public static SensorDataService getInstance() {
        if (instance == null) {
            instance = new SensorDataService();
        }
        return instance;
    }

    private void initializeSensorData() {
        // Здесь мы будем получать данные с внешних API и заполнять sensorData
        sensorData.put(Device.TYPE_WATER_SENSOR, new SensorData(85.0, "water_level"));
        sensorData.put(Device.TYPE_ELECTRICITY_SENSOR, new SensorData(12.5, "electricity_usage"));
        sensorData.put(Device.TYPE_AIR_SENSOR, new SensorData(8.75, "air_quality"));
    }

    public SensorData getSensorData(String type) {
        return sensorData.get(type);
    }

    public static class SensorData {
        private double value;
        private String parameter;

        public SensorData(double value, String parameter) {
            this.value = value;
            this.parameter = parameter;
        }

        public double getValue() {
            return value;
        }

        public String getParameter() {
            return parameter;
        }
    }
}