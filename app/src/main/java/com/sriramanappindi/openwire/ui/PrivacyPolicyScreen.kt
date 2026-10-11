package com.sriramanappindi.openwire.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Renders the privacy policy directly in the app instead of opening an
 * external link. The text below is the same policy published separately
 * for the Play Store listing (Play Console requires a hosted URL there),
 * kept word-for-word so the in-app copy and the hosted copy never drift
 * apart from each other.
 */
@Composable
private fun PolicySection(heading: String, content: @Composable () -> Unit) {
    Text(
        heading,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)
    )
    content()
}

@Composable
private fun Body(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun Bullet(text: String) {
    Text(
        "•  $text",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                title = { Text("Privacy policy") },
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
                "Last updated: October 9, 2026",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
            )

            Body(
                "Open Wire is a news reader built around one idea: pull in public RSS " +
                    "and news feeds from around the world, and let you read them without " +
                    "creating an account or handing anything over. This page explains " +
                    "exactly what the app does and doesn't do with your data, in plain " +
                    "language, with nothing left out."
            )

            PolicySection("The short version") {
                Bullet("No account, ever — there's no sign-up, login, or user ID of any kind. Nobody can tie anything the app does back to you by name.")
                Bullet("Nothing leaves your phone — your saved articles, region choice, and settings are stored only on your device, not on any server we run.")
                Bullet("Location, used once — approximate location is read once on first launch, to pick a default country, then never requested again unless you reset it.")
                Bullet("No ads, no selling data — Open Wire has no advertising SDK and we don't sell, rent, or trade data to anyone, for any reason.")
            }

            PolicySection("What the app accesses, and why") {
                Body(
                    "Internet access. Open Wire fetches publicly published RSS and Atom feeds " +
                        "(for example, BBC, Al Jazeera, Times of India, CBC, ABC, and Google News) " +
                        "directly from each publisher's own servers, plus their article pages when " +
                        "a feed doesn't include a preview photo. These requests go straight from your " +
                        "device to that publisher, the same way your phone's browser would reach their " +
                        "website. Each publisher's own privacy policy governs that connection, since " +
                        "Open Wire is only the app making the request, not a server sitting in between."
                )
                Body(
                    "Approximate location. If you grant it, Open Wire reads your device's coarse " +
                        "(network-based, not GPS-precise) location one time, right after install, " +
                        "purely to set your default country feed. That location is converted to a " +
                        "country on your device and is never transmitted anywhere, stored in any file " +
                        "we control, or logged. You can deny the permission with no loss of " +
                        "functionality beyond needing to pick a region yourself; you can also change " +
                        "your region at any time from within the app."
                )
                Body(
                    "Notifications. If you enable them, new-story alerts are generated entirely on " +
                        "your device by periodically re-checking your own feeds in the background. No " +
                        "notification content is generated or routed through any server we operate."
                )
            }

            PolicySection("What we store, and where") {
                Bullet(
                    "On your device only: your chosen region and category filters, your " +
                        "saved/bookmarked articles, your notification preference, and a short-lived " +
                        "local cache of recent headlines so the app opens instantly. Uninstalling the " +
                        "app removes all of it."
                )
                Bullet(
                    "Crash and usage data: Open Wire may use Google Firebase to receive anonymous " +
                        "crash reports and basic, aggregate usage signals (such as which screens are " +
                        "opened or which app version is running), so we can find and fix bugs and " +
                        "understand which features are actually used. This data is not linked to your " +
                        "name, email, or any account, because no account exists. Firebase's own privacy " +
                        "practices are described at firebase.google.com/support/privacy."
                )
                Bullet("Nothing else. We don't collect contacts, photos, messages, browsing history outside the app, or any other device data.")
            }

            PolicySection("Third-party content") {
                Body(
                    "Every headline, summary, and photo you see is published by the original news " +
                        "outlet, not by us. Tapping a story opens that outlet's own article, and from " +
                        "that point you're subject to their site and their policies, same as visiting " +
                        "it directly in a browser. Open Wire doesn't host, edit, or claim ownership of " +
                        "any of this content — it's a reader, not a publisher."
                )
            }

            PolicySection("Children's privacy") {
                Body(
                    "Open Wire is a general news app, not directed at children, and we don't " +
                        "knowingly collect any information from anyone under 13. If you believe a " +
                        "child has provided us information, contact us below and we'll address it."
                )
            }

            PolicySection("Your choices") {
                Bullet("Deny or revoke location access any time in your device's app settings — the app still works, you'll just pick a region manually.")
                Bullet("Turn notifications off from within the app or your device settings.")
                Bullet("Clear saved articles and local cache by clearing the app's storage, or simply by uninstalling it.")
            }

            PolicySection("Changes to this policy") {
                Body(
                    "If what the app collects or how it's used changes meaningfully, this page " +
                        "will be updated and the \"last updated\" date above will change. Significant " +
                        "changes will also be noted in the app's Play Store release notes."
                )
            }

            Text(
                "Questions about this policy or your data? Reach the developer at ram18042001@gmail.com.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 18.dp, bottom = 36.dp)
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:ram18042001@gmail.com")))
                    }
            )
        }
    }
}
