package com.ogken.safeorbit.ui.circle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
fun CreateCircleScreen(
    onNavigateBack: () -> Unit,
    onCircleCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CircleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Family Circle", color = SafeOrbitWhite, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SafeOrbitNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = null,
                    tint = SafeOrbitTeal,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Start a Safe Orbit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SafeOrbitWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Create a private circle and invite your loved ones.",
                style = MaterialTheme.typography.bodyMedium,
                color = SafeOrbitSecondaryText
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Circle Name Input
            OutlinedTextField(
                value = uiState.createNameInput,
                onValueChange = { viewModel.onCreateNameChanged(it) },
                label = { Text("Circle Name") },
                placeholder = { Text("e.g. The Sharma Family, Core Team") },
                isError = uiState.createNameError != null,
                supportingText = {
                    if (uiState.createNameError != null) {
                        Text(uiState.createNameError ?: "", color = SafeOrbitStatusOff)
                    } else {
                        Text("Give your family orbit a recognizable name.", color = SafeOrbitSecondaryText)
                    }
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

            // Description Input
            OutlinedTextField(
                value = uiState.createDescInput,
                onValueChange = { viewModel.onCreateDescChanged(it) },
                label = { Text("Description (Optional)") },
                placeholder = { Text("e.g. Home safety & coordinate orbit") },
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

            Spacer(modifier = Modifier.height(24.dp))

            // Demo Notice Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SafeOrbitSurface)
                    .border(1.dp, SafeOrbitBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Demo Info",
                        tint = SafeOrbitMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Stage 2 Architecture Note",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SafeOrbitWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "A unique invite code (e.g. SO-XXX-XXX) will be generated. In Stage 2, circles are stored locally. Cloud sync activates in Stage 3.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SafeOrbitSecondaryText
                        )
                    }
                }
            }

            if (uiState.successMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SafeOrbitMint, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = uiState.successMessage ?: "", color = SafeOrbitMint, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.createCircle(onSuccess = onCircleCreated)
                },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = SafeOrbitTeal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = SafeOrbitDeepBackground,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create Circle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SafeOrbitDeepBackground
                    )
                }
            }
        }
    }
}
