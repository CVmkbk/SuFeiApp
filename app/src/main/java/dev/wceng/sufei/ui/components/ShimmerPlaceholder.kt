package dev.wceng.sufei.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 骨架屏闪烁效果占位组件。
 * 模拟内容加载时的占位卡片，带波浪闪光动画。
 *
 * @param modifier 修饰符
 * @param height 占位条高度
 * @param widthFraction 占父容器宽度的比例 (0..1)
 * @param cornerRadius 圆角
 */
@Composable
fun ShimmerBar(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    widthFraction: Float = 1f,
    cornerRadius: Dp = 4.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(translateAnim - 200f, 0f),
                    end = Offset(translateAnim, 0f)
                )
            )
    )
}

/**
 * 诗词卡片骨架屏 — 模拟 PoemPreviewCard 的加载占位
 */
@Composable
fun ShimmerPoemCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            )
            .padding(16.dp)
    ) {
        ShimmerBar(height = 18.dp, widthFraction = 0.6f)
        Spacer(Modifier.height(8.dp))
        ShimmerBar(height = 14.dp, widthFraction = 0.35f)
        Spacer(Modifier.height(12.dp))
        ShimmerBar(height = 12.dp, widthFraction = 0.9f)
        Spacer(Modifier.height(6.dp))
        ShimmerBar(height = 12.dp, widthFraction = 0.7f)
    }
}

/**
 * 诗人卡片骨架屏 — 模拟 PoetPreviewCard 的加载占位
 */
@Composable
fun ShimmerPoetCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            )
            .padding(16.dp)
    ) {
        Row {
            ShimmerBar(modifier = Modifier.width(32.dp), height = 14.dp)
            Spacer(Modifier.width(4.dp))
            ShimmerBar(modifier = Modifier.width(28.dp), height = 14.dp)
        }
        Spacer(Modifier.height(8.dp))
        ShimmerBar(height = 20.dp, widthFraction = 0.5f)
        Spacer(Modifier.height(6.dp))
        ShimmerBar(height = 14.dp, widthFraction = 0.3f)
        Spacer(Modifier.height(8.dp))
        ShimmerBar(height = 12.dp, widthFraction = 0.85f)
        Spacer(Modifier.height(4.dp))
        ShimmerBar(height = 12.dp, widthFraction = 0.6f)
    }
}

/**
 * 收藏列表骨架屏
 */
@Composable
fun ShimmerCollectionCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            )
            .padding(16.dp)
    ) {
        Row {
            Column(Modifier.weight(1f)) {
                ShimmerBar(height = 18.dp, widthFraction = 0.5f)
                Spacer(Modifier.height(6.dp))
                ShimmerBar(height = 14.dp, widthFraction = 0.3f)
                Spacer(Modifier.height(6.dp))
                ShimmerBar(height = 12.dp, widthFraction = 0.9f)
            }
            Spacer(Modifier.width(12.dp))
            ShimmerBar(modifier = Modifier.width(24.dp), height = 24.dp, cornerRadius = 12.dp)
        }
    }
}
