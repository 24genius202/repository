package com.uselessdev.tetramenai

// ===== StringStorage.kt - 문자열 저장/불러오기 클래스 =====
import android.content.Context
import android.content.SharedPreferences

class StringStorage(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("my_app_storage", Context.MODE_PRIVATE)

    // 문자열 저장
    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    // 문자열 불러오기 (기본값 설정 가능)
    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    // 저장된 문자열 삭제
    fun removeString(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    // 모든 저장된 데이터 삭제
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    // 특정 키가 존재하는지 확인
    fun hasKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}
