package com.example.aliolio

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import androidx.core.net.toUri
import androidx.core.view.isVisible
import java.util.Date
import java.util.Calendar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Logger : AppCompatActivity() {
    private lateinit var stringstorage: StringStorage
    private lateinit var messagestorage: StringStorage
    private lateinit var deeplearnstorage: StringStorage
    private lateinit var pushNotification: PushNotification
    private val PERMISSION_REQUEST_CODE = 1000

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val serviceIntent = Intent(context, ForegroundService::class.java)
                    context!!.startForegroundService(serviceIntent)
                } else {
                    val serviceIntent = Intent(context, ForegroundService::class.java)
                    context!!.startService(serviceIntent)
                }
            }
            Log.d("Logger", "브로드캐스트 수신됨!")

            val packageName = intent?.getStringExtra("package")
            val title = intent?.getStringExtra("title")
            val text = intent?.getStringExtra("text")
            val timestamp = intent?.getLongExtra("timestamp", 0L)

            Log.d("Logger", "받은 데이터 - 패키지: $packageName, 제목: $title, 내용: $text")

            // UI 업데이트
            runOnUiThread {
                if(timestamp != null) updateNotificationList(packageName, title, text, timestamp)
            }
        }
    }

    private var isReceiverRegistered = false


    @SuppressLint("ServiceCast", "BatteryLife", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stringstorage = StringStorage(this)
        messagestorage = StringStorage(this)
        deeplearnstorage = StringStorage(this)

        pushNotification = PushNotification(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = "package:$packageName".toUri()
                startActivity(intent)
            }
        }

        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        var lastday = stringstorage.getString("lastday", "0")

        if(stringstorage.getString("DeepLearningEnable", "0") != "0" && day.toString() != lastday && hour == 4){
            stringstorage.saveString("lastday", day.toString())
            deeplearncycle()
        }

        // 알림 권한 요청 (Android 13 이상)
        //requestNotificationPermission()

        try {
            setContentView(R.layout.logger)
            Log.d("Logger", "logger 레이아웃 설정 완료")
        } catch (e: Exception) {
            Log.e("Logger", "레이아웃 설정 실패", e)
            // 레이아웃 파일이 없을 경우 기본 레이아웃 사용
            setContentView(android.R.layout.activity_list_item)
            return
        }

        Log.d("Logger", "========== Logger onCreate 시작됨 ==========")

        try {
            // 뒤로가기 버튼 설정
            val backButton = findViewById<Button>(R.id.goback)
            backButton?.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                Log.d("Logger", "뒤로가기 버튼 클릭")

                finish()
            }
        } catch (e: Exception) {
            Log.e("Logger", "뒤로가기 버튼 설정 실패", e)
        }

        try {
            // 알림 접근 권한 확인
            if (!isNotificationServiceEnabled()) {
                Log.d("Logger", "알림 접근 권한이 없음 - 권한 요청")
                requestNotificationPermission()
            } else {
                Log.d("Logger", "알림 접근 권한이 있음")
            }
        } catch (e: Exception) {
            Log.e("Logger", "권한 확인 실패", e)
        }

        Log.d("Logger", "========== Logger onCreate 완료됨 ==========")

        val logg = findViewById<TextView>(R.id.tv1)

        logg.text = stringstorage.getString("svlog")

        val reset = findViewById<Button>(R.id.reset)

        reset.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            stringstorage.saveString("svlog", "")
            logg.text = stringstorage.getString("svlog")
        }

        val gotodeeplearnstats = findViewById<Button>(R.id.gotodeeplearnstats)

        gotodeeplearnstats.isVisible = stringstorage.getString("DeepLearningEnable", "0") != "0"

        gotodeeplearnstats.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val intent = Intent(this, DeepLearnStats::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Logger", "onStart 호출됨")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Logger", "onResume 호출됨")

        if (!isReceiverRegistered) {
            try {
                registerNotificationReceiver()
            } catch (e: Exception) {
                Log.e("Logger", "onResume에서 리시버 등록 실패", e)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("Logger", "onPause 호출됨")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Logger", "onStop 호출됨")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Logger", "onDestroy 호출됨")
        unregisterNotificationReceiver()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerNotificationReceiver() {
        try {
            if (isReceiverRegistered) {
                Log.d("Logger", "BroadcastReceiver가 이미 등록됨")
                return
            }

            val filter = IntentFilter().apply {
                addAction("NOTIFICATION_RECEIVED")
            }

            LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver, filter)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(notificationReceiver, filter)
            }

            isReceiverRegistered = true
            Log.d("Logger", "BroadcastReceiver 등록됨")
        } catch (e: Exception) {
            Log.e("Logger", "BroadcastReceiver 등록 실패", e)
        }
    }

    private fun unregisterNotificationReceiver() {
        if (!isReceiverRegistered) {
            Log.d("Logger", "BroadcastReceiver가 등록되지 않음")
            return
        }

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver)
            unregisterReceiver(notificationReceiver)
            isReceiverRegistered = false
            Log.d("Logger", "BroadcastReceiver 해제됨")
        } catch (e: IllegalArgumentException) {
            Log.w("Logger", "BroadcastReceiver가 이미 해제됨", e)
            isReceiverRegistered = false
        } catch (e: Exception) {
            Log.e("Logger", "BroadcastReceiver 해제 실패", e)
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
        try {
            AlertDialog.Builder(this)
                .setTitle("알림 접근 권한 필요")
                .setMessage("앱이 알림을 읽으려면 알림 접근 권한이 필요합니다.")
                .setPositiveButton("설정으로 이동") { _, _ ->
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("취소", null)
                .show()
        } catch (e: Exception) {
            Log.e("Logger", "권한 요청 다이얼로그 실패", e)
        }
    }

    private fun updateNotificationList(packageName: String?, title: String?, text: String?, timestamp: Long) {
        try {
            if((title == null && text == null) ||
                (stringstorage.getString("svlog").contains("${text}") && stringstorage.getString("svlog").contains("${java.util.Date(timestamp)}")) ||
                packageName == "com.android.systemui" ||
                packageName == "com.samsung.android.incallui" ||
                packageName == "com.example.aliolio" ||
                (packageName == "com.samsung.android.messaging" && text == "메시지 보기")) {
                return
            }

            applylog("패키지: $packageName\n제목: $title\n내용: $text\n시간: ${java.util.Date(timestamp)}\n\n")
//            stringstorage.saveString("svlog", "패키지: $packageName\n제목: $title\n내용: $text\n시간: ${java.util.Date(timestamp)}\n\n" + stringstorage.getString("svlog"))

            val logg = findViewById<TextView>(R.id.tv1)

            val userPrefs = stringstorage.getString("preferences") ?: ""

            val systemPrompt = """""".trimIndent()
            val safeTitle = title ?: ""
            val safeText = text ?: ""
            val safePackageName = packageName ?: ""
            val safetime = java.util.Date(timestamp) ?: ""
            val userPrompt = """
        패키지: $safePackageName
        제목: $safeTitle
        내용: $safeText
        시간: $safetime
    """.trimIndent()

            if (userPrompt.isBlank()) {
                Log.e("NULLERROR", "⚠️ systemPrompt 또는 userPrompt가 비어 있음")
                return
            }

            val fixedpackagename = packageName!!.split(".")[1]

            if(stringstorage.getString("DeepLearningEnable", "0") == "0") {
                OpenAiClient.sendMessages(
                    systemPrompt = userPrefs, userPrompt = userPrompt
                ) { reply ->
                    if (reply != null) {
                        runOnUiThread {
                            Log.d("GPT 응답", reply)
                            if (reply != "0") {
                                applylog(reply)
                                if (!reply.contains("This endpoint is deprecated")) {
                                    if (ActivityCompat.checkSelfPermission(
                                            this,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        pushNotification.sendBasicNotification(
                                            "중요한 메시지: $safeTitle",
                                            safeText
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e("GPT 응답", "Null 응답")
                    }
                }
            } else{
                val client = messagestorage.getString(safeTitle + "@" + fixedpackagename, "")
//문자열 리스트 escape 처리 적용
                val newEntry = encode(listOf(safeText, java.util.Date(timestamp).toString()))

                if (client != "") {
                    messagestorage.saveString(
                        safeTitle + "@" + fixedpackagename,
                        client + " " + newEntry
                    )
                } else {
                    messagestorage.saveString(
                        safeTitle + "@" + fixedpackagename,
                        newEntry
                    )
                }

//문자열 리스트 escape 처리 적용
                val clients = messagestorage.getString("clients", "")
                val clientList = if (clients.isNotEmpty()) decode(clients).toMutableList() else mutableListOf()

                if (!clientList.contains(safeTitle + "@" + fixedpackagename)) {
                    if (client != "") {
                        clientList.add(safeTitle + "@" + fixedpackagename)
                        messagestorage.saveString("clients", encode(clientList)) // 🔧 escape 적용 저장
                    }
                }
                OpenAiClient.sendMessageswithDeepLearn(
                    systemPrompt1 = stringstorage.getString(title + "@" + fixedpackagename, "@@@@@@@") ,systemPrompt2 = userPrefs, userPrompt = userPrompt
                ) { reply ->
                    if (reply != null) {
                        runOnUiThread {
                            Log.d("GPT(DL) 응답", reply)
                            if (reply != "0") {
                                applylog(reply)
                                if (!reply.contains("This endpoint is deprecated")) {
                                    if (ActivityCompat.checkSelfPermission(
                                            this,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        pushNotification.sendBasicNotification(
                                            "중요한 메시지: $safeTitle",
                                            safeText
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e("GPT(DL) 응답", "Null 응답")
                    }
                }
            }

//            val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
//            scope.launch {
//                try {
//                    val result = preprocess("패키지: $packageName\n제목: $title\n내용: $text\n시간: ${java.util.Date(timestamp)}\n", stringstorage.getString("preferences"))
//                    if(result != "0" && result != null) {
//                        Log.d("Logger", "GPT 처리 성공!")
//                    }
//                    stringstorage.saveString("svlog", "$result \n\n" + stringstorage.getString("svlog"))
//                } catch (e: Exception) {
//                    Log.e("Logger", "GPT 코루틴 실패", e)
//                }
//            }
        } catch (e: Exception) {
            Log.e("Logger", "알림 업데이트 실패", e)
        }
    }

    private fun applylog(logvalue:String = ""){
        val logg = findViewById<TextView>(R.id.tv1)
        stringstorage.saveString("svlog", "$logvalue\n\n" + stringstorage.getString("svlog"))
        logg?.text = stringstorage.getString("svlog")
    }

    private fun deeplearncycle(){
        val clients = messagestorage.getString("clients", "")
        val clientList = if (clients.isNotEmpty()) decode(clients).map { it.trim() }.filter { it.isNotEmpty() } else emptyList()
// 🔧 decode 적용
        if (clients != "") {
            for (i in clientList) {
                val index = i
                // value separator는 @로 함
                // steve@kakaotalk: <관계>@<Formal>@<Friendly>@<Close>@<Transactional>@<Hierarchical>@<Conflicted>@<요약본>
                val usrPrompt = messagestorage.getString(index)

                OpenAiClient.sendDeepLearnMessages(
                    systemPrompt = deeplearnstorage.getString(index),
                    userPrompt = usrPrompt
                ) { reply ->
                    if (reply != null) {
                        runOnUiThread {
                            Log.d("DeepLearnCycle", reply)
                            deeplearnstorage.saveString(index, reply)
                            Log.d("DeepLearnCycle", "Updated Weight")
                        }
                    }
                }
            }
        }
    }

    fun escape(s: String): String = s.replace(",", "<<COMMA>>")
    fun unescape(s: String): String = s.replace("<<COMMA>>", ",")

    fun encode(list: List<String>): String = list.joinToString(",") { escape(it) }
    fun decode(encoded: String): List<String> = encoded.split(",").map { unescape(it) }
}