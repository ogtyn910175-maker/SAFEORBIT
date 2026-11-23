package com.ogken.safeorbit

import android.app.Application
import android.util.Log
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
 * 1. MapLibre Native SDK initialization (completely offline / open-source, zero API keys or billing).
 * 2. OkHttp client with a compliant, identifiable SafeOrbit User-Agent and standard HTTP disk cache
 *    to strictly honor OpenStreetMap Tile Usage Policies.
 *
 * All operations are guarded against Throwable so the app can never crash on startup.
 */
class SafeOrbitApp : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // 1. Initialize MapLibre SDK first with applicationContext
            MapLibre.getInstance(applicationContext)

            // 2. Setup OkHttp client with custom User-Agent and safe disk cache
            val okHttpClientBuilder = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", MapTileConfig.currentProvider.userAgent)
                        .build()
                    chain.proceed(request)
                }

            // Safely initialize disk cache without throwing if storage is unavailable
            try {
                cacheDir?.let { cDir ->
                    val cacheDirFile = File(cDir, "osm_tile_cache")
                    okHttpClientBuilder.cache(Cache(cacheDirFile, 20L * 1024L * 1024L))
                }
            } catch (t: Throwable) {
                Log.w("SafeOrbitApp", "Tile cache initialization warning: ${t.message}")
            }

            val okHttpClient = okHttpClientBuilder.build()
            HttpRequestUtil.setOkHttpClient(okHttpClient)
        } catch (t: Throwable) {
            // Catch all Throwables (including UnsatisfiedLinkError, NoClassDefFoundError, etc.)
            // to ensure the application process always starts successfully
            Log.e("SafeOrbitApp", "MapLibre setup handled gracefully: ${t.message}", t)
        }
    }
}
