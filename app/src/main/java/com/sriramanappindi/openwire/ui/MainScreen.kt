package com.sriramanappindi.openwire.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.sriramanappindi.openwire.data.Story
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: NewsViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val filtered = viewModel.filtered(state)
    val regions = viewModel.regionsFor(state.stories)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Open Wire", fontWeight = FontWeight.Bold)
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
                            CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CategoryTabs(
                selected = state.category,
                onSelect = viewModel::setCategory
            )

            SearchAndRegionRow(
                query = state.query,
                onQueryChange = viewModel::setQuery,
                region = state.region,
                regions = regions,
                onRegionChange = viewModel::setRegion
            )

            when {
                filtered.isEmpty() && state.error != null && state.stories.isEmpty() -> {
                    ErrorState(message = state.error.orEmpty(), onRetry = viewModel::refresh)
                }
                filtered.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filtered, key = { it.id }) { story ->
                            StoryRow(story) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(story.link)))
                            }
                        }
                    }
                }
            }
        }
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

@Composable
private fun CategoryTabs(selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(com.sriramanappindi.openwire.data.Feeds.CATEGORIES) { cat ->
            FilterChip(
                selected = cat == selected,
                onClick = { onSelect(cat) },
                label = { Text(cat) }
            )
        }
    }
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
            placeholder = { Text("Search headlines") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.wrapContentWidth()) {
            Button(onClick = { expanded = true }) {
                Text(region)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                regions.forEach { r ->
                    DropdownMenuItem(
                        text = { Text(r) },
                        onClick = {
                            onRegionChange(r)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StoryRow(story: Story, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                story.category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Text(story.region, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(timeAgo(story.publishedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        androidx.compose.material3.TextButton(
            onClick = onClick,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                story.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )
        }
        if (story.summary.isNotBlank()) {
            Text(
                story.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )
        }
        Text(
            story.source,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
