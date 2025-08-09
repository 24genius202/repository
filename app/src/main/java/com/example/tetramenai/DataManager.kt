package com.uselessdev.tetramenai

import android.content.Context

class NameMap(private val namestorage: StringStorage) {

    fun createnewnamemap(name: String){
        //기존 리스트 불러오기
        val clientlist = EncodeDecode().decode(namestorage.getString("clientlist", "")).toMutableList()
        val originallist = EncodeDecode().decode(namestorage.getString("namemap", "")).toMutableList()
        //가명 생성(4자리)
        val newname = RandomNameGenerator.generateName()

        clientlist.add(name)
        //이름과 가명 매칭하여 저장

        originallist.add("$name:$newname")

        namestorage.saveString("clientlist", EncodeDecode().encode(clientlist))
        namestorage.saveString("namemap", EncodeDecode().encode(originallist))
    }

    fun getnamemap(name: String): String{

        val namelist = EncodeDecode().decode(namestorage.getString("namemap", ""))

        if(!namelist.isEmpty()){
            val foundname = namelist.find { it.split(":")[0] == name }
            if(foundname != null){
                return foundname.split(":")[1]
            }
            else{
                //없으면 생성하고 함수 재귀
                createnewnamemap(name)
                return getnamemap(name)
            }
        }

        return ""
    }

    fun getnamemapbynewname(newname: String): String{
        val namelist = EncodeDecode().decode(namestorage.getString("namemap", ""))

        if(!namelist.isEmpty()){
            val foundname = namelist.find { it.split(":")[1] == newname }
            if(foundname != null){
                return foundname.split(":")[0]
            }
            else{
                return ""
            }
        }

        return ""
    }

    fun getnamemaplist(): List<String>{
        return EncodeDecode().decode(namestorage.getString("clientlist"))
    }
}

class MessageMap(private val messagedata: StringStorage, private val rawdata: StringStorage) {

    fun mesasagemask(context: Context, name: String ,message: String, timestamp: String){
        val nameMap = NameMap(StringStorage(context))


        val maskedmessage = PrivacyMasker().mask(message)

        val processedtimestamp = java.util.Date(timestamp)

        //메시지 저장

        val messagelist = EncodeDecode().decode(rawdata.getString(name, "")).toMutableList()//rawdata 에서는 찐 이름으로 저장

        val maskedmessagelist = EncodeDecode().decode(messagedata.getString(nameMap.getnamemap(name), "")).toMutableList()//messagedata 에서는 가명으로 저장

        messagelist.add("$message@$processedtimestamp")

        maskedmessagelist.add("$maskedmessage@$processedtimestamp")

        rawdata.saveString(name, EncodeDecode().encode(messagelist))

        messagedata.saveString(nameMap.getnamemap(name), EncodeDecode().encode(maskedmessagelist))
    }

    fun getlatestmessage(context: Context, name: String): List<String>{
        val nameMap = NameMap(StringStorage(context))

        val messagelist = EncodeDecode().decode(messagedata.getString(nameMap.getnamemap(name), "")).toMutableList()

        val latestmessage = messagelist[messagelist.size - 1]

        return latestmessage.split("@")
    }

    fun getfullmessagelist(context: Context, name: String): List<String>{
        val nameMap = NameMap(StringStorage(context))
        return EncodeDecode().decode((messagedata.getString(nameMap.getnamemap(name), "")))
    }

}