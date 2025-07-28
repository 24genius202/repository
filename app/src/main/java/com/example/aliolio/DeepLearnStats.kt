package com.example.aliolio

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DeepLearnStats : AppCompatActivity() {

    data class Person(val name: String, val relation: String, val formal: String, val friendly: String, val close: String, val transactional: String, val hierarchical: String, val conflicted: String)
    private lateinit var stringstorage: StringStorage

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.deeplearnstats)

        val tableLayout = findViewById<TableLayout>(R.id.tablelayout)
        stringstorage = StringStorage(this)

        // 헤더 행 추가
        val headerRow = TableRow(this)
        headerRow.addView(createTextView("이름", true))
        headerRow.addView(createTextView("관계", true))
        headerRow.addView(createTextView("Formal", true))
        headerRow.addView(createTextView("Friendly", true))
        headerRow.addView(createTextView("Close", true))
        headerRow.addView(createTextView("Transactional", true))
        headerRow.addView(createTextView("Hierarchical", true))
        headerRow.addView(createTextView("Conflicted", true))
        tableLayout.addView(headerRow)

        var people = mutableListOf<Person>()

        if(stringstorage.getString("savedpeople", "") != "") {
            for (i in stringstorage.getString("savedpeople", "").split("|")) {
                val savedValue = stringstorage.getString(i, "")
                val info = savedValue.split("@")
                if (info.size >= 7) {
                    people.add(Person(i, info[0], info[1], info[2], info[3], info[4], info[5], info[6]))
                }
            }
        }

        // 데이터 행 추가
        for (person in people) {
            val row = TableRow(this)
            row.addView(createTextView(person.name))
            row.addView(createTextView(person.relation))
            row.addView(createTextView(person.formal))
            row.addView(createTextView(person.friendly))
            row.addView(createTextView(person.close))
            row.addView(createTextView(person.transactional))
            row.addView(createTextView(person.hierarchical))
            row.addView(createTextView(person.conflicted))
            tableLayout.addView(row)
        }

        val goback = findViewById<com.google.android.material.button.MaterialButton>(R.id.goback4)
        goback.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            finish()
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