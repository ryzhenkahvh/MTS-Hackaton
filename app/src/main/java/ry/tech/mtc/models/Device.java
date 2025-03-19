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
