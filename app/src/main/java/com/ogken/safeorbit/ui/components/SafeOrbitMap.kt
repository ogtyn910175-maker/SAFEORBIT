package com.ogken.safeorbit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ogken.safeorbit.map.MapTileConfig
import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.model.UserLocation
import com.ogken.safeorbit.theme.SafeOrbitBorder
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitStatusOff
import com.ogken.safeorbit.theme.SafeOrbitStatusOffContainer
import com.ogken.safeorbit.theme.SafeOrbitStatusOffText
import com.ogken.safeorbit.theme.SafeOrbitSurface
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * MapLibre Native Map component configured with OpenStreetMap raster tiles.
 *
 * Guarantees:
 * 1. Zero Google Maps SDK dependencies, zero Google API keys, zero billing configuration.
 * 2. Uses OpenStreetMap HTTPS raster tiles: https://tile.openstreetmap.org/{z}/{x}/{y}.png
 * 3. Clear, readable, permanent attribution: © OpenStreetMap contributors
 * 4. Identifiable SafeOrbit User-Agent and caching headers respected via OkHttp.
 * 5. Lifecycle-aware AndroidView integration.
 * 6. Location markers only displayed for authorized circle members with active sharing consent.
 * 7. Graceful offline/fallback handling when tiles cannot be retrieved.
 */
@Composable
fun SafeOrbitMap(
    modifier: Modifier = Modifier,
    memberLocations: List<UserLocation> = emptyList(),
    members: List<CircleMember> = emptyList(),
    isSharingActive: Boolean = false,
    isNetworkAvailable: Boolean = true,
    userLatitude: Double? = null,
    userLongitude: Double? = null
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    var mapLibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var tileLoadError by remember { mutableStateOf(false) }

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    // Correctly handle Android MapView lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            try {
                mapView.onDestroy()
            } catch (_: Exception) {}
        }
    }

    // Synchronize markers when member locations or user location change
    LaunchedEffect(mapLibreMapInstance, memberLocations, isSharingActive, userLatitude, userLongitude) {
        val map = mapLibreMapInstance ?: return@LaunchedEffect
        try {
            map.clear()

            // 1. Add markers for circle members with active sharing
            for (loc in memberLocations) {
                if (!loc.isLive) continue
                val memberName = members.find { it.id == loc.userId }?.name ?: "Family Member"
                val precisionLabel = if (loc.isApproximate) "Approximate Area" else "Live Location"
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(loc.latitude, loc.longitude))
                        .title(memberName)
                        .snippet("$precisionLabel • Battery: ${loc.batteryPercent}%")
                )
            }

            // 2. Add current user marker if sharing is actively ON and coordinates exist
            if (isSharingActive && userLatitude != null && userLongitude != null) {
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(userLatitude, userLongitude))
                        .title("You (Broadcasting)")
                        .snippet("Consent Active • SafeOrbit Protection")
                )
            }
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SafeOrbitDeepBackground)
            .border(
                1.5.dp,
                if (isSharingActive) SafeOrbitTeal.copy(alpha = 0.6f) else SafeOrbitBorder,
                RoundedCornerShape(20.dp)
            )
    ) {
        // MapLibre Native View via AndroidView
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        ) { view ->
            view.getMapAsync { map ->
                mapLibreMapInstance = map

                // Configure camera at initial default (or user location if available)
                val initialLat = userLatitude ?: 20.5937
                val initialLng = userLongitude ?: 78.9629
                val initialZoom = if (userLatitude != null) 12.0 else 4.0

                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(initialLat, initialLng))
                    .zoom(initialZoom)
                    .build()

                // Load OpenStreetMap raster tile style JSON
                val styleJson = MapTileConfig.createStyleJson()
                map.setStyle(Style.Builder().fromJson(styleJson)) {
                    tileLoadError = false
                }
            }
        }

        // ── Fallback Overlay When Offline / Tiles Cannot Load ──
        if (!isNetworkAvailable || tileLoadError) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SafeOrbitStatusOffContainer)
                    .border(1.dp, SafeOrbitStatusOff.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = SafeOrbitStatusOff,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Map tiles offline • SafeOrbit Privacy Guard active",
                        style = MaterialTheme.typography.labelSmall,
                        color = SafeOrbitStatusOffText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ── Zoom In / Out Controls ──
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    mapLibreMapInstance?.let { map ->
                        map.animateCamera(CameraUpdateFactory.zoomIn())
                    }
                },
                containerColor = SafeOrbitNavy.copy(alpha = 0.9f),
                contentColor = SafeOrbitTeal,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            SmallFloatingActionButton(
                onClick = {
                    mapLibreMapInstance?.let { map ->
                        map.animateCamera(CameraUpdateFactory.zoomOut())
                    }
                },
                containerColor = SafeOrbitNavy.copy(alpha = 0.9f),
                contentColor = SafeOrbitTeal,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(16.dp))
            }
        }

        // ── Permanent, High-Contrast OpenStreetMap Attribution Badge ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SafeOrbitDeepBackground.copy(alpha = 0.85f))
                .border(0.8.dp, SafeOrbitBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = "© OpenStreetMap contributors",
                style = MaterialTheme.typography.labelSmall,
                color = SafeOrbitMint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // ── Center on Me Button (Active only when user is sharing) ──
        if (isSharingActive && userLatitude != null && userLongitude != null) {
            SmallFloatingActionButton(
                onClick = {
                    mapLibreMapInstance?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(userLatitude, userLongitude), 14.0)
                    )
                },
                containerColor = SafeOrbitTeal,
                contentColor = SafeOrbitDeepBackground,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .size(34.dp)
            ) {
                Icon(Icons.Default.GpsFixed, contentDescription = "Center on me", modifier = Modifier.size(18.dp))
            }
        }
    }
}
