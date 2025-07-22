package com.example.aliolio

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class FilteredNotificationListener : NotificationListenerService() {

    // 읽고 싶은 앱들의 패키지명
    private val targetPackages = listOf(
        "com.kakao.talk",      // 카카오톡
        "com.facebook.orca",   // 메신저
        "com.whatsapp",        // 왓츠앱
        "com.samsung.android.messaging" // 삼성 메시지
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let { notification ->
            val packageName = notification.packageName

            // 타겟 패키지만 처리
            if (targetPackages.contains(packageName)) {
                val extras = notification.notification.extras
                val title = extras.getString(Notification.EXTRA_TITLE)
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

                Log.d("FilteredNotificationListener", "필터된 알림: $packageName - $title: $text")

                // 필터된 알림만 브로드캐스트
                val intent = Intent("FILTERED_NOTIFICATION_RECEIVED").apply {
                    putExtra("package", packageName)
                    putExtra("title", title)
                    putExtra("text", text)
                    putExtra("timestamp", notification.postTime)
                }
                sendBroadcast(intent)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)

        sbn?.let { notification ->
            val packageName = notification.packageName
            if (targetPackages.contains(packageName)) {
                Log.d("FilteredNotificationListener", "필터된 알림 제거됨: $packageName")
            }
        }
    }
}