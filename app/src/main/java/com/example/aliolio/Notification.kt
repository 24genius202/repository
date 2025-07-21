package com.example.aliolio

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class Notification : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")

        Log.d("NotificationListener", "Posted: $packageName -> $title: $text")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        Log.d("NotificationListener", "Removed: $packageName")
    }
}