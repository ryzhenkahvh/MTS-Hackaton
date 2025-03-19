package ry.tech.mtc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ry.tech.mtc.R;
import ry.tech.mtc.models.DeviceHealth;

public class DeviceHealthAdapter extends RecyclerView.Adapter<DeviceHealthAdapter.ViewHolder> {
    private List<DeviceHealth> deviceHealthList;

    public DeviceHealthAdapter(List<DeviceHealth> deviceHealthList) {
        this.deviceHealthList = deviceHealthList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device_health, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceHealth health = deviceHealthList.get(position);

        holder.deviceName.setText(health.getDeviceName());
        holder.deviceHealth.setText(String.format(Locale.getDefault(),
                "Здоровье: %.1f%%", health.getHealth()));

        // Формируем строку с факторами здоровья
        StringBuilder factors = new StringBuilder("Факторы влияния:\n");
        for (Map.Entry<String, Double> factor : health.getHealthFactors().entrySet()) {
            factors.append(String.format(Locale.getDefault(),
                    "%s: %.2f\n", factor.getKey(), factor.getValue()));
        }
        holder.healthFactors.setText(factors.toString());

        // Показываем последние события
        StringBuilder events = new StringBuilder("Последние события:\n");
        List<DeviceHealth.HealthEvent> deviceEvents = health.getEvents();
        int eventsToShow = Math.min(3, deviceEvents.size());
        for (int i = deviceEvents.size() - eventsToShow; i < deviceEvents.size(); i++) {
            DeviceHealth.HealthEvent event = deviceEvents.get(i);
            events.append(String.format("%s: %s\n",
                    event.getType(), event.getDescription()));
        }
        holder.events.setText(events.toString());
    }

    @Override
    public int getItemCount() {
        return deviceHealthList.size();
    }

    public void updateDeviceHealth(List<DeviceHealth> newHealthList) {
        this.deviceHealthList = newHealthList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceHealth;
        TextView healthFactors;
        TextView events;

        ViewHolder(View view) {
            super(view);
            deviceName = view.findViewById(R.id.textDeviceName);
            deviceHealth = view.findViewById(R.id.textDeviceHealth);
            healthFactors = view.findViewById(R.id.textHealthFactors);
            events = view.findViewById(R.id.textEvents);
        }
    }
}