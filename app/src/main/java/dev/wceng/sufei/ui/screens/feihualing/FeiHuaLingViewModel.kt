package dev.wceng.sufei.ui.screens.feihualing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.network.api.DeepSeekApiService
import dev.wceng.sufei.data.network.dto.DeepSeekChatRequest
import dev.wceng.sufei.data.network.dto.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import retrofit2.HttpException

data class FeiHuaLingUiState(
    val gamePhase: GamePhase = GamePhase.SETUP,
    val keyword: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val roundCount: Int = 0
)

enum class GamePhase {
    SETUP,
    PLAYING,
    FINISHED
}

data class ChatMessage(
    val role: String,
    val content: String
)

@HiltViewModel
class FeiHuaLingViewModel @Inject constructor(
    private val deepSeekApiService: DeepSeekApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeiHuaLingUiState())
    val uiState: StateFlow<FeiHuaLingUiState> = _uiState

    private val conversationHistory = mutableListOf<Message>()

    fun setKeyword(keyword: String) {
        if (keyword.isBlank()) return

        val trimmed = keyword.trim()
        conversationHistory.clear()
        conversationHistory.add(
            Message(
                role = "system",
                content = buildSystemPrompt(trimmed)
            )
        )

        _uiState.value = FeiHuaLingUiState(
            gamePhase = GamePhase.PLAYING,
            keyword = trimmed,
            messages = listOf(
                ChatMessage("system_info", "飞花令开始！关键字：「$trimmed」\n请说出一句包含「$trimmed」的古诗词。")
            )
        )
    }

    fun sendPoem(poem: String) {
        if (poem.isBlank()) return
        val currentState = _uiState.value
        if (currentState.gamePhase != GamePhase.PLAYING) return
        if (currentState.isLoading) return

        val keyword = currentState.keyword
        val trimmedPoem = poem.trim()

        val newMessages = currentState.messages + ChatMessage("user", trimmedPoem)
        _uiState.value = currentState.copy(
            messages = newMessages,
            isLoading = true,
            error = null
        )

        conversationHistory.add(Message(role = "user", content = trimmedPoem))

        viewModelScope.launch {
            try {
                val request = DeepSeekChatRequest(
                    messages = conversationHistory.toList(),
                    temperature = 0.3,
                    maxTokens = 512
                )
                val response = deepSeekApiService.chatCompletions(request)
                val aiContent = response.choices.firstOrNull()?.message?.content ?: ""

                if (aiContent.isBlank()) {
                    handleAiGiveUp(keyword)
                    return@launch
                }

                conversationHistory.add(Message(role = "assistant", content = aiContent))

                if (aiContent.contains("我认输了")) {
                    val finishedState = _uiState.value
                    _uiState.value = finishedState.copy(
                        gamePhase = GamePhase.FINISHED,
                        messages = finishedState.messages + ChatMessage("assistant", aiContent) +
                                ChatMessage("system_info", "🎉 恭喜！DeepSeek 认输了！你是飞花令高手！"),
                        isLoading = false,
                        error = null
                    )
                } else {
                    val playingState = _uiState.value
                    _uiState.value = playingState.copy(
                        messages = playingState.messages + ChatMessage("assistant", aiContent),
                        roundCount = playingState.roundCount + 1,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun restartGame() {
        conversationHistory.clear()
        _uiState.value = FeiHuaLingUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun handleAiGiveUp(keyword: String) {
        val state = _uiState.value
        _uiState.value = state.copy(
            gamePhase = GamePhase.FINISHED,
            messages = state.messages + ChatMessage("system_info", "🎉 恭喜！DeepSeek 词穷了！\n你赢得了「$keyword」的飞花令！"),
            isLoading = false,
            error = null
        )
    }

    private fun handleError(e: Exception) {
        val message = when (e) {
            is HttpException -> {
                val errorBody = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                when (e.code()) {
                    400 -> "请求参数错误：${errorBody ?: "请检查请求格式"}"
                    401 -> "API 密钥无效，请检查密钥配置"
                    403 -> "API 密钥无权限，请检查密钥配置"
                    429 -> "请求过于频繁，请稍后重试"
                    500, 502, 503 -> "DeepSeek 服务异常，请稍后重试"
                    else -> "请求失败 (${e.code()})：${errorBody ?: e.message()}"
                }
            }
            is SocketTimeoutException -> "响应超时，请重试"
            is UnknownHostException -> "网络连接失败，请检查网络后重试"
            is IOException -> when {
                e.message?.contains("timeout", true) == true -> "请求超时，请稍后重试"
                e.message?.contains("Unable to resolve host") == true -> "网络连接失败，请检查网络后重试"
                else -> "网络错误：${e.message}"
            }
            else -> e.message ?: "未知错误，请重试"
        }

        val state = _uiState.value
        conversationHistory.removeLastOrNull()
        _uiState.value = state.copy(
            isLoading = false,
            error = message
        )
    }

    private fun buildSystemPrompt(keyword: String): String {
        return """
你正在和用户进行"飞花令"游戏，你是用户的对手。

【关键字】「$keyword」

【规则】
1. 用户会发送一句完整的、包含「$keyword」的古诗词。
2. 你必须从记忆中独立检索，回应另一句不同的、包含「$keyword」的古诗词。
3. 回复格式：仅输出「古诗词原句」+ 括号注明「作者《作品名》」。
4. 不能说重复的诗句，不能自己编造，必须是真实存在的古诗词。
5. 实在找不到合适的诗句时，只回复三个字：我认输了

【严禁行为】
- 严禁续写、接龙或补全用户的诗句。用户的诗句和你的回应是完全独立的。
- 严禁对用户的诗句进行点评、赏析、解释或追问。
- 严禁输出除「诗句（作者《作品名》）」或「我认输了」以外的任何内容。

【示例】
用户：春风又绿江南岸（王安石《泊船瓜洲》）
你：春风得意马蹄疾（孟郊《登科后》）  ← 正确：列出了完全不同的诗句

错误示范（绝对禁止）：
你：明月何时照我还 ← 错误！这是续写了《泊船瓜洲》的下一句！

现在请等待用户出诗。
        """.trimIndent()
    }
}