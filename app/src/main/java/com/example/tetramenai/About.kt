package com.uselessdev.tetramenai

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button

class About: AppCompatActivity(){
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)
        val button = findViewById<Button>(R.id.goback3)
        button.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            finish()
        }
    }
}