package com.uselessdev.tetramenai

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()

        // Foreground notification
//        val notification = NotificationCompat.Builder(this, "default")
//            .setContentTitle("TetramenAI")
//            .setContentText("백그라운드 실행 중입니다")
//            .setSmallIcon(R.drawable.tetramenai)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .build()

        //startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 서비스에서 하고 싶은 작업 수행
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null
}