package ry.tech.mtc.imitation;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockSensorData {
    private Random random = new Random();
    private Handler handler = new Handler();
    private static final int UPDATE_INTERVAL = 100; // ms

    private float[] mockAccelerometer = new float[3];
    private float[] mockGyroscope = new float[3];
    private float[] mockMagnetometer = new float[3];
    private float mockProximity;

    private List<SensorManager.OnSensorDataChangedListener> listeners = new ArrayList<>();

    public MockSensorData() {
        startMockDataGeneration();
    }

    private void startMockDataGeneration() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                generateMockData();
                notifyListeners();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    private void generateMockData() {
        // Имитация акселерометра
        mockAccelerometer[0] = (float) (random.nextGaussian() * 0.5);
        mockAccelerometer[1] = (float) (random.nextGaussian() * 0.5);
        mockAccelerometer[2] = (float) (9.81 + random.nextGaussian() * 0.1);

        // Имитация гироскопа
        mockGyroscope[0] = (float) (random.nextGaussian() * 0.1);
        mockGyroscope[1] = (float) (random.nextGaussian() * 0.1);
        mockGyroscope[2] = (float) (random.nextGaussian() * 0.1);

        // Имитация магнитометра
        mockMagnetometer[0] = (float) (20 + random.nextGaussian() * 2);
        mockMagnetometer[1] = (float) (40 + random.nextGaussian() * 2);
        mockMagnetometer[2] = (float) (30 + random.nextGaussian() * 2);

        // Имитация датчика приближения
        mockProximity = random.nextFloat() * 10;
    }

    private void notifyListeners() {
        SensorManager.SensorData data = new SensorManager.SensorData(
                mockAccelerometer.clone(),
                mockGyroscope.clone(),
                mockMagnetometer.clone(),
                mockProximity
        );

        for (SensorManager.OnSensorDataChangedListener listener : listeners) {
            listener.onDataChanged(data);
        }
    }

    public void addListener(SensorManager.OnSensorDataChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SensorManager.OnSensorDataChangedListener listener) {
        listeners.remove(listener);
    }

    public void release() {
        handler.removeCallbacksAndMessages(null);
    }
}