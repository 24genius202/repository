package com.uselessdev.tetramenai


data class MessageData(val packageName: String, val sender: String, val message: String, val timestamp: Long)

class PrivacyMasker() {
    // 개인정보 유형별 정규식
    private val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val rrnRegex = Regex("(\\d{6})-\\d{7}")
    private val phoneRegex = Regex("(\\d{2,3})-?(\\d{3,4})-?(\\d{4})\\b")
    private val accountRegex = Regex("\\b\\d+\\b")
    private val ipv4Regex = Regex("\\b((25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\b")
    private val dobRegex = Regex("\\b(19|20)?\\d{2}[-/]?(0[1-9]|1[0-2])[-/]?(0[1-9]|[12]\\d|3[01])\\b")
    private val carRegex = Regex("\\b(?:\\d{1,4}[가-힣]\\d{4}|[가-힣]{1,3}\\d{1,4}[가-힣]\\d{4})\\b")
    private val passportRegex = Regex("\\b[MSROD]\\d{8}\\b|\\b[MSROD]\\d{3}[A-Za-z]\\d{4}\\b")
    private val driverRegex = Regex("\\b\\d{2}-?\\d{6}-?\\d{2}\\b|\\b(?:1[1-9]|2[0-6]|28)\\d-?\\d{6}-?\\d{2}\\b")
    private val cardRegex = Regex("(\\d{4})-?(\\d{4})-?(\\d{4})-?(\\d{3,4})$")
    private val otpRegex = Regex("\\b\\d{6}\\b")
//    private val nameWordRegex = Regex("\\b[가-힣]{2,3}\\b")

