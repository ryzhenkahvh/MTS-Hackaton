package ry.tech.mtc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

import ry.tech.mtc.R;
import ry.tech.mtc.interfaces.DeviceClickListener;
import ry.tech.mtc.models.Device;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private List<Device> devices;
    private DeviceClickListener listener;

    public DeviceAdapter(List<Device> devices, DeviceClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceName;
        private SwitchMaterial deviceSwitch;
        private ImageView deviceIcon;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceSwitch = itemView.findViewById(R.id.deviceSwitch);
            deviceIcon = itemView.findViewById(R.id.deviceIcon);

            deviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeviceStateChanged(devices.get(position), isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeviceSettingsClick(devices.get(position));
                }
            });
        }

        public void bind(Device device) {
            deviceName.setText(device.getName());
            deviceSwitch.setChecked(device.isOn());

            // Установка иконки в зависимости от типа устройства
            switch (device.getType()) {
                case "light":
                    deviceIcon.setImageResource(R.drawable.ic_lightbulb);
                    deviceIcon.setColorFilter(itemView.getContext().getColor(R.color.lampColor));
                    break;
                case "ac":
                    deviceIcon.setImageResource(R.drawable.ic_ac);
                    deviceIcon.setColorFilter(itemView.getContext().getColor(R.color.acColor));
                    break;
                case "temperature_sensor":
                case "humidity_sensor":
                    deviceIcon.setImageResource(R.drawable.ic_sensor);
                    deviceIcon.setColorFilter(itemView.getContext().getColor(R.color.temperatureColor));
                    break;
            }
        }
    }
}