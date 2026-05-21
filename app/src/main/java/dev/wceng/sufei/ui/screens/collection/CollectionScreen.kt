package dev.wceng.sufei.ui.screens.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.ui.components.LoginPromptDialog
import dev.wceng.sufei.ui.theme.SuFeiTheme
import dev.wceng.sufei.ui.theme.sealRedLight
import kotlinx.coroutines.launch

@Composable
fun CollectionScreen(
    onPoemClick: (String) -> Unit,
    onLoginClick: () -> Unit = {},
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showLoginDialog by viewModel.showLoginDialog.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CollectionContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onPoemClick = onPoemClick,
        onToggleFavorite = { id, isFav ->
            viewModel.toggleFavorite(id, isFav)
            if (!isFav) {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "已取消收藏",
                        actionLabel = "撤销",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.toggleFavorite(id, true)
                    }
                }
            }
        },
        onRefresh = { viewModel.refresh() }
    )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionContent(
    uiState: CollectionUiState,
    snackbarHostState: SnackbarHostState,
    onPoemClick: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("收藏", fontWeight = FontWeight.Bold)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is CollectionUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CollectionUiState.Empty -> {
                    EmptyCollectionState(modifier = Modifier.align(Alignment.Center))
                }
                is CollectionUiState.Success -> {
                    FavoritePoemList(
                        poems = uiState.poems,
                        onPoemClick = onPoemClick,
                        onToggleFavorite = onToggleFavorite
                    )
                }
                is CollectionUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = onRefresh) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritePoemList(
    poems: List<UserPoem>,
    onPoemClick: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(poems, key = { it.poem.id }) { userPoem ->
            FavoritePoemItem(
                modifier = Modifier.animateItem(),
                userPoem = userPoem,
                onClick = { onPoemClick(userPoem.poem.id) },
                onToggleFavorite = { onToggleFavorite(userPoem.poem.id, false) }
            )
        }
    }
}

@Composable
fun FavoritePoemItem(
    modifier: Modifier,
    userPoem: UserPoem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val poem = userPoem.poem
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poem.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${poem.dynasty} · ${poem.author}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = poem.content.replace("\n", " "),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "取消收藏",
                    tint = sealRedLight
                )
            }
        }
    }
}

@Composable
fun EmptyCollectionState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "暂无枕边书",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            )
            Text(
                text = "且向万卷求",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionContentPreview() {
    SuFeiTheme {
        CollectionContent(
            uiState = CollectionUiState.Success(
                poems = listOf(
                    UserPoem(
                        poem = Poem(
                            id = "1",
                            sourceUrl = "",
                            title = "春晓",
                            author = "孟浩然",
                            dynasty = "唐",
                            content = "春眠不觉晓，处处闻啼鸟。\n夜来风雨声，花落知多少。",
                            tags = listOf()
                        ),
                        isFavorite = true
                    )
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onPoemClick = {},
            onToggleFavorite = { _, _ -> },
            onRefresh = {}
        )
    }
}