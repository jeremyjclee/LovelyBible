package com.lovelybible.domain.model

/**
 * 성경 위치 (책/장/절)
 */
data class BiblePosition(
    val book: String,
    val chapter: Int,
    val verse: Int
) {
    override fun toString(): String = "$book $chapter:$verse"
}
