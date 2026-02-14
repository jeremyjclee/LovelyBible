package com.lovelybible.test.fake

import com.lovelybible.domain.repository.SettingsRepository

class FakeSettingsRepository : SettingsRepository {
    private var isAutoPptOn = false // Test default: false

    override fun isAutoPptOnSearch(): Boolean = isAutoPptOn

    override fun setAutoPptOnSearch(enabled: Boolean) {
        isAutoPptOn = enabled
    }

    private var maxLineWidth = 1300 // default

    override fun getMaxLineWidth(): Int = maxLineWidth

    override fun setMaxLineWidth(width: Int) {
        maxLineWidth = width
    }
}
