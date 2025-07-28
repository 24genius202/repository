package com.example.aliolio

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AlertDialog

class Information: AppCompatActivity() {
    private lateinit var stringStorage: StringStorage

    @SuppressLint("MissingInflatedId", "UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.information)

        val pref = findViewById<EditText>(R.id.ed1)

        stringStorage = StringStorage(this)

        pref.setText(stringStorage.getString("preferences"))

        val button = findViewById<Button>(R.id.save)
        button.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            stringStorage.saveString("preferences", pref.text.toString())
            finish()
        }

        val switch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.deeplearningenable)

        if(stringStorage.getString("DeepLearningEnable","0") == "1"){
            switch.isChecked = true
        } else switch.isChecked = false

        switch.setOnCheckedChangeListener { compoundButton, isChecked ->
            if(isChecked){
                AlertDialog.Builder(this)
                    .setTitle("경고")
                    .setMessage("개인화 딥러닝 기술을 사용하시겠습니까?\n(해당 서비스 사용 시 사용자와 주변인과의 관계를 포함한 추가 개인정보가 OpenAI에 전송될 수 있습니다. 자세한 사항은 개인정보처리방침을 읽어주시기 바랍니다.)")
                    .setPositiveButton("예") { dialog, _ ->
                        // 확인 버튼 클릭 시 동작
                        AlertDialog.Builder(this)
                            .setTitle("안내")
                            .setMessage("개인화 딥러닝 기술은 아직 BETA TESTING 단계로, 정확하지 않을 수 있습니다.")
                            .setPositiveButton("확인했습니다") { dialog, _ ->
                                // 확인 버튼 클릭 시 동작
                                stringStorage.saveString("DeepLearningEnable", "1")
                                switch.isChecked = true
                                dialog.dismiss()
                            }
                            .setNegativeButton("사용하지 않겠습니다") { dialog, _ ->
                                // 취소 버튼 클릭 시 동작
                                stringStorage.saveString("DeepLearningEnable", "0")
                                switch.isChecked = false
                                dialog.dismiss()
                            }
                            .show()
                        dialog.dismiss()
                    }
                    .setNegativeButton("아니오") { dialog, _ ->
                        // 취소 버튼 클릭 시 동작
                        stringStorage.saveString("DeepLearningEnable", "0")
                        switch.isChecked = false
                        dialog.dismiss()
                    }
                    .show()
            }
            else{
                stringStorage.saveString("DeepLearningEnable", "0")
            }
        }
    }
}