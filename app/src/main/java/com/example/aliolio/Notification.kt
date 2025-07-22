package com.example.aliolio

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.Date

class Notification : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let { notification ->
            val packageName = notification.packageName
            val extras = notification.notification.extras

            // 알림 정보 추출
            val title = extras.getString(android.app.Notification.EXTRA_TITLE)
            val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
            val bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString()
            val subText = extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT)?.toString()
            val timestamp = notification.postTime

            Log.d("NotificationListener", """
                패키지: $packageName
                제목: $title
                내용: $text
                큰 텍스트: $bigText
                부제목: $subText
                시간: ${Date(timestamp)}
            """.trimIndent())

            // 브로드캐스트로 MainActivity에 전달
            sendNotificationBroadcast(packageName, title, text, bigText, subText, timestamp)
        }
    }

    private fun sendNotificationBroadcast(
        packageName: String,
        title: String?,
        text: String?,
        bigText: String?,
        subText: String?,
        timestamp: Long
    ) {
        val intent = Intent("NOTIFICATION_RECEIVED").apply {
            // 자신의 앱 패키지로 설정 (MainActivity가 있는 앱)
            setPackage("com.example.aliolio")
            putExtra("package", packageName)  // 알림을 보낸 앱의 패키지명
            putExtra("title", title)
            putExtra("text", text ?: bigText) // text가 없으면 bigText 사용
            putExtra("bigText", bigText)
            putExtra("subText", subText)
            putExtra("timestamp", timestamp)
        }

        try {
            // LocalBroadcastManager로 전송 (같은 앱 내)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            Log.d("NotificationListener", "LocalBroadcast 전송됨")

            // 일반 브로드캐스트로도 전송 (안전을 위해)
            sendBroadcast(intent)
            Log.d("NotificationListener", "일반 브로드캐스트 전송됨: $packageName")
        } catch (e: Exception) {
            Log.e("NotificationListener", "브로드캐스트 전송 실패", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d("NotificationListener", "알림 제거됨: ${sbn?.packageName}")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationListener", "NotificationListenerService 연결됨")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("NotificationListener", "NotificationListenerService 연결 해제됨")
    }
}