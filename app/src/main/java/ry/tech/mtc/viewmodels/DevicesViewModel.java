package ry.tech.mtc.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import ry.tech.mtc.models.Device;
import ry.tech.mtc.imitation.EnhancedDeviceSimulator;

public class DevicesViewModel extends ViewModel {
    private final MutableLiveData<List<Device>> devices = new MutableLiveData<>(new ArrayList<>());
    private final EnhancedDeviceSimulator simulator;

    public DevicesViewModel() {
        simulator = new EnhancedDeviceSimulator();
        initializeDevices();
    }

    private void initializeDevices() {
        List<Device> initialDevices = new ArrayList<>();
        initialDevices.add(new Device("1", "Умная лампа", "light"));
        initialDevices.add(new Device("2", "Кондиционер", "ac"));
        initialDevices.add(new Device("3", "Датчик температуры", "temperature_sensor"));
        initialDevices.add(new Device("4", "Датчик влажности", "humidity_sensor"));
        initialDevices.add(new Device("5", "Датчик воды", Device.TYPE_WATER_SENSOR));
        initialDevices.add(new Device("6", "Датчик электричества", Device.TYPE_ELECTRICITY_SENSOR));
        initialDevices.add(new Device("7", "Датчик воздуха", Device.TYPE_AIR_SENSOR));

        for (Device device : initialDevices) {
            simulator.addDevice(device.getId(), device.getType());
        }

        devices.setValue(initialDevices);
    }

    public LiveData<List<Device>> getDevices() {
        return devices;
    }

    public void addDevice(Device device) {
        List<Device> currentDevices = devices.getValue();
        if (currentDevices != null) {
            currentDevices.add(device);
            simulator.addDevice(device.getId(), device.getType());
            devices.setValue(currentDevices);
        }
    }

    public void removeDevice(Device device) {
        List<Device> currentDevices = devices.getValue();
        if (currentDevices != null) {
            currentDevices.remove(device);
            simulator.removeDevice(device.getId());
            devices.setValue(currentDevices);
        }
    }

    public EnhancedDeviceSimulator getSimulator() {
        return simulator;
    }
}