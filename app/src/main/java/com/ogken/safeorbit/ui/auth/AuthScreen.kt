package com.ogken.safeorbit.ui.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import com.ogken.safeorbit.ui.components.SafeOrbitLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    if (uiState.isResetDialogOpen) {
        ResetPasswordDialog(
            email = uiState.resetEmailInput,
            onEmailChanged = { viewModel.onResetEmailChanged(it) },
            onDismiss = { viewModel.dismissResetDialog() },
            onSend = { viewModel.sendPasswordReset() },
            isLoading = uiState.isLoading
        )
    }

    Scaffold(
        containerColor = SafeOrbitDeepBackground,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Branding Logo
            SafeOrbitLogo(
                size = 80.dp,
                showTitle = true,
                showTagline = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (uiState.isLoginMode) "Sign In to Your Orbit" else "Create SafeOrbit Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SafeOrbitWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (uiState.isLoginMode) "Access your synchronized family circles across devices." else "Join the consent-first family safety network.",
                style = MaterialTheme.typography.bodySmall,
                color = SafeOrbitSecondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Display Name (Registration only)
            AnimatedVisibility(visible = !uiState.isLoginMode) {
                Column {
                    OutlinedTextField(
                        value = uiState.displayNameInput,
                        onValueChange = { viewModel.onDisplayNameChanged(it) },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g. Tanmay, Mom") },
                        isError = uiState.nameError != null,
                        supportingText = {
                            if (uiState.nameError != null) {
                                Text(uiState.nameError ?: "", color = SafeOrbitStatusOff)
                            }
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SafeOrbitTeal)
                        },
                        singleLine = true,
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Your Role in Circle:",
                        style = MaterialTheme.typography.labelMedium,
                        color = SafeOrbitSecondaryText
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Creator", "Parent", "Child", "Member").forEach { role ->
                            val selected = uiState.roleInput == role
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onRoleChanged(role) },
                                label = { Text(role, style = MaterialTheme.typography.labelSmall) },
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
                }
            }

            // Email Field
            OutlinedTextField(
                value = uiState.emailInput,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = { Text("Email Address") },
                placeholder = { Text("you@example.com") },
                isError = uiState.emailError != null,
                supportingText = {
                    if (uiState.emailError != null) {
                        Text(uiState.emailError ?: "", color = SafeOrbitStatusOff)
                    }
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = SafeOrbitTeal)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = authTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = uiState.passwordInput,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = { Text("Password") },
                placeholder = { Text("At least 6 characters") },
                isError = uiState.passwordError != null,
                supportingText = {
                    if (uiState.passwordError != null) {
                        Text(uiState.passwordError ?: "", color = SafeOrbitStatusOff)
                    }
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SafeOrbitMint)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = SafeOrbitSecondaryText
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = authTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Confirm Password Field (Registration only)
            AnimatedVisibility(visible = !uiState.isLoginMode) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.confirmPasswordInput,
                        onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                        label = { Text("Confirm Password") },
                        isError = uiState.confirmPasswordError != null,
                        supportingText = {
                            if (uiState.confirmPasswordError != null) {
                                Text(uiState.confirmPasswordError ?: "", color = SafeOrbitStatusOff)
                            }
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SafeOrbitMint)
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Forgot Password (Login mode)
            if (uiState.isLoginMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.openResetDialog() }) {
                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.labelMedium,
                            color = SafeOrbitMint
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Error feedback message
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SafeOrbitStatusOff.copy(alpha = 0.15f))
                        .border(1.dp, SafeOrbitStatusOff.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeOrbitStatusOff
                    )
                }
            }

            // Success feedback message
            if (uiState.successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SafeOrbitTeal.copy(alpha = 0.15f))
                        .border(1.dp, SafeOrbitTeal.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = uiState.successMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeOrbitTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Auth Button
            Button(
                onClick = { viewModel.submitAuth(onSuccess = onAuthSuccess) },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = SafeOrbitTeal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = SafeOrbitDeepBackground,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (uiState.isLoginMode) "Sign In" else "Create Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitDeepBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (uiState.isLoginMode) "Don't have an account?" else "Already have an account?",
                    style = MaterialTheme.typography.bodySmall,
                    color = SafeOrbitSecondaryText
                )
                TextButton(onClick = { viewModel.toggleMode() }) {
                    Text(
                        text = if (uiState.isLoginMode) "Sign Up" else "Log In",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitMint
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy Security Assurance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SafeOrbitSurface)
                    .border(1.dp, SafeOrbitBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Security Guarantee: SafeOrbit uses Firebase Authentication. Passwords are encrypted by Google Cloud security and never stored in plaintext.",
                    style = MaterialTheme.typography.labelSmall,
                    color = SafeOrbitSecondaryText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ResetPasswordDialog(
    email: String,
    onEmailChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
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
                    text = "Reset Password",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SafeOrbitWhite
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your account email to receive a password recovery link.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SafeOrbitSecondaryText
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChanged,
                    label = { Text("Account Email") },
                    singleLine = true,
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
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
                        onClick = onSend,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = SafeOrbitTeal),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = SafeOrbitDeepBackground,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Send Link", color = SafeOrbitDeepBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SafeOrbitTeal,
    unfocusedBorderColor = SafeOrbitBorder,
    focusedLabelColor = SafeOrbitTeal,
    unfocusedLabelColor = SafeOrbitSecondaryText,
    focusedTextColor = SafeOrbitWhite,
    unfocusedTextColor = SafeOrbitWhite
)
