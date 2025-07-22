package com.example.aliolio

import GPT
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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.savedstate.serialization.saved
import java.sql.Date
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    var savedlog: String = ""

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

    private var isReceiverRegistered = false

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

        // onResume에서 리시버 등록
        if (!isReceiverRegistered) {
            try {
                registerNotificationReceiver()
            } catch (e: Exception) {
                Log.e("MainActivity", "onResume에서 리시버 등록 실패", e)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause 호출됨")

        // onPause에서는 리시버를 해제하지 않음 (백그라운드에서도 수신 가능하도록)
        // unregisterNotificationReceiver()
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop 호출됨")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy 호출됨")
        unregisterNotificationReceiver()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerNotificationReceiver() {
        try {
            if (isReceiverRegistered) {
                Log.d("MainActivity", "BroadcastReceiver가 이미 등록됨")
                return
            }

            val filter = IntentFilter().apply {
                addAction("NOTIFICATION_RECEIVED")
            }

            // LocalBroadcastManager 사용 (같은 앱 내에서 통신)
            LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver, filter)

            // 또는 일반 브로드캐스트도 함께 등록 (안전을 위해)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(notificationReceiver, filter)
            }

            isReceiverRegistered = true
            Log.d("MainActivity", "BroadcastReceiver 등록됨 (LocalBroadcast + Normal)")
        } catch (e: Exception) {
            Log.e("MainActivity", "BroadcastReceiver 등록 실패", e)
        }
    }

    private fun unregisterNotificationReceiver() {
        if (!isReceiverRegistered) {
            Log.d("MainActivity", "BroadcastReceiver가 등록되지 않음")
            return
        }

        try {
            // LocalBroadcastManager 해제
            LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver)

            // 일반 브로드캐스트 해제
            unregisterReceiver(notificationReceiver)

            isReceiverRegistered = false
            Log.d("MainActivity", "BroadcastReceiver 해제됨")
        } catch (e: IllegalArgumentException) {
            Log.w("MainActivity", "BroadcastReceiver가 이미 해제됨", e)
            isReceiverRegistered = false
        } catch (e: Exception) {
            Log.e("MainActivity", "BroadcastReceiver 해제 실패", e)
        }
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

    private suspend fun preprocess(message: String, bginfo: String): String?{
        val gpt = GPT
        val response = gpt.askGPTWithHistory(message, "Conversation History + $bginfo")
        return response
    }

    @SuppressLint("SetTextI18n")
    private fun updateNotificationList(packageName: String?, title: String?, text: String?, timestamp: Long) {
        // 알림 정보를 UI에 표시하는 로직
        // 예: RecyclerView 업데이트, 텍스트뷰 업데이트 등
        //if(((title != null && text != null) && !(savedlog.contains("${text}") && savedlog.contains("${java.util.Date(timestamp)}"))) || packageName!! != "com.android.systemui") savedlog = "패키지: $packageName\n제목: $title\n내용: $text\n시간: ${java.util.Date(timestamp)}\n\n" + savedlog
        if((title == null && text == null) || (savedlog.contains("${text}") && savedlog.contains("${java.util.Date(timestamp)}")) || packageName!! == "com.android.systemui" || (packageName == "com.samsung.android.messaging" && text == "메시지 보기"))
        else savedlog = "패키지: $packageName\n제목: $title\n내용: $text\n시간: ${java.util.Date(timestamp)}\n\n" + savedlog
        val logg = findViewById<TextView>(R.id.tv1)
        val info = findViewById<EditText>(R.id.ed1)
        val button = findViewById<ImageButton>(R.id.btn1)
        var bginfo: String = ""
        button.setOnClickListener{
            bginfo = info.text.toString()
        }
        logg.text = savedlog
        Log.d("MainActivity", "새 알림: $packageName - $title: $text (시간: $timestamp)")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        fun runGPT(){
            scope.launch {
                val result = preprocess("패키지: $packageName\n" + "제목: $title\n" + "내용: $text\n" + "시간: ${java.util.Date(timestamp)}\n" + "\n", bginfo)
                if(result != "0"){
                    //알림 주는 코드
                    Log.d("MainActivity", "Return Success!")
                }
            }
        }
    }
}