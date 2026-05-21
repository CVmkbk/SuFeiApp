package dev.wceng.sufei.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.SearchResult
import dev.wceng.sufei.data.model.Tag
import dev.wceng.sufei.data.model.Tune
import dev.wceng.sufei.data.repository.PoemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val poemRepository: PoemRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedDynasty = MutableStateFlow<String?>(null)
    val selectedDynasty = _selectedDynasty.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag = _selectedTag.asStateFlow()

    private val _selectedTune = MutableStateFlow<String?>(null)
    val selectedTune = _selectedTune.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchTrigger = MutableStateFlow(0L)

    private val hotTagNames = listOf(
        "唐诗三百首", "宋词三百首", "古诗三百首", "送别", "思乡",
        "山水", "边塞", "咏物", "抒情", "爱情",
        "爱国", "哲理", "闺怨", "豪放", "婉约"
    )

    /**
     * 热门标签：固定显示 15 个热门标签，并确保选中项可见
     */
    val recommendedTags: StateFlow<List<Tag>> = _selectedTag
        .map { selected ->
            val top = hotTagNames.map { Tag(it) }.toMutableList()
            if (selected != null && top.none { it.name == selected }) {
                top.add(0, Tag(selected)) // 如果选中的不在热门列表中，插到最前面
            }
            top
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = hotTagNames.map { Tag(it) }
        )

    private val hotTuneNames = listOf(
        "浣溪沙", "水调歌头", "菩萨蛮", "鹧鸪天", "满江红",
        "临江仙", "蝶恋花", "西江月", "念奴娇", "减字木兰花",
        "沁园春", "点绛唇", "贺新郎", "清平乐", "虞美人"
    )

    /**
     * 热门词牌：固定显示 15 个热门词牌，并确保选中项可见
     */
    val recommendedTunes: StateFlow<List<Tune>> = _selectedTune
        .map { selected ->
            val top = hotTuneNames.map { Tune(it) }.toMutableList()
            if (selected != null && top.none { it.name == selected }) {
                top.add(0, Tune(selected))
            }
            top
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = hotTuneNames.map { Tune(it) }
        )

    val searchResults: StateFlow<SearchResult> =
        _searchTrigger.flatMapLatest {
            _isSearching.value = true
            val query = _searchQuery.value
            val dynasty = _selectedDynasty.value
            val tag = _selectedTag.value
            val tune = _selectedTune.value

            if (query.isBlank() && dynasty == null && tag == null && tune == null) {
                poemRepository.getAllUserPoems(limit = 50).map { poems ->
                    SearchResult(poems = poems)
                }
            } else {
                poemRepository.searchAll(query, dynasty, tag, tune, limit = 50)
            }
        }
            .onEach { _isSearching.value = false }
            .catch { e ->
                _isSearching.value = false
                emit(SearchResult())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SearchResult()
            )

    fun onSearchClick() {
        _searchTrigger.value = _searchTrigger.value + 1
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onDynastySelect(dynasty: String?) {
        _selectedDynasty.value = dynasty
        _searchTrigger.value = _searchTrigger.value + 1
    }

    fun onTagSelect(tag: String?) {
        _selectedTag.value = tag
        _searchTrigger.value = _searchTrigger.value + 1
    }

    fun onTuneSelect(tune: String?) {
        _selectedTune.value = tune
        _searchTrigger.value = _searchTrigger.value + 1
    }
}
