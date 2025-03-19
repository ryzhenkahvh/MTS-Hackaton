package ry.tech.mtc;

import java.util.ArrayList;
import java.util.List;

import ry.tech.mtc.models.Device;

public class MockDeviceData {
    private static final List<Device> devices = new ArrayList<>();

    static {
        devices.add(new Device("1", "Умная лампа", "light"));
        devices.add(new Device("2", "Кондиционер", "ac"));
        devices.add(new Device("3", "Датчик температуры", "temperature_sensor"));
        devices.add(new Device("4", "Датчик влажности", "humidity_sensor"));
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

    public static void updateDeviceState(Device device, boolean isOn) {
        device.setOn(isOn);
    }

    public static void updateDeviceParameter(Device device, String parameter, Object value) {
        // Обновление параметров устройства
    }
}
