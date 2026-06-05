package dev.wceng.sufei.data.media

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局背景音乐管理器。
 * 播放 res/raw/bg_music.mp3，循环播放，支持开关。
 */
@Singleton
class BackgroundMusicManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var _musicEnabled: Boolean = false

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    /**
     * 启动背景音乐（如果已启用且未播放）。
     * 应在 Activity onResume 时调用。
     */
    fun start() {
        if (!_musicEnabled) return
        if (mediaPlayer?.isPlaying == true) return

        try {
            if (mediaPlayer == null) {
                val resId = context.resources.getIdentifier(
                    "bg_music", "raw", context.packageName
                )
                if (resId == 0) return // 音乐文件不存在，静默跳过

                mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                    isLooping = true
                    setVolume(0.4f, 0.4f)
                    start()
                }
            } else {
                mediaPlayer?.start()
            }
        } catch (_: Exception) {
            // 音乐播放失败不影响主流程
        }
    }

    /**
     * 暂停背景音乐。
     * 应在 Activity onPause 时调用。
     */
    fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) { }
    }

    /**
     * 释放 MediaPlayer 资源。
     * 应在 Activity onDestroy 时调用。
     */
    fun release() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) { }
    }

    /**
     * 设置音乐开关状态。
     * @param enabled true 开启并立即播放，false 暂停
     */
    fun setEnabled(enabled: Boolean) {
        _musicEnabled = enabled
        if (enabled) {
            start()
        } else {
            pause()
        }
    }
}