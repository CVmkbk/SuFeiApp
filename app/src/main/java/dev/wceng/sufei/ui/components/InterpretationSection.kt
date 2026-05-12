package dev.wceng.sufei.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 诗词解读区域组件，用于展示注释、译文、赏析、背景等内容。
 * 当 content 为空时自动隐藏。
 */
@Composable
fun InterpretationSection(
    title: String,
    content: String?,
    multiplier: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    if (content.isNullOrBlank()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = (16 * multiplier).sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = (24 * multiplier).sp,
                fontSize = (14 * multiplier).sp
            )
        )
    }
}