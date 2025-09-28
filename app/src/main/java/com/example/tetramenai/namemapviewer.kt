package com.uselessdev.tetramenai

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class namemapviewer : AppCompatActivity() {

    data class Person(val name: String, val relation: String, val formal: String, val friendly: String, val close: String, val transactional: String, val hierarchical: String, val conflicted: String)
    private lateinit var namestorage: StringStorage

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.namemapviewer)

        val tableLayout = findViewById<TableLayout>(R.id.namemapview)
        namestorage = StringStorage(this)

        // 헤더 행 추가
        val headerRow = TableRow(this)
        headerRow.addView(createTextView("본명", true))
        headerRow.addView(createTextView("가명", true))
        tableLayout.addView(headerRow)

        var people = mutableListOf<Pair<String, String>>()

        var namelist = EncodeDecode().decode(namestorage.getString("namemap", ""))

        if (!namelist.isEmpty()) {
            for (i in namelist) {
                val data = i.split(":")
                if (data.size < 2) continue // ":" 없는 항목은 건너뜀
                people.add(Pair(data[0], data[1]))
            }
        }

        // 데이터 행 추가
        for (person in people) {
            val row = TableRow(this)
            row.addView(createTextView(person.first))
            row.addView(createTextView(person.second))
            tableLayout.addView(row)
        }

        val goback = findViewById<com.google.android.material.button.MaterialButton>(R.id.goback5)
        goback.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            finish()
        }

        val reset = findViewById<com.google.android.material.button.MaterialButton>(R.id.resetnamemap)
        reset.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            AlertDialog.Builder(this)
                .setTitle("경고")
                .setMessage("가명 리스트를 초기화하시겠습니까? 초기화 시 딥러닝이 정상적으로 동작하지 않을 수도 있습니다.")
                .setPositiveButton("초기화") { dialog, _ ->
                    namestorage.saveString("namemap", "")
                    namestorage.saveString("clientlist", "")

                    dialog.dismiss()
                    finish()
                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            }
        }

    private fun createTextView(text: String, isHeader: Boolean = false): TextView {
        return TextView(this).apply {
            setText(text)
            setPadding(16, 16, 16, 16)
            if (isHeader) {
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
    }
}