package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SensorOrange

@Composable
fun CalibrationGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = SensorOrange,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Panduan Kalibrasi TCRT5000",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Prinsip Kerja Sensor:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Sensor TCRT5000 memancarkan sinar inframerah (IR) dan membaca intensitas pantulannya dari kelopak mata melalui pin Analog (AO) pada rentang ADC 0–4095.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Langkah Menentukan Ambang Ideal:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )

                StepItem(step = "1", title = "Baca Nilai Mata Terbuka", desc = "Minta pengguna membuka mata secara normal dan rileks. Catat nilai ADC aktual pada panel (misalnya terbaca 420).")
                StepItem(step = "2", title = "Baca Nilai Saat Berkedip / Terpejam", desc = "Minta pengguna memejamkan mata. Catat kenaikan/penurunan nilai ADC (misalnya menjadi 780).")
                StepItem(step = "3", title = "Hitung Titik Tengah (Ambang)", desc = "Gunakan rumus: Ambang = (ADC_Terbuka + ADC_Tertutup) / 2. Contoh: (420 + 780) / 2 = 600.")
                StepItem(step = "4", title = "Terapkan ke ESP32", desc = "Masukkan angka 600 pada slider lalu tekan tombol 'Terapkan'. Periksa apakah badge 'KEDIP' menyala hanya ketika mata berkedip.")

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Catatan Pencahayaan:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hindari sinar matahari langsung atau lampu halogen berdaya tinggi yang dapat mempengaruhi pembacaan inframerah modul TCRT5000.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_close_guide")
            ) {
                Text("Mengerti")
            }
        }
    )
}

@Composable
private fun StepItem(step: String, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(20.dp)
        ) {
            Text(
                text = step,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(2.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        Column {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
