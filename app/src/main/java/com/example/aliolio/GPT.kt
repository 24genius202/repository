package com.example.aliolio

// ===== GPT.kt 파일 =====
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// 데이터 클래스들
data class GPTRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<Message>,
    val max_tokens: Int = 100,
    val temperature: Double = 0.7
)

data class Message(
    val role: String, // "user", "assistant", "system"
    val content: String
)

data class GPTResponse(
    val id: String,
    val choices: List<Choice>
)

data class Choice(
    val message: Message,
    val finish_reason: String
)

// API 인터페이스
interface GPTApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: GPTRequest
    ): Response<GPTResponse>
}

// GPT 클래스
class GPT {
    private val API_KEY = "your-openai-api-key-here" // 실제 API 키로 교체
    private val BASE_URL = "https://api.openai.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: GPTApiService = retrofit.create(GPTApiService::class.java)

    // 단순 메시지 전송
    suspend fun sendMessage(userMessage: String): String? {
        return try {
            val request = GPTRequest(
                messages = listOf(
                    Message("user", userMessage)
                )
            )

            val response = apiService.getChatCompletion(
                authorization = "Bearer $API_KEY",
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.choices?.firstOrNull()?.message?.content
            } else {
                "Error: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            "Exception: ${e.message}"
        }
    }

    // 시스템 메시지와 함께 전송
    suspend fun sendMessageWithSystem(userMessage: String, systemMessage: String): String? {
        return try {
            val request = GPTRequest(
                messages = listOf(
                    Message("system", "You are a helpful assistant who determines whether the following message is important for the user based on the given background information of the user. If so, phrase the message so that it only contains what is considered 'important' for the user. Else, return 0. No part of the background information or the result shall be stored, nor shall it affect any future determinations. The given background information is: $systemMessage"),
                    Message("user", userMessage)
                )
            )

            val response = apiService.getChatCompletion(
                authorization = "Bearer $API_KEY",
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.choices?.firstOrNull()?.message?.content
            } else {
                "Error: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            "Exception: ${e.message}"
        }
    }

    // 대화 내역과 함께 전송
    suspend fun sendConversation(messages: List<Message>): String? {
        return try {
            val request = GPTRequest(messages = messages)

            val response = apiService.getChatCompletion(
                authorization = "Bearer $API_KEY",
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.choices?.firstOrNull()?.message?.content
            } else {
                "Error: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            "Exception: ${e.message}"
        }
    }

    // 커스텀 설정으로 전송
    suspend fun sendMessageCustom(
        userMessage: String,
        model: String = "gpt-3.5-turbo",
        maxTokens: Int = 150,
        temperature: Double = 0.7
    ): String? {
        return try {
            val request = GPTRequest(
                model = model,
                messages = listOf(Message("user", userMessage)),
                max_tokens = maxTokens,
                temperature = temperature
            )

            val response = apiService.getChatCompletion(
                authorization = "Bearer $API_KEY",
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.choices?.firstOrNull()?.message?.content
            } else {
                "Error: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            "Exception: ${e.message}"
        }
    }
}