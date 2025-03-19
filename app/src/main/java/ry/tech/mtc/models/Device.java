package ry.tech.mtc.models;

import java.util.HashMap;
import java.util.Map;

public class Device {
    public static final String TYPE_LIGHT = "light";
    public static final String TYPE_AC = "ac";
    public static final String TYPE_TEMPERATURE_SENSOR = "temperature_sensor";
    public static final String TYPE_HUMIDITY_SENSOR = "humidity_sensor";
    public static final String TYPE_WATER_SENSOR = "water_sensor";
    public static final String TYPE_ELECTRICITY_SENSOR = "electricity_sensor";
    public static final String TYPE_AIR_SENSOR = "air_sensor";

    private String id;
    private String name;
    private String type;
    private boolean isOnline;
    private boolean isOn;
    private Map<String, Object> parameters;
    private Map<String, String> parameterUnits;
    private long lastUpdateTime;

    public Device(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.parameters = new HashMap<>();
        this.parameterUnits = new HashMap<>();
        this.isOnline = true;
        this.isOn = false;
        this.lastUpdateTime = System.currentTimeMillis();
        initializeDefaultParameters();
    }

    private void initializeDefaultParameters() {
        switch (type) {
            case TYPE_LIGHT:
                parameters.put("brightness", 70);
                parameters.put("color_temp", 4000);
                parameters.put("power", 0.0);
                parameterUnits.put("brightness", "%");
                parameterUnits.put("color_temp", "K");
                parameterUnits.put("power", "W");
                break;

            case TYPE_AC:
                parameters.put("temperature", 22);
                parameters.put("mode", "cool");
                parameters.put("fan_speed", 2);
                parameters.put("power", 0.0);
                parameters.put("humidity", 45);
                parameterUnits.put("temperature", "°C");
                parameterUnits.put("fan_speed", "level");
                parameterUnits.put("power", "W");
                parameterUnits.put("humidity", "%");
                break;

            case TYPE_TEMPERATURE_SENSOR:
                parameters.put("current_temp", 23.5);
                parameters.put("humidity", 45);
                parameters.put("battery", 85);
                parameters.put("accuracy", 0.1);
                parameterUnits.put("current_temp", "°C");
                parameterUnits.put("humidity", "%");
                parameterUnits.put("battery", "%");
                parameterUnits.put("accuracy", "°C");
                break;

            case TYPE_HUMIDITY_SENSOR:
                parameters.put("humidity", 45);
                parameters.put("temperature", 23.5);
                parameters.put("battery", 90);
                parameters.put("accuracy", 1.0);
                parameterUnits.put("humidity", "%");
                parameterUnits.put("temperature", "°C");
                parameterUnits.put("battery", "%");
                parameterUnits.put("accuracy", "%");
                break;

            case TYPE_WATER_SENSOR:
                parameters.put("water_level", 50.0);
                parameters.put("temperature", 15.0);
                parameters.put("pressure", 1.0);
                parameters.put("flow_rate", 2.0);
                parameterUnits.put("water_level", "cm");
                parameterUnits.put("temperature", "°C");
                parameterUnits.put("pressure", "bar");
                parameterUnits.put("flow_rate", "L/min");
                break;

            case TYPE_ELECTRICITY_SENSOR:
                parameters.put("power", 0.0);
                parameters.put("voltage", 220.0);
                parameters.put("current", 0.0);
                parameters.put("frequency", 50.0);
                parameterUnits.put("power", "W");
                parameterUnits.put("voltage", "V");
                parameterUnits.put("current", "A");
                parameterUnits.put("frequency", "Hz");
                break;

            case TYPE_AIR_SENSOR:
                parameters.put("co2", 400.0);
                parameters.put("tvoc", 250.0);
                parameters.put("pm25", 10.0);
                parameters.put("pm10", 20.0);
                parameterUnits.put("co2", "ppm");
                parameterUnits.put("tvoc", "ppb");
                parameterUnits.put("pm25", "µg/m³");
                parameterUnits.put("pm10", "µg/m³");
                break;
        }
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public void setParameter(String key, Object value) {
        parameters.put(key, value);
        lastUpdateTime = System.currentTimeMillis();
    }

    public String getParameterUnit(String key) {
        return parameterUnits.get(key);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
        lastUpdateTime = System.currentTimeMillis();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
        lastUpdateTime = System.currentTimeMillis();
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public Map<String, Object> getAllParameters() {
        return new HashMap<>(parameters);
    }

    public Map<String, String> getAllParameterUnits() {
        return new HashMap<>(parameterUnits);
    }
}