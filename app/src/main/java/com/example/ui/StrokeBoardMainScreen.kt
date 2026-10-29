package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppScreen
import com.example.data.model.BleConnectionState
import com.example.ui.components.CalibrationGuideDialog
import com.example.ui.components.DeviceScanDialog
import com.example.ui.components.DiagnosticScreen
import com.example.ui.screens.CommunicationScreen
import com.example.ui.screens.QuickPhrasesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SensorGreen
import com.example.ui.theme.SensorOrange
import com.example.ui.theme.SensorRed
import com.example.viewmodel.StrokeBoardViewModel

@Composable
fun StrokeBoardMainScreen(
    viewModel: StrokeBoardViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val scannedDevices by viewModel.scannedDevices.collectAsState()
    val sensorData by viewModel.sensorData.collectAsState()
    val composedText by viewModel.composedText.collectAsState()
    val keyboardLayoutMode by viewModel.keyboardLayoutMode.collectAsState()

    val navigationZone by viewModel.navigationZone.collectAsState()
    val gridRow by viewModel.gridRow.collectAsState()
    val gridCol by viewModel.gridCol.collectAsState()
    val phraseRow by viewModel.phraseRow.collectAsState()
    val phraseCol by viewModel.phraseCol.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    val showClearDialog by viewModel.showClearConfirmDialog.collectAsState()
    val showScanDialog by viewModel.showScanDialog.collectAsState()
    val showDiagnostics by viewModel.showDiagnosticsScreen.collectAsState()
    val showGuide by viewModel.showCalibrationGuide.collectAsState()

    val editingTl by viewModel.editingThresholdLeft.collectAsState()
    val editingTr by viewModel.editingThresholdRight.collectAsState()
    val applyMessage by viewModel.thresholdApplyMessage.collectAsState()
    val diagnosticLogs by viewModel.diagnosticLogs.collectAsState()
    val lastSentCmd by viewModel.lastSentCommand.collectAsState()
    val lastReceivedPacket by viewModel.lastReceivedPacket.collectAsState()

    if (showDiagnostics) {
        DiagnosticScreen(
            connectionState = connectionState,
            sensorData = sensorData,
            lastSentCommand = lastSentCmd,
            lastReceivedPacket = lastReceivedPacket,
            logs = diagnosticLogs,
            onSendCommand = { viewModel.bleManagerSendCommand(it) },
            onBackClicked = { viewModel.setShowDiagnosticsScreen(false) }
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MainTopAppBar(
                currentScreen = currentScreen,
                connectionState = connectionState,
                isSimulationMode = preferences.simulationMode,
                onScreenSelected = { viewModel.setScreen(it) },
                onDiagnosticsClicked = { viewModel.setShowDiagnosticsScreen(true) }
            )
        },
        bottomBar = {
            // Caregiver/Developer Touch Simulation Bar
            CaregiverSimulationRow(
                onSimulateLeftBlink = { viewModel.handleKeyEvent(android.view.KeyEvent.KEYCODE_DPAD_DOWN) },
                onSimulateRightBlink = { viewModel.handleKeyEvent(android.view.KeyEvent.KEYCODE_DPAD_RIGHT) },
                onSimulateBothEyes = { viewModel.handleKeyEvent(android.view.KeyEvent.KEYCODE_ENTER) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.COMMUNICATION -> {
                    CommunicationScreen(
                        composedText = composedText,
                        fontSizeScale = preferences.fontSizeScale,
                        isSpeaking = isSpeaking,
                        keyboardLayoutMode = keyboardLayoutMode,
                        navigationZone = navigationZone,
                        activeRow = gridRow,
                        activeCol = gridCol,
                        getCell = { r, c -> viewModel.getGridCell(r, c) },
                        onCellClicked = { r, c -> viewModel.onGridCellClicked(r, c) },
                        onToggleLayoutClicked = { viewModel.toggleKeyboardLayout() }
                    )
                }

                AppScreen.QUICK_PHRASES -> {
                    QuickPhrasesScreen(
                        activeRow = phraseRow,
                        activeCol = phraseCol,
                        fontSizeScale = preferences.fontSizeScale,
                        onPhraseSelected = { viewModel.onQuickPhraseSelected(it) }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        connectionState = connectionState,
                        sensorData = sensorData,
                        preferences = preferences,
                        editingThresholdLeft = editingTl,
                        editingThresholdRight = editingTr,
                        applyMessage = applyMessage,
                        lastReceivedPacket = lastReceivedPacket,
                        onConnectClicked = { viewModel.startScan() },
                        onDisconnectClicked = { viewModel.disconnectBle() },
                        onReconnectClicked = { viewModel.reconnectBle() },
                        onLeftThresholdChanged = { viewModel.setEditingThresholdLeft(it) },
                        onRightThresholdChanged = { viewModel.setEditingThresholdRight(it) },
                        onApplyThresholds = { viewModel.applyThresholds() },
                        onResetThresholds = { viewModel.resetThresholds() },
                        onRequestStatus = { viewModel.requestStatus() },
                        onThemeChanged = { viewModel.setThemeMode(it) },
                        onKeyboardLayoutChanged = { viewModel.toggleKeyboardLayout() },
                        onDebounceChanged = { viewModel.setDebounceMs(it) },
                        onResetDebounceDefault = { viewModel.resetDebounceToDefault() },
                        onSpeechRateChanged = { viewModel.setSpeechRate(it) },
                        onLanguageChanged = { viewModel.setTtsLanguage(it) },
                        onSimulationChanged = { viewModel.setSimulationMode(it) },
                        onOpenGuide = { viewModel.setShowCalibrationGuide(true) },
                        onOpenDiagnostics = { viewModel.setShowDiagnosticsScreen(true) }
                    )
                }
            }
        }
    }

    // Confirmation dialog for All Clear
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearConfirmDialog() },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = {
                Text(stringResource(R.string.dialog_clear_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(stringResource(R.string.dialog_clear_message))
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllText() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("dialog_btn_confirm_clear")
                ) {
                    Text(stringResource(R.string.dialog_btn_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissClearConfirmDialog() },
                    modifier = Modifier.testTag("dialog_btn_cancel_clear")
                ) {
                    Text(stringResource(R.string.dialog_btn_cancel))
                }
            }
        )
    }

    // BLE Scan Dialog
    if (showScanDialog) {
        DeviceScanDialog(
            connectionState = connectionState,
            scannedDevices = scannedDevices,
            onDeviceSelected = { viewModel.connectDevice(it) },
            onStartScan = { viewModel.startScan() },
            onStopScan = { viewModel.stopScan() },
            onDismiss = { viewModel.setShowScanDialog(false) }
        )
    }

    // Calibration Guide Dialog
    if (showGuide) {
        CalibrationGuideDialog(
            onDismiss = { viewModel.setShowCalibrationGuide(false) }
        )
    }
}

