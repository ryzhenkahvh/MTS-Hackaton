package ry.tech.mtc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class IoTService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private final IBinder binder = new LocalBinder();
    private Handler handler;
    private boolean isRunning;

    public class LocalBinder extends Binder {
        IoTService getService() {
            return IoTService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(
                "iot_service",
                "IoT Service",
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, "iot_service")
                .setContentTitle("IoT Service")
                .setContentText("Мониторинг устройств")
                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }
}