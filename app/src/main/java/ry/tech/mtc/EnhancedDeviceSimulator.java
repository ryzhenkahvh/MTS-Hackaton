package ry.tech.mtc;

import android.os.Handler;
import android.os.Looper;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

import ry.tech.mtc.models.Device;

public class EnhancedDeviceSimulator {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Map<String, DeviceSimulationData> deviceData = new HashMap<>();
    private static final int UPDATE_INTERVAL = 2000; // 2 секунды

    public class DeviceSimulationData {
        public double temperature;
        public double humidity;
        public double waterLevel;
        public double powerConsumption;
        public double voltage;
        public double current;
        public boolean isConnected;
        public int signalStrength;
        public long lastUpdateTime;
        public Map<String, Object> additionalParams;

        public DeviceSimulationData() {
            this.temperature = 20 + random.nextDouble() * 10;
            this.humidity = 40 + random.nextDouble() * 30;
            this.waterLevel = 70 + random.nextDouble() * 30;
            this.powerConsumption = 100 + random.nextDouble() * 900;
            this.voltage = 220 + random.nextDouble() * 10;
            this.current = 0.5 + random.nextDouble() * 4.5;
            this.isConnected = true;
            this.signalStrength = 60 + random.nextInt(40);
            this.lastUpdateTime = System.currentTimeMillis();
            this.additionalParams = new HashMap<>();
        }
    }

    public interface SimulationUpdateListener {
        void onDeviceDataUpdated(String deviceId, DeviceSimulationData data);
        void onDeviceStatusChanged(String deviceId, boolean isConnected);
    }

    private SimulationUpdateListener listener;

    public void setUpdateListener(SimulationUpdateListener listener) {
        this.listener = listener;
    }

    public void startSimulation() {
        // Инициализация устройств из MockDeviceData
        for (Device device : MockDeviceData.getAllDevices()) {
            deviceData.put(device.getId(), new DeviceSimulationData());
        }

        // Запуск периодического обновления
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateSimulation();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    private void updateSimulation() {
        for (Map.Entry<String, DeviceSimulationData> entry : deviceData.entrySet()) {
            String deviceId = entry.getKey();
            DeviceSimulationData data = entry.getValue();
            Device device = MockDeviceData.getDevice(deviceId);

            if (device != null) {
                // Симуляция случайных отключений
                if (random.nextDouble() < 0.02) { // 2% шанс отключения
                    data.isConnected = !data.isConnected;
                    if (listener != null) {
                        listener.onDeviceStatusChanged(deviceId, data.isConnected);
                    }
                }

                if (data.isConnected) {
                    // Обновление данных в зависимости от типа устройства
                    switch (device.getType()) {
                        case Device.TYPE_TEMPERATURE_SENSOR:
                            data.temperature += (random.nextDouble() - 0.5) * 0.5;
                            data.humidity += (random.nextDouble() - 0.5) * 2;
                            break;

                        case Device.TYPE_WATER_SENSOR:
                            data.waterLevel += (random.nextDouble() - 0.5) * 1;
                            data.waterLevel = Math.max(0, Math.min(100, data.waterLevel));
                            break;

                        case Device.TYPE_ELECTRICITY_SENSOR:
                            data.powerConsumption += (random.nextDouble() - 0.5) * 50;
                            data.voltage += (random.nextDouble() - 0.5) * 2;
                            data.current += (random.nextDouble() - 0.5) * 0.1;
                            break;

                        case Device.TYPE_LIGHT:
                            if (device.isOn()) {
                                data.powerConsumption = 40 + random.nextDouble() * 20;
                                Number brightness = (Number) device.getParameter("brightness");
                                data.additionalParams.put("realBrightness",
                                        brightness.doubleValue() + (random.nextDouble() - 0.5) * 5);
                            } else {
                                data.powerConsumption = 0;
                            }
                            break;

                        case Device.TYPE_AC:
                            if (device.isOn()) {
                                Number targetTemp = (Number) device.getParameter("temperature");
                                double currentTemp = (double) data.additionalParams
                                        .getOrDefault("roomTemp", 25.0);
                                String mode = (String) device.getParameter("mode");

                                // Симуляция изменения температуры в зависимости от режима
                                if ("cool".equals(mode)) {
                                    currentTemp -= 0.2 + random.nextDouble() * 0.3;
                                } else if ("heat".equals(mode)) {
                                    currentTemp += 0.2 + random.nextDouble() * 0.3;
                                }

                                data.additionalParams.put("roomTemp", currentTemp);
                                data.powerConsumption = 800 + random.nextDouble() * 400;
                            } else {
                                data.powerConsumption = 0;
                            }
                            break;
                    }

                    // Симуляция качества сигнала
                    data.signalStrength += (random.nextInt(3) - 1);
                    data.signalStrength = Math.max(0, Math.min(100, data.signalStrength));

                    data.lastUpdateTime = System.currentTimeMillis();

                    if (listener != null) {
                        listener.onDeviceDataUpdated(deviceId, data);
                    }
                }
            }
        }
    }

    public DeviceSimulationData getDeviceData(String deviceId) {
        return deviceData.get(deviceId);
    }

    public void stopSimulation() {
        handler.removeCallbacksAndMessages(null);
    }
}