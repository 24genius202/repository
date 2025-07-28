package com.example.aliolio

import android.app.AlertDialog
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class Agreement_Popup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                dialog.dismiss()
                finish()
            }
        }

        dialog.show()
    }
}