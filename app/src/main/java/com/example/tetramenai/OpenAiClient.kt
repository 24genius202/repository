package com.uselessdev.tetramenai

import android.util.Log
import android.widget.TextView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object OpenAiClient {
    private const val TAG = "OpenAiClient"
    private const val SERVER_URL_1 = "https://www.yjproj.org/tetramenai-gpt/msg" // Raspberry Pi IP
    private const val SERVER_URL_2 = "https://www.yjproj.org/tetramenai-gpt/deeplearn"
    private const val SERVER_URL_3 = "https://www.yjproj.org/tetramenai-gpt/msgdl"

    private val client = OkHttpClient()

    fun sendMessages(systemPrompt: String, userPrompt: String, callback: (String?) -> Unit) {
        // 메시지 구성
        val safeSystemPrompt = systemPrompt ?: ""
        val safeUserPrompt = userPrompt ?: ""

        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", "Background Information: $safeSystemPrompt"))
            put(JSONObject().put("role", "user").put("content", "Message: $safeUserPrompt"))
        }

        val json = JSONObject().put("messages", messages)

        Log.d(TAG, "Sending JSON to server: ${json.toString(2)}")

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(SERVER_URL_1)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "요청 실패", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val reply = JSONObject(responseBody ?: "{}").optString("reply", null)
                    callback(reply)
                } else {
                    Log.e(TAG, "응답 실패: ${response.code}")
                    callback(null)
                }
            }
        })
    }

    fun sendDeepLearnMessages(systemPrompt: String, userPrompt: String, callback: (String?) -> Unit) {
        // 메시지 구성
        val safeSystemPrompt = systemPrompt ?: ""
        val safeUserPrompt = userPrompt ?: ""

        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", "Background Information: $safeSystemPrompt"))
            put(JSONObject().put("role", "user").put("content", "Message: $safeUserPrompt"))
        }

        val json = JSONObject().put("messages", messages)

        Log.d(TAG, "Sending JSON to server: ${json.toString(2)}")

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(SERVER_URL_2)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "요청 실패", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val reply = JSONObject(responseBody ?: "{}").optString("reply", null)
                    callback(reply)
                } else {
                    Log.e(TAG, "응답 실패: ${response.code}")
                    callback(null)
                }
            }
        })
    }

    fun sendMessageswithDeepLearn(systemPrompt1: String, systemPrompt2: String, userPrompt: String, callback: (String?) -> Unit){
        // 메시지 구성
        val safeSystemPrompt1 = systemPrompt1 ?: ""
        val safeSystemPrompt2 = systemPrompt2 ?: ""
        val safeUserPrompt = userPrompt ?: ""

        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", "Previous Assistant's Estimation: $safeSystemPrompt1"))
            put(JSONObject().put("role", "system").put("content", "Background Information(User wants): $safeSystemPrompt2"))
            put(JSONObject().put("role", "user").put("content", "Message: $safeUserPrompt"))
        }

        val json = JSONObject().put("messages", messages)

        Log.d(TAG, "Sending JSON to server: ${json.toString(2)}")

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(SERVER_URL_3)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "요청 실패", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val reply = JSONObject(responseBody ?: "{}").optString("reply", null)
                    callback(reply)
                } else {
                    Log.e(TAG, "응답 실패: ${response.code}")
                    callback(null)
                }
            }
        })
    }
}