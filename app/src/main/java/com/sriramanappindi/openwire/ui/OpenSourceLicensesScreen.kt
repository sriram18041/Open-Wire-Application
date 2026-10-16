package com.sriramanappindi.openwire.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * One open-source component this app is built on. Kept as plain data
 * (rather than reading it off the classpath at runtime) because the app
 * ships a small, fixed dependency list — see app/build.gradle.kts, which
 * is the source of truth this list is kept in sync with by hand.
 */
private data class LicenseEntry(val name: String, val license: String, val note: String)

private val LICENSES = listOf(
    LicenseEntry(
        "AndroidX (Core, Lifecycle, Activity, Compose, WorkManager)",
        "Apache License 2.0",
        "Google's Jetpack libraries — the UI toolkit, background work, and lifecycle handling this app is built on."
    ),
    LicenseEntry(
        "Kotlin & kotlinx.coroutines",
        "Apache License 2.0",
        "The language the app is written in, and its asynchronous-work library."
    ),
    LicenseEntry(
        "Coil",
        "Apache License 2.0",
        "Loads and caches story thumbnail images."
    ),
    LicenseEntry(
        "Firebase (Analytics, Crashlytics)",
        "Apache License 2.0 (SDK); use of the Firebase service also follows Google's Firebase Terms of Service",
        "Anonymous crash reporting and aggregate usage counts — see the privacy policy for what this does and doesn't collect."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                title = { Text("Open-source licenses") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Open Wire is built on these open-source projects. Each remains the " +
                    "property of its own authors under its own license, reproduced below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp, bottom = 20.dp)
            )

            LICENSES.forEach { entry ->
                Text(
                    entry.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                )
                Text(
                    entry.license,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    entry.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}
