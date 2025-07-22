package com.example.aliolio

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("MainActivity", "브로드캐스트 수신됨!")

            val packageName = intent?.getStringExtra("package")
            val title = intent?.getStringExtra("title")
            val text = intent?.getStringExtra("text")
            val timestamp = intent?.getLongExtra("timestamp", 0L)

            Log.d("MainActivity", "받은 데이터 - 패키지: $packageName, 제목: $title, 내용: $text")

            // UI 업데이트
            runOnUiThread {
                // 여기서 받은 알림 정보를 처리
                if(timestamp != null) updateNotificationList(packageName, title, text, timestamp)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 첫 번째로 로그 확인
        Log.d("MainActivity", "========== MainActivity onCreate 시작됨 ==========")

        try {
            setContentView(R.layout.activity_main)
            Log.d("MainActivity", "setContentView 완료")
        } catch (e: Exception) {
            Log.e("MainActivity", "setContentView 실패", e)
            return
        }

        try {
            // 브로드캐스트 리시버 등록
            registerNotificationReceiver()
            Log.d("MainActivity", "BroadcastReceiver 등록 시도 완료")
        } catch (e: Exception) {
            Log.e("MainActivity", "BroadcastReceiver 등록 실패", e)
        }

        try {
            // 알림 접근 권한 확인
            if (!isNotificationServiceEnabled()) {
                Log.d("MainActivity", "알림 접근 권한이 없음 - 권한 요청")
                requestNotificationPermission()
            } else {
                Log.d("MainActivity", "알림 접근 권한이 있음")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "권한 확인 실패", e)
        }

        Log.d("MainActivity", "========== MainActivity onCreate 완료됨 ==========")
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart 호출됨")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume 호출됨")

        // onResume에서도 리시버 등록 확인
        try {
            registerNotificationReceiver()
        } catch (e: Exception) {
            Log.e("MainActivity", "onResume에서 리시버 등록 실패", e)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause 호출됨")

        // onPause에서 리시버 해제
        unregisterNotificationReceiver()
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop 호출됨")
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerNotificationReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction("NOTIFICATION_RECEIVED")
                // 패키지 명시적으로 설정 (Android 8.0+ 대응)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // 명시적 브로드캐스트로 변경
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(notificationReceiver, filter)
            }

            Log.d("MainActivity", "BroadcastReceiver 등록됨")
        } catch (e: Exception) {
            Log.e("MainActivity", "BroadcastReceiver 등록 실패", e)
        }
    }

    private fun unregisterNotificationReceiver() {
        try {
            unregisterReceiver(notificationReceiver)
            Log.d("MainActivity", "BroadcastReceiver 해제됨")
        } catch (e: IllegalArgumentException) {
            Log.w("MainActivity", "BroadcastReceiver가 이미 해제됨", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNotificationReceiver()
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val packageName = packageName
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )

        if (flat != null && flat.isNotEmpty()) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val componentName = ComponentName.unflattenFromString(name)
                if (componentName != null) {
                    if (packageName == componentName.packageName) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun requestNotificationPermission() {
        AlertDialog.Builder(this)
            .setTitle("알림 접근 권한 필요")
            .setMessage("앱이 알림을 읽으려면 알림 접근 권한이 필요합니다.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateNotificationList(packageName: String?, title: String?, text: String?, timestamp: Long) {
        // 알림 정보를 UI에 표시하는 로직
        // 예: RecyclerView 업데이트, 텍스트뷰 업데이트 등
        Log.d("MainActivity", "새 알림: $packageName - $title: $text")
    }
}