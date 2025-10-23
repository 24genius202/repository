package com.example.tetramenai

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.uselessdev.tetramenai.NameMap
import com.uselessdev.tetramenai.OpenAiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MessageQueue {

    private var isrunning: Boolean = false

    private val messagequeue = ArrayDeque<Pair<String, Pair<String, Pair<String, String>>>>()

    fun register(msgtype: String, userPrompt: String, systemPrompt1: String, systemPrompt2: String?){
        messagequeue.add(Pair(msgtype, Pair(userPrompt, Pair(systemPrompt1, systemPrompt2 ?: ""))))
        if(!isrunning){
            isrunning = true
            RunQueueUntilDepleted()
        }
    }

    private fun RunQueueUntilDepleted(){
        CoroutineScope(Dispatchers.IO).launch {
            while(!messagequeue.isEmpty()){
                val msgdata = messagequeue.first()

                val msgtype = msgdata.first
                val userPrompt = msgdata.second.first
                val systemPrompt1 = msgdata.second.second.first
                val systemPrompt2 = msgdata.second.second.second

                var reply: String?

                when(msgtype){
                    "Norm" -> {
                        reply = OpenAiClient.sendMessagesSuspend(systemPrompt = systemPrompt1, userPrompt = userPrompt)
                    }
                    "MsgDL" -> {
                        reply = OpenAiClient.sendMessageswithDeepLearnSuspend(systemPrompt1 = systemPrompt1, systemPrompt2 = systemPrompt2, userPrompt = userPrompt)
                    }
                    "DL" -> {
                        reply = OpenAiClient.sendDeepLearnMessagesSuspend(systemPrompt = systemPrompt1, userPrompt = userPrompt)
                    }
                }


                //다른 코드에서 이 구조와 비슷하게 register 받고 순차적으로 후처리 실행
            }
        }
        isrunning = false
        return
    }
}