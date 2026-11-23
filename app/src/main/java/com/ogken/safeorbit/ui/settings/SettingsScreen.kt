package com.ogken.safeorbit.ui.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ogken.safeorbit.theme.SafeOrbitBorder
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitNavyLight
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitStatusOff
import com.ogken.safeorbit.theme.SafeOrbitStatusOffText
import com.ogken.safeorbit.theme.SafeOrbitSurface
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite
import com.ogken.safeorbit.ui.components.LocationConsentDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateProfileSettings: () -> Unit,
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    if (uiState.isConsentDialogOpen) {
        LocationConsentDialog(
            onConfirmConsent = { viewModel.confirmConsentAndEnableSharing() },
            onDismissRequest = { viewModel.dismissConsentDialog() }
        )
    }

    if (showDeleteAccountDialog) {
        Dialog(onDismissRequest = { showDeleteAccountDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SafeOrbitSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SafeOrbitBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Delete Account & Data?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitStatusOffText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "This action is permanent. Your user profile, circle memberships, and location records will be immediately erased. You will be signed out permanently.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SafeOrbitSecondaryText
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteAccountDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = SafeOrbitSecondaryText)
                        }
                        Button(
                            onClick = {
                                showDeleteAccountDialog = false
                                viewModel.deleteAccount(context = context, onDeleted = onNavigateToAuth)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeOrbitStatusOff),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete", color = SafeOrbitWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.signOut(context = context, onSignedOut = onNavigateToAuth) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SafeOrbitStatusOffText
                    ),
                    border = BorderStroke(1.dp, SafeOrbitStatusOff.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = SafeOrbitStatusOffText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitStatusOffText
                    )
                }

                OutlinedButton(
                    onClick = { showDeleteAccountDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SafeOrbitStatusOff
                    ),
                    border = BorderStroke(1.dp, SafeOrbitStatusOff.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Account",
                        tint = SafeOrbitStatusOff,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delete Data",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitStatusOff
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
                                    text = uiState.sharingState.statusBadgeText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.sharingState.isSharingActive) SafeOrbitTeal else SafeOrbitWhite
                                )
                                Text(
                                    text = if (uiState.sharingState.isSharingActive) "Active with Family Circle Consent" else "Default Private State • No Tracking",
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
                        text = "Consent Policy: SafeOrbit strictly enforces voluntary location sharing. In Stage 4A, permission flows and transparent controls are active. No hidden background tracking occurs.",
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
                        text = "Version 1.2.0 • Release Candidate (API 26-35)",
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
                                text = "• Stage 1: Foundation & Cloud Build CI [Completed]\n• Stage 2: Profile & Family Circle Interface [Completed]\n• Stage 3: Firebase Auth & Cloud Firestore Circles [Completed]\n• Stage 4: Live Consent GPS, MapLibre & Family Map [Completed]",
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
