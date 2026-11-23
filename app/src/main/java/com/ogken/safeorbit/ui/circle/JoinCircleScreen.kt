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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun JoinCircleScreen(
    onNavigateBack: () -> Unit,
    onCircleJoined: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CircleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join a Circle", color = SafeOrbitWhite, fontWeight = FontWeight.Bold) },
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
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = SafeOrbitTeal,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter Family Invite Code",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SafeOrbitWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ask your family circle organizer for the 6 to 10 character code.",
                style = MaterialTheme.typography.bodyMedium,
                color = SafeOrbitSecondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Code Input Field
            OutlinedTextField(
                value = uiState.joinCodeInput,
                onValueChange = { viewModel.onJoinCodeChanged(it) },
                label = { Text("Invite Code") },
                placeholder = { Text("e.g. SO-ORBIT-729") },
                isError = uiState.joinCodeError != null,
                supportingText = {
                    if (uiState.joinCodeError != null) {
                        Text(uiState.joinCodeError ?: "", color = SafeOrbitStatusOff)
                    } else {
                        Text("Example code: SO-ORBIT-729", color = SafeOrbitSecondaryText)
                    }
                },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color = SafeOrbitTeal
                ),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Demo Simulation Notice
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
                        contentDescription = "Demo simulation",
                        tint = SafeOrbitMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Stage 2 Demo Simulation",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SafeOrbitWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Entering any valid format code (such as SO-ORBIT-729) demonstrates the circle member sync UI with sample demo profiles. Live peer-to-peer pairing activates in Stage 3.",
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
                    if (viewModel.joinCircle()) {
                        onCircleJoined()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafeOrbitTeal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Join Circle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SafeOrbitDeepBackground
                )
            }
        }
    }
}
