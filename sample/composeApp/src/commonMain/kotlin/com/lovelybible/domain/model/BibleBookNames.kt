package com.lovelybible.domain.model

/**
 * 성경책 이름 매핑 유틸리티
 * 줄임말 ↔ 풀네임 변환
 */
object BibleBookNames {
    
    /**
     * 줄임말 → 풀네임 매핑
     */
    private val shortToFullName = mapOf(
        // 구약 39권
        "창" to "창세기",
        "출" to "출애굽기",
        "레" to "레위기",
        "민" to "민수기",
        "신" to "신명기",
        "수" to "여호수아",
        "삿" to "사사기",
        "룻" to "룻기",
        "삼상" to "사무엘상",
        "삼하" to "사무엘하",
        "왕상" to "열왕기상",
        "왕하" to "열왕기하",
        "대상" to "역대상",
        "대하" to "역대하",
        "스" to "에스라",
        "느" to "느헤미야",
        "에" to "에스더",
        "욥" to "욥기",
        "시" to "시편",
        "잠" to "잠언",
        "전" to "전도서",
        "아" to "아가",
        "사" to "이사야",
        "렘" to "예레미야",
        "애" to "예레미야애가",
        "겔" to "에스겔",
        "단" to "다니엘",
        "호" to "호세아",
        "욜" to "요엘",
        "암" to "아모스",
        "옵" to "오바댜",
        "욘" to "요나",
        "미" to "미가",
        "나" to "나훔",
        "합" to "하박국",
        "습" to "스바냐",
        "학" to "학개",
        "슥" to "스가랴",
        "말" to "말라기",
        
        // 신약 27권
        "마" to "마태복음",
        "막" to "마가복음",
        "눅" to "누가복음",
        "요" to "요한복음",
        "행" to "사도행전",
        "롬" to "로마서",
        "고전" to "고린도전서",
        "고후" to "고린도후서",
        "갈" to "갈라디아서",
        "엡" to "에베소서",
        "빌" to "빌립보서",
        "골" to "골로새서",
        "살전" to "데살로니가전서",
        "살후" to "데살로니가후서",
        "딤전" to "디모데전서",
        "딤후" to "디모데후서",
        "딛" to "디도서",
        "몬" to "빌레몬서",
        "히" to "히브리서",
        "약" to "야고보서",
        "벧전" to "베드로전서",
        "벧후" to "베드로후서",
        "요일" to "요한일서",
        "요이" to "요한이서",
        "요삼" to "요한삼서",
        "유" to "유다서",
        "계" to "요한계시록",
        
        // 신경/신조
        "사신" to "사도신경"
    )
    
    /**
     * 풀네임 → 줄임말 매핑 (역변환용)
     */
    private val fullToShortName = shortToFullName.entries.associate { (k, v) -> v to k }
    
    /**
     * 줄임말을 풀네임으로 변환
     */
    fun toFullName(shortName: String): String {
        return shortToFullName[shortName] ?: shortName
    }
    
    /**
     * 풀네임을 줄임말로 변환
     */
    fun toShortName(fullName: String): String {
        return fullToShortName[fullName] ?: fullName
    }
    
    // ========== 한글 자모 분리 유틸리티 ==========
    
    // 한글 유니코드 범위
    private const val HANGUL_START = 0xAC00
    private const val HANGUL_END = 0xD7A3
    
    // 초성, 중성, 종성 개수
    private const val JONGSUNG_COUNT = 28
    private const val JUNGSUNG_COUNT = 21
    
