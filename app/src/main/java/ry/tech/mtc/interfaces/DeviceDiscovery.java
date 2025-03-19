package ry.tech.mtc.interfaces;

import java.util.List;

import ry.tech.mtc.models.Device;

public interface DeviceDiscovery {
    List<Device> scanForDevices();
    boolean connectDevice(Device device);
    void disconnectDevice(Device device);
}