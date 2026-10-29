package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SensorData
import com.example.ui.theme.SensorGreen
import com.example.ui.theme.SensorOrange
import com.example.ui.theme.SensorRed

@Composable
fun SensorMonitorCard(
    sensorData: SensorData,
    isConnected: Boolean,
    onOpenCalibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isStale = sensorData.isStale() || !isConnected

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sensor_monitor_row"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left Eye Sensor (GPIO 35)
        SingleSensorCard(
            title = "Mata Kiri",
            gpioLabel = "GPIO 35 (AO)",
            adcValue = if (isStale) 0 else sensorData.leftAdc,
            threshold = sensorData.thresholdLeft,
            isTriggered = if (isStale) false else sensorData.isLeftTriggered,
            isStale = isStale,
            arrowAction = "Arrow Down (Baris)",
            modifier = Modifier.weight(1f)
        )

        // Right Eye Sensor (GPIO 34)
        SingleSensorCard(
            title = "Mata Kanan",
            gpioLabel = "GPIO 34 (AO)",
            adcValue = if (isStale) 0 else sensorData.rightAdc,
            threshold = sensorData.thresholdRight,
            isTriggered = if (isStale) false else sensorData.isRightTriggered,
            isStale = isStale,
            arrowAction = "Arrow Right (Kolom)",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SingleSensorCard(
    title: String,
    gpioLabel: String,
    adcValue: Int,
    threshold: Int,
    isTriggered: Boolean,
    isStale: Boolean,
    arrowAction: String,
    modifier: Modifier = Modifier
) {
    val progress = (adcValue / 4095f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200),
        label = "adc_progress"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Title & Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = gpioLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Trigger or Live badge
                if (isTriggered && !isStale) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SensorGreen.copy(alpha = 0.2f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SensorGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "KEDIP",
                                color = SensorGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else if (isStale) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "DATA MATI",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large ADC Value Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isStale) "----" else adcValue.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isStale) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "/ 4095",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Level Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isTriggered) SensorGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Threshold label and HID mapping hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ambang: $threshold",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = arrowAction,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
