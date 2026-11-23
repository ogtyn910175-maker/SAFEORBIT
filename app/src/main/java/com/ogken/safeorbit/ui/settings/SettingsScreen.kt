package com.ogken.safeorbit.ui.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ogken.safeorbit.theme.SafeOrbitBorder
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitNavyLight
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitStatusOff
import com.ogken.safeorbit.theme.SafeOrbitSurface
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite
import com.ogken.safeorbit.ui.components.LocationConsentDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateProfileSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isConsentDialogOpen) {
        LocationConsentDialog(
            onConfirmConsent = { viewModel.confirmConsentAndEnableSharing() },
            onDismissRequest = { viewModel.dismissConsentDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = SafeOrbitWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SafeOrbitWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SafeOrbitDeepBackground)
            )
        },
        containerColor = SafeOrbitDeepBackground,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Account Settings Section
            SectionHeader(title = "Account Settings")

            Card(
                onClick = onNavigateProfileSettings,
                colors = CardDefaults.cardColors(containerColor = SafeOrbitSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SafeOrbitBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SafeOrbitTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.user.avatarInitial,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SafeOrbitDeepBackground
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SafeOrbitWhite
                        )
                        Text(
                            text = "${uiState.user.role} • ${uiState.user.emergencyPhone.ifBlank { "No phone set" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SafeOrbitSecondaryText
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = SafeOrbitTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. Location Sharing Controls UI
            SectionHeader(title = "Location Sharing Controls")

            Card(
                colors = CardDefaults.cardColors(containerColor = SafeOrbitSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SafeOrbitBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.sharingState.isSharingActive) Icons.Default.Sensors else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (uiState.sharingState.isSharingActive) SafeOrbitTeal else SafeOrbitStatusOff,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (uiState.sharingState.isSharingActive) "Sharing Active (Simulation)" else "Location Sharing: OFF",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.sharingState.isSharingActive) SafeOrbitTeal else SafeOrbitWhite
                                )
                                Text(
                                    text = if (uiState.sharingState.isSharingActive) "Testing Mode • GPS Offline" else "Default Private State",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SafeOrbitSecondaryText
                                )
                            }
                        }

                        Switch(
                            checked = uiState.sharingState.isSharingActive,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    viewModel.requestEnableSharing()
                                } else {
                                    viewModel.disableSharing()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SafeOrbitDeepBackground,
                                checkedTrackColor = SafeOrbitTeal,
                                uncheckedThumbColor = SafeOrbitSecondaryText,
                                uncheckedTrackColor = SafeOrbitNavy
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Consent Policy: SafeOrbit strictly enforces voluntary location sharing. In Stage 2, sensors are inactive. In Stage 3, coordinates will only transmit when enabled knowingly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeOrbitSecondaryText
                    )
                }
            }

            // 3. Privacy & Safety Guarantees
            SectionHeader(title = "Privacy Guarantees")

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrivacyFeatureItem(
                    icon = Icons.Default.Shield,
                    title = "Zero Covert Tracking",
                    description = "No hidden background coordinates collection. You always have full visibility and control."
                )
                PrivacyFeatureItem(
                    icon = Icons.Default.Security,
                    title = "Consent-First Architecture",
                    description = "Location broadcasting requires affirmative consent before activation."
                )
                PrivacyFeatureItem(
                    icon = Icons.Default.Verified,
                    title = "End-to-End Circle Boundary",
                    description = "Coordinates will be encrypted and shared only within your chosen circle."
                )
            }

            // 4. Help & About Section
            SectionHeader(title = "Help & About")

            Card(
                colors = CardDefaults.cardColors(containerColor = SafeOrbitSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SafeOrbitBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "SafeOrbit — Family Safety & Location Sharing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitMint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.1.0 • Stage 2 Architecture (API 35)",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeOrbitWhite
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Developer Studio: OG KEN Studio\nCreator Credit: Tanmay\nTagline: Stay connected. Share only when you choose.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeOrbitSecondaryText
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SafeOrbitNavy)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Development Roadmap:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SafeOrbitTeal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "• Stage 1: Foundation & Cloud Build CI [Completed]\n• Stage 2: Profile & Family Circle Interface [Active]\n• Stage 3: Firebase Auth & Google Maps GPS [Upcoming]",
                                style = MaterialTheme.typography.labelSmall,
                                color = SafeOrbitWhite.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = SafeOrbitTeal
    )
}

@Composable
private fun PrivacyFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SafeOrbitSurface)
            .border(1.dp, SafeOrbitBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SafeOrbitNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SafeOrbitMint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
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
