package com.sriramanappindi.openwire

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sriramanappindi.openwire.notify.NotificationScheduler
import com.sriramanappindi.openwire.ui.MainScreen
import com.sriramanappindi.openwire.ui.NewsViewModel
import com.sriramanappindi.openwire.ui.theme.OpenWireTheme

private const val PREFS_NAME = "openwire_prefs"
private const val KEY_LOCATION_PROMPTED = "location_prompted"
private const val KEY_NOTIF_PROMPTED = "notif_permission_prompted"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenWireTheme {
                val viewModel: NewsViewModel = viewModel(
                    factory = NewsViewModel.factory(applicationContext)
                )
                val context = LocalContext.current

                val requestLocationPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) viewModel.maybeApplyLocationRegion(context)
                }
                val requestNotificationPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* Whether granted or not, the worker checks for itself before posting. */ }

                // Ask once, on first launch: location is only ever used to
                // default the feed to the person's own country; after that
                // (or if they say no) they pick regions manually as usual.
                LaunchedEffect(Unit) {
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        viewModel.maybeApplyLocationRegion(context)
                    } else if (!prefs.getBoolean(KEY_LOCATION_PROMPTED, false)) {
                        prefs.edit().putBoolean(KEY_LOCATION_PROMPTED, true).apply()
                        requestLocationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }

                    // Same "ask once" pattern for notifications — only exists
                    // as a runtime permission from Android 13 onward, and
                    // only asked if the person hasn't already decided.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !prefs.getBoolean(KEY_NOTIF_PROMPTED, false) &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        prefs.edit().putBoolean(KEY_NOTIF_PROMPTED, true).apply()
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    NotificationScheduler.ensureScheduled(context)
                }

                MainScreen(viewModel)
            }
        }
    }
}
