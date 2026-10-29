package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppPreferences
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsDialog(
    preferences: AppPreferences,
    onThemeChanged: (ThemeMode) -> Unit,
    onFontSizeChanged: (Float) -> Unit,
    onSpeechRateChanged: (Float) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onConfirmClearChanged: (Boolean) -> Unit,
    onHapticChanged: (Boolean) -> Unit,
    onSimulationChanged: (Boolean) -> Unit,
    onDebounceChanged: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pengaturan & Aksesibilitas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tema Tampilan
                SettingsSection(title = "Tema Tampilan") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = preferences.themeMode == ThemeMode.LIGHT,
                            onClick = { onThemeChanged(ThemeMode.LIGHT) },
                            label = { Text("Terang") },
                            modifier = Modifier.testTag("theme_light")
                        )
                        FilterChip(
                            selected = preferences.themeMode == ThemeMode.DARK,
                            onClick = { onThemeChanged(ThemeMode.DARK) },
                            label = { Text("Gelap") },
                            modifier = Modifier.testTag("theme_dark")
                        )
                        FilterChip(
                            selected = preferences.themeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeChanged(ThemeMode.SYSTEM) },
                            label = { Text("Sistem") },
                            modifier = Modifier.testTag("theme_system")
                        )
                    }
                }

                // Ukuran Huruf Alfabet & Teks
                SettingsSection(title = "Ukuran Huruf") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = preferences.fontSizeScale == 1.0f,
                            onClick = { onFontSizeChanged(1.0f) },
                            label = { Text("Normal") },
                            modifier = Modifier.testTag("font_normal")
                        )
                        FilterChip(
                            selected = preferences.fontSizeScale == 1.25f,
                            onClick = { onFontSizeChanged(1.25f) },
                            label = { Text("Besar") },
                            modifier = Modifier.testTag("font_large")
                        )
                        FilterChip(
                            selected = preferences.fontSizeScale == 1.5f,
                            onClick = { onFontSizeChanged(1.5f) },
                            label = { Text("Sangat Besar") },
                            modifier = Modifier.testTag("font_xlarge")
                        )
                    }
                }

                // Bahasa Suara TTS
                SettingsSection(title = "Bahasa Suara (Text-to-Speech)") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = preferences.ttsLanguage == "id",
                            onClick = { onLanguageChanged("id") },
                            label = { Text("Indonesia (id)") },
                            modifier = Modifier.testTag("lang_id")
                        )
                        FilterChip(
                            selected = preferences.ttsLanguage == "en",
                            onClick = { onLanguageChanged("en") },
                            label = { Text("English (en)") },
                            modifier = Modifier.testTag("lang_en")
                        )
                    }
                }

                // Kecepatan Bicara TTS
                SettingsSection(title = "Kecepatan Suara: ${String.format("%.2fx", preferences.speechRate)}") {
                    Slider(
                        value = preferences.speechRate,
                        onValueChange = onSpeechRateChanged,
                        valueRange = 0.5f..1.8f,
                        steps = 5,
                        modifier = Modifier.testTag("slider_speech_rate")
                    )
                }

                // Sensitivitas Debounce Kedipan
                SettingsSection(title = "Debounce Kedipan (Mencegah Klik Ganda): ${preferences.debounceMs} ms") {
                    Slider(
                        value = preferences.debounceMs.toFloat(),
                        onValueChange = { onDebounceChanged(it.toLong()) },
                        valueRange = 150f..600f,
                        steps = 8,
                        modifier = Modifier.testTag("slider_debounce")
                    )
                }

                // Konfirmasi Hapus Semua Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Konfirmasi Hapus Semua", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Mencegah teks hilang karena ketidaksengajaan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = preferences.confirmClearAll,
                        onCheckedChange = onConfirmClearChanged,
                        modifier = Modifier.testTag("switch_confirm_clear")
                    )
                }

                // Getar / Haptic Feedback Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Getaran Umpan Balik (Haptic)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Bergetar saat huruf dipilih", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = preferences.hapticFeedbackEnabled,
                        onCheckedChange = onHapticChanged,
                        modifier = Modifier.testTag("switch_haptic")
                    )
                }

                // Virtual ESP32 Simulator Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mode Simulator ESP32", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Mensimulasikan sensor TCRT5000 tanpa perangkat fisik", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = preferences.simulationMode,
                        onCheckedChange = onSimulationChanged,
                        modifier = Modifier.testTag("switch_simulation")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_settings")) {
                Text("Selesai")
            }
        }
    )
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}
