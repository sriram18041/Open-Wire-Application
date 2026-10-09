package com.sriramanappindi.openwire.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.sriramanappindi.openwire.data.Story
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: NewsViewModel) {
    val state by viewModel.state.collectAsState()
    val compareState by viewModel.compare.collectAsState()
    val context = LocalContext.current
    val filtered = viewModel.filtered(state)
    val regions = viewModel.allRegions()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // The refresh button (and auto-refresh) were replacing the story list
    // in place, leaving the reader scrolled wherever they happened to be —
    // so new stories landed off-screen above. Snap back to the top whenever
    // fresh content arrives or the person switches what they're looking at.
    LaunchedEffect(state.lastUpdated, state.category, state.region, state.query) {
        if (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0) {
            listState.scrollToItem(0)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.background) {
                Text(
                    "Categories",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
                )
                com.sriramanappindi.openwire.data.Feeds.CATEGORIES.forEach { cat ->
                    val isSelected = cat == state.category
                    NavigationDrawerItem(
                        selected = isSelected,
                        icon = { Text(iconFor(cat), fontSize = 18.sp) },
                        label = { Text(cat, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            viewModel.setCategory(cat)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Categories", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        title = {
                            Column {
                                Text("Open Wire", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = statusLine(state),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.refresh() }) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .height(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                    androidx.compose.material3.HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                SearchAndRegionRow(
                    query = state.query,
                    onQueryChange = viewModel::setQuery,
                    region = state.region,
                    regions = regions,
                    onRegionChange = viewModel::setRegion
                )

                when {
                    filtered.isEmpty() && state.isLoading -> {
                        LoadingState(region = state.region)
                    }
                    filtered.isEmpty() && state.error != null && state.stories.isEmpty() -> {
                        ErrorState(message = state.error.orEmpty(), onRetry = viewModel::refresh)
                    }
                    filtered.isEmpty() -> {
                        EmptyState()
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filtered, key = { it.id }) { story ->
                                StoryCard(
                                    story = story,
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(story.link)))
                                    },
                                    onCompare = { viewModel.compareCoverage(story) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (compareState.sourceStory != null) {
        CompareCoverageDialog(
            compareState = compareState,
            onOpen = { link -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) },
            onDismiss = viewModel::clearCompare
        )
    }
}

private fun statusLine(state: UiState): String {
    if (state.lastUpdated == 0L) return if (state.isLoading) "Loading…" else "Not updated yet"
    val minutes = ((System.currentTimeMillis() - state.lastUpdated) / 60000).toInt()
    val ago = when {
        minutes < 1 -> "just now"
        minutes == 1 -> "1 min ago"
        minutes < 60 -> "$minutes min ago"
        else -> "${minutes / 60}h ago"
    }
    return "Live · updated $ago"
}

/** Emoji read fine on every Android version/OEM skin without bundling an icon font. */
private fun iconFor(category: String): String = when (category) {
    "All" -> "📰"
    "World" -> "🌍"
    "Politics" -> "🏛️"
    "Business" -> "💼"
    "Tech" -> "💻"
    "Science" -> "🔬"
    "Sports" -> "⚽"
    "Entertainment" -> "🎬"
    else -> "•"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndRegionRow(
    query: String,
    onQueryChange: (String) -> Unit,
    region: String,
    regions: List<String>,
    onRegionChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            placeholder = { Text("Search headlines", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        var showPicker by remember { mutableStateOf(false) }
        Button(
            onClick = { showPicker = true },
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(region, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        }

        if (showPicker) {
            RegionPickerDialog(
                regions = regions,
                selected = region,
                onSelect = onRegionChange,
                onDismiss = { showPicker = false }
            )
        }
    }
}

@Composable
private fun RegionPickerDialog(
    regions: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, regions) {
        if (query.isBlank()) regions else regions.filter { it.contains(query, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 460.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Choose a region",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    placeholder = { Text("Search country or region", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(Modifier.height(4.dp))
                if (filtered.isEmpty()) {
                    Text(
                        "No region matches \"$query\". More countries are added over time — tell the developer which one you'd like to see.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn {
                        items(filtered) { r ->
                            val isSelected = r == selected
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSelect(r)
                                        onDismiss()
                                    }
                                    .padding(vertical = 13.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    r,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareCoverageDialog(
    compareState: CompareState,
    onOpen: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val source = compareState.sourceStory ?: return
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "🌍  Compare coverage",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    source.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                Text(
                    "As covered in ${source.region}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                when {
                    compareState.isLoading -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(
                                    "Checking other countries' coverage…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                    compareState.matches.isEmpty() -> {
                        Text(
                            compareState.message ?: "No close matches elsewhere right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(compareState.matches, key = { it.id }) { match ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onOpen(match.link) }
                                        .padding(vertical = 10.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!match.imageUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = match.imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        Spacer(Modifier.width(10.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "${match.region}  ·  ${match.source}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            match.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryCard(story: Story, onClick: () -> Unit, onCompare: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 0.dp)
    ) {
        if (!story.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = story.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    story.category.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "  ·  ${story.region}  ·  ${timeAgo(story.publishedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                story.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 6.dp)
            )

            if (story.summary.isNotBlank()) {
                Text(
                    story.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    story.source,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Read full story  →",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onClick)
                )
            }

            Text(
                "🌍  Compare coverage",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clickable(onClick = onCompare)
            )
        }
    }
}

private fun timeAgo(publishedAt: Long): String {
    if (publishedAt <= 0L) return ""
    val minutes = ((System.currentTimeMillis() - publishedAt) / 60000).toInt()
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(java.util.Date(publishedAt))
    }
}

@Composable
private fun LoadingState(region: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                if (region == "All regions") "Loading headlines…" else "Loading $region headlines…",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "No stories match. Try another category, region or search.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 32.dp))
            Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                Text("Retry")
            }
        }
    }
}
