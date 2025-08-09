package com.uselessdev.tetramenai

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class PushNotification(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "default_channel"
        const val CHANNEL_NAME = "기본 알림"
        const val NOTIFICATION_ID = 1001
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    // 알림 채널 생성 (Android 8.0 이상 필수)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "앱의 기본 알림 채널입니다"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // 기본 알림 보내기
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendBasicNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.tetramenai)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 높은 우선순위
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 소리, 진동, LED
            .build()

        notificationManager.notify(1001, notification)
    }

    // 긴 텍스트 알림
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendBigTextNotification(title: String, shortText: String, longText: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.tetramenai)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(longText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}