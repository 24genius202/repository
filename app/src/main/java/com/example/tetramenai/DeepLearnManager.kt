package com.uselessdev.tetramenai

import android.content.Context
import android.util.Log
import com.uselessdev.tetramenai.OpenAiClient

class DeepLearnManager() {
    private lateinit var messagedata: StringStorage

    private lateinit var rawdata: StringStorage

    private lateinit var namestorage: StringStorage
    private lateinit var deeplearnstorage: StringStorage

    fun deeplearncycle(context: Context){
        val encodedecode = EncodeDecode()
        val clients = NameMap(namestorage).getnamemaplist()
// 🔧 decode 적용
        if (!clients.isEmpty()) {
            for (index in clients) {
                // value separator는 @로 함
                // steve: <관계>@<Formal>@<Friendly>@<Close>@<Transactional>@<Hierarchical>@<Conflicted>@<요약본>

                //각 인물에 대한 메시지를 리스트 형태로 가져와서 String 으로 변환. 이후 Unescape 처리 한 후 다시 String 으로 자료형 변경
                val usrPrompt = EncodeDecode().decode(MessageMap(messagedata, rawdata).getfullmessagelist(context, index).toString()).toString()

                OpenAiClient.sendDeepLearnMessages(
                    systemPrompt = deeplearnstorage.getString(index),
                    userPrompt = usrPrompt
                ) { reply ->
                    if (reply != null) {
                        Log.d("DeepLearnCycle", reply)
                        deeplearnstorage.saveString(index, reply)
                        Log.d("DeepLearnCycle", "Updated Weight")
                    }
                }
            }
        }
    }
}