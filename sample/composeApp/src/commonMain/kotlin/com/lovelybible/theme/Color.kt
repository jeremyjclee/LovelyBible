package com.lovelybible.theme

import androidx.compose.ui.graphics.Color

/**
 * LovelyBible 앱 색상 정의
 * Deep Dark Blue / Neon Purple 테마
 */
object AppColors {
    // 배경 - Deep Dark Blue
    val Background = Color(0xFF0B0E1A)  // 더 진한 네이비
    val CardBackground = Color(0xFF1A1F35)  // 약간 밝은 남색
    val CardBackgroundAlpha = Color(0xFF1A1F35).copy(alpha = 0.90f)
    
    // 테두리
    val BorderColor = Color.White.copy(alpha = 0.12f)
    val BorderColorLight = Color.White.copy(alpha = 0.06f)
    
    // 강조색 - Neon Purple
    val Accent = Color(0xFF7C3AED)  // 생생한 보라색
    val AccentHover = Color(0xFF8B5CF6)  // 밝은 보라색
    val AccentLight = Color(0xFFA78BFA)  // 연한 보라색
    
    // 텍스트
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
    
    // 상태 색상
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    
    // 그라디언트
    val GradientStart = Color(0xFF1E1B4B)  // 진한 보라-네이비
    val GradientEnd = Color(0xFF0F0A1E)  // 거의 검은 보라
}
