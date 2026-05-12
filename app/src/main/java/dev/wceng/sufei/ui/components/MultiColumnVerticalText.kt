package dev.wceng.sufei.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 多列竖排文本组件，用于处理过长的标题。
 * 当文本超过 maxCharsPerColumn 时自动向左分列，列序从右向左排列以符合古籍阅读习惯。
 */
@Composable
fun MultiColumnVerticalText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    spacing: Dp = 4.dp,
    columnSpacing: Dp = 12.dp,
    maxCharsPerColumn: Int = 8
) {
    val columns = text.chunked(maxCharsPerColumn)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(columnSpacing),
        verticalAlignment = Alignment.Top
    ) {
        columns.asReversed().forEach { columnText ->
            VerticalText(
                text = columnText,
                style = style,
                spacing = spacing
            )
        }
    }
}