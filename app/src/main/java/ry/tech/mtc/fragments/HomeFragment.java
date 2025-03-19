package ry.tech.mtc.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ry.tech.mtc.R;
import ry.tech.mtc.adapters.DeviceAdapter;
import ry.tech.mtc.controllers.IoTDeviceController;
import ry.tech.mtc.interfaces.DeviceClickListener;
import ry.tech.mtc.models.Device;

public class HomeFragment extends Fragment {
    private IoTDeviceController deviceController;
    private List<Device> devices;
    private DeviceAdapter deviceAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceController = IoTDeviceController.getInstance();
        devices = deviceController.getAllDevices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Отображение основных показателей
        TextView temperatureValue = view.findViewById(R.id.temperatureValue);
        TextView humidityValue = view.findViewById(R.id.humidityValue);

        temperatureValue.setText("23°C");
        humidityValue.setText("45%");

        // Отображение списка устройств
        RecyclerView devicesRecyclerView = view.findViewById(R.id.devicesRecyclerView);
        deviceAdapter = new DeviceAdapter(devices, new DeviceClickListener() {
            @Override
            public void onDeviceStateChanged(Device device, boolean isOn) {
                if (isOn) {
                    deviceController.turnOn(device.getId());
                } else {
                    deviceController.turnOff(device.getId());
                }
            }

            @Override
            public void onDeviceSettingsClick(Device device) {
                showDeviceSettings(device);
            }
        });

        devicesRecyclerView.setAdapter(deviceAdapter);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void showDeviceSettings(Device device) {
        DeviceSettingsDialog dialog = DeviceSettingsDialog.newInstance(device);
        dialog.show(getChildFragmentManager(), "device_settings");
    }
}