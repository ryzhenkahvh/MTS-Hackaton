package ry.tech.mtc.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ry.tech.mtc.R;
import ry.tech.mtc.controllers.IoTDeviceController;
import ry.tech.mtc.models.Device;

public class DeviceFragment extends Fragment {
    private IoTDeviceController deviceController;
    private Device device;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceController = IoTDeviceController.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);

        // Инициализация элементов управления устройством на основе моковых данных

        return view;
    }

    private void updateDeviceState() {
        device = deviceController.getDeviceState(device.getId());
        // Обновление UI с новым состоянием устройства
    }

    private void turnOnDevice() {
        deviceController.turnOn(device.getId());
        updateDeviceState();
    }

    private void turnOffDevice() {
        deviceController.turnOff(device.getId());
        updateDeviceState();
    }

    private void setDeviceParameter(String parameter, Object value) {
        deviceController.setParameter(device.getId(), parameter, value);
        updateDeviceState();
    }
}