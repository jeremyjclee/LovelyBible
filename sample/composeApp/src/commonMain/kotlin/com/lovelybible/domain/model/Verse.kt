package com.lovelybible.domain.model

/**
 * 성경 구절 모델
 */
data class Verse(
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val text: String
)
