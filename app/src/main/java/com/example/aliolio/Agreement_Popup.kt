package com.example.aliolio

import android.app.AlertDialog
import android.os.Bundle
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

        val agreeCollectionGroup = dialogView.findViewById<RadioGroup>(R.id.agreecollectiongroup)
        val agreeProvideGroup = dialogView.findViewById<RadioGroup>(R.id.agreeprovidegroup)
        val saveButton = dialogView.findViewById<Button>(R.id.agreementsave)

        var agree1 = false
        var agree2 = false

        agreeCollectionGroup.setOnCheckedChangeListener { _, checkedId ->
            agree1 = (checkedId == R.id.collection_agree)
        }

        agreeProvideGroup.setOnCheckedChangeListener { _, checkedId ->
            agree2 = (checkedId == R.id.provide_agree)
        }

        saveButton.setOnClickListener {
            if (agree1 && agree2) {
                dialog.dismiss()
                finish()
            }
        }

        dialog.show()
    }
}