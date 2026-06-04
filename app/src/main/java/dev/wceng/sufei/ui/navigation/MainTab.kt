package dev.wceng.sufei.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val title: String, val icon: ImageVector) {
    Home("今日", Icons.AutoMirrored.Filled.MenuBook),
    Explore("搜索", Icons.Default.Search),
    Chat("飞花令", Icons.Default.FilterVintage),
    Profile("我的", Icons.Default.AccountCircle)
}

fun MainTab.toRoute(): Any = when (this) {
    MainTab.Home -> Home
    MainTab.Explore -> Explore
    MainTab.Chat -> Chat
    MainTab.Profile -> Profile
}
