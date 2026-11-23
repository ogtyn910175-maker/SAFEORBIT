package com.ogken.safeorbit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ogken.safeorbit.R
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitWhite

@Composable
fun SafeOrbitLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    showTitle: Boolean = true,
    showTagline: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_safeorbit_logo),
            contentDescription = "SafeOrbit Shield & Orbital Pin Logo",
            modifier = Modifier.size(size)
        )

        if (showTitle) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SafeOrbit",
                style = MaterialTheme.typography.headlineLarge,
                color = SafeOrbitWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        if (showTagline) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Stay connected. Share only when you choose.",
                style = MaterialTheme.typography.bodyMedium,
                color = SafeOrbitMint,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "OG KEN Studio • Creator: Tanmay",
                style = MaterialTheme.typography.labelSmall,
                color = SafeOrbitSecondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}
