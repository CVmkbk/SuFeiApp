package dev.wceng.sufei.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.ui.theme.SuFeiTheme
import dev.wceng.sufei.ui.theme.sealRedLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onPoemClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userPreferences by viewModel.userPreferences.collectAsState()
    val favoritePoems by viewModel.favoritePoems.collectAsState()
    val isLoggedIn = viewModel.isLoggedIn
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("我的", fontWeight = FontWeight.Bold)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ProfileSectionTitle(title = "收藏", icon = Icons.Default.Favorite)

            val enabled = isLoggedIn
            val sectionModifier = if (!enabled) Modifier.alpha(0.4f) else Modifier

            Card(
                modifier = sectionModifier
                    .fillMaxWidth()
                    .then(
                        if (enabled) {
                            Modifier.clickable { }
                        } else {
                            Modifier.clickable {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "请先登录",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (enabled && favoritePoems.isNotEmpty()) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (enabled) sealRedLight else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (enabled) "我的收藏" else "收藏（请先登录）",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = if (enabled && favoritePoems.isNotEmpty()) {
                                "共${favoritePoems.size}首"
                            } else if (!enabled) {
                                "登录后查看收藏"
                            } else {
                                "暂无收藏"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (enabled && favoritePoems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                favoritePoems.take(3).forEach { userPoem ->
                    MiniFavoriteItem(
                        userPoem = userPoem,
                        onPoemClick = { onPoemClick(userPoem.poem.id) },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(userPoem.poem.id, false)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "已取消收藏",
                                    actionLabel = "撤销",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.toggleFavorite(userPoem.poem.id, true)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (favoritePoems.size > 3) {
                    Text(
                        text = "查看全部 ${favoritePoems.size} 首收藏 >",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            ProfileSectionTitle(title = "阅读偏好", icon = Icons.Default.TextFormat)

            ProfileSliderItem(
                label = "字体大小",
                value = userPreferences.fontSizeMultiplier,
                onValueChange = viewModel::setFontSizeMultiplier,
                valueRange = 0.8f..1.5f
            )

            ProfileSliderItem(
                label = "行间距",
                value = userPreferences.lineHeightMultiplier,
                onValueChange = viewModel::setLineHeightMultiplier,
                valueRange = 1.0f..2.5f
            )

            ProfileSectionTitle(title = "外观定制", icon = Icons.Default.Palette)

            ProfileSwitchItem(
                label = "夜间模式",
                checked = userPreferences.useDarkTheme,
                onCheckedChange = viewModel::setUseDarkTheme
            )

            ProfileSectionTitle(title = "账号", icon = Icons.Default.AccountCircle)

            if (isLoggedIn) {
                OutlinedButton(
                    onClick = viewModel::logout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("退出登录")
                }
            } else {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("登录 / 注册")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileSectionTitle(title: String, icon: ImageVector) {
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ProfileSliderItem(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ProfileSwitchItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun MiniFavoriteItem(
    userPoem: UserPoem,
    onPoemClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val poem = userPoem.poem
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPoemClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poem.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${poem.dynasty} · ${poem.author}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = poem.content.replace("\n", " "),
                    style = MaterialTheme.typography.bodySmall.copy(
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

@Preview(showBackground = true)
@Composable
fun ProfileContentPreview() {
    SuFeiTheme {
        ProfileContentPreviewInner()
    }
}

@Composable
private fun ProfileContentPreviewInner() {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ProfileSectionTitle(title = "收藏", icon = Icons.Default.Favorite)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.4f)
                    .clickable { },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "收藏（请先登录）",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "登录后查看收藏",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
            ProfileSectionTitle(title = "阅读偏好", icon = Icons.Default.TextFormat)
            ProfileSectionTitle(title = "外观定制", icon = Icons.Default.Palette)
            ProfileSectionTitle(title = "账号", icon = Icons.Default.AccountCircle)
        }
    }
}