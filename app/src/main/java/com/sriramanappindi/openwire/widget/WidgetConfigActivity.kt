package com.sriramanappindi.openwire.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sriramanappindi.openwire.data.Feeds
import com.sriramanappindi.openwire.ui.theme.OpenWireTheme

/**
 * Shown once, right when the person drags the widget onto their home
 * screen, so they can pick which category it should show instead of
 * always defaulting to everything. The system (the home screen launcher,
 * a different app) launches this directly via an explicit intent, so it
 * has to be exported — same reasoning as the widget's own receiver.
 */
class WidgetConfigActivity : ComponentActivity() {
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If the person backs out without picking anything, the widget
        // host treats this as cancelled and never adds the widget.
        setResult(Activity.RESULT_CANCELED)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            OpenWireTheme {
                WidgetConfigScreen(categories = Feeds.CATEGORIES, onPick = ::finishWithCategory)
            }
        }
    }

    private fun finishWithCategory(category: String) {
        OpenWireWidgetProvider.setWidgetCategory(this, widgetId, category)
        OpenWireWidgetProvider.refreshOne(this, widgetId)
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(categories: List<String>, onPick: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose what this widget shows") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(categories) { category ->
                Text(
                    if (category == "All") "All categories" else category,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(category) }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }
    }
}
