package ry.tech.mtc.fragments;

import static ry.tech.mtc.MockDeviceData.updateDeviceState;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

import ry.tech.mtc.EnhancedDeviceSimulator;
import ry.tech.mtc.R;
import ry.tech.mtc.adapters.DeviceAdapter;
import ry.tech.mtc.controllers.IoTDeviceController;
import ry.tech.mtc.interfaces.DeviceClickListener;
import ry.tech.mtc.models.Device;

public class HomeFragment extends Fragment {
    private IoTDeviceController deviceController;
    private List<Device> devices;
    private DeviceAdapter deviceAdapter;
    private Handler updateHandler;
    private static final int UPDATE_INTERVAL = 5000;
    private EnhancedDeviceSimulator simulator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceController = IoTDeviceController.getInstance();
        devices = new ArrayList<>();
        updateHandler = new Handler();
        simulator = new EnhancedDeviceSimulator();
        simulator.setUpdateListener(new EnhancedDeviceSimulator.SimulationUpdateListener() {
            @Override
            public void onDeviceDataUpdated(String deviceId, EnhancedDeviceSimulator.DeviceSimulationData data) {
                updateDeviceData(deviceId, data);
            }

            @Override
            public void onDeviceStatusChanged(String deviceId, boolean isConnected) {
                updateDeviceStatus(deviceId, isConnected);
            }
        });
        initializeDevices();
    }

    private void initializeDevices() {
        devices.add(new Device("1", "Умная лампа", "light"));
        devices.add(new Device("2", "Кондиционер", "ac"));
        devices.add(new Device("3", "Датчик температуры", "temperature_sensor"));
        devices.add(new Device("4", "Датчик влажности", "humidity_sensor"));
        devices.add(new Device("5", "Датчик воды", Device.TYPE_WATER_SENSOR));
        devices.add(new Device("6", "Датчик электричества", Device.TYPE_ELECTRICITY_SENSOR));
        devices.add(new Device("7", "Датчик воздуха", Device.TYPE_AIR_SENSOR));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setupUI(view);
        simulator.startSimulation();
        return view;
    }

    private void setupUI(View view) {
        MaterialButton addDeviceButton = view.findViewById(R.id.addDeviceButton);
        addDeviceButton.setOnClickListener(v -> showAddDeviceDialog());

        RecyclerView devicesRecyclerView = view.findViewById(R.id.devicesRecyclerView);
        deviceAdapter = new DeviceAdapter(devices, new DeviceClickListener() {
            @Override
            public void onDeviceStateChanged(Device device, boolean isOn) {
                device.setOn(isOn);
                updateSensorData(getView());
            }

            @Override
            public void onDeviceSettingsClick(Device device) {
                showDeviceSettings(device);
            }
        });
        devicesRecyclerView.setAdapter(deviceAdapter);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupDeviceControls(view);
        updateSensorData(view);
    }

    private void setupDeviceControls(View view) {
        SwitchMaterial lampSwitch = view.findViewById(R.id.lampSwitch);
        lampSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Device lamp = findDeviceByType("light");
            if (lamp != null) {
                lamp.setOn(isChecked);
                updateSensorData(view);
            }
        });

        SeekBar brightnessSeekBar = view.findViewById(R.id.brightnessSeekBar);
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Device lamp = findDeviceByType("light");
                    if (lamp != null && lamp.isOn()) {
                        lamp.setParameter("brightness", progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SwitchMaterial acSwitch = view.findViewById(R.id.acSwitch);
        acSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Device ac = findDeviceByType("ac");
            if (ac != null) {
                ac.setOn(isChecked);
                updateSensorData(view);
            }
        });
    }

    private void updateDeviceData(String deviceId, EnhancedDeviceSimulator.DeviceSimulationData data) {
        Device device = findDeviceById(deviceId);
        if (device != null) {
            switch (device.getType()) {
                case "temperature_sensor":
                    device.setParameter("current_temp", data.temperature);
                    break;
                case "humidity_sensor":
                    device.setParameter("humidity", data.humidity);
                    break;
                case Device.TYPE_WATER_SENSOR:
                    device.setParameter("water_level", data.waterLevel);
                    break;
                case Device.TYPE_ELECTRICITY_SENSOR:
                    device.setParameter("power_consumption", data.powerConsumption);
                    break;
                case "light":
                    if (device.isOn()) {
                        device.setParameter("real_brightness", data.additionalParams.getOrDefault("realBrightness", 70.0));
                    }
                    break;
                case "ac":
                    if (device.isOn()) {
                        device.setParameter("room_temperature", data.additionalParams.getOrDefault("roomTemp", 25.0));
                    }
                    break;
            }
            updateSensorData(getView());
        }
    }

    private void updateDeviceStatus(String deviceId, boolean isConnected) {
        Device device = findDeviceById(deviceId);
        if (device != null) {
            device.setParameter("is_connected", isConnected);
            updateSensorData(getView());
        }
    }

    private void updateSensorData(View view) {
        if (view == null) return;

        TextView temperatureValue = view.findViewById(R.id.temperatureValue);
        TextView humidityValue = view.findViewById(R.id.humidityValue);
        SwitchMaterial lampSwitch = view.findViewById(R.id.lampSwitch);
        SeekBar brightnessSeekBar = view.findViewById(R.id.brightnessSeekBar);
        SwitchMaterial acSwitch = view.findViewById(R.id.acSwitch);
        TextView acTemperature = view.findViewById(R.id.acTemperature);

        Device tempSensor = findDeviceByType("temperature_sensor");
        Device humSensor = findDeviceByType("humidity_sensor");
        Device lamp = findDeviceByType("light");
        Device ac = findDeviceByType("ac");

        if (tempSensor != null) {
            Number temp = (Number) tempSensor.getParameter("current_temp");
            temperatureValue.setText(String.format("%.1f°C", temp.doubleValue()));
        }

        if (humSensor != null) {
            Number humidity = (Number) humSensor.getParameter("humidity");
            humidityValue.setText(humidity.intValue() + "%");
        }

        if (lamp != null) {
            lampSwitch.setChecked(lamp.isOn());
            Number brightness = (Number) lamp.getParameter("brightness");
            brightnessSeekBar.setProgress(brightness.intValue());
        }

        if (ac != null) {
            acSwitch.setChecked(ac.isOn());
            Number temp = (Number) ac.getParameter("temperature");
            acTemperature.setText(String.format("Установленная температура: %d°C", temp.intValue()));
        }
    }

    private void showAddDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.DialogTheme);
        builder.setTitle("Добавить устройство");

        final String[] deviceTypes = {"Умная лампа", "Кондиционер", "Датчик температуры", "Датчик влажности"};

        builder.setItems(deviceTypes, (dialog, which) -> {
            String type;
            switch (which) {
                case 0: type = "light"; break;
                case 1: type = "ac"; break;
                case 2: type = "temperature_sensor"; break;
                case 3: type = "humidity_sensor"; break;
                default: return;
            }

            String newId = String.valueOf(devices.size() + 1);
            Device newDevice = new Device(newId, deviceTypes[which], type);
            devices.add(newDevice);
            deviceAdapter.notifyItemInserted(devices.size() - 1);
        });

        builder.show();
    }

    private void showDeviceSettings(Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.DialogTheme);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_device_settings, null);

        setupDeviceSettingsDialog(dialogView, device);

        builder.setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    saveDeviceSettings(device, dialogView);
                    updateSensorData(getView());
                    deviceAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("Отмена", null);

        builder.show();
    }

    private void setupDeviceSettingsDialog(View dialogView, Device device) {
        TextView deviceNameTitle = dialogView.findViewById(R.id.deviceNameTitle);
        ImageView deviceTypeIcon = dialogView.findViewById(R.id.deviceTypeIcon);
        SwitchMaterial deviceMainSwitch = dialogView.findViewById(R.id.deviceMainSwitch);

        deviceNameTitle.setText(device.getName());
        deviceMainSwitch.setChecked(device.isOn());

        View lightSettings = dialogView.findViewById(R.id.lightSettings);
        View acSettings = dialogView.findViewById(R.id.acSettings);
        View sensorSettings = dialogView.findViewById(R.id.sensorSettings);

        lightSettings.setVisibility(View.GONE);
        acSettings.setVisibility(View.GONE);
        sensorSettings.setVisibility(View.GONE);

        switch (device.getType()) {
            case "light":
                deviceTypeIcon.setImageResource(R.drawable.ic_lightbulb);
                deviceTypeIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.lampColor));
                lightSettings.setVisibility(View.VISIBLE);
                setupLightSettings(dialogView, device);
                break;
            case "ac":
                deviceTypeIcon.setImageResource(R.drawable.ic_ac);
                deviceTypeIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.acColor));
                acSettings.setVisibility(View.VISIBLE);
                setupAcSettings(dialogView, device);
                break;
            case "temperature_sensor":
            case "humidity_sensor":
                deviceTypeIcon.setImageResource(R.drawable.ic_sensor);
                deviceTypeIcon.setColorFilter(ContextCompat.getColor(requireContext(),
                        device.getType().equals("temperature_sensor") ? R.color.temperatureColor : R.color.humidityColor));
                sensorSettings.setVisibility(View.VISIBLE);
                setupSensorSettings(dialogView, device);
                break;
        }
    }

    private void setupLightSettings(View dialogView, Device device) {
        SeekBar brightnessSeekBar = dialogView.findViewById(R.id.brightnessSeekBar);
        SeekBar colorTempSeekBar = dialogView.findViewById(R.id.colorTempSeekBar);

        Number brightness = (Number) device.getParameter("brightness");
        Number colorTemp = (Number) device.getParameter("color_temp");

        brightnessSeekBar.setProgress(brightness.intValue());
        colorTempSeekBar.setProgress(colorTemp.intValue() / 100);
    }

    private void setupAcSettings(View dialogView, Device device) {
        SeekBar temperatureSeekBar = dialogView.findViewById(R.id.temperatureSeekBar);
        RadioGroup modeRadioGroup = dialogView.findViewById(R.id.acModeRadioGroup);

        Number temperature = (Number) device.getParameter("temperature");
        temperatureSeekBar.setProgress(temperature.intValue());

        String mode = (String) device.getParameter("mode");
        switch (mode) {
            case "cool":
                modeRadioGroup.check(R.id.coolMode);
                break;
            case "heat":
                modeRadioGroup.check(R.id.heatMode);
                break;
            case "auto":
                modeRadioGroup.check(R.id.autoMode);
                break;
        }
    }

    private void setupSensorSettings(View dialogView, Device device) {
        Spinner updateIntervalSpinner = dialogView.findViewById(R.id.updateIntervalSpinner);
        EditText minThresholdInput = dialogView.findViewById(R.id.minThresholdInput);
        EditText maxThresholdInput = dialogView.findViewById(R.id.maxThresholdInput);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"1 минута", "5 минут", "15 минут", "30 минут", "1 час"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        updateIntervalSpinner.setAdapter(adapter);

        if (device.getType().equals("temperature_sensor")) {
            minThresholdInput.setText("18");
            maxThresholdInput.setText("25");
        } else {
            minThresholdInput.setText("30");
            maxThresholdInput.setText("70");
        }
    }

    private void saveDeviceSettings(Device device, View dialogView) {
        SwitchMaterial deviceMainSwitch = dialogView.findViewById(R.id.deviceMainSwitch);
        device.setOn(deviceMainSwitch.isChecked());

        switch (device.getType()) {
            case "light":
                SeekBar brightnessSeekBar = dialogView.findViewById(R.id.brightnessSeekBar);
                SeekBar colorTempSeekBar = dialogView.findViewById(R.id.colorTempSeekBar);
                device.setParameter("brightness", brightnessSeekBar.getProgress());
                device.setParameter("color_temp", colorTempSeekBar.getProgress() * 100);
                break;
            case "ac":
                SeekBar temperatureSeekBar = dialogView.findViewById(R.id.temperatureSeekBar);
                RadioGroup modeRadioGroup = dialogView.findViewById(R.id.acModeRadioGroup);
                device.setParameter("temperature", temperatureSeekBar.getProgress());

                int checkedId = modeRadioGroup.getCheckedRadioButtonId();
                String mode;
                if (checkedId == R.id.coolMode) {
                    mode = "cool";
                } else if (checkedId == R.id.heatMode) {
                    mode = "heat";
                } else {
                    mode = "auto";
                }
                device.setParameter("mode", mode);
                break;

            case "temperature_sensor":
            case "humidity_sensor":
                Spinner updateIntervalSpinner = dialogView.findViewById(R.id.updateIntervalSpinner);
                EditText minThresholdInput = dialogView.findViewById(R.id.minThresholdInput);
                EditText maxThresholdInput = dialogView.findViewById(R.id.maxThresholdInput);

                device.setParameter("update_interval", updateIntervalSpinner.getSelectedItemPosition());
                device.setParameter("min_threshold", Double.parseDouble(minThresholdInput.getText().toString()));
                device.setParameter("max_threshold", Double.parseDouble(maxThresholdInput.getText().toString()));
                break;
        }
    }

    private Device findDeviceByType(String type) {
        for (Device device : devices) {
            if (device.getType().equals(type)) {
                return device;
            }
        }
        return null;
    }

    private Device findDeviceById(String id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                return device;
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        simulator.stopSimulation();
    }
}