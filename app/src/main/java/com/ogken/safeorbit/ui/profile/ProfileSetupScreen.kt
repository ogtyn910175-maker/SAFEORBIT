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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
fun ProfileSetupScreen(
    onNavigateBack: () -> Unit,
    onProfileSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val roles = listOf("Circle Creator", "Parent", "Child", "Member")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Setup", color = SafeOrbitWhite, fontWeight = FontWeight.Bold) },
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
            // Avatar Preview
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(SafeOrbitTeal)
                    .border(3.dp, SafeOrbitMint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.nameInput.trim().take(1).uppercase().ifBlank { "S" },
                    style = MaterialTheme.typography.headlineLarge,
                    color = SafeOrbitDeepBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your Family Avatar",
                style = MaterialTheme.typography.labelMedium,
                color = SafeOrbitMint
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Display Name Input
            OutlinedTextField(
                value = uiState.nameInput,
                onValueChange = { viewModel.onNameChanged(it) },
                label = { Text("Display Name") },
                placeholder = { Text("e.g. Tanmay, Mom, Sarah") },
                isError = uiState.nameError != null,
                supportingText = {
                    if (uiState.nameError != null) {
                        Text(uiState.nameError ?: "", color = SafeOrbitStatusOff)
                    } else {
                        Text("${uiState.nameInput.length}/30 characters", color = SafeOrbitSecondaryText)
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Name",
                        tint = SafeOrbitTeal
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafeOrbitTeal,
                    unfocusedBorderColor = SafeOrbitBorder,
                    focusedLabelColor = SafeOrbitTeal,
                    unfocusedLabelColor = SafeOrbitSecondaryText,
                    focusedTextColor = SafeOrbitWhite,
                    unfocusedTextColor = SafeOrbitWhite,
                    errorBorderColor = SafeOrbitStatusOff
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Role in Family Circle
            Text(
                text = "Select Your Role in the Circle:",
                style = MaterialTheme.typography.titleSmall,
                color = SafeOrbitWhite,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                                    contentDescription = "Selected",
                                    tint = SafeOrbitDeepBackground,
                                    modifier = Modifier.size(16.dp)
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

            Spacer(modifier = Modifier.height(20.dp))

            // Emergency Phone Input (Optional)
            OutlinedTextField(
                value = uiState.emergencyPhoneInput,
                onValueChange = { viewModel.onEmergencyPhoneChanged(it) },
                label = { Text("Emergency Contact (Optional)") },
                placeholder = { Text("+1 (555) 000-0000") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = SafeOrbitMint
                    )
                },
                supportingText = {
                    Text("Only visible to your authorized family circle.", color = SafeOrbitSecondaryText)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.saveProfile(onSuccess = onProfileSaved)
                },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = SafeOrbitTeal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (uiState.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = SafeOrbitDeepBackground,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitDeepBackground
                    )
                }
            }
        }
    }
}
