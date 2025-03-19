package ry.tech.mtc.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ry.tech.mtc.R;
import ry.tech.mtc.models.Device;

public class DeviceSettingsDialog extends DialogFragment {
    private Device device;

    public static DeviceSettingsDialog newInstance(Device device) {
        DeviceSettingsDialog dialog = new DeviceSettingsDialog();
        Bundle args = new Bundle();
        args.putString("device_id", device.getId());
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_device_settings, null);

        // Настройка элементов управления

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setTitle("Настройки устройства")
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    saveSettings();
                })
                .setNegativeButton("Отмена", null)
                .create();
    }

    private void saveSettings() {
        // Сохранение настроек устройства
    }
}