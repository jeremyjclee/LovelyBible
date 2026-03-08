package com.lovelybible.domain.repository

interface SettingsRepository {
    fun isAutoPptOnSearch(): Boolean
    fun setAutoPptOnSearch(enabled: Boolean)

    fun getMaxLineWidthBible(): Int
    fun setMaxLineWidthBible(width: Int)

    fun getMaxLineWidthCreed(): Int
    fun setMaxLineWidthCreed(width: Int)
}
