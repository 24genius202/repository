package com.uselessdev.tetramenai


object RandomNameGenerator {

    private val surnames = listOf(
        "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
        "한", "오", "서", "신", "권", "황", "안", "송", "류", "홍"
    )

    private val nameSyllables = listOf(
        "가", "강", "건", "경", "고", "관", "구", "규", "기",
        "나", "남", "누", "다", "도", "동", "라", "려", "로", "리",
        "마", "민", "명", "미", "문", "민", "박", "범", "병", "보",
        "사", "상", "서", "석", "선", "설", "성", "세", "소", "송",
        "수", "숙", "순", "승", "시", "신", "아", "안", "애", "양",
        "연", "영", "예", "오", "용", "우", "운", "원", "유", "윤",
        "은", "의", "이", "인", "임", "재", "정", "제", "조", "준",
        "지", "진", "찬", "창", "채", "천", "철", "청", "초", "춘",
        "치", "태", "하", "한", "해", "현", "호", "홍", "화", "환"
    )

    fun generateName(): String {
        val surname = surnames.random()
        val first = nameSyllables.random()
        val second = nameSyllables.random()
        val psu = nameSyllables.random()
        return "$surname$first$second$psu"
    }
}