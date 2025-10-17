package com.uselessdev.tetramenai

import android.content.Context
import android.util.Log
import com.uselessdev.tetramenai.OpenAiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeepLearnManager() {
    private lateinit var messagedata: StringStorage

    private lateinit var rawdata: StringStorage

    private lateinit var namestorage: StringStorage
    private lateinit var deeplearnstorage: StringStorage
    private lateinit var stringstorage: StringStorage

    fun initstorages(ss: StringStorage, dls: StringStorage, ns: StringStorage, rd: StringStorage, md: StringStorage){
        stringstorage = ss
        deeplearnstorage = dls
        namestorage = ns
        rawdata = rd
        messagedata = md
    }

    fun deeplearncycle(context: Context) {
        CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            if (stringstorage.getString("DeepLearningEnable", "0") != "0") {
                val encodedecode = EncodeDecode()
                val clients = NameMap(namestorage).getnamemaplist()
                // 🔧 decode 적용
                if (clients.isNotEmpty()) {
                    for (index in clients) {
                        // value separator는 @로 함
                        // steve: <관계>@<Formal>@<Friendly>@<Close>@<Transactional>@<Hierarchical>@<Conflicted>@<요약본>

                        //각 인물에 대한 메시지를 리스트 형태로 가져와서 String 으로 변환. 이후 Unescape 처리 한 후 다시 String 으로 자료형 변경
                        val usrPrompt = EncodeDecode().decode(
                            MessageMap(messagedata, rawdata).getfullmessagelist(context, index)
                                .toString()
                        ).toString()

                        CoroutineScope(Dispatchers.IO).launch {
                            val reply = OpenAiClient.sendDeepLearnMessagesSuspend(
                                systemPrompt = deeplearnstorage.getString(index),
                                userPrompt = usrPrompt
                            )
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
    }
}