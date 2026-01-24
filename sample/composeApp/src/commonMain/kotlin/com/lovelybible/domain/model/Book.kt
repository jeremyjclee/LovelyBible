package com.lovelybible.domain.model

/**
 * 성경 책 모델
 */
data class Book(
    val id: Int,
    val name: String,
    val testament: Testament,
    val chapterCount: Int
)

/**
 * 구약/신약 구분
 */
enum class Testament {
    OLD,  // 구약
    NEW   // 신약
}