@Composable
private fun MainTopAppBar(
    currentScreen: AppScreen,
    connectionState: BleConnectionState,
    isSimulationMode: Boolean,
    onScreenSelected: (AppScreen) -> Unit,
    onDiagnosticsClicked: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Title & BLE pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "StrokeBoard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Small BLE connection indicator
                    val (bleColor, bleText) = when (connectionState) {
                        is BleConnectionState.Connected -> SensorGreen to "Terhubung"
                        is BleConnectionState.Connecting, is BleConnectionState.DiscoveringServices -> SensorOrange to "Hubungkan..."
                        is BleConnectionState.Scanning -> MaterialTheme.colorScheme.primary to "Pindai..."
                        is BleConnectionState.Error -> SensorRed to "Error"
                        is BleConnectionState.Disconnected -> MaterialTheme.colorScheme.outline to "Terputus"
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bleColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(bleColor))
                            Text(bleText, color = bleColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isSimulationMode) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SensorOrange.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "SIMULASI",
                                color = SensorOrange,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Diagnostics shortcut
                IconButton(
                    onClick = onDiagnosticsClicked,
                    modifier = Modifier.size(36.dp).testTag("btn_top_diagnostics")
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Diagnostik",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Screen Tabs (Komunikasi, Frasa Cepat, Pengaturan & Sensor)
            TabRow(
                selectedTabIndex = currentScreen.ordinal,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = currentScreen == AppScreen.COMMUNICATION,
                    onClick = { onScreenSelected(AppScreen.COMMUNICATION) },
                    text = { Text("Komunikasi", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_communication")
                )
                Tab(
                    selected = currentScreen == AppScreen.QUICK_PHRASES,
                    onClick = { onScreenSelected(AppScreen.QUICK_PHRASES) },
                    text = { Text("Frasa Cepat", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_quick_phrases")
                )
                Tab(
                    selected = currentScreen == AppScreen.SETTINGS,
                    onClick = { onScreenSelected(AppScreen.SETTINGS) },
                    text = { Text("Pengaturan & Sensor", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        }
    }
}

@Composable
private fun CaregiverSimulationRow(
    onSimulateLeftBlink: () -> Unit,
    onSimulateRightBlink: () -> Unit,
    onSimulateBothEyes: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onSimulateLeftBlink,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_sim_left_blink")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kedip Kiri (↓)", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onSimulateRightBlink,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_sim_right_blink")
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kedip Kanan (→)", fontSize = 11.sp)
                }

                Button(
                    onClick = onSimulateBothEyes,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_sim_both_blink")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pilih (↵)", fontSize = 11.sp)
                }
            }
        }
    }
}
