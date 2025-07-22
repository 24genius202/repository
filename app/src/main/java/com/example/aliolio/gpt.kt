// GPTHelper.kt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * GPT API 호출을 위한 유틸리티 클래스
 */
object GPT {

    // API 키 설정 - 실제 키로 교체하세요
    private const val API_KEY = ""
    private const val BASE_URL = "https://api.openai.com/v1/chat/completions"

    /**
     * 대화 기록과 함께 GPT에게 질문하는 함수
     *
     * @param message 현재 사용자 메시지
     * @param conversationHistory 이전 대화 기록 (role, content 쌍의 리스트)
     * @param model 사용할 모델
     * @param maxTokens 최대 토큰 수
     * @param temperature 창의성 정도
     * @return GPT 응답 또는 오류 시 null
     */
    suspend fun askGPTWithHistory(
        message: String,
        conversationHistory: String,
        model: String = "gpt-4o-mini",
        maxTokens: Int = 500,
        temperature: Double = 0.7
    ): String? = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf<Map<String, String>>()

            // 시스템 메시지 추가
            messages.add(mapOf("role" to "system", "content" to "Based on the conversation history, reply 0 if given message is considered unimportant to user. If it is considered important, phrase and reply the message to only leave important information to user."))

            // 대화 기록 추가
            messages.add(mapOf("role" to "user", "content" to conversationHistory))

            // 현재 사용자 메시지 추가
            messages.add(mapOf("role" to "user", "content" to message))

            val response = callAPI(
                messages = messages,
                model = model,
                maxTokens = maxTokens,
                temperature = temperature
            )
            response
        } catch (e: Exception) {
            null
        }
    }


    /**
     * 실제 API 호출을 수행하는 내부 함수
     */
    private suspend fun callAPI(
        messages: List<Map<String, String>>,
        model: String,
        maxTokens: Int,
        temperature: Double
    ): String = withContext(Dispatchers.IO) {
        val url = URL(BASE_URL)
        val connection = url.openConnection() as HttpURLConnection

        try {
            // HTTP 연결 설정
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $API_KEY")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            // JSON 요청 데이터 생성
            val requestBody = JSONObject().apply {
                put("model", model)
                put("max_tokens", maxTokens)
                put("temperature", temperature)

                val messagesArray = JSONArray()
                messages.forEach { message ->
                    messagesArray.put(JSONObject().apply {
                        put("role", message["role"])
                        put("content", message["content"])
                    })
                }
                put("messages", messagesArray)
            }

            // 요청 전송
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(requestBody.toString())
            writer.flush()
            writer.close()

            // 응답 처리
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val messageObj = firstChoice.getJSONObject("message")
                    return@withContext messageObj.getString("content").trim()
                } else {
                    throw Exception("Empty response from API")
                }
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw Exception("API Error ($responseCode): $errorResponse")
            }

        } finally {
            connection.disconnect()
        }
    }
}

/**
 * GPT 호출 결과를 나타내는 sealed class
 */
sealed class GPTResult {
    data class Success(val response: String) : GPTResult()
    data class Error(val message: String) : GPTResult()
}

 /* 4. 대화 기록과 함께:
 * val history = listOf(
 *     mapOf("role" to "user", "content" to "내 이름은 김철수야"),
 *     mapOf("role" to "assistant", "content" to "안녕하세요 김철수님!")
 * )
 * val response = GPTHelper.askGPTWithHistory("내 이름이 뭐라고 했지?", history)
*/