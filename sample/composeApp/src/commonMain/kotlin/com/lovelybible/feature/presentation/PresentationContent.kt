package com.lovelybible.feature.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lovelybible.domain.model.Verse
import com.lovelybible.theme.AppColors

import androidx.compose.ui.unit.sp
import com.lovelybible.util.rememberBackgroundImage

// 진한 초록색 상수
private val DarkGreen = Color(0xFF2E7D32)

/**
 * 폰트 크기 레벨(1~10)에 따른 sp 값 반환
 */
private fun getFontSizeForLevel(level: Int): Int {
    return when (level) {
        1 -> 36
        2 -> 40
        3 -> 44
        4 -> 48
        5 -> 52
        6 -> 56
        7 -> 60
        8 -> 64
        9 -> 68
        10 -> 72
        else -> 48 // 기본값
    }
}

/**
 * 프레젠테이션 화면 콘텐츠
 * 전체화면으로 성경 구절 표시
 * 
 * @param scaleFactor 화면 크기에 따른 축소 비율 (1.0 = 전체화면, 0.4 = 앱 화면)
 */
@Composable
fun PresentationContent(
    verses: List<Verse>,
    title: String,
    fontSizeLevel: Int = 4,
    scaleFactor: Float = 1.0f,  // 기본값 1.0 (전체화면용)
    modifier: Modifier = Modifier
) {
    val baseFontSize = getFontSizeForLevel(fontSizeLevel)
    // scaleFactor 적용: 앱 화면에서는 폰트 크기 축소
    val fontSize = (baseFontSize * scaleFactor).toInt()
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 배경 이미지
        val backgroundImage = rememberBackgroundImage()
        if (backgroundImage != null) {
            Image(
                bitmap = backgroundImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // 고정된 최대 너비 설정 (1300dp 기준, scaleFactor 적용)
        // 폰트 크기와 관계없이 일정한 화면 비율 유지
        // 큰 폰트 → 적은 글자/줄, 작은 폰트 → 많은 글자/줄
        val fixedMaxWidth = (1300 * scaleFactor).dp
        
        /* 
         * 변경 내역:
         * scaleFactor 추가: 앱 화면(0.4)과 PPT 화면(1.0)에서 동일한 시각적 밀도 제공
         */
        
        // padding도 scaleFactor 적용
        val paddingSize = (80 * scaleFactor).dp
        
        // IntrinsicSize.Min을 사용하여 가장 긴 줄에 맞춰 중앙 정렬 (최대 너비는 고정값으로 제한)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(paddingSize)
                .widthIn(max = fixedMaxWidth)
        ) {
            // 콘텐츠를 IntrinsicSize로 감싸서 가장 긴 줄 기준 정렬
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                // 제목 - 폰트 크기에 비례하여 조절
                if (title.isNotEmpty()) {
                    val titleFontSize = (fontSize * 1.2).toInt()
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = titleFontSize.sp),
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height((24 * scaleFactor).dp))
                    
                    // 구분선 - 콘텐츠 너비에 맞춤
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = (3 * scaleFactor).dp,
                        color = DarkGreen
                    )
                    
                    Spacer(modifier = Modifier.height((32 * scaleFactor).dp))
                }
                
                // 구절들
                if (verses.isEmpty()) {
                    Text(
                        text = "구절을 검색해주세요",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = fontSize.sp),
                        color = AppColors.TextSecondary
                    )
                } else {
                    verses.forEach { verse ->
                        val isCreed = verse.bookName == "사신"
                        PresentationVerseRow(
                            verse = verse, 
                            hideVerseNumber = isCreed,
                            fontSize = fontSize,
                            scaleFactor = scaleFactor
                        )
                        Spacer(modifier = Modifier.height((24 * scaleFactor).dp))
                    }
                }
            }
        }
    }
}

/**
 * 프레젠테이션용 구절 행
 */
@Composable
fun PresentationVerseRow(
    verse: Verse,
    hideVerseNumber: Boolean = false,
    fontSize: Int = 50,
    scaleFactor: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
    ) {
        val baseStyle = MaterialTheme.typography.headlineLarge
        // 동적 폰트 크기 적용, 줄 간격도 그에 맞춰 약 1.6배로 설정
        val verseStyle = baseStyle.copy(
            fontSize = fontSize.sp,
            lineHeight = fontSize.sp * 1.6
        )
        
        // 절 번호 너비도 폰트 크기에 비례하여 조절
        val verseNumberWidth = (fontSize * 1.6).dp

        // 절 번호 (hideVerseNumber가 false일 때만 표시)
        if (!hideVerseNumber) {
            Text(
                text = "${verse.verse}",
                style = verseStyle,
                color = DarkGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(verseNumberWidth),
                textAlign = TextAlign.End
            )
            
            Spacer(modifier = Modifier.width((16 * scaleFactor).dp))
        }
        
        // 본문
        Text(
            text = verse.text.keepAll(),
            style = verseStyle,
            color = Color.Black,
            textAlign = TextAlign.Start
        )
    }
}

/**
 * 단어 단위 줄바꿈을 위한 유틸리티 (Word Break: Keep All)
 * 한글의 경우 글자 단위 줄바꿈을 방지하기 위해 단어 내부 글자 사이에 Word Joiner(U+2060)를 삽입
 */
private fun String.keepAll(): String {
    return this.split(" ").joinToString(" ") { word ->
        // 단어의 각 글자 사이에 Word Joiner 삽입
        word.toCharArray().joinToString("\u2060")
    }
}
