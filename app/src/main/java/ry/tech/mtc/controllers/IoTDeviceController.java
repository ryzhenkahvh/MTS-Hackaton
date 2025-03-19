package ry.tech.mtc.controllers;

import java.util.List;

import ry.tech.mtc.MockDeviceData;
import ry.tech.mtc.interfaces.DeviceController;
import ry.tech.mtc.models.Device;

public class IoTDeviceController implements DeviceController {
    private static IoTDeviceController instance;

    private IoTDeviceController() {
    }

    public static IoTDeviceController getInstance() {
        if (instance == null) {
            instance = new IoTDeviceController();
        }
        return instance;
    }

    @Override
    public void turnOn(String deviceId) {
        Device device = MockDeviceData.getDevice(deviceId);
        if (device != null) {
            MockDeviceData.updateDeviceState(device, true);
        }
    }

    @Override
    public void turnOff(String deviceId) {
        Device device = MockDeviceData.getDevice(deviceId);
        if (device != null) {
            MockDeviceData.updateDeviceState(device, false);
        }
    }

    @Override
    public void setParameter(String deviceId, String parameter, Object value) {
        Device device = MockDeviceData.getDevice(deviceId);
        if (device != null) {
            MockDeviceData.updateDeviceParameter(device, parameter, value);
        }
    }

    @Override
    public Device getDeviceState(String deviceId) {
        return MockDeviceData.getDevice(deviceId);
    }

    public List<Device> getAllDevices() {
        return MockDeviceData.getAllDevices();
    }
}