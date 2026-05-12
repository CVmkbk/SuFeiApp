package dev.wceng.sufei.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 竖排文本组件，支持中文标点符号特殊偏移处理。
 * 标点符号（，。；！？）采用右上偏移，模拟古籍排版效果。
 */
@Composable
fun VerticalText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    spacing: Dp = 4.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        text.forEach { char ->
            val isPunctuation = char == '，' || char == '。' || char == '；' || char == '！' || char == '？'
            Text(
                text = char.toString(),
                style = if (isPunctuation) style.copy(fontSize = style.fontSize * 0.9f) else style,
                modifier = if (isPunctuation) {
                    Modifier.offset(x = 3.dp, y = (-3).dp)
                } else {
                    Modifier
                }
            )
        }
    }
}