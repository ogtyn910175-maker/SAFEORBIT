package com.ogken.safeorbit.ui.profile

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.ogken.safeorbit.theme.SafeOrbitBorder
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitStatusOff
import com.ogken.safeorbit.theme.SafeOrbitSurface
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val roles = listOf("Circle Creator", "Parent", "Child", "Member")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Profile", color = SafeOrbitWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SafeOrbitWhite
                        )
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Identity Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SafeOrbitSurface)
                    .border(1.dp, SafeOrbitBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(SafeOrbitTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.nameInput.take(1).uppercase().ifBlank { "T" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SafeOrbitDeepBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = uiState.user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SafeOrbitWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "User ID: ${uiState.user.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SafeOrbitSecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display Name
            OutlinedTextField(
                value = uiState.nameInput,
                onValueChange = { viewModel.onNameChanged(it) },
                label = { Text("Display Name") },
                isError = uiState.nameError != null,
                supportingText = {
                    if (uiState.nameError != null) {
                        Text(uiState.nameError ?: "", color = SafeOrbitStatusOff)
                    } else {
                        Text("Visible to all members in your family circle.", color = SafeOrbitSecondaryText)
                    }
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SafeOrbitTeal)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafeOrbitTeal,
                    unfocusedBorderColor = SafeOrbitBorder,
                    focusedLabelColor = SafeOrbitTeal,
                    unfocusedLabelColor = SafeOrbitSecondaryText,
                    focusedTextColor = SafeOrbitWhite,
                    unfocusedTextColor = SafeOrbitWhite
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Role Selector
            Text(
                text = "Circle Role:",
                style = MaterialTheme.typography.titleSmall,
                color = SafeOrbitWhite,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                roles.forEach { role ->
                    val selected = uiState.roleInput == role
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onRoleChanged(role) },
                        label = { Text(role, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SafeOrbitDeepBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SafeOrbitTeal,
                            selectedLabelColor = SafeOrbitDeepBackground,
                            containerColor = SafeOrbitNavy,
                            labelColor = SafeOrbitWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selected) SafeOrbitTeal else SafeOrbitBorder,
                            enabled = true,
                            selected = selected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency Contact Phone
            OutlinedTextField(
                value = uiState.emergencyPhoneInput,
                onValueChange = { viewModel.onEmergencyPhoneChanged(it) },
                label = { Text("Emergency Contact Phone") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = SafeOrbitMint)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafeOrbitTeal,
                    unfocusedBorderColor = SafeOrbitBorder,
                    focusedLabelColor = SafeOrbitTeal,
                    unfocusedLabelColor = SafeOrbitSecondaryText,
                    focusedTextColor = SafeOrbitWhite,
                    unfocusedTextColor = SafeOrbitWhite
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.feedbackMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = SafeOrbitMint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = uiState.feedbackMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeOrbitMint
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveProfile() },
                colors = ButtonDefaults.buttonColors(containerColor = SafeOrbitTeal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Update Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SafeOrbitDeepBackground
                )
            }
        }
    }
}
