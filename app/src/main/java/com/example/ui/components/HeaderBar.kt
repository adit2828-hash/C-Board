package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BleConnectionState
import com.example.ui.theme.SensorGreen
import com.example.ui.theme.SensorOrange
import com.example.ui.theme.SensorRed

@Composable
fun HeaderBar(
    connectionState: BleConnectionState,
    isSimulationMode: Boolean,
    onConnectClicked: () -> Unit,
    onDisconnectClicked: () -> Unit,
    onReconnectClicked: () -> Unit,
    onCalibrationClicked: () -> Unit,
    onDiagnosticsClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("header_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: App Title & Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isSimulationMode) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SensorOrange.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "SIMULASI",
                                        color = SensorOrange,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "AAC Kedipan Mata • ESP32 BLE",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Top Toolbar Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCalibrationClicked,
                        modifier = Modifier.testTag("btn_calibration_header")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Kalibrasi Sensor",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDiagnosticsClicked,
                        modifier = Modifier.testTag("btn_diagnostics_header")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = stringResource(R.string.cd_diagnostics),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onSettingsClicked,
                        modifier = Modifier.testTag("btn_settings_header")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.cd_settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bottom Row: Status Pill & Bluetooth Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Indicator Pill
                val (statusColor, statusIcon, statusText) = when (connectionState) {
                    is BleConnectionState.Connected -> Triple(
                        SensorGreen,
                        Icons.Default.BluetoothConnected,
                        "Terhubung: ${connectionState.deviceName}"
                    )
                    is BleConnectionState.Connecting, is BleConnectionState.DiscoveringServices -> Triple(
                        SensorOrange,
                        Icons.Default.BluetoothSearching,
                        "Menghubungkan ke ESP32..."
                    )
                    is BleConnectionState.Scanning -> Triple(
                        MaterialTheme.colorScheme.primary,
                        Icons.Default.BluetoothSearching,
                        "Memindai StrokeBoard..."
                    )
                    is BleConnectionState.Error -> Triple(
                        SensorRed,
                        Icons.Default.BluetoothDisabled,
                        connectionState.message
                    )
                    is BleConnectionState.Disconnected -> Triple(
                        MaterialTheme.colorScheme.outline,
                        Icons.Default.Bluetooth,
                        connectionState.reason ?: "BLE Terputus"
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = stringResource(R.string.cd_ble_status),
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        maxLines = 1
                    )
                }

                // Action Button (Connect / Disconnect / Reconnect)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    when (connectionState) {
                        is BleConnectionState.Connected -> {
                            OutlinedButton(
                                onClick = onDisconnectClicked,
                                modifier = Modifier.testTag("btn_disconnect_header")
                            ) {
                                Text(stringResource(R.string.btn_disconnect), fontSize = 12.sp)
                            }
                        }
                        is BleConnectionState.Connecting, is BleConnectionState.DiscoveringServices, is BleConnectionState.Scanning -> {
                            OutlinedButton(
                                onClick = onDisconnectClicked,
                                modifier = Modifier.testTag("btn_cancel_header")
                            ) {
                                Text("Batal", fontSize = 12.sp)
                            }
                        }
                        is BleConnectionState.Error -> {
                            Button(
                                onClick = onReconnectClicked,
                                modifier = Modifier.testTag("btn_reconnect_header"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_reconnect), fontSize = 12.sp)
                            }
                        }
                        is BleConnectionState.Disconnected -> {
                            Button(
                                onClick = onConnectClicked,
                                modifier = Modifier.testTag("btn_connect_header"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(R.string.btn_connect), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
