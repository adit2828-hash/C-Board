package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SensorData
import com.example.ui.theme.SensorGreen
import com.example.ui.theme.SensorOrange

@Composable
fun CalibrationDialog(
    sensorData: SensorData,
    editingThresholdLeft: Int,
    editingThresholdRight: Int,
    applyMessage: String?,
    onLeftThresholdChanged: (Int) -> Unit,
    onRightThresholdChanged: (Int) -> Unit,
    onApplyClicked: () -> Unit,
    onResetClicked: () -> Unit,
    onRequestStatusClicked: () -> Unit,
    onOpenGuideClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Kalibrasi Ambang ESP32",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButtonWithTag(
                    icon = Icons.Default.HelpOutline,
                    contentDescription = "Panduan Kalibrasi",
                    onClick = onOpenGuideClicked,
                    testTag = "btn_open_calibration_guide"
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Sesuaikan nilai ambang (0–4095) agar kedipan mata terdeteksi secara presisi sesuai pantulan IR modul TCRT5000.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = applyMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Sensor Kiri (GPIO 35) Card
                ThresholdEditorCard(
                    title = "Sensor Kiri (GPIO 35 - Arrow Down)",
                    currentAdc = sensorData.leftAdc,
                    appliedThreshold = sensorData.thresholdLeft,
                    editingThreshold = editingThresholdLeft,
                    onThresholdChange = onLeftThresholdChanged,
                    sliderTestTag = "slider_threshold_left",
                    inputTestTag = "input_threshold_left"
                )

                // Sensor Kanan (GPIO 34) Card
                ThresholdEditorCard(
                    title = "Sensor Kanan (GPIO 34 - Arrow Right)",
                    currentAdc = sensorData.rightAdc,
                    appliedThreshold = sensorData.thresholdRight,
                    editingThreshold = editingThresholdRight,
                    onThresholdChange = onRightThresholdChanged,
                    sliderTestTag = "slider_threshold_right",
                    inputTestTag = "input_threshold_right"
                )

                // Action buttons: Apply, Reset, Refresh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetClicked,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_reset_thresholds")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onRequestStatusClicked,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_get_status")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GET", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onApplyClicked,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_apply_thresholds"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Terapkan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_close_calibration")
            ) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun ThresholdEditorCard(
    title: String,
    currentAdc: Int,
    appliedThreshold: Int,
    editingThreshold: Int,
    onThresholdChange: (Int) -> Unit,
    sliderTestTag: String,
    inputTestTag: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Status Values row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ADC Aktual:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$currentAdc", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Text("Ambang di ESP32:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$appliedThreshold", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SensorGreen)
                }
                Column {
                    Text("Nilai Diedit:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$editingThreshold", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SensorOrange)
                }
            }

            // Slider
            Slider(
                value = editingThreshold.toFloat(),
                onValueChange = { onThresholdChange(it.toInt()) },
                valueRange = 0f..4095f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(sliderTestTag)
            )

            // Manual Number Input
            OutlinedTextField(
                value = editingThreshold.toString(),
                onValueChange = { str ->
                    val num = str.filter { it.isDigit() }.toIntOrNull()
                    if (num != null) onThresholdChange(num.coerceIn(0, 4095))
                    else if (str.isEmpty()) onThresholdChange(0)
                },
                label = { Text("Nilai Angka Manual") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(inputTestTag)
            )
        }
    }
}

@Composable
private fun IconButtonWithTag(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = Modifier.testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
