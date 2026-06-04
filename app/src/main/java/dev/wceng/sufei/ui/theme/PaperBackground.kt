package dev.wceng.sufei.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import kotlin.random.Random

/**
 * 全局书页纹理背景容器。
 * 在根主题层包裹所有内容，提供逼真的纸张纤维纹理。
 *
 * @param isDark 是否为深色主题，影响纹理基色
 * @param modifier 应用于容器的 Modifier
 * @param content 前景内容
 */
@Composable
fun PaperBackground(
    isDark: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val textureBitmap = remember(isDark) {
        generatePaperTexture(isDark = isDark)
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = ShaderBrush(
                    ImageShader(textureBitmap, TileMode.Repeated, TileMode.Repeated)
                ),
                size = size
            )
        }
        content()
    }
}

/**
 * 程序化生成一张可平铺的纸张纹理位图。
 *
 * 纹理包含两层：
 * 1. 暖色纸基底色
 * 2. 随机噪点颗粒（模拟纸张表面粗糙度）
 *
 * @param width  纹理宽度（像素）
 * @param height 纹理高度（像素）
 * @param isDark 是否为深色主题
 */
private fun generatePaperTexture(
    width: Int = 256,
    height: Int = 256,
    isDark: Boolean = false
): ImageBitmap {
    val bitmap = ImageBitmap(width, height)
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
    val random = Random(42)

    // 第一层：纸基底色 — 浅色暖宣纸色 / 深色暖灰墨色
    val baseColor = if (isDark) Color(0xFF1E1B18) else Color(0xFFF7F2E8)
    canvas.drawRect(
        Rect(0f, 0f, width.toFloat(), height.toFloat()),
        Paint().apply { color = baseColor }
    )

    // 第二层：表面颗粒 — 优化为更精细的噪点分布
    val grainColor = if (isDark) Color.White else Color(0xFF8B7355)
    repeat(800) {
        val x = random.nextFloat() * width
        val y = random.nextFloat() * height
        canvas.drawCircle(
            Offset(x, y),
            radius = random.nextFloat() * 1.2f,
            Paint().apply {
                color = grainColor.copy(alpha = random.nextFloat() * 0.015f)
            }
        )
    }

    return bitmap
}
