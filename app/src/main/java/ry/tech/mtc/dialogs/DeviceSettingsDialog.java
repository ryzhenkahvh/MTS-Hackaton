package ry.tech.mtc.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import ry.tech.mtc.R;
import ry.tech.mtc.controllers.IoTDeviceController;
import ry.tech.mtc.models.Device;

public class DeviceSettingsDialog extends DialogFragment {
    private Device device;
    private View dialogView;
    private IoTDeviceController deviceController;

    public static DeviceSettingsDialog newInstance(Device device) {
        DeviceSettingsDialog dialog = new DeviceSettingsDialog();
        Bundle args = new Bundle();
        args.putString("device_id", device.getId());
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceController = IoTDeviceController.getInstance();
        String deviceId = getArguments().getString("device_id");
        device = deviceController.getDeviceState(deviceId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_device_settings, null);

        initializeViews();
        setupDeviceSpecificSettings();

        return new MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    saveSettings();
                })
                .setNegativeButton("Отмена", null)
                .create();
    }

    private void initializeViews() {
        ImageView deviceTypeIcon = dialogView.findViewById(R.id.deviceTypeIcon);
        TextView deviceNameTitle = dialogView.findViewById(R.id.deviceNameTitle);
        SwitchMaterial mainSwitch = dialogView.findViewById(R.id.deviceMainSwitch);

        deviceNameTitle.setText(device.getName());
        mainSwitch.setChecked(device.isOn());

        // Установка иконки в зависимости от типа устройства
        switch (device.getType()) {
            case "light":
                deviceTypeIcon.setImageResource(R.drawable.ic_lightbulb);
                deviceTypeIcon.setColorFilter(getResources().getColor(R.color.lampColor));
                break;
            case "ac":
                deviceTypeIcon.setImageResource(R.drawable.ic_ac);
                deviceTypeIcon.setColorFilter(getResources().getColor(R.color.acColor));
                break;
            case "temperature_sensor":
            case "humidity_sensor":
                deviceTypeIcon.setImageResource(R.drawable.ic_sensor);
                deviceTypeIcon.setColorFilter(getResources().getColor(R.color.temperatureColor));
                break;
        }
    }

    private void setupDeviceSpecificSettings() {
        View lightSettings = dialogView.findViewById(R.id.lightSettings);
        View acSettings = dialogView.findViewById(R.id.acSettings);
        View sensorSettings = dialogView.findViewById(R.id.sensorSettings);

        // Скрываем все настройки по умолчанию
        lightSettings.setVisibility(View.GONE);
        acSettings.setVisibility(View.GONE);
        sensorSettings.setVisibility(View.GONE);

        // Показываем настройки в зависимости от типа устройства
        switch (device.getType()) {
            case "light":
                lightSettings.setVisibility(View.VISIBLE);
                setupLightSettings();
                break;
            case "ac":
                acSettings.setVisibility(View.VISIBLE);
                setupAcSettings();
                break;
            case "temperature_sensor":
            case "humidity_sensor":
                sensorSettings.setVisibility(View.VISIBLE);
                setupSensorSettings();
                break;
        }
    }

    private void setupLightSettings() {
        SeekBar brightnessSeekBar = dialogView.findViewById(R.id.brightnessSeekBar);
        SeekBar colorTempSeekBar = dialogView.findViewById(R.id.colorTempSeekBar);

        // Установка текущих значений
        brightnessSeekBar.setProgress(70); // Моковые данные
        colorTempSeekBar.setProgress(50); // Моковые данные

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Обработка изменения яркости
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupAcSettings() {
        SeekBar temperatureSeekBar = dialogView.findViewById(R.id.temperatureSeekBar);
        RadioGroup modeRadioGroup = dialogView.findViewById(R.id.acModeRadioGroup);

        // Установка текущих значений
        temperatureSeekBar.setProgress(22); // Моковые данные
        modeRadioGroup.check(R.id.coolMode); // Моковые данные

        temperatureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Обработка изменения температуры
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupSensorSettings() {
        Spinner updateIntervalSpinner = dialogView.findViewById(R.id.updateIntervalSpinner);
        EditText minThreshold = dialogView.findViewById(R.id.minThresholdInput);
        EditText maxThreshold = dialogView.findViewById(R.id.maxThresholdInput);

        // Настройка спиннера
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"1 минута", "5 минут", "15 минут", "30 минут", "1 час"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        updateIntervalSpinner.setAdapter(adapter);

        // Установка текущих значений
        minThreshold.setText("18"); // Моковые данные
        maxThreshold.setText("25"); // Моковые данные
    }

    private void saveSettings() {
        // Сохранение настроек в зависимости от типа устройства
        switch (device.getType()) {
            case "light":
                saveLightSettings();
                break;
            case "ac":
                saveAcSettings();
                break;
            case "temperature_sensor":
            case "humidity_sensor":
                saveSensorSettings();
                break;
        }
    }

    private void saveLightSettings() {
        SeekBar brightnessSeekBar = dialogView.findViewById(R.id.brightnessSeekBar);
        SeekBar colorTempSeekBar = dialogView.findViewById(R.id.colorTempSeekBar);

        // Сохранение настроек освещения
        deviceController.setParameter(device.getId(), "brightness", brightnessSeekBar.getProgress());
        deviceController.setParameter(device.getId(), "colorTemp", colorTempSeekBar.getProgress());
    }

    private void saveAcSettings() {
        SeekBar temperatureSeekBar = dialogView.findViewById(R.id.temperatureSeekBar);
        RadioGroup modeRadioGroup = dialogView.findViewById(R.id.acModeRadioGroup);

        // Сохранение настроек кондиционера
        deviceController.setParameter(device.getId(), "temperature", temperatureSeekBar.getProgress());
        deviceController.setParameter(device.getId(), "mode",
                modeRadioGroup.getCheckedRadioButtonId() == R.id.coolMode ? "cool" :
                        modeRadioGroup.getCheckedRadioButtonId() == R.id.heatMode ? "heat" : "auto");
    }

    private void saveSensorSettings() {
        Spinner updateIntervalSpinner = dialogView.findViewById(R.id.updateIntervalSpinner);
        EditText minThreshold = dialogView.findViewById(R.id.minThresholdInput);
        EditText maxThreshold = dialogView.findViewById(R.id.maxThresholdInput);

        // Сохранение настроек датчика
        deviceController.setParameter(device.getId(), "updateInterval", updateIntervalSpinner.getSelectedItemPosition());
        deviceController.setParameter(device.getId(), "minThreshold", minThreshold.getText().toString());
        deviceController.setParameter(device.getId(), "maxThreshold", maxThreshold.getText().toString());
    }
}