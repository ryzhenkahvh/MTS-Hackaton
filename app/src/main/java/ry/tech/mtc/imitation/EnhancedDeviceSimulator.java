package ry.tech.mtc.imitation;

import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EnhancedDeviceSimulator {
    private final Map<String, DeviceSimulationData> devices = new HashMap<>();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int UPDATE_INTERVAL = 2000;
    private boolean isRunning = false;
    private SimulationUpdateListener updateListener;

    public static class DeviceSimulationData {
        public double temperature;
        public double humidity;
        public double waterLevel;
        public double powerConsumption;
        public double voltage;
        public double current;
        public double signalStrength;
        public long lastUpdateTime;
        public boolean isConnected;
        public Map<String, Double> additionalParams;

        public DeviceSimulationData() {
            this.temperature = 20 + random.nextDouble() * 10;
            this.humidity = 40 + random.nextDouble() * 30;
            this.waterLevel = random.nextDouble() * 100;
            this.powerConsumption = random.nextDouble() * 1000;
            this.voltage = 220 + random.nextDouble() * 10;
            this.current = random.nextDouble() * 10;
            this.signalStrength = 70 + random.nextDouble() * 30;
            this.lastUpdateTime = System.currentTimeMillis();
            this.isConnected = true;
            this.additionalParams = new HashMap<>();
        }

        private static final Random random = new Random();
    }

    public interface SimulationUpdateListener {
        void onDeviceDataUpdated(String deviceId, DeviceSimulationData data);
        void onDeviceStatusChanged(String deviceId, boolean isConnected);
    }

    public void setUpdateListener(SimulationUpdateListener listener) {
        this.updateListener = listener;
    }

    public void startSimulation() {
        if (!isRunning) {
            isRunning = true;
            scheduleUpdate();
        }
    }

    public void stopSimulation() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void scheduleUpdate() {
        if (!isRunning) return;

        handler.postDelayed(() -> {
            updateDevices();
            scheduleUpdate();
        }, UPDATE_INTERVAL);
    }

    private void updateDevices() {
        for (Map.Entry<String, DeviceSimulationData> entry : devices.entrySet()) {
            String deviceId = entry.getKey();
            DeviceSimulationData data = entry.getValue();

            // Симулируем случайные отключения устройств
            if (random.nextDouble() < 0.01) { // 1% шанс отключения
                boolean newConnectionState = !data.isConnected;
                data.isConnected = newConnectionState;
                if (updateListener != null) {
                    updateListener.onDeviceStatusChanged(deviceId, newConnectionState);
                }
            }

            if (data.isConnected) {
                updateDeviceData(data);
                if (updateListener != null) {
                    updateListener.onDeviceDataUpdated(deviceId, data);
                }
            }
        }
    }

    private void updateDeviceData(DeviceSimulationData data) {
        // Обновляем значения с небольшими случайными изменениями
        data.temperature += (random.nextDouble() - 0.5) * 0.5;
        data.humidity += (random.nextDouble() - 0.5) * 2;
        data.waterLevel += (random.nextDouble() - 0.5) * 1;
        data.powerConsumption += (random.nextDouble() - 0.5) * 50;
        data.voltage += (random.nextDouble() - 0.5) * 2;
        data.current += (random.nextDouble() - 0.5) * 0.5;
        data.signalStrength += (random.nextDouble() - 0.5) * 5;

        // Ограничиваем значения в разумных пределах
        data.temperature = clamp(data.temperature, 15, 35);

        data.humidity = clamp(data.humidity, 30, 70);
        data.waterLevel = clamp(data.waterLevel, 0, 100);
        data.powerConsumption = Math.max(0, data.powerConsumption);
        data.voltage = clamp(data.voltage, 210, 230);
        data.current = Math.max(0, data.current);
        data.signalStrength = clamp(data.signalStrength, 0, 100);

        data.lastUpdateTime = System.currentTimeMillis();
    }

    private double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public DeviceSimulationData getDeviceData(String deviceId) {
        return devices.get(deviceId);
    }

    public void addDevice(String deviceId, String type) {
        DeviceSimulationData data = new DeviceSimulationData();

        // Устанавливаем начальные значения в зависимости от типа устройства
        switch (type) {
            case "light":
                data.additionalParams.put("brightness", 70.0);
                data.additionalParams.put("colorTemp", 5000.0);
                break;
            case "ac":
                data.additionalParams.put("targetTemp", 24.0);
                data.additionalParams.put("fanSpeed", 2.0);
                break;
            case "temperature_sensor":
                data.temperature = 23.0;
                break;
            case "humidity_sensor":
                data.humidity = 50.0;
                break;
            case "water_sensor":
                data.waterLevel = 50.0;
                break;
            case "electricity_sensor":
                data.powerConsumption = 500.0;
                data.voltage = 220.0;
                data.current = 2.3;
                break;
        }

        devices.put(deviceId, data);
    }

    public void updateDeviceState(String deviceId, boolean isOn) {
        DeviceSimulationData data = devices.get(deviceId);
        if (data != null) {
            data.additionalParams.put("isOn", isOn ? 1.0 : 0.0);
        }
    }

    public void updateDeviceParameter(String deviceId, String parameter, double value) {
        DeviceSimulationData data = devices.get(deviceId);
        if (data != null) {
            data.additionalParams.put(parameter, value);
        }
    }

    public void removeDevice(String deviceId) {
        devices.remove(deviceId);
    }
}