package com.example.coolweather.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.coolweather.R;
import com.example.coolweather.WeatherActivity;

public class ForeService extends Service {

    public ForeService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent i = new Intent(ForeService.this, WeatherActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ForeService.this,
                0, i,PendingIntent.FLAG_MUTABLE);
        NotificationChannel channel = new NotificationChannel("001","channel1", NotificationManager.IMPORTANCE_DEFAULT);
        // 自定义设置通知声音、震动等
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200});
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        // 构建一个 Notification
        Notification notification = new NotificationCompat.Builder(this, "001")
                .setContentTitle("CoolWeather")
                .setContentText("天气服务正在运行...")
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setSmallIcon(R.drawable.ic_baseline_cloud_24)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_brightness_7_24))
                .setContentIntent(pendingIntent)
                .build();
        // 启动前台服务
        // 通知栏标识符 前台进程对象唯一SERVICE_ID
        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}