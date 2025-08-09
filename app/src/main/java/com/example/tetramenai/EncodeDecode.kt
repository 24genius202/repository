package com.uselessdev.tetramenai

class EncodeDecode {
    fun escape(s: String): String = s.replace(",", "<<COMMA>>")
    fun unescape(s: String): String = s.replace("<<COMMA>>", ",")

    fun encode(list: List<String>): String = list.joinToString(",") { escape(it) }
    fun decode(encoded: String): List<String> = encoded.split(",").map { unescape(it) }
}