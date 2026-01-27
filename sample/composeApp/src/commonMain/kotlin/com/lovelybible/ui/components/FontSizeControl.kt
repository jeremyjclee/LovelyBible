package com.lovelybible.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lovelybible.theme.AppColors

@Composable
fun FontSizeControl(
    currentLevel: Int,
    onLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "글자 크기",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextSecondary
            )
            
            Text(
                text = "$currentLevel 단계",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Accent
            )
        }
        
        Slider(
            value = currentLevel.toFloat(),
            onValueChange = { onLevelChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8, // 1 to 10 has 9 intervals, so 8 steps in between
            colors = SliderDefaults.colors(
                thumbColor = AppColors.Accent,
                activeTrackColor = AppColors.Accent,
                inactiveTrackColor = AppColors.BorderColor,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "작게",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
            Text(
                text = "크게",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
        }
    }
}
