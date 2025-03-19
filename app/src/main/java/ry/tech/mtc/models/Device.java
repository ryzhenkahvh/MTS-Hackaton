package ry.tech.mtc.models;

import java.util.HashMap;
import java.util.Map;

public class Device {
    private String id;
    private String name;
    private String type;
    private boolean isOnline;
    private boolean isOn;
    private Map<String, Object> parameters;

    public Device(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.parameters = new HashMap<>();
        this.isOnline = true;
        this.isOn = false;
        initializeDefaultParameters();
    }

    private void initializeDefaultParameters() {
        switch (type) {
            case "light":
                parameters.put("brightness", 70);
                parameters.put("color_temp", 4000);
                break;
            case "ac":
                parameters.put("temperature", 22);
                parameters.put("mode", "cool");
                parameters.put("fan_speed", 2);
                break;
            case "temperature_sensor":
                parameters.put("current_temp", 23.5);
                parameters.put("humidity", 45);
                parameters.put("battery", 85);
                break;
            case "humidity_sensor":
                parameters.put("humidity", 45);
                parameters.put("temperature", 23.5);
                parameters.put("battery", 90);
                break;
        }
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    // next?
}
