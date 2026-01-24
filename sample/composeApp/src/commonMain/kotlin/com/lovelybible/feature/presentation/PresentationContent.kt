package com.lovelybible.feature.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lovelybible.domain.model.Verse
import com.lovelybible.theme.AppColors

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
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF0a0a14)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(80.dp)
        ) {
            // 제목
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayMedium,
                    color = AppColors.Accent,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(48.dp))
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
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        // 절 번호 (hideVerseNumber가 false일 때만 표시)
        if (!hideVerseNumber) {
            Text(
                text = "${verse.verse}",
                style = MaterialTheme.typography.headlineLarge,
                color = AppColors.Accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.End
            )
            
            Spacer(modifier = Modifier.width(24.dp))
        }
        
        // 본문
        Text(
            text = verse.text,
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
    }
}
