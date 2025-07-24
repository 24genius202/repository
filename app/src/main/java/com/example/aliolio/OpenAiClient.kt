package com.example.aliolio

import android.util.Log
import android.widget.TextView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object OpenAiClient {
    private const val TAG = "OpenAiClient"
    private const val SERVER_URL = "https://www.yjproj.org/ask" // Raspberry Pi IP

    private val client = OkHttpClient()

    fun sendMessages(systemPrompt: String, userPrompt: String, callback: (String?) -> Unit) {
        // 메시지 구성
        val safeSystemPrompt = systemPrompt ?: ""
        val safeUserPrompt = userPrompt ?: ""

        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", safeSystemPrompt))
            put(JSONObject().put("role", "user").put("content", safeUserPrompt))
        }

        val json = JSONObject().put("messages", messages)

        Log.d(TAG, "📤 Sending JSON to server: ${json.toString(2)}")

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(SERVER_URL)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "❌ 요청 실패", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val reply = JSONObject(responseBody ?: "{}").optString("reply", null)
                    callback(reply)
                } else {
                    Log.e(TAG, "❌ 응답 실패: ${response.code}")
                    callback(null)
                }
            }
        })
    }
}