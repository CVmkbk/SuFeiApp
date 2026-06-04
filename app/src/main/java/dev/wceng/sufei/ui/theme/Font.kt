package dev.wceng.sufei.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.wceng.sufei.R

/**
 * 自定义思源宋体 (Noto Serif SC) 字体族
 * 用于诗词正文、标题等文学性内容的展示。
 */
val NotoSerifSC = FontFamily(
    Font(R.font.noto_serif_sc_light, FontWeight.Light),
    Font(R.font.noto_serif_sc_regular, FontWeight.Normal),
    Font(R.font.noto_serif_sc_bold, FontWeight.Bold)
)

/**
 * 系统无衬线字体
 * 用于 UI 控件（标签、按钮、芯片等），与诗词正文的衬线体形成对比，提高可读性。
 */
val SystemUISans = FontFamily.Default
