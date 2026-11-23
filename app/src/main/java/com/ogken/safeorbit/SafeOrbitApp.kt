package com.ogken.safeorbit

import android.app.Application
import com.ogken.safeorbit.map.MapTileConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.module.http.HttpRequestUtil
import java.io.File

/**
 * Custom Application class for SafeOrbit.
 *
 * Configures:
 * 1. OkHttp client with a compliant, identifiable SafeOrbit User-Agent and standard HTTP disk cache
 *    to strictly honor OpenStreetMap Tile Usage Policies.
 * 2. MapLibre Native SDK initialization (completely offline / open-source, zero API keys or billing).
 */
class SafeOrbitApp : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Setup 20 MB disk cache for tile requests to respect caching headers and reduce bandwidth
            val cacheDir = File(cacheDir, "osm_tile_cache")
            val cache = Cache(cacheDir, 20L * 1024L * 1024L)

            val okHttpClient = OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", MapTileConfig.currentProvider.userAgent)
                        .build()
                    chain.proceed(request)
                }
                .build()

            HttpRequestUtil.setOkHttpClient(okHttpClient)

            // Initialize MapLibre SDK
            MapLibre.getInstance(this)
        } catch (_: Exception) {
            // Graceful initialization
        }
    }
}
