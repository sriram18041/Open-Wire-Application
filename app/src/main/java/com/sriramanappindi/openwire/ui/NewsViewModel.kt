package com.sriramanappindi.openwire.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sriramanappindi.openwire.data.FeedSource
import com.sriramanappindi.openwire.data.Feeds
import com.sriramanappindi.openwire.data.LocationRegion
import com.sriramanappindi.openwire.data.NewsCache
import com.sriramanappindi.openwire.data.NewsRepository
import com.sriramanappindi.openwire.data.SavedStore
import com.sriramanappindi.openwire.data.Story
import com.sriramanappindi.openwire.data.StoryMatcher
import kotlinx.coroutines.Dispatchers
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

/** Result of asking "who else is covering this story?" for one tapped headline. */
data class CompareState(
    val sourceStory: Story? = null,
    val isLoading: Boolean = false,
    val matches: List<Story> = emptyList(),
    val message: String? = null
)

class NewsViewModel(
    private val repository: NewsRepository,
    private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(isLoading = true))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _compare = MutableStateFlow(CompareState())
    val compare: StateFlow<CompareState> = _compare.asStateFlow()

    private val savedStore = SavedStore(appContext)
    private val _saved = MutableStateFlow(savedStore.load())
    val saved: StateFlow<List<Story>> = _saved.asStateFlow()

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

    /**
     * Tries, once ever per install, to default the feed to wherever the
     * person actually is. Only does anything when location permission has
     * already been granted — the caller (MainActivity) is what asks for it.
     * A location that doesn't resolve to a recognised country, or any
     * failure along the way, just leaves the default "All regions" view in
     * place; this never blocks or retries on later launches.
     */
    fun maybeApplyLocationRegion(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_INITIAL_REGION_TRIED, false)) return
        prefs.edit().putBoolean(KEY_INITIAL_REGION_TRIED, true).apply()

        viewModelScope.launch {
            val region = runCatching { LocationRegion.resolve(context) }.getOrNull()
            if (!region.isNullOrBlank()) {
                setRegion(region)
            }
        }
    }

    /**
     * "Compare coverage": fetches the same category's headlines from a
     * spread of other countries and uses [StoryMatcher] to find the ones
     * most likely covering the same event as [story], so the person can
     * see how the story reads in other countries' own press. The matching
     * is a keyword-overlap heuristic, not real story identity — good
     * enough to surface genuinely related coverage without a backend or
     * any ML model.
     */
    fun compareCoverage(story: Story) {
        _compare.update { CompareState(sourceStory = story, isLoading = true) }
        viewModelScope.launch {
            val feeds = Feeds.compareFeedsFor(story)
            if (feeds.isEmpty()) {
                _compare.update { it.copy(isLoading = false, message = "Comparison isn't available for this category yet.") }
                return@launch
            }
            val fetched = runCatching { repository.fetchSupplementary(feeds) }.getOrDefault(emptyList())
            if (fetched.isEmpty()) {
                _compare.update { it.copy(isLoading = false, message = "Couldn't reach other countries' editions right now.") }
                return@launch
            }
            val matches = StoryMatcher.crossCountryMatches(story, fetched)
            val enriched = runCatching { repository.enrichImages(matches) }.getOrDefault(matches)
            _compare.update {
                it.copy(
                    isLoading = false,
                    matches = enriched,
                    message = if (enriched.isEmpty()) "No close matches elsewhere right now — try again shortly." else null
                )
            }
        }
    }

    fun clearCompare() {
        _compare.update { CompareState() }
    }

    fun isSaved(storyId: String): Boolean = _saved.value.any { it.id == storyId }

    /** Bookmarks a story for later, or un-bookmarks it if it's already saved. */
    fun toggleSaved(story: Story) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = savedStore.toggle(story)
            _saved.update { updated }
        }
    }

    fun filtered(state: UiState): List<Story> {
        val q = state.query.trim().lowercase()
        return state.stories.filter { story ->
            (state.category == "All" || story.category == state.category) &&
                (state.region == "All regions" || story.region == state.region) &&
                (q.isEmpty() || (story.title + " " + story.summary + " " + story.source).lowercase().contains(q))
        }
    }

    companion object {
        private const val PREFS_NAME = "openwire_prefs"
        private const val KEY_INITIAL_REGION_TRIED = "initial_region_tried"

        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                NewsViewModel(NewsRepository(NewsCache(appContext)), appContext)
            }
        }
    }
}
