package com.ogken.safeorbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ogken.safeorbit.navigation.SafeOrbitNavGraph
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
