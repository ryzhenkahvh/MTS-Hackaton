package ry.tech.mtc.sensors;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ry.tech.mtc.R;
import ry.tech.mtc.MainActivity;

public class SensorNotificationManager {
    private static final String TAG = "SensorNotificationManager";
    private static SensorNotificationManager instance;
    private Context context;
    private NotificationManager notificationManager;
    private final Map<String, NotificationConfig> notificationConfigs;
    private final Map<String, List<AlertHistory>> alertHistory;
    private final Handler mainHandler;
    private static final String CHANNEL_ID = "sensor_alerts";
    private static final String CHANNEL_NAME = "Sensor Alerts";
    private int notificationId = 1000;

    public static class NotificationConfig {
        public boolean isEnabled;
        public int priority;
        public long cooldownPeriod;
        public boolean vibrationEnabled;
        public boolean soundEnabled;
        public String customSound;
        public List<String> notificationRecipients;
        public Map<String, AlertThreshold> thresholds;

        public NotificationConfig() {
            this.isEnabled = true;
            this.priority = NotificationCompat.PRIORITY_HIGH;
            this.cooldownPeriod = 5 * 60 * 1000; // 5 минут
            this.vibrationEnabled = true;
            this.soundEnabled = true;
            this.notificationRecipients = new ArrayList<>();
            this.thresholds = new HashMap<>();
        }
    }

    public static class AlertThreshold {
        public double warningLevel;
        public double criticalLevel;
        public String unit;
        public boolean isEnabled;

        public AlertThreshold(double warning, double critical, String unit) {
            this.warningLevel = warning;
            this.criticalLevel = critical;
            this.unit = unit;
            this.isEnabled = true;
        }
    }

    public static class AlertHistory {
        public String deviceId;
        public String sensorType;
        public double value;
        public String unit;
        public AlertLevel level;
        public LocalDateTime timestamp;

        public AlertHistory(String deviceId, String sensorType, double value,
                            String unit, AlertLevel level) {
            this.deviceId = deviceId;
            this.sensorType = sensorType;
            this.value = value;
            this.unit = unit;
            this.level = level;
            this.timestamp = LocalDateTime.now();
        }
    }

    public enum AlertLevel {
        INFO,
        WARNING,
        CRITICAL
    }

    private SensorNotificationManager() {
        notificationConfigs = new ConcurrentHashMap<>();
        alertHistory = new ConcurrentHashMap<>();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized SensorNotificationManager getInstance() {
        if (instance == null) {
            instance = new SensorNotificationManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for sensor alerts");
            channel.enableVibration(true);
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendAlert(String deviceId, String sensorType, double value,
                          String unit, double minThreshold, double maxThreshold) {
        NotificationConfig config = notificationConfigs.get(deviceId);
        if (config == null || !config.isEnabled) return;

        // Определение уровня важности оповещения
        AlertLevel alertLevel = determineAlertLevel(value, minThreshold, maxThreshold);

        // Проверка периода охлаждения
        if (shouldSendAlert(deviceId, alertLevel)) {
            // Создание записи в истории
            AlertHistory alert = new AlertHistory(deviceId, sensorType, value, unit, alertLevel);
            addToHistory(deviceId, alert);

            // Отправка уведомления
            sendNotification(deviceId, sensorType, value, unit, alertLevel);

            // Отправка уведомления получателям
            notifyRecipients(config, alert);
        }
    }

    private AlertLevel determineAlertLevel(double value, double minThreshold, double maxThreshold) {
        double warningMargin = (maxThreshold - minThreshold) * 0.1;

        if (value < minThreshold || value > maxThreshold) {
            return AlertLevel.CRITICAL;
        } else if (value < minThreshold + warningMargin || value > maxThreshold - warningMargin) {
            return AlertLevel.WARNING;
        }
        return AlertLevel.INFO;
    }

    private boolean shouldSendAlert(String deviceId, AlertLevel level) {
        List<AlertHistory> history = alertHistory.get(deviceId);
        if (history == null || history.isEmpty()) return true;

        AlertHistory lastAlert = history.get(history.size() - 1);
        NotificationConfig config = notificationConfigs.get(deviceId);

        if (config == null) return true;

        long timeSinceLastAlert =
                System.currentTimeMillis() - lastAlert.timestamp.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        return timeSinceLastAlert > config.cooldownPeriod || level == AlertLevel.CRITICAL;
    }

    private void sendNotification(String deviceId, String sensorType,
                                  double value, String unit, AlertLevel level) {
        String title = String.format("Alert: %s Sensor", sensorType);
        String message = String.format("Value: %.2f %s - Level: %s", value, unit, level);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationConfig config = notificationConfigs.get(deviceId);
        if (config != null) {
            if (config.vibrationEnabled) {
                builder.setVibrate(new long[]{0, 500, 200, 500});
            }
            if (config.soundEnabled) {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
            }
        }

        mainHandler.post(() -> notificationManager.notify(notificationId++, builder.build()));
    }

    private void notifyRecipients(NotificationConfig config, AlertHistory alert) {
        if (config.notificationRecipients == null || config.notificationRecipients.isEmpty()) return;

        String message = String.format("Sensor Alert:\nDevice: %s\nType: %s\nValue: %.2f %s\nLevel: %s",
                alert.deviceId, alert.sensorType, alert.value, alert.unit, alert.level);

        for (String recipient : config.notificationRecipients) {
            // Здесь можно добавить логику отправки уведомлений через email, SMS или другие каналы
            Log.d(TAG, "Sending notification to recipient: " + recipient + "\nMessage: " + message);
        }
    }

    private void addToHistory(String deviceId, AlertHistory alert) {
        List<AlertHistory> history = alertHistory.computeIfAbsent(deviceId, k -> new ArrayList<>());
        history.add(alert);

        // Ограничение размера истории
        if (history.size() > 100) {
            history.remove(0);
        }
    }

    public void setNotificationConfig(String deviceId, NotificationConfig config) {
        notificationConfigs.put(deviceId, config);
    }

    public NotificationConfig getNotificationConfig(String deviceId) {
        return notificationConfigs.getOrDefault(deviceId, new NotificationConfig());
    }

    public List<AlertHistory> getAlertHistory(String deviceId) {
        return alertHistory.getOrDefault(deviceId, new ArrayList<>());
    }

    public void clearHistory(String deviceId) {
        alertHistory.remove(deviceId);
    }

    public void clearAllHistory() {
        alertHistory.clear();
    }

    public void addNotificationRecipient(String deviceId, String recipient) {
        NotificationConfig config = notificationConfigs.computeIfAbsent(
                deviceId, k -> new NotificationConfig());
        config.notificationRecipients.add(recipient);
    }

    public void removeNotificationRecipient(String deviceId, String recipient) {
        NotificationConfig config = notificationConfigs.get(deviceId);
        if (config != null) {
            config.notificationRecipients.remove(recipient);
        }
    }

    public void setAlertThreshold(String deviceId, String parameter,
                                  AlertThreshold threshold) {
        NotificationConfig config = notificationConfigs.computeIfAbsent(
                deviceId, k -> new NotificationConfig());
        config.thresholds.put(parameter, threshold);
    }

    public AlertThreshold getAlertThreshold(String deviceId, String parameter) {
        NotificationConfig config = notificationConfigs.get(deviceId);
        return config != null ? config.thresholds.get(parameter) : null;
    }
}