package com.example.aliolio

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PersonalInformationUsage :AppCompatActivity(){
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.personalinformationusage)
        val button = findViewById<Button>(R.id.goback5)
        val unallow = findViewById<Button>(R.id.unallow)
        button.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            finish()
        }
        unallow.setOnClickListener {
            Log.d("PersonalInformationUsage", "앱 삭제 버튼 클릭됨")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("알림")
                .setMessage("개인정보처리방침 동의를 철회하시겠습니까?")
                .setPositiveButton("철회(앱 데이터 삭제)") { dialog, _ ->
                    restartApp(this)
                    dialog.dismiss()
                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    fun restartApp(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.clearApplicationUserData()
        }
    }

}