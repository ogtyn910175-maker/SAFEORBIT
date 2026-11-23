package com.ogken.safeorbit.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ogken.safeorbit.model.LocationPermissionStatus
import com.ogken.safeorbit.model.LocationSharingState
import com.ogken.safeorbit.model.SharingStatusSummary
import com.ogken.safeorbit.theme.SafeOrbitAmber
import com.ogken.safeorbit.theme.SafeOrbitAmberContainer
import com.ogken.safeorbit.theme.SafeOrbitAmberText
import com.ogken.safeorbit.theme.SafeOrbitBorder
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitStatusOff
import com.ogken.safeorbit.theme.SafeOrbitStatusOffContainer
import com.ogken.safeorbit.theme.SafeOrbitStatusOffText
import com.ogken.safeorbit.theme.SafeOrbitSurface
import com.ogken.safeorbit.theme.SafeOrbitSurfaceVariant
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite

@Composable
fun LocationSharingSection(
    sharingState: LocationSharingState,
    circleName: String,
    onStartSharingClick: () -> Unit,
    onStopSharingClick: () -> Unit,
    onJoinOrCreateCircleClick: () -> Unit,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSharingOn = sharingState.isSharingActive

    // Container visual theme based on state
    val containerBorder = when {
        isSharingOn -> SafeOrbitTeal.copy(alpha = 0.6f)
        sharingState.statusSummary == SharingStatusSummary.PERMISSION_DENIED -> SafeOrbitStatusOff.copy(alpha = 0.5f)
        sharingState.statusSummary == SharingStatusSummary.LOCATION_SERVICES_DISABLED -> SafeOrbitAmber.copy(alpha = 0.5f)
        else -> SafeOrbitBorder
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SafeOrbitSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.2.dp, containerBorder, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // ── Section 1: Main Status Header & Indicator ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    StatusIndicatorDot(summary = sharingState.statusSummary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = sharingState.statusBadgeText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = getStatusTextColor(sharingState.statusSummary)
                        )
                        Text(
                            text = if (isSharingOn) "Live broadcast enabled with consent" else "Privacy Guard active • Zero GPS broadcast",
                            style = MaterialTheme.typography.labelSmall,
                            color = SafeOrbitSecondaryText
                        )
                    }
                }

                // Quick State Badge Pill
                StatusPill(summary = sharingState.statusSummary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Section 2: Detailed Parameters (Circle, Permission, Services) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SafeOrbitNavy.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Family Circle Name Row
                    StatusRowItem(
                        icon = Icons.Default.Group,
                        label = "Family Circle:",
                        value = if (sharingState.hasFamilyCircle) circleName else "No circle connected",
                        valueColor = if (sharingState.hasFamilyCircle) SafeOrbitMint else SafeOrbitAmberText
                    )

                    // Permission Status Row (Approximate vs Precise)
                    StatusRowItem(
                        icon = Icons.Default.Security,
                        label = "Permission:",
                        value = sharingState.permissionDescription,
                        valueColor = when (sharingState.permissionStatus) {
                            LocationPermissionStatus.PRECISE_GRANTED -> SafeOrbitTeal
                            LocationPermissionStatus.APPROXIMATE_GRANTED -> SafeOrbitMint
                            LocationPermissionStatus.DENIED -> SafeOrbitStatusOffText
                            LocationPermissionStatus.NOT_REQUESTED -> SafeOrbitSecondaryText
                        }
                    )

                    // Device Location Services Row
                    StatusRowItem(
                        icon = Icons.Default.GpsFixed,
                        label = "Device Location:",
                        value = if (sharingState.isLocationServicesEnabled) "Services Enabled" else "Disabled in Android Settings",
                        valueColor = if (sharingState.isLocationServicesEnabled) SafeOrbitTeal else SafeOrbitAmberText
                    )

                    // Network Status Row
                    StatusRowItem(
                        icon = Icons.Default.NetworkCheck,
                        label = "Network Status:",
                        value = if (sharingState.isNetworkAvailable) "Connected (Online)" else "Offline / No Connection",
                        valueColor = if (sharingState.isNetworkAvailable) SafeOrbitTeal else SafeOrbitStatusOffText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Section 3: Transparent Explanations ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SafeOrbitDeepBackground.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // What is shared
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SafeOrbitTeal,
                        modifier = Modifier.size(16.dp).padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "What is shared: ${sharingState.explanationWhatIsShared}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeOrbitSecondaryText
                    )
                }

                // Who can see it
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SafeOrbitMint,
                        modifier = Modifier.size(16.dp).padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Who can see it: ${sharingState.explanationWhoCanSee}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeOrbitSecondaryText
                    )
                }
            }

            // Notice banner for specific blocked states
            AnimatedVisibility(visible = sharingState.statusSummary == SharingStatusSummary.PERMISSION_DENIED) {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SafeOrbitStatusOffContainer)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SafeOrbitStatusOff,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Location permission is required to broadcast coordinates to your circle.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SafeOrbitStatusOffText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Section 4: Action Controls (Start & Stop Sharing) ──
            if (isSharingOn) {
                // STOP SHARING BUTTON (Always accessible, high visibility)
                Button(
                    onClick = onStopSharingClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafeOrbitStatusOff,
                        contentColor = SafeOrbitWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Sharing",
                        tint = SafeOrbitWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stop Sharing Location",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // START SHARING BUTTON (When sharing is OFF)
                if (sharingState.hasFamilyCircle) {
                    Button(
                        onClick = onStartSharingClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafeOrbitTeal,
                            contentColor = SafeOrbitDeepBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Sharing",
                            tint = SafeOrbitDeepBackground,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Sharing Location",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Guidance when no family circle exists
                    OutlinedButton(
                        onClick = onJoinOrCreateCircleClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = null,
                            tint = SafeOrbitMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create or Join a Circle to Share",
                            color = SafeOrbitMint,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SafeOrbitSecondaryText,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = SafeOrbitSecondaryText
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun StatusIndicatorDot(summary: SharingStatusSummary) {
    val color = when (summary) {
        SharingStatusSummary.ON -> SafeOrbitTeal
        SharingStatusSummary.OFF -> SafeOrbitSecondaryText
        SharingStatusSummary.PERMISSION_DENIED -> SafeOrbitStatusOff
        SharingStatusSummary.LOCATION_SERVICES_DISABLED -> SafeOrbitAmber
        SharingStatusSummary.NO_FAMILY_CIRCLE -> SafeOrbitSecondaryText
        SharingStatusSummary.NETWORK_UNAVAILABLE -> SafeOrbitStatusOff
        SharingStatusSummary.LOCATION_UNAVAILABLE -> SafeOrbitAmber
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun StatusPill(summary: SharingStatusSummary) {
    val (bgColor, textColor, label) = when (summary) {
        SharingStatusSummary.ON -> Triple(SafeOrbitTeal.copy(alpha = 0.2f), SafeOrbitTeal, "ON")
        SharingStatusSummary.OFF -> Triple(SafeOrbitNavy, SafeOrbitSecondaryText, "OFF")
        SharingStatusSummary.PERMISSION_DENIED -> Triple(SafeOrbitStatusOffContainer, SafeOrbitStatusOffText, "DENIED")
        SharingStatusSummary.LOCATION_SERVICES_DISABLED -> Triple(SafeOrbitAmberContainer, SafeOrbitAmberText, "GPS OFF")
        SharingStatusSummary.NO_FAMILY_CIRCLE -> Triple(SafeOrbitNavy, SafeOrbitSecondaryText, "NO CIRCLE")
        SharingStatusSummary.NETWORK_UNAVAILABLE -> Triple(SafeOrbitStatusOffContainer, SafeOrbitStatusOffText, "OFFLINE")
        SharingStatusSummary.LOCATION_UNAVAILABLE -> Triple(SafeOrbitAmberContainer, SafeOrbitAmberText, "UNAVAILABLE")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private fun getStatusTextColor(summary: SharingStatusSummary): Color = when (summary) {
    SharingStatusSummary.ON -> SafeOrbitTeal
    SharingStatusSummary.OFF -> SafeOrbitWhite
    SharingStatusSummary.PERMISSION_DENIED -> SafeOrbitStatusOffText
    SharingStatusSummary.LOCATION_SERVICES_DISABLED -> SafeOrbitAmberText
    SharingStatusSummary.NO_FAMILY_CIRCLE -> SafeOrbitWhite
    SharingStatusSummary.NETWORK_UNAVAILABLE -> SafeOrbitStatusOffText
    SharingStatusSummary.LOCATION_UNAVAILABLE -> SafeOrbitAmberText
}
