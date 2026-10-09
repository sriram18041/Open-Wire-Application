package com.sriramanappindi.openwire.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Resolves the device's approximate location to one of the app's own
 * region names (e.g. "France", "India"), so a first launch can default to
 * wherever the person actually is instead of a generic global mix. Only
 * ever used once permission has already been granted — callers check that
 * before calling [resolve].
 */
object LocationRegion {

    suspend fun resolve(context: Context): String? {
        val location = withTimeoutOrNull(8_000) { lastKnownOrSingleUpdate(context) } ?: return null
        val countryCode = withTimeoutOrNull(5_000) { countryCodeOf(context, location) } ?: return null
        return regionNameFor(countryCode)
    }

    private fun regionNameFor(countryCode: String): String? {
        val code = countryCode.uppercase(Locale.ROOT)
        return Feeds.COUNTRIES.firstOrNull { it.second == code }?.first
    }

    @SuppressLint("MissingPermission")
    private suspend fun lastKnownOrSingleUpdate(context: Context): Location? = withContext(Dispatchers.Main) {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

        // A fix the device already has is instant and good enough for
        // country-level accuracy — no need to wait on a fresh one.
        providers.mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
            ?.let { return@withContext it }

        val provider = providers.firstOrNull() ?: return@withContext null
        suspendCancellableCoroutine { cont ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (cont.isActive) cont.resume(location)
                    runCatching { manager.removeUpdates(this) }
                }
            }
            try {
                manager.requestSingleUpdate(provider, listener, null)
            } catch (e: Exception) {
                if (cont.isActive) cont.resume(null)
                return@suspendCancellableCoroutine
            }
            cont.invokeOnCancellation { runCatching { manager.removeUpdates(listener) } }
        }
    }

    private suspend fun countryCodeOf(context: Context, location: Location): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                try {
                    Geocoder(context, Locale.getDefault())
                        .getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            if (cont.isActive) cont.resume(addresses.firstOrNull()?.countryCode)
                        }
                } catch (e: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault())
                        .getFromLocation(location.latitude, location.longitude, 1)
                        ?.firstOrNull()?.countryCode
                }.getOrNull()
            }
        }
    }
}
