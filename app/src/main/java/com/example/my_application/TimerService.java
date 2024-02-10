package com.example.my_application;

import static com.example.my_application.AddDialog.NOTIFICATION_ID;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimerService extends Service {
    private long startTimeMillis;
    private long cancelTimeMillis;
    private CountDownTimer countDownTimer;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private static final int NOTIFICATION_ID = 123;
    private long remainingTimeMillis;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TimerService", "Received STOP_TIMER_ACTION");
        if ("STOP_TIMER_ACTION".equals(intent.getAction())) {
            stopTimer();
        }
        if (startTimeMillis == 0) {
            startTimeMillis = System.currentTimeMillis();
        }
        int selectedMinutes = intent.getIntExtra("selectedMinutes", 0);
        long durationMillis = (long) selectedMinutes * 60 * 1000;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Таймер")
                .setContentText("Время таймера: " + selectedMinutes + " минут")
                .setAutoCancel(true);


        // Создание интента для действия отмены таймера
        Intent cancelIntent = new Intent(this, NotificationReceiver.class);
        cancelIntent.setAction("STOP_TIMER_ACTION");
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

// Добавление кнопки "Отмена" в уведомление
        notificationBuilder.addAction(R.drawable.close_app, "Отмена", cancelPendingIntent);

        startTimer(durationMillis);

        return START_NOT_STICKY;
    }

    private void startTimer(long durationMillis) {
            countDownTimer = new CountDownTimer(durationMillis, 1000) {
                public void onTick(long millisUntilFinished) {
                    remainingTimeMillis = millisUntilFinished;
                    updateNotification(millisUntilFinished);
                }

                public void onFinish() {
                    notificationBuilder.setContentTitle("Возвращайтесь в приложение")
                            .setContentText("Прошло времени: " + getElapsedTimeFormatted())
                            .setAutoCancel(true);
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                }
            }.start();
    }

    private void updateNotification(long millisUntilFinished) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        notificationBuilder.setContentText("Оставшееся время: " + timeLeftFormatted);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "Timer Service", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            Log.d("TimerService", "Stopping timer");
        }
    }

    public long getRemainingTimeMillis() {
        return remainingTimeMillis;
    }
    public long getElapsedTimeMillis() {
        if (cancelTimeMillis > 0) {
            // Если кнопка "Отмена" была нажата, вычисляем разницу между временем нажатия на кнопку и временем начала таймера
            return cancelTimeMillis - startTimeMillis;
        } else {
            // Если кнопка "Отмена" не была нажата, возвращаем текущее время минус время начала таймера
            return System.currentTimeMillis() - startTimeMillis;
        }
    }

    public String getElapsedTimeFormatted() {
        long elapsedTimeMillis = getElapsedTimeMillis();
        // Преобразовываем время в формат MM:SS
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}



