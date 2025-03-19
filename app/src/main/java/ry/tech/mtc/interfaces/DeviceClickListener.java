package ry.tech.mtc.interfaces;

import ry.tech.mtc.models.Device;

public interface DeviceClickListener {
    void onDeviceStateChanged(Device device, boolean isOn);
    void onDeviceSettingsClick(Device device);
}