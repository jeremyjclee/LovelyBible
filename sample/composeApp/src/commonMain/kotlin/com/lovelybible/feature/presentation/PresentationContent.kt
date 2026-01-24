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

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.lovelybible.util.rememberBackgroundImage

// 진한 초록색 상수
private val DarkGreen = Color(0xFF2E7D32)

/**
 * 프레젠테이션 화면 콘텐츠
 * 전체화면으로 성경 구절 표시
 */
@Composable
fun PresentationContent(
    verses: List<Verse>,
    title: String,
    modifier: Modifier = Modifier
) {
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
        
        // 1. 너비 기준 텍스트 설정
        val referenceText = "이것들을 증언하신 이가 이르시되 내가 진실로 속히 오리라 하시거늘 아멘"
        // 글자 크기를 38.sp로 증가 (기본 headlineLarge 약 32sp -> 38sp)
        val textStyle = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp)
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        
        // 2. 기준 텍스트의 너비 측정 (keepAll 적용)
        val measuredWidth = remember(referenceText, textStyle, density) {
            val result = textMeasurer.measure(
                text = referenceText.keepAll(),
                style = textStyle
            )
            with(density) { result.size.width.toDp() }
        }
        
        /* 
         * 변경 내역:
         * 기존: .widthIn(max = 1200.dp) - 임의의 고정값 1200dp를 사용
         * 변경: .widthIn(max = measuredWidth) - 요한계시록 22:20 전반부 텍스트 길이를 기준으로 동적 계산
         * 이유: 특정 긴 구절이 한 줄에 꽉 차게 보이되, 너무 길어지지 않도록 자연스러운 줄바꿈 유도
         */
        
        // IntrinsicSize.Min을 사용하여 가장 긴 줄에 맞춰 중앙 정렬 (최대 너비는 기준 텍스트로 제한)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(80.dp)
                .widthIn(max = measuredWidth)
        ) {
            // 콘텐츠를 IntrinsicSize로 감싸서 가장 긴 줄 기준 정렬
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                // 제목
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displayMedium,
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 구분선 - 콘텐츠 너비에 맞춤
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 3.dp,
                        color = DarkGreen
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                // 구절들
                if (verses.isEmpty()) {
                    Text(
                        text = "구절을 검색해주세요",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.TextSecondary
                    )
                } else {
                    verses.forEach { verse ->
                        val isCreed = verse.bookName == "사신"
                        PresentationVerseRow(verse, hideVerseNumber = isCreed)
                        Spacer(modifier = Modifier.height(24.dp))
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
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
    ) {
        val baseStyle = MaterialTheme.typography.headlineLarge
        // 글자 크기를 38.sp로 증가시키고, 줄 간격도 그에 맞춰 약 1.6배로 설정
        val verseStyle = baseStyle.copy(
            fontSize = 38.sp,
            lineHeight = 38.sp * 1.6
        )

        // 절 번호 (hideVerseNumber가 false일 때만 표시)
        if (!hideVerseNumber) {
            Text(
                text = "${verse.verse}",
                style = verseStyle,
                color = DarkGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.End
            )
            
            Spacer(modifier = Modifier.width(16.dp))
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
