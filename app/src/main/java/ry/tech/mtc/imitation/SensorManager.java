package ry.tech.mtc.imitation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.ArrayList;
import java.util.List;

public class SensorManager {
    private static final String TAG = "SensorManager";
    private android.hardware.SensorManager sensorManager;
    private Context context;

    // Список сенсоров
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;
    private Sensor proximity;

    // Данные сенсоров
    private float[] accelerometerData = new float[3];
    private float[] gyroscopeData = new float[3];
    private float[] magnetometerData = new float[3];
    private float proximityData;

    public SensorManager(Context context) {
        this.context = context;
        sensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        initializeSensors();
    }

    private void initializeSensors() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Регистрируем слушателей для каждого сенсора
        sensorManager.registerListener(sensorEventListener, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, gyroscope, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, magnetometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, proximity, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    System.arraycopy(event.values, 0, accelerometerData, 0, 3);
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    System.arraycopy(event.values, 0, gyroscopeData, 0, 3);
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    System.arraycopy(event.values, 0, magnetometerData, 0, 3);
                    break;

                case Sensor.TYPE_PROXIMITY:
                    proximityData = event.values[0];
                    break;
            }

            // Уведомляем об обновлении данных
            notifyDataChanged();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Обработка изменения точности сенсора если необходимо
        }
    };

    // Интерфейс для слушателя изменений данных сенсоров
    public interface OnSensorDataChangedListener {
        void onDataChanged(SensorData data);
    }

    private List<OnSensorDataChangedListener> listeners = new ArrayList<>();

    public void addListener(OnSensorDataChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OnSensorDataChangedListener listener) {
        listeners.remove(listener);
    }

    private void notifyDataChanged() {
        SensorData data = new SensorData(
                accelerometerData.clone(),
                gyroscopeData.clone(),
                magnetometerData.clone(),
                proximityData
        );

        for (OnSensorDataChangedListener listener : listeners) {
            listener.onDataChanged(data);
        }
    }

    // Класс для хранения данных сенсоров
    public static class SensorData {
        public final float[] accelerometer;
        public final float[] gyroscope;
        public final float[] magnetometer;
        public final float proximity;

        public SensorData(float[] accelerometer, float[] gyroscope, float[] magnetometer, float proximity) {
            this.accelerometer = accelerometer;
            this.gyroscope = gyroscope;
            this.magnetometer = magnetometer;
            this.proximity = proximity;
        }
    }

    // Методы для получения текущих данных
    public float[] getAccelerometerData() {
        return accelerometerData.clone();
    }

    public float[] getGyroscopeData() {
        return gyroscopeData.clone();
    }

    public float[] getMagnetometerData() {
        return magnetometerData.clone();
    }

    public float getProximityData() {
        return proximityData;
    }

    // Метод для освобождения ресурсов
    public void release() {
        sensorManager.unregisterListener(sensorEventListener);
    }
}