package com.uselessdev.tetramenai

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import okhttp3.internal.http2.Settings

class Agreement_Popup : AppCompatActivity() {
    private lateinit var stringstorage: StringStorage
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stringstorage = StringStorage(this)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.agreement_popup, null)

         dialog = AlertDialog.Builder(this)
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
                dialog?.dismiss()
                finish()
            }
        }

        dialog?.show()
    }

    override fun onDestroy() {
        dialog?.dismiss()  // Activity 종료 시 다이얼로그 닫기
        super.onDestroy()
    }
}