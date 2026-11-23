package com.ogken.safeorbit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ogken.safeorbit.theme.SafeOrbitBorder
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitSurface
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite

@Composable
fun LocationConsentDialog(
    circleName: String = "Your Family Circle",
    onConfirmConsent: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SafeOrbitSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, SafeOrbitBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SafeOrbitNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Consent Required",
                            tint = SafeOrbitTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Location Sharing Consent",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SafeOrbitWhite
                        )
                        Text(
                            text = "Explicit Permission & Privacy Notice",
                            style = MaterialTheme.typography.labelSmall,
                            color = SafeOrbitMint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Circle Notice Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafeOrbitNavy.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = SafeOrbitTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Active Circle: $circleName",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SafeOrbitWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explanations
                ConsentPoint(
                    icon = Icons.Default.GpsFixed,
                    title = "Why Location is Needed",
                    description = "SafeOrbit shares your location with your family circle so members can verify your safety in real time."
                )

                Spacer(modifier = Modifier.height(10.dp))

                ConsentPoint(
                    icon = Icons.Default.Shield,
                    title = "Who Can See Your Location",
                    description = "Only verified members of '$circleName' can see your coordinates. Outside users, advertisers, and third parties cannot access this."
                )

                Spacer(modifier = Modifier.height(10.dp))

                ConsentPoint(
                    icon = Icons.Default.Lock,
                    title = "Approximate & Precise Options",
                    description = "In the upcoming system prompt, you may choose either 'Approximate' (city-level) or 'Precise' (exact) location. Both are fully supported."
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Notice on zero background tracking in Stage 4A
                Text(
                    text = "• Location sharing is OFF by default.\n• You can tap 'Stop Sharing' at any moment to turn it OFF.\n• No background tracking or continuous sensor harvesting is active in Stage 4A.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SafeOrbitSecondaryText
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancel",
                            color = SafeOrbitSecondaryText
                        )
                    }

                    Button(
                        onClick = onConfirmConsent,
                        colors = ButtonDefaults.buttonColors(containerColor = SafeOrbitTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text(
                            text = "I Agree & Continue",
                            color = SafeOrbitDeepBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsentPoint(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(SafeOrbitNavy),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SafeOrbitTeal,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
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
