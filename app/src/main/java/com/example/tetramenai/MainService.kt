package com.uselessdev.tetramenai

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.app.Notification
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.tetramenai.MessageQueue

class MainService : Service() {
    // CoroutineScope for service-wide coroutines
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var stringstorage: StringStorage
    private lateinit var messagestorage: StringStorage
    private lateinit var deeplearnstorage: StringStorage
    private lateinit var pushNotification: PushNotification
    private lateinit var namestorage: StringStorage
    private lateinit var rawdata: StringStorage
    private lateinit var messagequeue: MessageQueue
    private var safetitle: String = ""
    private var safetext: String = ""

    // LocalBroadcastManager 기반 리시버
    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "NOTIFICATION_RECEIVED") {
                Log.d("MainService", "LocalBroadcastManager: NOTIFICATION_RECEIVED 수신됨")
                // 코루틴에서 OpenAiClient.sendMessagesSuspend 실행
                serviceScope.launch(Dispatchers.IO) {
                    try {
                        val packageName = intent.getStringExtra("package")
                        val title = intent.getStringExtra("title")
                        val text = intent.getStringExtra("text")
                        val timestamp = intent.getLongExtra("timestamp", 0L)
                        // 기존 processNotification 로직을 그대로 사용
                        processNotification(this@MainService, packageName, title, text, timestamp)
                        Log.d("MainService", "OpenAiClient.sendMessagesSuspend 실행 완료")
                    } catch (e: Exception) {
                        Log.e("MainService", "LocalBroadcastManager 코루틴 예외", e)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        stringstorage = StringStorage(this)
        messagestorage = StringStorage(this)
        deeplearnstorage = StringStorage(this)
        pushNotification = PushNotification(this)
        namestorage = StringStorage(this)
        rawdata = StringStorage(this)

        DeepLearnStats().setnamestorage(namestorage)

        DeepLearnManager().initstorages(stringstorage, deeplearnstorage, namestorage, rawdata, messagestorage)

        // LocalBroadcastManager 리시버 항상 등록
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            localReceiver,
            IntentFilter("NOTIFICATION_RECEIVED")
        )

        // 포그라운드 알림 표시
        startForeground(1, createForegroundNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        DailyWorker.schedule(this)

        messagequeue = MessageQueue { reply ->
            Log.d("Logger", "콜백 호출됨")
            register(reply)
        }



        Log.d("MainService", "")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 포그라운드 알림은 onCreate에서 이미 표시됨, 필요시 다시 표시
        startForeground(1, createForegroundNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        return START_STICKY // 강제 종료 시 자동 재시작
    }

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Launch processing in serviceScope coroutine
            serviceScope.launch {
                val packageName = intent?.getStringExtra("package")
                val title = intent?.getStringExtra("title")
                val text = intent?.getStringExtra("text")
                val timestamp = intent?.getLongExtra("timestamp", 0L) ?: 0L
                processNotification(this@MainService, packageName, title, text, timestamp)
            }
        }
    }

    private suspend fun processNotification(service: Context, packageName: String?, title: String?, text: String?, timestamp: Long) {
        try {
            //패키지명 exeption
                val packageexeption = DataBase.packageexeption
                // Null-safe access for title/text, and timestamp fallback
                var safeTitle = title ?: ""
                val safeText = text ?: ""
                val safePackageName = packageName ?: ""
                val safetime = java.util.Date(timestamp)

                if ((safeTitle.isBlank() && safeText.isBlank()) ||
                    (stringstorage.getString("svlog")
                        .contains("$safeText") && stringstorage.getString("svlog")
                        .contains("$safetime")) ||
                    //일반 예외처리
                    (safePackageName.isNotBlank() && packageexeption.any { safePackageName.contains(it) }) ||
                    //특수 예외처리
                    (safePackageName == "com.samsung.android.messaging" && safeText == "메시지 보기")
                ) {
                    return
                }

//            applylog("패키지: $packageName\n제목: $title\n내용: $text\n시간: ${java.util.Date(timestamp)}\n\n")
            //            stringstorage.saveString("svlog", "패키지: $packageName\n제목: $title\n내용: $text\n시간: ${java.util.Date(timestamp)}\n\n" + stringstorage.getString("svlog"))

//            val logg = findViewById<TextView>(R.id.tv1)


            val rawuserPrefs = stringstorage.getString("preferences")
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

//            applylog("유저 설정 변경됨: $processeduserPrefs")


            val randomname = RandomNameGenerator
            val systemPrompt = """""".trimIndent()

            //------------------------------------------------------

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
                service,
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
                    service,
                    NameMap(namestorage).getnamemapbynewname(safeTitle)
                )
            }
        시간: $safetime
    """.trimIndent()

            //userPrompt null 예외처리
            if (userPrompt.isBlank()) {
                Log.e("NULLERROR", "⚠️ systemPrompt 또는 userPrompt가 비어 있음")
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

    private fun createForegroundNotification(): Notification {
        val channelId = "main_service_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                "Main Service",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.tetramenai)
            .setContentTitle("TetramenAI 백그라운드 실행중")
            .setContentText("")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        // LocalBroadcastManager 리시버 해제
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(localReceiver)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}