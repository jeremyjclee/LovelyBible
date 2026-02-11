package com.lovelybible.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lovelybible.theme.AppColors

/**
 * 설정 다이얼로그
 * 
 * 확장 가능한 디자인으로 향후 설정 항목 추가 용이
 */
@Composable
fun SettingsDialog(
    state: SettingsState,
    onUpdateAutoPpt: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.CardBackground
            ),
            modifier = Modifier
                .width(400.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 제목
                Text(
                    text = "설정",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.TextPrimary
                )
                
                HorizontalDivider(color = AppColors.BorderColor)
                
                // 설정 항목들 (확장 가능)
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 자동 PPT ON 설정
                    SettingItem(
                        title = "검색 시 자동 PPT ON",
                        description = "성경 구절 검색 완료 시 자동으로 PPT 모드를 켭니다",
                        checked = state.autoPptOnSearch,
                        onCheckedChange = onUpdateAutoPpt
                    )
                    
                    // 향후 추가 설정 항목은 여기에 추가
                    // SettingItem(...)
                }
                
                HorizontalDivider(color = AppColors.BorderColor)
                
                // 하단 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    // 취소 버튼
                    OutlinedButton(
                        onClick = {
                            onCancel()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("취소", color = AppColors.TextPrimary)
                    }
                    
                    // 저장 버튼
                    Button(
                        onClick = {
                            onSave()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Accent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("저장", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * 개별 설정 항목 (재사용 가능)
 */
@Composable
private fun SettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.Accent,
                checkedTrackColor = AppColors.Accent.copy(alpha = 0.5f)
            )
        )
    }
}
