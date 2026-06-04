package dev.wceng.sufei.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.wceng.sufei.ui.theme.NotoSerifSC

/**
 * 素扉印章 Logo — 传统风雅，仿朱文印
 * 包含描边圆角方形外框 + 四角云纹竖线装饰。
 * 入场时带描边绘制动画。
 *
 * @param modifier 修饰符
 * @param sizeDp 印章尺寸
 * @param sealColor 印章颜色，默认取主题 primary 妃红色
 */
@Composable
fun SealStampLogo(
    modifier: Modifier = Modifier,
    sizeDp: Float = 96f,
    sealColor: Color = MaterialTheme.colorScheme.primary
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(1200))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val w = size.width
            val h = size.height
            val margin = 0.06f * w
            val strokeWidth = 2.5f * density
            val cornerR = 0.14f * w
            val progress = animProgress.value

            // === 外框圆角矩形 ===
            drawRoundRect(
                color = sealColor.copy(alpha = 0.5f * progress),
                topLeft = Offset(margin, margin),
                size = Size(w - 2 * margin, h - 2 * margin),
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = strokeWidth)
            )

            // === 四角云纹竖线装饰 ===
            drawCornerDecorations(
                margin = margin,
                w = w,
                h = h,
                cornerR = cornerR,
                color = sealColor,
                strokeWidth = strokeWidth * 0.6f,
                alpha = progress * 0.7f
            )
        }
    }
}

private fun DrawScope.drawCornerDecorations(
    margin: Float,
    w: Float,
    h: Float,
    cornerR: Float,
    color: Color,
    strokeWidth: Float,
    alpha: Float
) {
    val lineLen = 8f * density
    // 左上角
    drawLine(color.copy(alpha = 0.55f * alpha), Offset(margin + cornerR * 0.5f, margin * 1.8f), Offset(margin + cornerR * 0.5f, margin * 1.8f + lineLen), strokeWidth)
    // 右上角
    drawLine(color.copy(alpha = 0.55f * alpha), Offset(w - margin - cornerR * 0.5f, margin * 1.8f), Offset(w - margin - cornerR * 0.5f, margin * 1.8f + lineLen), strokeWidth)
    // 左下角
    drawLine(color.copy(alpha = 0.55f * alpha), Offset(margin + cornerR * 0.5f, h - margin * 1.8f - lineLen), Offset(margin + cornerR * 0.5f, h - margin * 1.8f), strokeWidth)
    // 右下角
    drawLine(color.copy(alpha = 0.55f * alpha), Offset(w - margin - cornerR * 0.5f, h - margin * 1.8f - lineLen), Offset(w - margin - cornerR * 0.5f, h - margin * 1.8f), strokeWidth)
}

/**
 * 完整启动 Logo 组合：印章图案 + 下方 "素扉" 文字 + 副标题
 */
@Composable
fun SplashLogoGroup(
    modifier: Modifier = Modifier,
    sealSizeDp: Float = 96f
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SealStampLogo(sizeDp = sealSizeDp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "素扉",
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = NotoSerifSC,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 8.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "— 数字诗集 —",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                letterSpacing = 4.sp
            )
        )
    }
}
