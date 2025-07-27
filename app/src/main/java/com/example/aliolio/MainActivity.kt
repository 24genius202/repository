package com.example.aliolio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.HapticFeedbackConstants
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var stringstorage: StringStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stringstorage = StringStorage(this)

        val serviceIntent = Intent(this, ForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        if(stringstorage.getString("entercnt", "0") == "0"){
            val intent = Intent(this, Agreement_Popup::class.java)
            startActivity(intent)
            stringstorage.saveString("entercnt", "1")
        }

        try {
            val loggerButton = findViewById<Button>(R.id.gotolog)
            val settingsButton = findViewById<Button>(R.id.gotosettings)
            val changelogbutton = findViewById<Button>(R.id.changelog)
            val creditsbutton = findViewById<Button>(R.id.credits)
            val aboutbutton = findViewById<Button>(R.id.about)

            loggerButton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                try {
                    Log.d("MainActivity", "Logger 버튼 클릭됨")
                    val intent = Intent(this, Logger::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Logger 이동 실패", e)
                }
            }

            settingsButton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                try {
                    Log.d("MainActivity", "Settings 버튼 클릭됨")
                    val intent = Intent(this, Information::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Information 이동 실패", e)
                }
            }

            changelogbutton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                try {
                    Log.d("MainActivity", "Changelog 버튼 클릭됨")
                    val intent = Intent(this, Changelog::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Changelog 이동 실패", e)
                }
            }

            creditsbutton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                try {
                    Log.d("MainActivity", "Credits 버튼 클릭됨")
                    val intent = Intent(this, Credits::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Credits 이동 실패", e)
                }
            }

            aboutbutton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                try {
                    Log.d("MainActivity", "About 버튼 클릭됨")
                    val intent = Intent(this, About::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "About 이동 실패", e)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "버튼 초기화 실패", e)
        }
    }
}