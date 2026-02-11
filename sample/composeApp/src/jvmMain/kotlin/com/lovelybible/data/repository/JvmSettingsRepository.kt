package com.lovelybible.data.repository

import com.lovelybible.domain.repository.SettingsRepository
import java.util.prefs.Preferences

class JvmSettingsRepository : SettingsRepository {
    private val prefs = Preferences.userNodeForPackage(JvmSettingsRepository::class.java)
    private val keyAutoPpt = "settings_auto_ppt_on_search"

    override fun isAutoPptOnSearch(): Boolean {
        // 기본값: true (요청사항 2: ppt 자동으로 켜는 기능에 대해서 초기 기본값을 on으로 설정)
        return prefs.getBoolean(keyAutoPpt, true)
    }

    override fun setAutoPptOnSearch(enabled: Boolean) {
        prefs.putBoolean(keyAutoPpt, enabled)
    }
}
