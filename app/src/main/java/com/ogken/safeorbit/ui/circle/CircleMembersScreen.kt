package com.ogken.safeorbit.ui.circle

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ogken.safeorbit.model.CircleMember
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleMembersScreen(
    onNavigateBack: () -> Unit,
    onNavigateCreateCircle: () -> Unit,
    onNavigateJoinCircle: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CircleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddMemberDialog by remember { mutableStateOf(false) }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onAdd = { name, role ->
                viewModel.addDemoMember(name, role)
                showAddMemberDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.circle.name, color = SafeOrbitWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SafeOrbitWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddMemberDialog = true }) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Member", tint = SafeOrbitTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SafeOrbitDeepBackground)
            )
        },
        containerColor = SafeOrbitDeepBackground,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Invite Code Banner
            item {
                InviteCodeCard(
                    inviteCode = uiState.circle.inviteCode,
                    onCopyCode = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SafeOrbit Invite Code", uiState.circle.inviteCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Invite code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Quick actions: Create new or join other circle
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onNavigateCreateCircle,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("New Circle", color = SafeOrbitTeal, style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onNavigateJoinCircle,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Join Other", color = SafeOrbitMint, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Section Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Members in Orbit (${uiState.members.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitWhite
                    )
                    Text(
                        text = "Stage 2 Local Demo",
                        style = MaterialTheme.typography.labelSmall,
                        color = SafeOrbitMint
                    )
                }
            }

            // Member list items
            items(uiState.members) { member ->
                MemberCard(member = member)
            }

            // Notice
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SafeOrbitNavy.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Safety Note: In Stage 2, members are simulated for UI flow demonstration. Live GPS transmission remains inactive until explicit consent in Stage 3.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SafeOrbitSecondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun InviteCodeCard(
    inviteCode: String,
    onCopyCode: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SafeOrbitSurface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, SafeOrbitBorder, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Family Invite Code",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SafeOrbitWhite
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SafeOrbitNavy)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Shareable",
                        style = MaterialTheme.typography.labelSmall,
                        color = SafeOrbitMint
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SafeOrbitDeepBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = inviteCode,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitTeal
                    )
                )

                IconButton(onClick = onCopyCode) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy invite code",
                        tint = SafeOrbitMint
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Share this code with family members to let them connect to this orbit.",
                style = MaterialTheme.typography.bodySmall,
                color = SafeOrbitSecondaryText
            )
        }
    }
}

@Composable
private fun MemberCard(member: CircleMember) {
    Card(
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

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SafeOrbitNavy)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = member.role,
                            style = MaterialTheme.typography.labelSmall,
                            color = SafeOrbitMint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeOrbitSecondaryText
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Battery5Bar,
                        contentDescription = "Battery",
                        tint = SafeOrbitTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${member.batteryPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = SafeOrbitWhite
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = member.lastSeenText,
                    style = MaterialTheme.typography.labelSmall,
                    color = SafeOrbitSecondaryText
                )
            }
        }
    }
}

@Composable
private fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, role: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Family Member") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SafeOrbitSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SafeOrbitBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add Demo Member",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SafeOrbitWhite
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Member Name") },
                    placeholder = { Text("e.g. Grandma, Alex") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafeOrbitTeal,
                        unfocusedBorderColor = SafeOrbitBorder,
                        focusedTextColor = SafeOrbitWhite,
                        unfocusedTextColor = SafeOrbitWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role") },
                    placeholder = { Text("e.g. Parent, Child, Guardian") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafeOrbitTeal,
                        unfocusedBorderColor = SafeOrbitBorder,
                        focusedTextColor = SafeOrbitWhite,
                        unfocusedTextColor = SafeOrbitWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = SafeOrbitSecondaryText)
                    }
                    Button(
                        onClick = { if (name.isNotBlank()) onAdd(name, role) },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeOrbitTeal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add", color = SafeOrbitDeepBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
