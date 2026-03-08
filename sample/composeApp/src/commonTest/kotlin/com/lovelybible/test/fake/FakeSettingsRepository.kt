package com.lovelybible.test.fake

import com.lovelybible.domain.repository.SettingsRepository

class FakeSettingsRepository : SettingsRepository {
    private var isAutoPptOn = false // Test default: false

    override fun isAutoPptOnSearch(): Boolean = isAutoPptOn

    override fun setAutoPptOnSearch(enabled: Boolean) {
        isAutoPptOn = enabled
    }

    private var maxLineWidthBible = 900 // default
    private var maxLineWidthCreed = 1120 // default

    override fun getMaxLineWidthBible(): Int = maxLineWidthBible

    override fun setMaxLineWidthBible(width: Int) {
        maxLineWidthBible = width
    }

    override fun getMaxLineWidthCreed(): Int = maxLineWidthCreed

    override fun setMaxLineWidthCreed(width: Int) {
        maxLineWidthCreed = width
    }
}