    fun mask(message: String): String {

        // 2) 메시지 본문 마스킹
        var text = message
        // 이메일 마스킹: '@' 앞부분 일부만 남기고 마스킹
        text = maskEmail(text)

        // 주민등록번호 마스킹: 뒷자리 7개 '*' 처리
        text = rrnRegex.replace(text) { match -> "${match.groupValues[1]}-*******" }

        // 전화번호 마스킹: 가운데 3~4자리 '*' 처리 [oai_citation:35‡develop-sense.tistory.com](https://develop-sense.tistory.com/entry/JAVA-%EC%A0%95%EA%B7%9C%EC%8B%9D%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%A7%88%EC%8A%A4%ED%82%B9%EC%A0%95%EA%B7%9C%ED%91%9C%ED%98%84%EC%8B%9D-%EB%A7%88%EC%8A%A4%ED%82%B9-%EC%B2%98%EB%A6%AC#:~:text=%2F%2F%20%ED%9C%B4%EB%8C%80%ED%8F%B0%EB%B2%88%ED%98%B8%20%EB%A7%88%EC%8A%A4%ED%82%B9,d%7B4)
        text = phoneRegex.replace(text) { match ->
            val part2 = match.groupValues[2]
            match.groupValues[1] + "*".repeat(part2.length) + match.groupValues[3]
        }

        // 계좌번호 마스킹: 숫자 문자열 길이가 5보다 크면 뒷 5자리 '*' 처리 [oai_citation:36‡develop-sense.tistory.com](https://develop-sense.tistory.com/entry/JAVA-%EC%A0%95%EA%B7%9C%EC%8B%9D%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%A7%88%EC%8A%A4%ED%82%B9%EC%A0%95%EA%B7%9C%ED%91%9C%ED%98%84%EC%8B%9D-%EB%A7%88%EC%8A%A4%ED%82%B9-%EC%B2%98%EB%A6%AC#:~:text=%2F%2F%20%EA%B3%84%EC%A2%8C%EB%B2%88%ED%98%B8%20%EB%A7%88%EC%8A%A4%ED%82%B9,9)
        text = accountRegex.replace(text) { match ->
            val acc = match.value
            if (acc.length > 5) {
                acc.substring(0, acc.length - 5) + "*".repeat(5)
            } else {
                acc
            }
        }

        // IP 주소 마스킹: 각 숫자를 '*'로 처리 [oai_citation:37‡jizard.tistory.com](https://jizard.tistory.com/371#:~:text=IP%20%EC%A3%BC%EC%86%8C%20)
        text = ipv4Regex.replace(text) { match ->
            match.value.replace("\\d".toRegex(), "*")
        }

        // 생년월일 마스킹: 모든 숫자 '*' 처리 [oai_citation:38‡develop-sense.tistory.com](https://develop-sense.tistory.com/entry/JAVA-%EC%A0%95%EA%B7%9C%EC%8B%9D%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%A7%88%EC%8A%A4%ED%82%B9%EC%A0%95%EA%B7%9C%ED%91%9C%ED%98%84%EC%8B%9D-%EB%A7%88%EC%8A%A4%ED%82%B9-%EC%B2%98%EB%A6%AC#:~:text=String%20regex%20%3D%20%22%5E%28%2819%7C20%29%5C%5Cd%5C%5Cd%29%3F%28%5B)
        text = dobRegex.replace(text) { match ->
            match.value.replace("\\d".toRegex(), "*")
        }

        // 차량번호 마스킹: 한글 문자를 '*'로 대체 [oai_citation:39‡hbesthee.tistory.com](https://hbesthee.tistory.com/2300#:~:text=%5E%5Cd%7B1%2C4%7D%5B%EA%B0%80)
        text = carRegex.replace(text) { match ->
            match.value.replace("[가-힣]".toRegex(), "*")
        }

        // 여권번호 마스킹: 숫자는 '*'로 대체 [oai_citation:40‡learn.microsoft.com](https://learn.microsoft.com/ko-kr/purview/sit-defn-south-korea-passport-number#:~:text=,%EC%88%AB%EC%9E%90)
        text = passportRegex.replace(text) { match ->
            match.value.replace("\\d".toRegex(), "*")
        }

        // 운전면허번호 마스킹: 숫자는 '*'로 대체 [oai_citation:41‡learn.microsoft.com](https://learn.microsoft.com/ko-kr/purview/sit-defn-south-korea-drivers-license-number#:~:text=%ED%8C%A8%ED%84%B4%201%3A)
        text = driverRegex.replace(text) { match ->
            match.value.replace("\\d".toRegex(), "*")
        }

        // 카드번호 마스킹: 가운데 8자리 '*' 처리 [oai_citation:42‡develop-sense.tistory.com](https://develop-sense.tistory.com/entry/JAVA-%EC%A0%95%EA%B7%9C%EC%8B%9D%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%A7%88%EC%8A%A4%ED%82%B9%EC%A0%95%EA%B7%9C%ED%91%9C%ED%98%84%EC%8B%9D-%EB%A7%88%EC%8A%A4%ED%82%B9-%EC%B2%98%EB%A6%AC#:~:text=%2F%2F%20%EC%B9%B4%EB%93%9C%EB%B2%88%ED%98%B8%20%EA%B0%80%EC%9A%B4%EB%8D%B0%208%EC%9E%90%EB%A6%AC%20%EB%A7%88%EC%8A%A4%ED%82%B9,d%7B3%2C4)
        text = cardRegex.replace(text) { match ->
            val middle = match.groupValues[2] + match.groupValues[3]
            match.value.replace(middle, "*".repeat(middle.length))
        }

        // OTP(6자리) 마스킹: 숫자 6자리 '*' 처리
        text = otpRegex.replace(text) { "******" }

        // 주소 마스킹: '동', '로', '길' 등이 포함된 경우 숫자 '*' 처리 [oai_citation:43‡develop-sense.tistory.com](https://develop-sense.tistory.com/entry/JAVA-%EC%A0%95%EA%B7%9C%EC%8B%9D%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%A7%88%EC%8A%A4%ED%82%B9%EC%A0%95%EA%B7%9C%ED%91%9C%ED%98%84%EC%8B%9D-%EB%A7%88%EC%8A%A4%ED%82%B9-%EC%B2%98%EB%A6%AC#:~:text=%2F%2F%20%EC%A3%BC%EC%86%8C%20%EB%A7%88%EC%8A%A4%ED%82%B9,d%7B1%2C5%7D%29%7C%5C%5Cd%7B1%2C5%7D%29%2B%28%EB%A1%9C%7C%EA%B8%B8) [oai_citation:44‡develop-sense.tistory.com](https://develop-sense.tistory.com/entry/JAVA-%EC%A0%95%EA%B7%9C%EC%8B%9D%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%A7%88%EC%8A%A4%ED%82%B9%EC%A0%95%EA%B7%9C%ED%91%9C%ED%98%84%EC%8B%9D-%EB%A7%88%EC%8A%A4%ED%82%B9-%EC%B2%98%EB%A6%AC#:~:text=%2F%2F%20%EC%A3%BC%EC%86%8C%20%EB%A7%88%EC%8A%A4%ED%82%B9,d%7B1%2C5%7D%29%7C%5C%5Cd%7B1%2C5%7D%29%2B%28%EB%A1%9C%7C%EA%B8%B8)
        if (text.contains("동") || text.contains("로") || text.contains("길")) {
            text = text.replace("\\d".toRegex(), "*")
        }

        // 사람 이름(2~3글자) 마스킹: 가운데 글자 '*' 처리 [oai_citation:45‡develop-sense.tistory.com](https://develop-sense.tistory.com/entry/JAVA-%EC%A0%95%EA%B7%9C%EC%8B%9D%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%A7%88%EC%8A%A4%ED%82%B9%EC%A0%95%EA%B7%9C%ED%91%9C%ED%98%84%EC%8B%9D-%EB%A7%88%EC%8A%A4%ED%82%B9-%EC%B2%98%EB%A6%AC#:~:text=String%20regex%20%3D%20%22%28%5E%5B%EA%B0%80)
//        text = nameWordRegex.replace(text) { match ->
//            val name = match.value
//            when (name.length) {
//                2 -> name[0] + "*"  // 예: 김민 -> 김*
//                3 -> name[0] + "*" + name[2]  // 예: 홍길동 -> 홍*동
//                else -> name
//            }
//        }

        return text
    }

    private fun maskEmail(text: String): String {
        return text.replace(emailRegex) { match ->
            val parts = match.value.split("@")
            if (parts.size == 2) {
                val local = parts[0]
                val domain = parts[1]
                val visible = if (local.length > 3) local.substring(0, 3) else local
                "$visible***@$domain"  // 예: abc@email.com -> abc***@email.com [oai_citation:46‡bumjae.tistory.com](https://bumjae.tistory.com/34#:~:text=fun%20getEmailMasking%28email%3A%20String%29%3A%20String%20,split)
            } else {
                match.value
            }
        }
    }
}