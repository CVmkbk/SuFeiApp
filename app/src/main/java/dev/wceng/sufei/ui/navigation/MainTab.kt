package dev.wceng.sufei.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val title: String, val icon: ImageVector) {
    Home("今日", Icons.Default.Home),
    Explore("搜索", Icons.Default.Search),
    Chat("飞花令", Icons.AutoMirrored.Filled.Chat),
    Profile("我的", Icons.Default.Person)
}

fun MainTab.toRoute(): Any = when (this) {
    MainTab.Home -> Home
    MainTab.Explore -> Explore
    MainTab.Chat -> Chat
    MainTab.Profile -> Profile
}
