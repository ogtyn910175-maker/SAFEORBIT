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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitNavyLight
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitStatusOff
import com.ogken.safeorbit.theme.SafeOrbitStatusOffContainer
import com.ogken.safeorbit.theme.SafeOrbitStatusOffText
import com.ogken.safeorbit.theme.SafeOrbitSurfaceVariant
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite

@Composable
fun LocationSharingStatusCard(
    modifier: Modifier = Modifier,
    isSharingActive: Boolean = false,
    onToggleClick: (() -> Unit)? = null
) {
    val containerBg = if (isSharingActive) SafeOrbitSurfaceVariant else SafeOrbitStatusOffContainer
    val borderColor = if (isSharingActive) SafeOrbitTeal.copy(alpha = 0.5f) else SafeOrbitStatusOff.copy(alpha = 0.4f)
    val statusText = if (isSharingActive) "SHARING ACTIVE (SIMULATED)" else "LOCATION SHARING: OFF"
    val textColor = if (isSharingActive) SafeOrbitTeal else SafeOrbitStatusOffText
    val indicatorColor = if (isSharingActive) SafeOrbitTeal else SafeOrbitStatusOff

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(containerBg)
            .border(1.2.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(indicatorColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                if (onToggleClick != null) {
                    Switch(
                        checked = isSharingActive,
                        onCheckedChange = { onToggleClick() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SafeOrbitDeepBackground,
                            checkedTrackColor = SafeOrbitTeal,
                            uncheckedThumbColor = SafeOrbitSecondaryText,
                            uncheckedTrackColor = SafeOrbitNavy
                        )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SafeOrbitNavy)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSharingActive) Icons.Default.Sensors else Icons.Default.Lock,
                                contentDescription = "Status",
                                tint = SafeOrbitSecondaryText,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSharingActive) "Simulated" else "Private",
                                style = MaterialTheme.typography.labelSmall,
                                color = SafeOrbitSecondaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSharingActive) {
                    "Simulation mode active for Stage 2 testing. In Stage 3, coordinates will be broadcast with GPS precision only while active."
                } else {
                    "Your location is completely private and not being broadcast. GPS tracking and background transmission are disabled by design."
                },
                style = MaterialTheme.typography.bodySmall,
                color = SafeOrbitWhite.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Consent protection",
                    tint = SafeOrbitMint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Consent-first: Coordinates will never be shared without your explicit agreement.",
                    style = MaterialTheme.typography.labelSmall,
                    color = SafeOrbitMint
                )
            }
        }
    }
}
