package dev.wceng.sufei.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 素扉 (SuFei) 全局字体配置
 *
 * 排版原则：
 * - 文学性内容（标题、正文、诗词）→ 思源宋体 (NotoSerifSC)，营造古籍阅读质感
 * - UI 控件（标签、按钮、芯片）→ 系统无衬线 (SystemUISans)，确保功能界面清晰可辨
 */
val Typography = Typography(
    // ========== 展示级 (Display) — 思源宋体 ==========
    displayLarge = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 36.sp,
        letterSpacing = 1.sp
    ),

    // ========== 标题级 (Headline) — 思源宋体 ==========
    headlineLarge = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // ========== 标题级 (Title) — 思源宋体 ==========
    titleLarge = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ========== 正文级 (Body) — 思源宋体 ==========
    bodyLarge = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = NotoSerifSC,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // ========== 标签级 (Label) — 系统 UI 无衬线 ==========
    labelLarge = TextStyle(
        fontFamily = SystemUISans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SystemUISans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SystemUISans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ==================== 诗词专用样式 ====================
/** 诗词正文竖排 */
val PoemBodyStyle = TextStyle(
    fontFamily = NotoSerifSC,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 36.sp,
    letterSpacing = 2.sp
)

/** 诗词标题 */
val PoemTitleStyle = TextStyle(
    fontFamily = NotoSerifSC,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 2.sp
)

/** 作者名 - 妃红印鉴风格 */
val SealedAuthorStyle = TextStyle(
    fontFamily = NotoSerifSC,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp
)

/** 注解、译文等辅助性文本 */
val AnnotationTextStyle = TextStyle(
    fontFamily = NotoSerifSC,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)

/** 诗句竖向展示（行内间距更大） */
val VerseLineStyle = TextStyle(
    fontFamily = NotoSerifSC,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 36.sp,
    letterSpacing = 1.5.sp
)
