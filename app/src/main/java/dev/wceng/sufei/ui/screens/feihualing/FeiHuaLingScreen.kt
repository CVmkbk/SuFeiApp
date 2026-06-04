package dev.wceng.sufei.ui.screens.feihualing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeiHuaLingScreen(
    viewModel: FeiHuaLingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterVintage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("飞花令", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (uiState.gamePhase == GamePhase.PLAYING || uiState.gamePhase == GamePhase.FINISHED) {
                        IconButton(onClick = { viewModel.restartGame() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "重新开始",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            AnimatedVisibility(
                visible = uiState.gamePhase == GamePhase.PLAYING || uiState.gamePhase == GamePhase.FINISHED,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌸",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "关键字：「${uiState.keyword}」",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (uiState.gamePhase == GamePhase.PLAYING) {
                                    if (uiState.isLoading) "第 ${uiState.roundCount + 1} 轮 · 对方思考中…"
                                    else "第 ${uiState.roundCount + 1} 轮 · 轮到你了"
                                } else {
                                    "游戏结束"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            when (uiState.gamePhase) {
                GamePhase.SETUP -> {
                    SetupPhase(
                        onStart = { keyword -> viewModel.setKeyword(keyword) }
                    )
                }

                GamePhase.PLAYING, GamePhase.FINISHED -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.messages) { message ->
                            when (message.role) {
                                "system_info" -> SystemInfoCard(message.content)
                                "user" -> ChatBubble(
                                    content = message.content,
                                    isUser = true
                                )
                                "assistant" -> ChatBubble(
                                    content = message.content,
                                    isUser = false
                                )
                            }
                        }
                        if (uiState.isLoading) {
                            item {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "DeepSeek 正在搜索诗句…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    if (uiState.gamePhase == GamePhase.PLAYING) {
                        InputBar(
                            inputText = inputText,
                            onInputChange = { inputText = it },
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendPoem(inputText)
                                    inputText = ""
                                    focusManager.clearFocus()
                                }
                            },
                            isLoading = uiState.isLoading
                        )
                    } else {
                        RestartBar(
                            onRestart = { viewModel.restartGame() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupPhase(
    onStart: (String) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterVintage,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "飞花令",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "行飞花令，以诗会友",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "选择一个关键字，与 DeepSeek 同台较量",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = keyword,
                onValueChange = { if (it.length <= 2) keyword = it },
                label = { Text("输入关键字（1-2字）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (keyword.isNotBlank()) {
                            onStart(keyword)
                            focusManager.clearFocus()
                        }
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("花", "月", "春", "风", "山").forEach { preset ->
                    Button(
                        onClick = {
                            keyword = preset
                            onStart(preset)
                            focusManager.clearFocus()
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = preset,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (keyword.isNotBlank()) {
                        onStart(keyword)
                        focusManager.clearFocus()
                    }
                },
                enabled = keyword.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("开始飞花令", color = Color.White)
            }
        }
    }
}

@Composable
private fun SystemInfoCard(content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = content,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChatBubble(
    content: String,
    isUser: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = if (isUser) "你" else "DeepSeek",
            style = MaterialTheme.typography.labelSmall,
            color = if (isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
        Card(
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                }
            )
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun InputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = { Text("请输入包含关键字的诗句…") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = !isLoading,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() })
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = inputText.isNotBlank() && !isLoading,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (inputText.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun RestartBar(
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onRestart,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("再来一局", color = Color.White)
        }
    }
}