package com.uselessdev.tetramenai

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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.example.tetramenai.MessageQueue
import java.util.Calendar
import com.uselessdev.tetramenai.DataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.jar.Attributes

class Logger : AppCompatActivity() {
    private lateinit var stringstorage: StringStorage
    private lateinit var messagestorage: StringStorage
    private lateinit var deeplearnstorage: StringStorage
    private lateinit var pushNotification: PushNotification
    private lateinit var namestorage: StringStorage
    private lateinit var rawdata: StringStorage
    private lateinit var messagequeue: MessageQueue
    private var safetitle: String = ""
    private var safetext: String = ""
    private val PERMISSION_REQUEST_CODE = 1000

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
                val serviceIntent = Intent(context, ForegroundService::class.java)
                context!!.startForegroundService(serviceIntent)
            }
            Log.d("Logger", "브로드캐스트 수신됨!")

            val packageName = intent.getStringExtra("package")
            val title = intent.getStringExtra("title")
            val text = intent.getStringExtra("text")
            val timestamp = intent.getLongExtra("timestamp", 0L)

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
        rawdata = StringStorage(this)
        deeplearnstorage = StringStorage(this)
        namestorage = StringStorage(this)

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

        val gotonamemapviewer = findViewById<Button>(R.id.gotonamemapview)

        gotonamemapviewer.setOnClickListener{
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val intent = Intent(this, namemapviewer::class.java)
            startActivity(intent)
        }

        messagequeue = MessageQueue { reply ->
            Log.d("Logger", "콜백 호출됨")
            register(reply)
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

    //---------------------------------실질적인 데이터 처리 구간 ---------------------------------------------------------


    private fun updateNotificationList(packageName: String?, title: String?, text: String?, timestamp: Long) {
        try {
            //패키지명 exeption
            val packageexeption = DataBase.packageexeption

            if ((title == null && text == null) ||
                (stringstorage.getString("svlog")
                    .contains("${text}") && stringstorage.getString("svlog")
                    .contains("${java.util.Date(timestamp)}")) ||
                //일반 예외처리
                (packageName != null && packageexeption.any { packageName.contains(it) }) ||
                //특수 예외처리
                (packageName == "com.samsung.android.messaging" && text == "메시지 보기")
            ) {
                return
            }

            applylog("패키지: $packageName\n제목: $title\n내용: $text\n시간: ${java.util.Date(timestamp)}\n\n")
            //            stringstorage.saveString("svlog", "패키지: $packageName\n제목: $title\n내용: $text\n시간: ${java.util.Date(timestamp)}\n\n" + stringstorage.getString("svlog"))

            val logg = findViewById<TextView>(R.id.tv1)


            val rawuserPrefs = stringstorage.getString("preferences") ?: ""
            //이름 가명 처리
            val namechart = DataBase.namechart //전역 처리 가능
            val famousnamechart = DataBase.FamousNames

            //메시지 개인정보 가림 처리
            //1단계: 이름 가명 처리
            var splittedprompt = rawuserPrefs.split(" ", "\n").toMutableList() //단어 단위로 나눔

            for (index in splittedprompt.indices) {
                val word = splittedprompt[index]
                if (namechart.any { word.contains(it) } &&
                    !famousnamechart.any { word.contains(it) }
                ) {
                    val found = namechart
                        .mapNotNull { candidate ->
                            val idx = word.indexOf(candidate)
                            if (idx >= 0) candidate to idx else null
                        }
                        .minByOrNull { it.second }  // 문자열에서 가장 앞에 있는 것 선택

                    if(found != null) {
                        val (match, position) = found
                        val name = splittedprompt[index].substring(position, position) //이름 부분만 선택
                        var front = ""
                        var back = ""
                        if(position != 0) front = splittedprompt[index].substring(0, position-1)
                        if(position+1 < splittedprompt[index].length) back = splittedprompt[index].substring(position, splittedprompt[index].length-1)
                        if(front != "") splittedprompt[index] = front
                        splittedprompt.add(index+1,name)
                        if(back != "") splittedprompt.add(index+2, back)
                    }
                }
            }
            //2딘계: 추가 개인정보 가림 처리
            val processeduserPrefs = PrivacyMasker().mask(splittedprompt.toString())

            applylog("유저 설정 변경됨: $processeduserPrefs")


            val randomname = RandomNameGenerator
            val systemPrompt = """""".trimIndent()

            //------------------------------------------------------

            var safeTitle = title ?: ""
            var safeText = text ?: ""
            val safePackageName = packageName ?: ""
            val safetime = java.util.Date(timestamp) ?: ""

            //이름 가명 처리
//            val namechart = DataBase.namechart //전역 처리 가능
//            val famousnamechart = DataBase.FamousNames

            if (namechart.any { safeTitle.contains(it) }) safeTitle =
                NameMap(namestorage).getnamemap(safeTitle)

            //메시지 개인정보 가림 처리
            //1단계: 이름 가명 처리
            var splittedmessage = safeText.split(" ", "\n").toMutableList() //단어 단위로 나눔

            for (index in splittedmessage.indices) {
                val word = splittedmessage[index]
                if (namechart.any { word.contains(it) } &&
                    !famousnamechart.any { word.contains(it) }
                ) {
                    val found = namechart
                        .mapNotNull { candidate ->
                            val idx = word.indexOf(candidate)
                            if (idx >= 0) candidate to idx else null
                        }
                        .minByOrNull { it.second }  // 문자열에서 가장 앞에 있는 것 선택

                    if(found != null) {
                        val (match, position) = found
                        val name =
                            splittedprompt[index].substring(position, position) //이름 부분만 선택
                        var front = ""
                        var back = ""
                        if (position != 0) front = splittedprompt[index].substring(0, position - 1)
                        if (position + 1 < splittedprompt[index].length) back =
                            splittedprompt[index].substring(
                                position,
                                splittedprompt[index].length - 1
                            )
                        if(front != "") splittedprompt[index] = front
                        splittedprompt.add(index + 1, name)
                        if(back != "") splittedprompt.add(index + 2, back)
                    }
                }
            }
            //2딘계: 추가 개인정보 가림 처리

            val namemaskedmessage = splittedmessage.toString()

            val originalname = NameMap(namestorage).getnamemapbynewname(safeTitle)
            // 안전하게 null fallback
            MessageMap(messagestorage, rawdata).mesasagemask(
                this,
                originalname,   // null이면 원래 제목 사용
                namemaskedmessage,
                safetime.toString()
            )
            //이제 개인정보 처리는 DataManager 가 담당함


            //변경된 내용으로 userPrompt 적용
            val userPrompt = """
        패키지: $safePackageName
        제목: $safeTitle
        내용: ${
                MessageMap(messagestorage, rawdata).getlatestmessage(
                    this,
                    NameMap(namestorage).getnamemapbynewname(safeTitle)
                )
            }
        시간: $safetime
    """.trimIndent()

            //userPrompt null 예외처리
            if (userPrompt.isBlank()) {
                Log.e("NULLERROR", "⚠️ systemPrompt 또는 userPrompt가 비어 있음")
                return
            }

            safetitle = safeTitle
            safetext = safeText

            //--------GPT 요청 생성 구역----------------------------------------------------------------------------

            if(stringstorage.getString("DeepLearningEnable", "") == "0") messagequeue.register("Norm", userPrompt, systemPrompt, null)
            else messagequeue.register("MsgDL", userPrompt, deeplearnstorage.getString(safeTitle, "@@@@@@@") , systemPrompt)

        } catch (e: Exception) {
            Log.e("Logger", "알림 업데이트 실패", e)
        }
    }

    //GPT reply 처리

    private var isrunning: Boolean = false

    private val replyqueue = ArrayDeque<String>()

    fun register(reply: String){
        replyqueue.add(reply)
        if(!isrunning){
            isrunning = true
            LoggerRunQueueUntilDepleted()
        }
    }

    private fun LoggerRunQueueUntilDepleted() {
        CoroutineScope(Dispatchers.IO).launch {
            while (replyqueue.isNotEmpty()) {
                val reply = replyqueue.removeFirst()

                withContext(Dispatchers.Main) {
                    Log.d("GPT 응답", reply)
                    if (reply != "0") {
                        applylog(reply)
                        if (!reply.contains("This endpoint is deprecated")) {
                            if (ActivityCompat.checkSelfPermission(
                                    applicationContext,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                val renormalisedTitle = NameMap(namestorage).getnamemapbynewname(safetitle)
                                var splittedresultText = safetext.split(" ", "\n").toMutableList()
                                for (i in splittedresultText.indices) {
                                    val namemap = NameMap(namestorage)
                                    val isvalidname = namemap.getnamemapbynewname(splittedresultText[i])
                                    if (isvalidname == "") continue
                                    splittedresultText[i] = isvalidname
                                }
                                val renormalisedText = splittedresultText.joinToString(" ")
                                pushNotification.sendBasicNotification(
                                    "중요한 메시지: $renormalisedTitle",
                                    renormalisedText
                                )
                            }
                        }
                    }
                }
            }
            isrunning = false
        }
    }


    private fun applylog(logvalue:String = ""){
        val logg = findViewById<TextView>(R.id.tv1)
        stringstorage.saveString("svlog", "$logvalue\n\n" + stringstorage.getString("svlog"))
        logg?.text = stringstorage.getString("svlog")
    }
}