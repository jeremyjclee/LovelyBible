package com.lovelybible.data.repository

import com.lovelybible.domain.repository.SettingsRepository
import java.util.prefs.Preferences

class JvmSettingsRepository : SettingsRepository {
    private val prefs: Preferences? = try {
        Preferences.userNodeForPackage(JvmSettingsRepository::class.java)
    } catch (e: Exception) {
        // 패키징된 exe 환경에서 SecurityException 등이 발생할 수 있음
        null
    }
    private val keyAutoPpt = "settings_auto_ppt_on_search"

    override fun isAutoPptOnSearch(): Boolean {
        // 기본값: true (요청사항 2: ppt 자동으로 켜는 기능에 대해서 초기 기본값을 on으로 설정)
        return try {
            prefs?.getBoolean(keyAutoPpt, true) ?: true
        } catch (e: Exception) {
            true
        }
    }

    override fun setAutoPptOnSearch(enabled: Boolean) {
        try {
            prefs?.putBoolean(keyAutoPpt, enabled)
        } catch (e: Exception) {
            // 설정 저장 실패 시 무시 (다음 실행 시 기본값 사용)
        }
    }

    private val keyMaxLineWidthBible = "settings_max_line_width_bible"
    private val keyMaxLineWidthCreed = "settings_max_line_width_creed"

    override fun getMaxLineWidthBible(): Int {
        // 기본값: 900
        return try {
            prefs?.getInt(keyMaxLineWidthBible, 900) ?: 900
        } catch (e: Exception) {
            900
        }
    }

    override fun setMaxLineWidthBible(width: Int) {
        try {
            prefs?.putInt(keyMaxLineWidthBible, width)
        } catch (e: Exception) {
            // 설정 저장 실패 시 무시
        }
    }

    override fun getMaxLineWidthCreed(): Int {
        // 기본값: 1120
        return try {
            prefs?.getInt(keyMaxLineWidthCreed, 1120) ?: 1120
        } catch (e: Exception) {
            1120
        }
    }

    override fun setMaxLineWidthCreed(width: Int) {
        try {
            prefs?.putInt(keyMaxLineWidthCreed, width)
        } catch (e: Exception) {
            // 설정 저장 실패 시 무시
        }
    }
}
