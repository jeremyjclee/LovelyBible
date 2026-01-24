package com.lovelybible.core.display

/**
 * 모니터 Bounds 정보
 */
data class DisplayBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

/**
 * 모니터 정보 (Common)
 */
data class DisplayInfo(
    val id: Int,
    val name: String,
    val bounds: DisplayBounds,
    val isPrimary: Boolean
)
