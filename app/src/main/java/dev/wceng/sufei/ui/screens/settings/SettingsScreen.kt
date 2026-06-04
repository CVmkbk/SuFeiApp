package dev.wceng.sufei.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.BuildConfig
import dev.wceng.sufei.data.model.UserPreferences
import dev.wceng.sufei.ui.components.PreferenceSectionTitle
import dev.wceng.sufei.ui.components.PreferenceSlider
import dev.wceng.sufei.ui.components.PreferenceSwitch
import dev.wceng.sufei.ui.theme.SuFeiTheme

@Composable
fun SettingsScreen(
    onLoginClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val userPreferences by viewModel.userPreferences.collectAsState()

    SettingsContent(
        userPreferences = userPreferences,
        isLoggedIn = viewModel.isLoggedIn,
        onFontSizeChange = viewModel::setFontSizeMultiplier,
        onLineHeightChange = viewModel::setLineHeightMultiplier,
        onDarkThemeToggle = viewModel::setUseDarkTheme,
        onFontFamilyChange = viewModel::setFontFamilyName,
        onLoginClick = onLoginClick,
        onLogoutClick = viewModel::logout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    userPreferences: UserPreferences,
    isLoggedIn: Boolean = false,
    onFontSizeChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit,
    onDarkThemeToggle: (Boolean) -> Unit,
    onFontFamilyChange: (String) -> Unit,
    onLoginClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "设置", 
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 阅读偏好组
            PreferenceSectionTitle(title = "阅读偏好", icon = Icons.Default.TextFormat)
            
            PreferenceSlider(
                label = "字体大小",
                value = userPreferences.fontSizeMultiplier,
                onValueChange = onFontSizeChange,
                valueRange = 0.8f..1.5f
            )

            PreferenceSlider(
                label = "行间距",
                value = userPreferences.lineHeightMultiplier,
                onValueChange = onLineHeightChange,
                valueRange = 1.0f..2.5f
            )

            // 外观组
            Spacer(modifier = Modifier.height(24.dp))
            PreferenceSectionTitle(title = "外观定制", icon = Icons.Default.Palette)

            PreferenceSwitch(
                label = "夜间模式",
                checked = userPreferences.useDarkTheme,
                onCheckedChange = onDarkThemeToggle
            )
            
            // 账号组
            Spacer(modifier = Modifier.height(24.dp))
            PreferenceSectionTitle(title = "账号", icon = Icons.Default.AccountCircle)

            if (isLoggedIn) {
                OutlinedButton(
                    onClick = onLogoutClick,
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsContentPreview() {
    SuFeiTheme {
        SettingsContent(
            userPreferences = UserPreferences(),
            isLoggedIn = false,
            onFontSizeChange = {},
            onLineHeightChange = {},
            onDarkThemeToggle = {},
            onFontFamilyChange = {},
            onLoginClick = {},
            onLogoutClick = {}
        )
    }
}
