package com.sriramanappindi.openwire.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sriramanappindi.openwire.data.FeedSource
import com.sriramanappindi.openwire.data.Feeds
import com.sriramanappindi.openwire.data.NewsCache
import com.sriramanappindi.openwire.data.NewsRepository
import com.sriramanappindi.openwire.data.Story
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val AUTO_REFRESH_INTERVAL_MS = 15 * 60 * 1000L

data class UiState(
    val stories: List<Story> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long = 0L,
    val category: String = "All",
    val region: String = "All regions",
    val query: String = "",
    /** Regions whose feeds have been fetched into [stories] this session, beyond the default Feeds.HOME set. */
    val loadedRegions: Set<String> = emptySet()
)

class NewsViewModel(private val repository: NewsRepository) : ViewModel() {

    private val _state = MutableStateFlow(UiState(isLoading = true))
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        val cached = repository.cached()
        if (cached.isNotEmpty()) {
            _state.update { it.copy(stories = cached, isLoading = false) }
        }
        refresh()
        viewModelScope.launch {
            while (true) {
                delay(AUTO_REFRESH_INTERVAL_MS)
                refresh()
            }
        }
    }

    /** Feeds.HOME plus the feeds for every region the person has opened this session. */
    private fun activeFeeds(state: UiState): List<FeedSource> {
        val extra = state.loadedRegions.flatMap { Feeds.feedsFor(it) }
        return (Feeds.HOME + extra).distinctBy { it.url to it.category }
    }

    /** Re-fetches everything currently in view: the default feeds plus any region the person has opened. */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val feeds = activeFeeds(_state.value)
            repository.refresh(feeds)
                .onSuccess { stories ->
                    _state.update {
                        it.copy(
                            stories = stories,
                            isLoading = false,
                            lastUpdated = System.currentTimeMillis(),
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Couldn't refresh") }
                }
        }
    }

    fun setCategory(category: String) = _state.update { it.copy(category = category) }
    fun setQuery(query: String) = _state.update { it.copy(query = query) }

    /** Picking a region the app hasn't fetched yet triggers a one-off fetch of just that region's feeds. */
    fun setRegion(region: String) {
        _state.update { it.copy(region = region) }
        val alreadyLoaded = region == "All regions" ||
            _state.value.loadedRegions.contains(region) ||
            Feeds.feedsFor(region).isEmpty()
        if (alreadyLoaded) return

        _state.update { it.copy(loadedRegions = it.loadedRegions + region) }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val feeds = activeFeeds(_state.value)
            repository.refresh(feeds)
                .onSuccess { stories ->
                    _state.update {
                        it.copy(
                            stories = stories,
                            isLoading = false,
                            lastUpdated = System.currentTimeMillis(),
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Couldn't load that region") }
                }
        }
    }

    /** Every region name the picker can offer, available before anything beyond HOME has loaded. */
    fun allRegions(): List<String> = listOf("All regions") + Feeds.ALL_REGION_NAMES

    fun filtered(state: UiState): List<Story> {
        val q = state.query.trim().lowercase()
        return state.stories.filter { story ->
            (state.category == "All" || story.category == state.category) &&
                (state.region == "All regions" || story.region == state.region) &&
                (q.isEmpty() || (story.title + " " + story.summary + " " + story.source).lowercase().contains(q))
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                NewsViewModel(NewsRepository(NewsCache(context.applicationContext)))
            }
        }
    }
}
