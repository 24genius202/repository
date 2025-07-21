package com.example.aliolio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class gpt {

    suspend fun callChatGPTAPI(prompt: String, apiKey: String): String? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val mediaType = "application/json; charset=utf-8".toMediaType()

        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini") // 필요하면 다른 모델명으로 변경 가능
            put("messages", messagesArray)
        }

        val body = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val responseBody = response.body?.string() ?: return@withContext null
                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val message = choices.getJSONObject(0).getJSONObject("message")
                    return@withContext message.getString("content")
                }
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}