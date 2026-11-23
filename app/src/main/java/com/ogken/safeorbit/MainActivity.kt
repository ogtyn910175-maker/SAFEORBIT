package com.ogken.safeorbit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ogken.safeorbit.navigation.SafeOrbitNavGraph
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitTheme
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure MapLibre is initialized safely
        try {
            MapLibre.getInstance(this)
        } catch (t: Throwable) {
            Log.w("MainActivity", "MapLibre secondary init: ${t.message}")
        }

        enableEdgeToEdge()
        setContent {
            SafeOrbitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SafeOrbitDeepBackground
                ) {
                    SafeOrbitNavGraph()
                }
            }
        }
    }
}
