package com.example.aliolio

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.Date

class Notification : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let { notification ->
            val packageName = notification.packageName
            val extras = notification.notification.extras

            // 알림 정보 추출
            val title = extras.getString(Notification.EXTRA_TITLE)
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            val timestamp = notification.postTime

            Log.d("NotificationListener", """
                패키지: $packageName
                제목: $title
                내용: $text
                큰 텍스트: $bigText
                부제목: $subText
                시간: ${Date(timestamp)}
            """.trimIndent())

            // 브로드캐스트로 다른 컴포넌트에 전달
            val intent = Intent("NOTIFICATION_RECEIVED").apply {
                // 명시적 브로드캐스트로 변경 (Android 8.0+ 호환성)
                setPackage(packageName)
                putExtra("package", packageName)
                putExtra("title", title)
                putExtra("text", text)
                putExtra("bigText", bigText)
                putExtra("subText", subText)
                putExtra("timestamp", timestamp)
            }

            try {
                sendBroadcast(intent)
                Log.d("NotificationListener", "브로드캐스트 전송됨: $packageName")
            } catch (e: Exception) {
                Log.e("NotificationListener", "브로드캐스트 전송 실패", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d("NotificationListener", "알림 제거됨: ${sbn?.packageName}")
    }
}