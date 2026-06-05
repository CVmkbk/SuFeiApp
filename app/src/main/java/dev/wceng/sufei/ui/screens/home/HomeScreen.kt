package dev.wceng.sufei.ui.screens.home

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.wceng.sufei.R
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.ui.components.LoginPromptDialog
import androidx.compose.ui.res.painterResource
import dev.wceng.sufei.ui.components.MultiColumnVerticalText
import dev.wceng.sufei.ui.components.VerticalText
import dev.wceng.sufei.ui.theme.PoemTitleStyle
import dev.wceng.sufei.ui.theme.SealedAuthorStyle
import dev.wceng.sufei.ui.theme.SuFeiTheme
import dev.wceng.sufei.ui.theme.VerseLineStyle
import dev.wceng.sufei.ui.theme.LocalTextScale
import dev.wceng.sufei.ui.theme.scaledBy


@Composable
fun HomeScreen(
    onPoemClick: (String) -> Unit,
    onLoginClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showLoginDialog by viewModel.showLoginDialog.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshCurrentPoem()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.15f)
        )
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is HomeUiState.Success -> {
                HomeContent(
                    userPoem = state.userPoem,
                    onPoemClick = { onPoemClick(state.userPoem.poem.id) },
                    onFavoriteToggle = { viewModel.toggleFavorite(state.userPoem.poem.id, it) },
                    onRefresh = { viewModel.refresh() }
                )
            }
            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "重试",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("重试")
                    }
                }
            }
        }
    }

    if (showLoginDialog) {
        LoginPromptDialog(
            onDismiss = { viewModel.dismissLoginDialog() },
            onLogin = {
                viewModel.dismissLoginDialog()
                onLoginClick()
            }
        )
    }
}

/**
 * 判断是否为词/曲
 *
 * 三个维度综合判断：
 * 1. 标签维度：是否显式标注"词""曲""诗余"
 * 2. 标题维度：是否含词牌间隔号 "·"（如"水调歌头·明月几时有"）
 * 3. 句式维度：按标点拆成短句后，检查是否等长五言/七言（诗的句式整齐，词则长短错落）
 */
private fun isCi(poem: Poem): Boolean {
    // 维度1：标签识别
    if (poem.tags.any { it.contains("词") || it.contains("曲") || it.contains("诗余") }) return true
    // 维度2：标题识别（词牌名通常含间隔号）
    if (poem.title.contains("·") || poem.title.contains("・")) return true

    // 维度3：句式分析 —— 按中文标点拆分为短句，检查是否为整齐的五言/七言
    val phrases = poem.content
        .split(Regex("[，。！？；、：\"\"''（）\\s]+"))
        .map { it.trim() }
        .filter { it.isNotBlank() }

    if (phrases.isEmpty()) return false

    // 使用 code 属性做 codePoint 级判断，确保中文字符被正确识别为 Letter
    val lengths = phrases.map { phrase ->
        phrase.count { char -> Character.isLetter(char.code) }
    }.filter { it > 0 }

    if (lengths.isEmpty()) return false

    // 所有短句长度相等 且 为 5 或 7 → 是整齐的诗，否则为词/曲
    val isRegularPoem = lengths.all { it == 5 || it == 7 } && lengths.distinct().size == 1
    return !isRegularPoem
}

/**
 * 提取精彩片段，确保是完整句子
 */
private fun extractHighlight(poem: Poem): List<String> {
    val content = poem.content
    val isCiPoem = isCi(poem)

    val fullSentences = content
        .split(Regex("(?<=[。！？])"))
        .map { it.trim() }
        .filter { it.isNotBlank() && it.length > 2 }

    if (fullSentences.isEmpty()) return listOf(content.take(12))

    val targetFullSentence = if (isCiPoem) {
        // 词：提取最后一句（结拍），往往是全词点睛之笔
        fullSentences.last()
    } else {
        // 诗：提取第二句，第一句起兴第二句进入主题，意境完整
        if (fullSentences.size >= 2) fullSentences[1] else fullSentences.first()
    }

    return targetFullSentence
        .split(Regex("(?<=[，；。！？])"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

@Composable
private fun HomeContent(
    userPoem: UserPoem,
    onPoemClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    val poem = userPoem.poem
    val displayLines = remember(poem.content) { extractHighlight(poem) }
    val context = LocalContext.current
    val textScale = LocalTextScale.current

    // 收藏按钮弹跳动画
    val favoriteScale by animateFloatAsState(
        targetValue = if (userPoem.isFavorite) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "favorite_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp)
    ) {
        // 诗词核心区 — 使用 AnimatedContent 实现切换渐变
        AnimatedContent(
            targetState = poem.id,
            modifier = Modifier.align(Alignment.Center),
            transitionSpec = {
                (fadeIn(animationSpec = tween(400)) + scaleIn(
                    initialScale = 0.94f,
                    animationSpec = tween(400)
                )) togetherWith
                (fadeOut(animationSpec = tween(300)) + scaleOut(
                    targetScale = 0.94f,
                    animationSpec = tween(300)
                ))
            },
            label = "poem_content"
        ) { _ ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .clickable(onClick = onPoemClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：标题与诗人
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    MultiColumnVerticalText(
                        text = poem.title,
                        spacing = 3.dp,
                        columnSpacing = 12.dp,
                        maxCharsPerColumn = 8,
                        style = PoemTitleStyle.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ).scaledBy(textScale)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .width(IntrinsicSize.Min)
                            .border(0.8.dp, MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 3.dp, vertical = 5.dp)
                    ) {
                        VerticalText(
                            text = poem.author,
                            spacing = 2.dp,
                            style = SealedAuthorStyle.copy(
                                color = MaterialTheme.colorScheme.primary
                            ).scaledBy(textScale)
                        )
                    }
                }

                // 右侧：诗词正文
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayLines.asReversed().forEachIndexed { index, line ->
                        VerticalText(
                            text = line,
                            spacing = 6.dp,
                            style = VerseLineStyle.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            ).scaledBy(textScale)
                        )
                        if (index < displayLines.size - 1) {
                            Spacer(modifier = Modifier.width(24.dp))
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = "换一首",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // 分享实现
            IconButton(onClick = {
                val shareText = "《${poem.title}》· ${poem.author} [${poem.dynasty}]\n\n" +
                        "${displayLines.joinToString("\n")}\n\n" +
                        "—— 来自「素扉」数字诗集"
                
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "分享诗词")
                context.startActivity(shareIntent)
            }) {
                Icon(
                    imageVector = Icons.Default.IosShare,
                    contentDescription = "分享",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = { onFavoriteToggle(!userPoem.isFavorite) },
                modifier = Modifier.scale(favoriteScale)
            ) {
                Icon(
                    imageVector = if (userPoem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = if (userPoem.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SuFeiTheme {
        Box(modifier = Modifier.background(Color(0xFFF8F3E9))) {
            HomeContent(
                userPoem = UserPoem(
                    poem = Poem(
                        id = "1",
                        sourceUrl = "",
                        title = "望岳",
                        author = "杜甫",
                        dynasty = "唐",
                        content = "岱宗夫如何？齐鲁青未了。\n造化钟神秀，阴阳割昏晓。\n荡胸生曾云，决眦入归鸟。\n会当凌绝顶，一览众山小。",
                        tags = listOf()
                    ),
                    isFavorite = false
                ),
                onPoemClick = {},
                onFavoriteToggle = {},
                onRefresh = {}
            )
        }
    }
}
