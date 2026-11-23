package com.ogken.safeorbit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.ogken.safeorbit.ui.components.LocationSharingStatusCard
import com.ogken.safeorbit.ui.components.RadarMapPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSettingsOpen) {
        SettingsDialog(onDismissRequest = { viewModel.closeSettings() })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
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
                                text = "OG KEN Studio",
                                style = MaterialTheme.typography.labelSmall,
                                color = SafeOrbitMint
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openSettings() }) {
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
            Text(
                text = "Welcome to SafeOrbit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SafeOrbitWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Hello, ${uiState.user.name}. Your consent-based circle is secure.",
                style = MaterialTheme.typography.bodyMedium,
                color = SafeOrbitSecondaryText
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Location Sharing Status: Explicitly OFF
            LocationSharingStatusCard(
                isSharingActive = uiState.sharingState.isSharingActive
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Visually Designed Map Placeholder
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
                    text = "Stage 1 Standby",
                    style = MaterialTheme.typography.labelSmall,
                    color = SafeOrbitMint
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            RadarMapPlaceholder()

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
                Text(
                    text = "${uiState.members.size} Members",
                    style = MaterialTheme.typography.labelSmall,
                    color = SafeOrbitSecondaryText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Family Circle Members Carousel / Row
            LazyRow(
                contentPadding = PaddingValues(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.members) { member ->
                    CircleMemberItem(member = member)
                }

                item {
                    AddMemberPlaceholderItem()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings & Architecture Card
            Card(
                onClick = { viewModel.openSettings() },
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SafeOrbitNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SafeOrbitTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Settings & Application Info",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SafeOrbitWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Manage preferences, inspect architecture and credits",
                            style = MaterialTheme.typography.bodySmall,
                            color = SafeOrbitSecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CircleMemberItem(
    member: CircleMember,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SafeOrbitSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .width(140.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = member.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SafeOrbitWhite
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = member.relation,
                style = MaterialTheme.typography.labelSmall,
                color = SafeOrbitSecondaryText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SafeOrbitNavy)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (member.isCurrentUser) "Owner" else "Member",
                    style = MaterialTheme.typography.labelSmall,
                    color = SafeOrbitMint
                )
            }
        }
    }
}

@Composable
private fun AddMemberPlaceholderItem(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(140.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SafeOrbitNavy.copy(alpha = 0.5f))
            .border(1.dp, SafeOrbitBorder, RoundedCornerShape(16.dp))
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
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Add Member",
                style = MaterialTheme.typography.labelLarge,
                color = SafeOrbitTeal,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Stage 2 Feature",
                style = MaterialTheme.typography.labelSmall,
                color = SafeOrbitSecondaryText
            )
        }
    }
}
