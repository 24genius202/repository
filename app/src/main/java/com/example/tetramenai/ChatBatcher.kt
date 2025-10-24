package com.example.tetramenai

import kotlinx.coroutines.*
import android.util.Log
import com.uselessdev.tetramenai.DataBase

class ChatBatcher(
    private val onBatchReady: (List<DataBase.ChatMessage>) -> Unit
) {
    private val batch = mutableListOf<DataBase.ChatMessage>()
    private var lastTimestamp = 0L
    private var flushJob: Job? = null

    // 1분 기준 (밀리초)
    private val INACTIVITY_LIMIT = 60_000L

    fun registerMessage(msg: DataBase.ChatMessage) {
        val text = msg.text.trim()
        if (text.isEmpty()) return

        // 무의미어 단독이면 무시
        if (DataBase.non_important_words.contains(text)) return

        // 시간 차 기준으로 batch 분리
        val delta = msg.timestamp - lastTimestamp
        lastTimestamp = msg.timestamp
        if (delta > 5 * 60 * 1000) flush()

        // batch에 추가
        batch.add(msg)
        Log.d("ChatBatcher", "메시지 추가됨: ${msg.text}")

        //응답어면 즉시 flush
        if (DataBase.end_of_topic_words.any { text.endsWith(it) }) {
            flush()
            return
        }

        //타이머 리셋
        resetFlushTimer()
    }

    private fun resetFlushTimer() {
        flushJob?.cancel()
        flushJob = CoroutineScope(Dispatchers.IO).launch {
            delay(INACTIVITY_LIMIT)
            Log.d("ChatBatcher", "1분간 새 메시지 없음 → 자동 flush")
            flush()
        }
    }

    private fun flush() {
        if (batch.isEmpty()) return
        Log.d("ChatBatcher", "Batch flush됨 (${batch.size}개 메시지)")
        onBatchReady(batch.toList())
        batch.clear()
        flushJob?.cancel()
        flushJob = null
    }
}