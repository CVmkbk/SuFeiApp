package dev.wceng.sufei.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isUnspecified

/**
 * 文本缩放系数，由用户在「阅读偏好」中调整。
 *
 * @param fontScale 字体大小倍率，默认 1.0f (0.8~1.5)
 * @param lineHeightScale 行间距倍率，默认 1.0f (0.8~1.5)
 */
data class TextScale(
    val fontScale: Float = 1.0f,
    val lineHeightScale: Float = 1.0f
)

/**
 * CompositionLocal：全局文本缩放。
 * 在 MainActivity 层由 userPreferences 流驱动，所有子页面可通过此访问。
 */
val LocalTextScale = compositionLocalOf { TextScale() }

/**
 * 应用全局文本缩放系数到 TextStyle。
 * fontSize 按 fontScale 缩放，lineHeight 按 lineHeightScale 缩放。
 */
fun TextStyle.scaledBy(scale: TextScale): TextStyle {
    if (scale.fontScale == 1.0f && scale.lineHeightScale == 1.0f) return this
    return copy(
        fontSize = if (!fontSize.isUnspecified) fontSize * scale.fontScale else fontSize,
        lineHeight = if (!lineHeight.isUnspecified) lineHeight * scale.lineHeightScale else lineHeight
    )
}
