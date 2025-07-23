package com.example.aliolio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Information: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.information)

        val button = findViewById<Button>(R.id.save)
        button.setOnClickListener {
            finish()
        }
    }
}