    // 초성 목록
    private val CHOSEONG = charArrayOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )
    
    // 중성 목록
    private val JUNGSEONG = charArrayOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    )
    
    /**
     * 완성형 한글인지 확인
     */
    private fun isHangulSyllable(char: Char): Boolean {
        return char.code in HANGUL_START..HANGUL_END
    }
    
    /**
     * 한글 자모 분리
     * @return Triple(초성 인덱스, 중성 인덱스, 종성 인덱스) - 종성이 없으면 0
     */
    private fun decomposeHangul(char: Char): Triple<Int, Int, Int>? {
        if (!isHangulSyllable(char)) return null
        
        val code = char.code - HANGUL_START
        val jongsung = code % JONGSUNG_COUNT
        val jungsung = (code / JONGSUNG_COUNT) % JUNGSUNG_COUNT
        val choseong = code / (JONGSUNG_COUNT * JUNGSUNG_COUNT)
        
        return Triple(choseong, jungsung, jongsung)
    }
    
    /**
     * 마지막 글자에 종성이 없는지 확인
     */
    private fun lastCharHasNoJongsung(text: String): Boolean {
        if (text.isEmpty()) return false
        val lastChar = text.last()
        val decomposed = decomposeHangul(lastChar) ?: return false
        return decomposed.third == 0  // 종성 인덱스가 0이면 종성 없음
    }
    
    /**
     * 두 글자의 초성+중성이 같은지 비교 (종성 무시)
     */
    private fun matchesWithoutJongsung(queryChar: Char, targetChar: Char): Boolean {
        val queryDecomp = decomposeHangul(queryChar) ?: return queryChar == targetChar
        val targetDecomp = decomposeHangul(targetChar) ?: return false
        
        // 초성과 중성만 비교
        return queryDecomp.first == targetDecomp.first && 
               queryDecomp.second == targetDecomp.second
    }
    
    /**
     * 한글 자모 분리 기반 문자열 시작 매칭
     * "여"가 "역대상"의 시작과 매칭되는지 확인 (종성 허용)
     */
    private fun koreanStartsWith(text: String, query: String): Boolean {
        if (query.isEmpty()) return true
        if (text.isEmpty()) return false
        if (query.length > text.length) return false
        
        // 쿼리의 마지막 글자가 종성 없는 한글인 경우 특별 처리
        val hasNoJongsung = lastCharHasNoJongsung(query)
        
        for (i in query.indices) {
            val queryChar = query[i]
            val textChar = text[i]
            
            // 마지막 글자이고 종성이 없는 경우: 초성+중성만 비교
            if (hasNoJongsung && i == query.length - 1) {
                if (!matchesWithoutJongsung(queryChar, textChar)) {
                    return false
                }
            } else {
                // 그 외에는 정확히 일치해야 함
                if (queryChar != textChar) {
                    return false
                }
            }
        }
        
        return true
    }
    
    /**
     * 한글 자모 분리 기반 문자열 포함 매칭
     */
    private fun koreanContains(text: String, query: String): Boolean {
        if (query.isEmpty()) return true
        if (text.isEmpty()) return false
        if (query.length > text.length) return false
        
        // 각 시작 위치에서 koreanStartsWith 체크
        for (startIdx in 0..(text.length - query.length)) {
            if (koreanStartsWith(text.substring(startIdx), query)) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * 검색 쿼리로 책 찾기 (줄임말, 풀네임, 부분일치 모두 지원)
     * 한글 자모 분리를 통해 종성 없이 입력해도 매칭됨
     * 예: "여" → "여호수아", "역대상", "역대하" 모두 매칭
     */
    fun matchesQuery(bookShortName: String, query: String): Boolean {
        if (query.isBlank()) return false
        val lowerQuery = query.lowercase()
        val fullName = toFullName(bookShortName).lowercase()
        val shortName = bookShortName.lowercase()
        
        // 기존 정확한 매칭 (빠른 경로)
        if (shortName.contains(lowerQuery) || 
            fullName.contains(lowerQuery) ||
            shortName.startsWith(lowerQuery) ||
            fullName.startsWith(lowerQuery)) {
            return true
        }
        
        // 한글 자모 분리 기반 매칭 (종성 허용)
        if (koreanStartsWith(shortName, lowerQuery) ||
            koreanStartsWith(fullName, lowerQuery) ||
            koreanContains(shortName, lowerQuery) ||
            koreanContains(fullName, lowerQuery)) {
            return true
        }
        
        // 종성을 다음 글자의 초성으로 해석하여 매칭 (예: "삿" -> "사" + "ㅅ" -> "사신", "사도신경")
        // 쿼리의 마지막 글자가 종성이 있는 경우에만 시도
        if (lowerQuery.isNotEmpty()) {
            val lastChar = lowerQuery.last()
            val decomposed = decomposeHangul(lastChar)
            
            // 종성이 있는 경우 (third != 0)
            if (decomposed != null && decomposed.third != 0) {
                // 종성을 초성으로 변환할 수 있는지 매핑 (간단히 초성 인덱스와 종성 인덱스가 유사하다고 가정하거나 매핑 테이블 필요)
                // 하지만 종성 인덱스와 초성 인덱스는 다르므로 변환 필요
                val jongsungIdx = decomposed.third
                val choseongChar = jongsungToChoseong(jongsungIdx)
                
                if (choseongChar != null) {
                    // "삿" -> "사"
                    val queryWithoutJongsung = lowerQuery.substring(0, lowerQuery.length - 1) + 
                        composeHangul(decomposed.first, decomposed.second, 0)
                    
                    // "사" + "ㅅ" 패턴으로 검색
                    // 대상 문자열이 "사"로 시작하고, 그 다음 글자의 초성이 "ㅅ"인지 확인
                    if (matchSplitQuery(shortName, queryWithoutJongsung, choseongChar) ||
                        matchSplitQuery(fullName, queryWithoutJongsung, choseongChar)) {
                        return true
                    }
                }
            }
        }
        
        return false
    }
    
    /**
     * 종성 인덱스를 초성 문자로 변환
     */
    private fun jongsungToChoseong(jongsungIdx: Int): Char? {
        // 종성 목록: (0: 없음), 1:ㄱ, 2:ㄲ, 3:ㄳ, 4:ㄴ, 5:ㄵ, 6:ㄶ, 7:ㄷ, 8:ㄹ, 9:ㄺ, 10:ㄻ, 11:ㄼ, 12:ㄽ, 13:ㄾ, 14:ㄿ, 15:ㅀ, 16:ㅁ, 17:ㅂ, 18:ㅄ, 19:ㅅ, 20:ㅆ, 21:ㅇ, 22:ㅈ, 23:ㅊ, 24:ㅋ, 25:ㅌ, 26:ㅍ, 27:ㅎ
        // 초성 목록: ㄱ, ㄲ, ㄴ, ㄷ, ㄸ, ㄹ, ㅁ, ㅂ, ㅃ, ㅅ, ㅆ, ㅇ, ㅈ, ㅉ, ㅊ, ㅋ, ㅌ, ㅍ, ㅎ
        
        return when (jongsungIdx) {
            1 -> 'ㄱ' // ㄱ
            2 -> 'ㄲ' // ㄲ
            4 -> 'ㄴ' // ㄴ
            7 -> 'ㄷ' // ㄷ
            8 -> 'ㄹ' // ㄹ
            16 -> 'ㅁ' // ㅁ
            17 -> 'ㅂ' // ㅂ
            19 -> 'ㅅ' // ㅅ
            20 -> 'ㅆ' // ㅆ
            21 -> 'ㅇ' // ㅇ
            22 -> 'ㅈ' // ㅈ
            23 -> 'ㅊ' // ㅊ
            24 -> 'ㅋ' // ㅋ
            25 -> 'ㅌ' // ㅌ
            26 -> 'ㅍ' // ㅍ
            27 -> 'ㅎ' // ㅎ
            else -> null // 복합 종성(ㄳ, ㄵ 등)은 단순 매핑 제외
        }
    }
    
    /**
     * 초성, 중성, 종성으로 한글 글자 조합
     */
    private fun composeHangul(choseong: Int, jungsung: Int, jongsung: Int): Char {
        return (0xAC00 + (choseong * 21 * 28) + (jungsung * 28) + jongsung).toChar()
    }
    
    /**
     * 분리된 쿼리 매칭 ("사", 'ㅅ' -> "사도신경" 매칭)
     */
    private fun matchSplitQuery(text: String, firstPart: String, secondPartChoseong: Char): Boolean {
        if (!text.startsWith(firstPart)) return false
        if (text.length <= firstPart.length) return false
        
        val nextChar = text[firstPart.length]
        val nextDecomp = decomposeHangul(nextChar) ?: return false
        
        return CHOSEONG[nextDecomp.first] == secondPartChoseong
    }
    
    /**
     * 모든 책 이름 목록 (검색용)
     */
    val allBookNames: List<Pair<String, String>>
        get() = shortToFullName.entries.map { it.key to it.value }
}
