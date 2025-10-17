package com.uselessdev.tetramenai

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.HapticFeedbackConstants
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

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
            val entryintentresult = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ){ result ->
                Handler(Looper.getMainLooper()).postDelayed({
                    // 약간의 시간차를 두고 AlertDialog 표시
                    val confirmdialog = AlertDialog.Builder(this)
                        .setTitle("경고")
                        .setMessage("귀하는 개인정보 처리방침을 준수하였으며, 미준수로 인한 법적 책임이 있음을 확인합니다. 귀하는 이를 거부할 수 있으며, 거부시 서비스 사용이 불가합니다.")
                        .setPositiveButton("확인") { d, _ ->
                            d.dismiss()
//
                        }
                        .setNegativeButton("거부") { d, _ ->
                            stringstorage.saveString("entercnt", "0")
                            d.dismiss()
                            finishAffinity()
                        }
                        .create()

                    confirmdialog.show()
                }, 500)
            }
            entryintentresult.launch(intent)

            AlertDialog.Builder(this)
                .setTitle("배터리 최적화 안내")
                .setMessage("앱의 정상 작동을 위해 '자동 절전 제외 앱'에서 본 앱을 선택해 주세요.\n\n" +
                        "설정 -> 배터리 -> 백그라운드 앱 사용 제한 -> 자동 절전 예외 앱 -> TetramenAI 앱 선택")
                .setPositiveButton("확인") { d, _ ->
                    d.dismiss()
                }
                .setNegativeButton("") { d, _ ->
                    d.dismiss()
                }
                .show()

            stringstorage.saveString("entercnt", "1")
        }

        try {
            val loggerButton = findViewById<Button>(R.id.gotolog)
            val settingsButton = findViewById<Button>(R.id.gotosettings)
            val changelogbutton = findViewById<Button>(R.id.changelog)
            val creditsbutton = findViewById<Button>(R.id.credits)
            val aboutbutton = findViewById<Button>(R.id.about)
            val piubutton = findViewById<MaterialButton>(R.id.personalinformationusage)

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

            piubutton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                try {
                    Log.d("MainActivity", "PIU 버튼 클릭됨")
                    val intent = Intent(this, PersonalInformationUsage::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "PIU 이동 실패", e)
                }
            }


            val intent = Intent(this, MainService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(intent)
            else
                startService(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "버튼 초기화 실패", e)
        }
    }
}