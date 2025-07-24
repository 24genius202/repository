package com.example.aliolio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class Information: AppCompatActivity() {
    private lateinit var stringStorage: StringStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.information)

        val pref = findViewById<EditText>(R.id.ed1)

        stringStorage = StringStorage(this)

        pref.setText(stringStorage.getString("preferences"))

        val button = findViewById<Button>(R.id.save)
        button.setOnClickListener {
            stringStorage.saveString("preferences", pref.text.toString())
            finish()
        }
    }
}