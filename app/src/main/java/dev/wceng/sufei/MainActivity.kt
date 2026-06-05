package dev.wceng.sufei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.media.BackgroundMusicManager
import dev.wceng.sufei.data.model.UserPreferences
import dev.wceng.sufei.data.repository.ImportState
import dev.wceng.sufei.data.repository.UserPreferencesRepository
import dev.wceng.sufei.ui.SuFeiApp
import dev.wceng.sufei.ui.screens.splash.SplashScreen
import dev.wceng.sufei.ui.screens.splash.SplashViewModel
import dev.wceng.sufei.ui.theme.LocalTextScale
import dev.wceng.sufei.ui.theme.SuFeiTheme
import dev.wceng.sufei.ui.theme.TextScale
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.wceng.sufei.ui.navigation.EntryProviderInstaller
import dev.wceng.sufei.ui.navigation.Navigator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 注入 Hilt 管理的全局导航器
    @Inject
    lateinit var navigator: Navigator

    // 注入所有模块注册的导航入口
    @Inject
    lateinit var entryProviderScopes: Set<@JvmSuppressWildcards EntryProviderInstaller>

    // 直接注入用户偏好仓库
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var musicManager: BackgroundMusicManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userPreferences by userPreferencesRepository.userPreferences
                .collectAsState(initial = UserPreferences())

            // 背景音乐：根据用户偏好开关
            LaunchedEffect(Unit) {
                snapshotFlow { userPreferences.musicEnabled }
                    .distinctUntilChanged()
                    .collect { enabled ->
                        musicManager.setEnabled(enabled)
                    }
            }

            val textScale = TextScale(
                fontScale = userPreferences.fontSizeMultiplier,
                lineHeightScale = userPreferences.lineHeightMultiplier
            )

            CompositionLocalProvider(LocalTextScale provides textScale) {
                SuFeiTheme(
                    darkTheme = userPreferences.useDarkTheme,
                    dynamicColor = userPreferences.useDynamicColor
                ) {
                    val splashViewModel: SplashViewModel = hiltViewModel()
                    val importState by splashViewModel.importState.collectAsState()

                    if (importState is ImportState.Success) {
                        // 使用注入的单例 navigator，确保全站状态同步
                        SuFeiApp(
                            navigator = navigator,
                            entryProviderScopes = entryProviderScopes
                        )
                    } else {
                        SplashScreen(onInitComplete = {})
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        musicManager.start()
    }

    override fun onPause() {
        super.onPause()
        musicManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicManager.release()
    }
}
