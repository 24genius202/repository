package com.uselessdev.tetramenai

import android.app.AlertDialog
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class Agreement_Popup : AppCompatActivity() {
    private lateinit var stringstorage: StringStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stringstorage = StringStorage(this)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.agreement_popup, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        val agreecollectiongroup = dialogView.findViewById<RadioGroup>(R.id.agreecollectiongroup)
        val agreeprovidegroup = dialogView.findViewById<RadioGroup>(R.id.agreeprovidegroup)
        val save = dialogView.findViewById<Button>(R.id.agreementsave)

        var agree1 = false
        var agree2 = false

        agreecollectiongroup.setOnCheckedChangeListener { _, checkedId ->
            agree1 = (checkedId == R.id.collection_agree)
        }

        agreeprovidegroup.setOnCheckedChangeListener { _, checkedId ->
            agree2 = (checkedId == R.id.provide_agree)
        }

        save.setOnClickListener {
            if (agree1 && agree2) {
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("경고")
                    .setMessage("귀하는 개인정보 처리방침을 준수하였으며, 미준수로 인한 법적 책임이 있음을 확인합니다. 귀하는 이를 거부할 수 있으며, 거부시 서비스 사용이 불가합니다.")
                    .setPositiveButton("확인") { dialog, _ ->
                        // 확인 버튼 클릭 시 동작
                        finish()
                        dialog.dismiss()
                    }
                    .setNegativeButton("거부") { dialog, _ ->
                        // 취소 버튼 클릭 시 동작
                        stringstorage.saveString("entercnt", "0")
                        finishAffinity()
                    }
                    .show()
            }
        }

        dialog.show()
    }
}