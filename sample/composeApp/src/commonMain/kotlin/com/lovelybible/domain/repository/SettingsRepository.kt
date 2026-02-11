package com.lovelybible.domain.repository

interface SettingsRepository {
    fun isAutoPptOnSearch(): Boolean
    fun setAutoPptOnSearch(enabled: Boolean)
}
