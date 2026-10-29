package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BleConnectionState
import com.example.data.model.DiagnosticLog
import com.example.data.model.LogDirection
import com.example.data.model.SensorData
import com.example.ui.theme.SensorGreen
import com.example.ui.theme.SensorOrange
import com.example.ui.theme.SensorRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    connectionState: BleConnectionState,
    sensorData: SensorData,
    lastSentCommand: String?,
    lastReceivedPacket: String?,
    logs: List<DiagnosticLog>,
    onSendCommand: (String) -> Unit,
    onBackClicked: () -> Unit
) {
    var manualCommandText by remember { mutableStateOf("") }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostik & Log ESP32 BLE", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked, modifier = Modifier.testTag("btn_diagnostic_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Status Summary Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Ringkasan Status Komunikasi:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Koneksi:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = when (connectionState) {
                                is BleConnectionState.Connected -> "Terhubung (${connectionState.deviceName})"
                                is BleConnectionState.Connecting -> "Menghubungkan..."
                                is BleConnectionState.DiscoveringServices -> "Menemukan Service..."
                                is BleConnectionState.Scanning -> "Memindai..."
                                is BleConnectionState.Error -> "Error: ${connectionState.message}"
                                is BleConnectionState.Disconnected -> "Terputus"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (connectionState is BleConnectionState.Connected) SensorGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Perintah Terakhir (RX):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(lastSentCommand ?: "(Belum ada)", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Paket Terakhir (TX):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(lastReceivedPacket ?: "(Belum ada)", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ADC Kiri/Kanan:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("L: ${sensorData.leftAdc} | R: ${sensorData.rightAdc}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ambang TL/TR:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("TL: ${sensorData.thresholdLeft} | TR: ${sensorData.thresholdRight}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Manual Command Sender
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = manualCommandText,
                    onValueChange = { manualCommandText = it },
                    label = { Text("Kirim Perintah Custom ke RX (misal: GET)") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_manual_command")
                )
                Button(
                    onClick = {
                        if (manualCommandText.isNotBlank()) {
                            onSendCommand(manualCommandText.trim())
                            manualCommandText = ""
                        }
                    },
                    modifier = Modifier.testTag("btn_send_manual_command")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Kirim", modifier = Modifier.size(16.dp))
                }
            }

            // Quick command pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickCommandPill("GET", onSendCommand)
                QuickCommandPill("RESET", onSendCommand)
                QuickCommandPill("LEFT=500", onSendCommand)
                QuickCommandPill("RIGHT=600", onSendCommand)
            }

            // Log Console Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log Event Real-Time (${logs.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Real-Time Diagnostic Log List
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada log tercatat", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { logItem ->
                            val time = timeFormat.format(Date(logItem.timestamp))
                            val (tagColor, tagText) = when (logItem.direction) {
                                LogDirection.RX_OUT -> Color(0xFF0284C7) to "RX OUT"
                                LogDirection.TX_IN -> Color(0xFF10B981) to "TX IN"
                                LogDirection.HID_INPUT -> Color(0xFFF59E0B) to "HID BLINK"
                                LogDirection.SYS_INFO -> Color(0xFF6B7280) to "INFO"
                                LogDirection.SYS_ERROR -> Color(0xFFEF4444) to "ERROR"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = time,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = tagColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = tagText,
                                        color = tagColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }

                                Text(
                                    text = logItem.message,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCommandPill(cmd: String, onClick: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        androidx.compose.material3.TextButton(
            onClick = { onClick(cmd) },
            modifier = Modifier.height(30.dp)
        ) {
            Text(cmd, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}
