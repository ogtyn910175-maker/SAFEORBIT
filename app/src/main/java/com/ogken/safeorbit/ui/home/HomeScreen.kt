package com.ogken.safeorbit.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ogken.safeorbit.model.CircleMember
import com.ogken.safeorbit.theme.SafeOrbitBorder
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitNavyLight
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitSurface
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite
import com.ogken.safeorbit.ui.components.LocationConsentDialog
import com.ogken.safeorbit.ui.components.LocationSharingStatusCard
import com.ogken.safeorbit.ui.components.RadarMapPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCircleMembers: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCreateCircle: () -> Unit = {},
    onNavigateToJoinCircle: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.isConsentDialogOpen) {
        LocationConsentDialog(
            onConfirmConsent = { viewModel.confirmConsentAndEnableSharing() },
            onDismissRequest = { viewModel.dismissConsentDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToCircleMembers() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SafeOrbitNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "SafeOrbit Icon",
                                tint = SafeOrbitTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SafeOrbit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SafeOrbitWhite
                            )
                            Text(
                                text = uiState.circleName,
                                style = MaterialTheme.typography.labelSmall,
                                color = SafeOrbitMint
                            )
                        }
                    }
                },
                actions = {
                    // Profile button
                    IconButton(onClick = onNavigateToProfile) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(SafeOrbitTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.user.avatarInitial,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SafeOrbitDeepBackground
                            )
                        }
                    }

                    // Settings button
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SafeOrbitTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SafeOrbitDeepBackground,
                    titleContentColor = SafeOrbitWhite
                )
            )
        },
        containerColor = SafeOrbitDeepBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Welcome Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Hello, ${uiState.user.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your consent-based circle is secure.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SafeOrbitSecondaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SafeOrbitNavy)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Stage 3",
                        style = MaterialTheme.typography.labelSmall,
                        color = SafeOrbitMint,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Location Sharing Status Card with Consent Toggle
            LocationSharingStatusCard(
                isSharingActive = uiState.sharingState.isSharingActive,
                onToggleClick = { viewModel.requestToggleSharing() }
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Visually Designed Map Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Live Radar Map",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SafeOrbitWhite
                )
                Text(
                    text = if (uiState.sharingState.isSharingActive) "Simulation Active" else "Consent Guard Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = SafeOrbitMint
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            RadarMapPlaceholder(isSimulating = uiState.sharingState.isSharingActive)

            Spacer(modifier = Modifier.height(24.dp))

            // Family Circle Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Circle",
                        tint = SafeOrbitTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = uiState.circleName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SafeOrbitWhite
                    )
                }

                TextButton(onClick = onNavigateToCircleMembers) {
                    Text(
                        text = "Manage (${uiState.members.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = SafeOrbitMint
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = SafeOrbitMint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Family Circle Members Row
            LazyRow(
                contentPadding = PaddingValues(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.members) { member ->
                    CircleMemberItem(
                        member = member,
                        onClick = onNavigateToCircleMembers
                    )
                }

                item {
                    AddMemberPlaceholderItem(
                        onClick = onNavigateToCircleMembers
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Invite Code Quick Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SafeOrbitSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SafeOrbitBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Family Invite Code",
                            style = MaterialTheme.typography.labelMedium,
                            color = SafeOrbitSecondaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.inviteCode,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeOrbitTeal
                            )
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("SafeOrbit Invite Code", uiState.inviteCode)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Code ${uiState.inviteCode} copied!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = SafeOrbitMint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Actions Navigation Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionTile(
                    title = "New Circle",
                    subtitle = "Create orbit",
                    onClick = onNavigateToCreateCircle,
                    modifier = Modifier.weight(1f)
                )

                ActionTile(
                    title = "Join Circle",
                    subtitle = "Enter code",
                    onClick = onNavigateToJoinCircle,
                    modifier = Modifier.weight(1f)
                )

                ActionTile(
                    title = "Settings",
                    subtitle = "Privacy & info",
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SafeOrbitSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, SafeOrbitBorder, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SafeOrbitWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = SafeOrbitSecondaryText
            )
        }
    }
}

@Composable
private fun CircleMemberItem(
    member: CircleMember,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SafeOrbitSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .width(145.dp)
            .border(1.dp, SafeOrbitBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (member.isCurrentUser) SafeOrbitTeal else SafeOrbitNavyLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.avatarInitial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (member.isCurrentUser) SafeOrbitDeepBackground else SafeOrbitMint
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = member.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SafeOrbitWhite,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = member.role,
                style = MaterialTheme.typography.labelSmall,
                color = SafeOrbitSecondaryText,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Battery5Bar,
                    contentDescription = null,
                    tint = SafeOrbitTeal,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${member.batteryPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = SafeOrbitMint
                )
            }
        }
    }
}

@Composable
private fun AddMemberPlaceholderItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(145.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SafeOrbitNavy.copy(alpha = 0.5f))
            .border(1.dp, SafeOrbitBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SafeOrbitSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Member",
                    tint = SafeOrbitTeal,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Manage Circle",
                style = MaterialTheme.typography.labelLarge,
                color = SafeOrbitTeal,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Invite & list",
                style = MaterialTheme.typography.labelSmall,
                color = SafeOrbitSecondaryText
            )
        }
    }
}
