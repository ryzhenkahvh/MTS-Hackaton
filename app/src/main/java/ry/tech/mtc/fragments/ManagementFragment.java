package ry.tech.mtc.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ry.tech.mtc.R;
import ry.tech.mtc.adapters.DeviceHealthAdapter;
import ry.tech.mtc.models.Device;
import ry.tech.mtc.models.DeviceHealth;
import ry.tech.mtc.MockDeviceData;

public class ManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private DeviceHealthAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Map<String, DeviceHealth> deviceHealthMap = new HashMap<>();
    private static final int UPDATE_INTERVAL = 5000; // 5 секунд

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_management, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewDeviceHealth);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        initializeDeviceHealth();
        startHealthMonitoring();

        return view;
    }

    private void initializeDeviceHealth() {
        List<Device> devices = MockDeviceData.getAllDevices();
        List<DeviceHealth> deviceHealthList = new ArrayList<>();

        for (Device device : devices) {
            DeviceHealth health = new DeviceHealth(
                    device.getId(),
                    device.getName(),
                    device.getType(),
                    100, // начальное здоровье
                    calculateInitialLifespan(device.getType()),
                    System.currentTimeMillis(),
                    new HashMap<>()
            );
            deviceHealthMap.put(device.getId(), health);
            deviceHealthList.add(health);
        }

        adapter = new DeviceHealthAdapter(deviceHealthList);
        recyclerView.setAdapter(adapter);
    }

    private int calculateInitialLifespan(String deviceType) {
        // Расчёт предполагаемого срока службы в днях
        switch (deviceType) {
            case Device.TYPE_TEMPERATURE_SENSOR:
            case Device.TYPE_HUMIDITY_SENSOR:
                return 365 * 2; // 2 года
            case Device.TYPE_WATER_SENSOR:
                return 365 * 3; // 3 года
            case Device.TYPE_ELECTRICITY_SENSOR:
                return 365 * 5; // 5 лет
            case Device.TYPE_AIR_SENSOR:
                return 365 * 2; // 2 года
            case Device.TYPE_LIGHT:
                return 365 * 3; // 3 года
            case Device.TYPE_AC:
                return 365 * 5; // 5 лет
            default:
                return 365 * 2; // По умолчанию 2 года
        }
    }

    private void startHealthMonitoring() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDevicesHealth();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    private void updateDevicesHealth() {
        List<DeviceHealth> updatedHealthList = new ArrayList<>();

        for (DeviceHealth health : deviceHealthMap.values()) {
            // Обновляем состояние здоровья
            updateDeviceHealthStatus(health);
            updatedHealthList.add(health);
        }

        // Обновляем UI
        adapter.updateDeviceHealth(updatedHealthList);
    }

    private void updateDeviceHealthStatus(DeviceHealth health) {
        // Базовое снижение здоровья
        double baseDecay = calculateBaseHealthDecay(health);

        // Факторы влияния на здоровье
        Map<String, Double> healthFactors = calculateHealthFactors(health);

        // Применяем все факторы
        double totalDecay = baseDecay;
        for (Double factor : healthFactors.values()) {
            totalDecay *= factor;
        }

        // Обновляем здоровье
        health.setHealth(Math.max(0, health.getHealth() - totalDecay));
        health.setLastUpdateTime(System.currentTimeMillis());
        health.setHealthFactors(healthFactors);

        // Генерируем случайные события
        generateRandomEvents(health);
    }

    private double calculateBaseHealthDecay(DeviceHealth health) {
        long timeInUse = (System.currentTimeMillis() - health.getLastUpdateTime()) / 1000; // в секундах
        double baseDecay = 0.01; // базовое снижение за период

        // Увеличиваем скорость деградации для старых устройств
        double ageFactorInDays = timeInUse / (24.0 * 60 * 60);
        double ageFactor = Math.pow(1.001, ageFactorInDays);

        return baseDecay * ageFactor;
    }

    private Map<String, Double> calculateHealthFactors(DeviceHealth health) {
        Map<String, Double> factors = new HashMap<>();

        // Фактор возраста
        double ageInDays = (System.currentTimeMillis() - health.getLastUpdateTime())
                / (1000.0 * 60 * 60 * 24);
        factors.put("age_factor", 1.0 + (ageInDays / health.getLifespan()) * 0.5);

        // Фактор использования (симуляция)
        factors.put("usage_factor", 1.0 + random.nextDouble() * 0.2);

        // Фактор окружающей среды (симуляция)
        factors.put("environment_factor", 1.0 + random.nextDouble() * 0.3);

        // Специфичные факторы для разных типов устройств
        switch (health.getType()) {
            case Device.TYPE_TEMPERATURE_SENSOR:
                factors.put("temperature_stress", 1.0 + random.nextDouble() * 0.4);
                break;
            case Device.TYPE_HUMIDITY_SENSOR:
                factors.put("moisture_exposure", 1.0 + random.nextDouble() * 0.3);
                break;
            case Device.TYPE_WATER_SENSOR:
                factors.put("water_exposure", 1.0 + random.nextDouble() * 0.5);
                break;
            case Device.TYPE_ELECTRICITY_SENSOR:
                factors.put("power_fluctuation", 1.0 + random.nextDouble() * 0.2);
                break;
            case Device.TYPE_AIR_SENSOR:
                factors.put("air_quality", 1.0 + random.nextDouble() * 0.3);
                break;
        }

        return factors;
    }

    private void generateRandomEvents(DeviceHealth health) {
        // Вероятность случайного события зависит от текущего здоровья
        double eventProbability = (100 - health.getHealth()) / 1000.0; // 0.1% при здоровье 0

        if (random.nextDouble() < eventProbability) {
            // Генерируем случайное событие
            switch (random.nextInt(3)) {
                case 0: // Временный сбой
                    health.addEvent("temporary_failure",
                            "Обнаружен временный сбой в работе устройства");
                    break;
                case 1: // Проблема с калибровкой
                    health.addEvent("calibration_issue",
                            "Требуется калибровка устройства");
                    break;
                case 2: // Критическая ошибка
                    health.addEvent("critical_error",
                            "Обнаружена критическая ошибка в работе устройства");
                    health.setHealth(Math.max(0, health.getHealth() - 20));
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}