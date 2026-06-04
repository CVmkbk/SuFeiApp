package dev.wceng.sufei.ui.screens.splash

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.repository.ImportState
import dev.wceng.sufei.ui.theme.SuFeiTheme

@Composable
fun SplashScreen(
    onInitComplete: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val importState by viewModel.importState.collectAsState()
    var readyToExit by remember { mutableStateOf(false) }

    LaunchedEffect(importState) {
        if (importState is ImportState.Success) {
            readyToExit = true
        }
    }

    // 延迟后触发跳转，让退出动画播放完成
    LaunchedEffect(readyToExit) {
        if (readyToExit) {
            kotlinx.coroutines.delay(600)
            onInitComplete()
        }
    }

    AnimatedVisibility(
        visible = !readyToExit,
        exit = fadeOut(animationSpec = tween(400)) +
                slideOutVertically(animationSpec = tween(400)) { it / 4 }
    ) {
        SplashContent(importState = importState)
    }
}

@Composable
fun SplashContent(importState: ImportState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App 名称 (Logo) — 带入场动画
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(800)) +
                        slideInVertically(
                            animationSpec = tween(800),
                            initialOffsetY = { it / 6 }
                        )
            ) {
                Text(
                    text = "素扉",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 8.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(64.dp))

            // 导入状态展示
            when (val state = importState) {
                is ImportState.Importing -> {
                    val message = when {
                        state.progress < 0.33f -> "正在为您裁切宣纸..."
                        state.progress < 0.66f -> "正为您整理万卷书..."
                        else -> "墨香已至，即将开启..."
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(200.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 提示语 — 使用 AnimatedContent 实现交叉淡入淡出
                        AnimatedContent(
                            targetState = message,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(500)) +
                                        slideInVertically(animationSpec = tween(500)) { it / 4 })
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(300)) +
                                                slideOutVertically(animationSpec = tween(300)) { -it / 4 }
                                    )
                            },
                            label = "splash_message"
                        ) { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
                is ImportState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashContentPreview() {
    SuFeiTheme {
        SplashContent(
            importState = ImportState.Importing(0.45f)
        )
    }
}
