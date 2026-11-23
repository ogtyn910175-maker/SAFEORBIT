package com.ogken.safeorbit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ogken.safeorbit.theme.SafeOrbitBorder
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitSurface
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite

@Composable
fun LocationConsentDialog(
    onConfirmConsent: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SafeOrbitSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SafeOrbitBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SafeOrbitNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Consent Required",
                            tint = SafeOrbitTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Consent Verification",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SafeOrbitWhite
                        )
                        Text(
                            text = "SafeOrbit Privacy Policy",
                            style = MaterialTheme.typography.labelSmall,
                            color = SafeOrbitMint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SafeOrbit never tracks your device without your explicit knowledge. By proceeding, you knowingly authorize sharing simulation with your family circle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SafeOrbitWhite.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafeOrbitNavy.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Stage 2 Notice",
                            tint = SafeOrbitMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Stage 2 Architecture: Real GPS sensors remain unbound. This toggle only activates the simulated sharing status for UI testing.",
                            style = MaterialTheme.typography.labelSmall,
                            color = SafeOrbitSecondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancel",
                            color = SafeOrbitSecondaryText
                        )
                    }

                    Button(
                        onClick = onConfirmConsent,
                        colors = ButtonDefaults.buttonColors(containerColor = SafeOrbitTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "I Agree",
                            color = SafeOrbitDeepBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
