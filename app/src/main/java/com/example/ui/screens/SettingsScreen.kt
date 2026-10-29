package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppPreferences
import com.example.data.model.BleConnectionState
import com.example.data.model.KeyboardLayoutMode
import com.example.data.model.SensorData
import com.example.ui.components.SensorMonitorCard
import com.example.ui.theme.SensorGreen
import com.example.ui.theme.SensorOrange
import com.example.ui.theme.SensorRed
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    connectionState: BleConnectionState,
    sensorData: SensorData,
    preferences: AppPreferences,
    editingThresholdLeft: Int,
    editingThresholdRight: Int,
    applyMessage: String?,
    lastReceivedPacket: String?,
    onConnectClicked: () -> Unit,
    onDisconnectClicked: () -> Unit,
    onReconnectClicked: () -> Unit,
    onLeftThresholdChanged: (Int) -> Unit,
    onRightThresholdChanged: (Int) -> Unit,
    onApplyThresholds: () -> Unit,
    onResetThresholds: () -> Unit,
    onRequestStatus: () -> Unit,
    onThemeChanged: (ThemeMode) -> Unit,
    onKeyboardLayoutChanged: (KeyboardLayoutMode) -> Unit,
    onDebounceChanged: (Long) -> Unit,
    onResetDebounceDefault: () -> Unit,
    onSpeechRateChanged: (Float) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onSimulationChanged: (Boolean) -> Unit,
    onOpenGuide: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section A: Koneksi StrokeBoard ESP32
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Koneksi StrokeBoard BLE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Status Indicator
                    val (statusColor, statusIcon, statusLabel) = when (connectionState) {
                        is BleConnectionState.Connected -> Triple(SensorGreen, Icons.Default.BluetoothConnected, "Terhubung")
                        is BleConnectionState.Connecting, is BleConnectionState.DiscoveringServices -> Triple(SensorOrange, Icons.Default.BluetoothSearching, "Menghubungkan...")
                        is BleConnectionState.Scanning -> Triple(MaterialTheme.colorScheme.primary, Icons.Default.BluetoothSearching, "Memindai...")
                        is BleConnectionState.Error -> Triple(SensorRed, Icons.Default.BluetoothDisabled, "Error")
                        is BleConnectionState.Disconnected -> Triple(MaterialTheme.colorScheme.outline, Icons.Default.Bluetooth, "Terputus")
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                            Text(statusLabel, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Text(
                    text = when (connectionState) {
                        is BleConnectionState.Connected -> "Perangkat: ${connectionState.deviceName} (${connectionState.address})\nSaat terhubung, tombol huruf 'A' otomatis dipilih."
                        is BleConnectionState.Error -> "Keterangan: ${connectionState.message}"
                        else -> "Hubungkan langsung ke ESP32 tanpa perlu pairing manual di menu HP."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Connection Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (connectionState) {
                        is BleConnectionState.Connected -> {
                            OutlinedButton(
                                onClick = onDisconnectClicked,
                                modifier = Modifier.weight(1f).testTag("btn_disconnect_settings")
                            ) {
                                Text("Putuskan BLE")
                            }
                        }
                        is BleConnectionState.Connecting, is BleConnectionState.Scanning -> {
                            OutlinedButton(
                                onClick = onDisconnectClicked,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Batal Pindai")
                            }
                        }
                        else -> {
                            Button(
                                onClick = onConnectClicked,
                                modifier = Modifier.weight(1f).testTag("btn_scan_settings"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pindai & Hubungkan")
                            }
                            OutlinedButton(
                                onClick = onReconnectClicked,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hubungkan Ulang")
                            }
                        }
                    }
                }
            }
        }

        // Section B: Pengaturan Jeda Kedipan (Blink Delay: 150ms - 500ms, Default 250ms)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("card_blink_delay")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Pengaturan Jeda Kedipan (Blink Delay)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onResetDebounceDefault,
                        modifier = Modifier.height(32.dp).testTag("btn_reset_blink_delay_default")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Default (250 ms)", fontSize = 11.sp)
                    }
                }

                Text(
                    text = "Mencegah satu kedipan diproses berulang kali dan menggeser tombol tanpa disengaja. Setelah kedipan terdeteksi, pemicu berikutnya diabaikan selama durasi jeda ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jeda kedipan: ${preferences.debounceMs} ms",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (preferences.debounceMs == 250L) "Nilai Default" else "Kustom",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Slider from 150ms to 500ms
                Slider(
                    value = preferences.debounceMs.toFloat(),
                    onValueChange = { onDebounceChanged(it.toLong()) },
                    valueRange = 150f..500f,
                    steps = 6, // 150, 200, 250, 300, 350, 400, 450, 500
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("slider_blink_delay")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("150 ms (Cepat)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("250 ms (Default)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("500 ms (Aman)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Section C: Nilai Sensor ADC Kiri & Kanan (Telemetry Langsung)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nilai Sensor TCRT5000 Real-Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = onOpenGuide,
                        modifier = Modifier.height(32.dp).testTag("btn_guide_settings")
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Panduan", fontSize = 11.sp)
                    }
                }

                SensorMonitorCard(
                    sensorData = sensorData,
                    isConnected = connectionState is BleConnectionState.Connected,
                    onOpenCalibration = {}
                )
            }
        }

        // Section D: Kalibrasi Threshold Masing-Masing Sensor
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Pengaturan Ambang Kedipan (Threshold)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                AnimatedVisibility(visible = applyMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                            Text(applyMessage ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                ThresholdSliderBox(
                    label = "Ambang Sensor Kiri (GPIO 35)",
                    currentAdc = sensorData.leftAdc,
                    appliedThreshold = sensorData.thresholdLeft,
                    editingThreshold = editingThresholdLeft,
                    onValueChange = onLeftThresholdChanged
                )

                ThresholdSliderBox(
                    label = "Ambang Sensor Kanan (GPIO 34)",
                    currentAdc = sensorData.rightAdc,
                    appliedThreshold = sensorData.thresholdRight,
                    editingThreshold = editingThresholdRight,
                    onValueChange = onRightThresholdChanged
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetThresholds,
                        modifier = Modifier.weight(1f).testTag("btn_reset_settings")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onRequestStatus,
                        modifier = Modifier.weight(1f).testTag("btn_get_settings")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GET", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onApplyThresholds,
                        modifier = Modifier.weight(1.3f).testTag("btn_apply_settings"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Terapkan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Section E: Status Koneksi HID dan GATT
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Status Koneksi HID & GATT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("GATT Notification (TX):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (connectionState is BleConnectionState.Connected) "Aktif (Notifikasi Diterima)" else "Tidak Aktif",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (connectionState is BleConnectionState.Connected) SensorGreen else MaterialTheme.colorScheme.outline
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("HID Keyboard Input:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Mata Kiri = ↓ (Baris), Kanan = → (Kolom), Kedua = ↵",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (lastReceivedPacket != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Data Raw Terakhir:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = lastReceivedPacket,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenDiagnostics,
                    modifier = Modifier.fillMaxWidth().testTag("btn_open_diagnostics")
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka Konsol Log Diagnostik Lengkap")
                }
            }
        }

        // Section F: Pengaturan Tema Tampilan & Keyboard
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Preferensi Tampilan & Keyboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Tema Terang / Gelap / Sistem
                Column {
                    Text("Pilihan Tema:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = preferences.themeMode == ThemeMode.LIGHT,
                            onClick = { onThemeChanged(ThemeMode.LIGHT) },
                            label = { Text("Terang") }
                        )
                        FilterChip(
                            selected = preferences.themeMode == ThemeMode.DARK,
                            onClick = { onThemeChanged(ThemeMode.DARK) },
                            label = { Text("Gelap") }
                        )
                        FilterChip(
                            selected = preferences.themeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeChanged(ThemeMode.SYSTEM) },
                            label = { Text("Sistem") }
                        )
                    }
                }

                // Keyboard Mode: A-Z vs QWERTY
                Column {
                    Text("Susunan Keyboard Utama:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = preferences.keyboardLayoutMode == KeyboardLayoutMode.ALPHABET_AZ,
                            onClick = { onKeyboardLayoutChanged(KeyboardLayoutMode.ALPHABET_AZ) },
                            label = { Text("Alfabet A-Z (5×6)") }
                        )
                        FilterChip(
                            selected = preferences.keyboardLayoutMode == KeyboardLayoutMode.QWERTY,
                            onClick = { onKeyboardLayoutChanged(KeyboardLayoutMode.QWERTY) },
                            label = { Text("QWERTY (5×6)") }
                        )
                    }
                }

                // Bahasa Suara TTS
                Column {
                    Text("Bahasa Suara (Text-to-Speech):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = preferences.ttsLanguage == "id",
                            onClick = { onLanguageChanged("id") },
                            label = { Text("Indonesia (id)") }
                        )
                        FilterChip(
                            selected = preferences.ttsLanguage == "en",
                            onClick = { onLanguageChanged("en") },
                            label = { Text("English (en)") }
                        )
                    }
                }

                // Mode Simulator ESP32 Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mode Simulator ESP32", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Simulasikan ADC dan kedipan tanpa modul kacamata fisik", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = preferences.simulationMode,
                        onCheckedChange = onSimulationChanged,
                        modifier = Modifier.testTag("switch_simulation_settings")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ThresholdSliderBox(
    label: String,
    currentAdc: Int,
    appliedThreshold: Int,
    editingThreshold: Int,
    onValueChange: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ADC: $currentAdc", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Aktif: $appliedThreshold", fontSize = 11.sp, color = SensorGreen, fontWeight = FontWeight.Bold)
                }
            }

            Slider(
                value = editingThreshold.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..4095f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nilai Ambang: $editingThreshold", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                OutlinedTextField(
                    value = editingThreshold.toString(),
                    onValueChange = { str ->
                        val n = str.filter { it.isDigit() }.toIntOrNull()
                        if (n != null) onValueChange(n.coerceIn(0, 4095))
                        else if (str.isEmpty()) onValueChange(0)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(90.dp).height(48.dp)
                )
            }
        }
    }
}
