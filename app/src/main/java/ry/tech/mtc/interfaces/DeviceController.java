package ry.tech.mtc.interfaces;

import ry.tech.mtc.models.Device;

public interface DeviceController {
    void turnOn(String deviceId);
    void turnOff(String deviceId);
    void setParameter(String deviceId, String parameter, Object value);
    Device getDeviceState(String deviceId);
}