package com.ogken.safeorbit.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ogken.safeorbit.theme.SafeOrbitBorder
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitSurface
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite
import com.ogken.safeorbit.ui.components.SafeOrbitLogo

@Composable
fun WelcomeScreen(
    onContinueClick: () -> Unit,
    onSetupProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SafeOrbitDeepBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo with Shield & Orbit Pin
            SafeOrbitLogo(
                size = 110.dp,
                showTitle = true,
                showTagline = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Philosophy & Feature Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FeaturePill(
                    icon = Icons.Default.Lock,
                    title = "Strict Consent-First Sharing",
                    description = "Location broadcasting defaults strictly to OFF. Your coordinates are never sent without your explicit agreement."
                )
                FeaturePill(
                    icon = Icons.Default.People,
                    title = "Private Family Circles",
                    description = "Connect only with your trusted family members through unique invite codes."
                )
                FeaturePill(
                    icon = Icons.Default.Shield,
                    title = "Zero-Surveillance Architecture",
                    description = "Built for mutual family safety and peace of mind — never covert tracking or commercial profiling."
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
        ) {
            Button(
                onClick = onSetupProfileClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafeOrbitTeal,
                    contentColor = SafeOrbitDeepBackground
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = SafeOrbitDeepBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set Up Profile & Get Started",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onContinueClick,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Go Directly to Dashboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SafeOrbitMint
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = SafeOrbitMint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SafeOrbit v1.1.0 • Stage 2 Architecture",
                style = MaterialTheme.typography.labelSmall,
                color = SafeOrbitSecondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeaturePill(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SafeOrbitSurface)
            .border(1.dp, SafeOrbitBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SafeOrbitNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SafeOrbitMint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SafeOrbitWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SafeOrbitSecondaryText
                )
            }
        }
    }
}
