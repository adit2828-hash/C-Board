package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ble.StrokeBoardBleManager
import com.example.data.model.AppPreferences
import com.example.data.model.AppScreen
import com.example.data.model.BleConnectionState
import com.example.data.model.BleDeviceItem
import com.example.data.model.ControlButtonType
import com.example.data.model.DiagnosticLog
import com.example.data.model.GridCell
import com.example.data.model.KeyboardLayoutMode
import com.example.data.model.LogDirection
import com.example.data.model.NavigationZone
import com.example.data.model.QuickActionType
import com.example.data.model.QuickPhraseItem
import com.example.data.model.QuickPhrasesData
import com.example.data.model.SensorData
import com.example.data.preferences.StrokeBoardPreferencesRepository
import com.example.data.tts.SpeechManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StrokeBoardViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = StrokeBoardPreferencesRepository(application)
    val preferences: StateFlow<AppPreferences> = prefsRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppPreferences()
    )

    private val bleManager = StrokeBoardBleManager(application, viewModelScope)
    val connectionState: StateFlow<BleConnectionState> = bleManager.connectionState
    val scannedDevices: StateFlow<List<BleDeviceItem>> = bleManager.scannedDevices
    val sensorData: StateFlow<SensorData> = bleManager.sensorData
    val diagnosticLogs: StateFlow<List<DiagnosticLog>> = bleManager.diagnosticLogs
    val lastSentCommand: StateFlow<String?> = bleManager.lastSentCommand
    val lastReceivedPacket: StateFlow<String?> = bleManager.lastReceivedPacket

    private val speechManager = SpeechManager(application)
    val isSpeaking: StateFlow<Boolean> = speechManager.isSpeaking
    val ttsStatusMessage: StateFlow<String?> = speechManager.ttsStatusMessage

    // Active Screen state
    private val _currentScreen = MutableStateFlow(AppScreen.COMMUNICATION)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Keyboard layout mode: A-Z vs QWERTY
    private val _keyboardLayoutMode = MutableStateFlow(KeyboardLayoutMode.ALPHABET_AZ)
    val keyboardLayoutMode: StateFlow<KeyboardLayoutMode> = _keyboardLayoutMode.asStateFlow()

    // Composed Text state
    private val _composedText = MutableStateFlow("")
    val composedText: StateFlow<String> = _composedText.asStateFlow()

    // Communication Screen Navigation state (5 columns x 6 rows)
    private val _navigationZone = MutableStateFlow(NavigationZone.KEYBOARD_GRID)
    val navigationZone: StateFlow<NavigationZone> = _navigationZone.asStateFlow()

    private val _gridRow = MutableStateFlow(0) // 0..5 (6 rows)
    val gridRow: StateFlow<Int> = _gridRow.asStateFlow()

    private val _gridCol = MutableStateFlow(0) // 0..4 (5 cols)
    val gridCol: StateFlow<Int> = _gridCol.asStateFlow()

    // Quick Phrases Screen Navigation state (3x3 grid)
    private val _phraseRow = MutableStateFlow(0) // 0..2
    val phraseRow: StateFlow<Int> = _phraseRow.asStateFlow()

    private val _phraseCol = MutableStateFlow(0) // 0..2
    val phraseCol: StateFlow<Int> = _phraseCol.asStateFlow()

    // Dialog & Screen Visibility states
    private val _showClearConfirmDialog = MutableStateFlow(false)
    val showClearConfirmDialog: StateFlow<Boolean> = _showClearConfirmDialog.asStateFlow()

    private val _showScanDialog = MutableStateFlow(false)
    val showScanDialog: StateFlow<Boolean> = _showScanDialog.asStateFlow()

    private val _showDiagnosticsScreen = MutableStateFlow(false)
    val showDiagnosticsScreen: StateFlow<Boolean> = _showDiagnosticsScreen.asStateFlow()

    private val _showCalibrationGuide = MutableStateFlow(false)
    val showCalibrationGuide: StateFlow<Boolean> = _showCalibrationGuide.asStateFlow()

    // Editable thresholds (staged before applying to ESP32)
    private val _editingThresholdLeft = MutableStateFlow(500)
    val editingThresholdLeft: StateFlow<Int> = _editingThresholdLeft.asStateFlow()

    private val _editingThresholdRight = MutableStateFlow(600)
    val editingThresholdRight: StateFlow<Int> = _editingThresholdRight.asStateFlow()

    private val _thresholdApplyMessage = MutableStateFlow<String?>(null)
    val thresholdApplyMessage: StateFlow<String?> = _thresholdApplyMessage.asStateFlow()

    // Debounce for hardware key events
    private var lastKeyTimestamp = 0L

    init {
        // Initial focus position: automatically on letter 'A'
        resetFocusToA()

        // Sync preferences changes to TTS, BLE simulation, and keyboard layout
        viewModelScope.launch {
            preferences.collect { pref ->
                speechManager.applyLanguage(pref.ttsLanguage)
                speechManager.applySettings(pref.speechRate, pref.speechPitch)
                bleManager.setSimulationMode(pref.simulationMode)
                _keyboardLayoutMode.value = pref.keyboardLayoutMode
            }
        }

        // When connected sensor data updates, update staged threshold values if first load
        viewModelScope.launch {
            sensorData.collect { sensor ->
                if (_editingThresholdLeft.value == 500 && sensor.thresholdLeft != 500) {
                    _editingThresholdLeft.value = sensor.thresholdLeft
                }
                if (_editingThresholdRight.value == 600 && sensor.thresholdRight != 600) {
                    _editingThresholdRight.value = sensor.thresholdRight
                }
            }
        }

        // Automatic selection of letter 'A' upon successful Bluetooth connection
        viewModelScope.launch {
            var wasConnected = false
            connectionState.collect { state ->
                val isNowConnected = state is BleConnectionState.Connected
                if (isNowConnected && !wasConnected) {
                    // Transitioned to Connected state: automatically position selection on 'A'
                    resetFocusToA()
                }
                wasConnected = isNowConnected
            }
        }
    }

    /**
     * Resets active selection to letter 'A' on the keyboard.
     * In A-Z mode: (Row 0, Col 0).
     * In QWERTY mode: (Row 2, Col 0).
     */
    fun resetFocusToA() {
        _navigationZone.value = NavigationZone.KEYBOARD_GRID
        if (_keyboardLayoutMode.value == KeyboardLayoutMode.ALPHABET_AZ) {
            _gridRow.value = 0
            _gridCol.value = 0 // 'A'
        } else {
            _gridRow.value = 2
            _gridCol.value = 0 // 'A' in QWERTY
        }
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.COMMUNICATION) {
            resetFocusToA()
        } else if (screen == AppScreen.QUICK_PHRASES) {
            _phraseRow.value = 0
            _phraseCol.value = 0
        }
    }

    fun toggleKeyboardLayout() {
        val next = if (_keyboardLayoutMode.value == KeyboardLayoutMode.ALPHABET_AZ) {
            KeyboardLayoutMode.QWERTY
        } else {
            KeyboardLayoutMode.ALPHABET_AZ
        }
        _keyboardLayoutMode.value = next
        viewModelScope.launch {
            prefsRepo.setKeyboardLayoutMode(next)
        }
        resetFocusToA()
    }

    /**
     * Sequential Alphabet A-Z (5x6) vs QWERTY (5x6)
     * Row 0..4: 25 letters.
     * Row 5: 1 letter (Z / M) + SPASI + HAPUS + HAPUS SEMUA + BICARA
     * Exactly 30 cells total across 6 rows x 5 columns.
     */
    private val qwertyAlphabetList = listOf(
        'Q', 'W', 'E', 'R', 'T',
        'Y', 'U', 'I', 'O', 'P',
        'A', 'S', 'D', 'F', 'G',
        'H', 'J', 'K', 'L', 'Z',
        'X', 'C', 'V', 'B', 'N',
        'M'
    )

    fun getGridCell(row: Int, col: Int): GridCell {
        if (row < 0 || row > 5 || col < 0 || col > 4) return GridCell.Empty
        val isAz = _keyboardLayoutMode.value == KeyboardLayoutMode.ALPHABET_AZ

        if (row < 5) {
            val index = row * 5 + col
            val char = if (isAz) ('A'.code + index).toChar() else qwertyAlphabetList[index]
            return GridCell.Letter(char)
        } else {
            // Row 5: 1 letter + 4 primary function keys (Zero duplicates!)
            return when (col) {
                0 -> {
                    val char = if (isAz) 'Z' else 'M'
                    GridCell.Letter(char)
                }
                1 -> GridCell.Action(QuickActionType.SPACE, "SPASI")
                2 -> GridCell.Action(QuickActionType.DELETE, "HAPUS")
                3 -> GridCell.Action(QuickActionType.CLEAR_ALL, "HAPUS SEMUA")
                4 -> GridCell.Action(QuickActionType.SPEAK, "BICARA")
                else -> GridCell.Empty
            }
        }
    }

    /**
     * Handles hardware HID keyboard events from ESP32:
     * - Arrow Down: Moves vertically to next row (wraps around 0..5, stays in same column)
     * - Arrow Right: Moves horizontally to next column (wraps around 0..4, stays in same row)
     * - Enter: Selects currently focused letter or function key
     *
     * Protected by Blink Delay (debounce) in the range 150ms - 500ms (default 250ms).
     */
    fun handleKeyEvent(keyCode: Int): Boolean {
        val now = System.currentTimeMillis()
        val debounce = preferences.value.debounceMs.coerceIn(150L, 500L)
        if (now - lastKeyTimestamp < debounce) {
            return true // Debounce dropped: prevents multiple accidental triggers
        }
        lastKeyTimestamp = now

        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> {
                bleManager.log(LogDirection.HID_INPUT, "Kedipan Kiri / Arrow Down (Vertikal)")
                onArrowDown()
                triggerHaptic()
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> {
                bleManager.log(LogDirection.HID_INPUT, "Kedipan Kanan / Arrow Right (Horizontal)")
                onArrowRight()
                triggerHaptic()
                true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_SPACE -> {
                bleManager.log(LogDirection.HID_INPUT, "Kedipan Kedua Mata / Enter (Pilih)")
                onEnter()
                triggerHaptic()
                true
            }
            else -> false
        }
    }

    private fun onArrowDown() {
        when (_currentScreen.value) {
            AppScreen.COMMUNICATION -> {
                _navigationZone.value = NavigationZone.KEYBOARD_GRID
                // Vertical navigation: next row (circular wrap 0..5), preserves column
                _gridRow.value = (_gridRow.value + 1) % 6
                _gridCol.value = _gridCol.value.coerceIn(0, 4)
            }
            AppScreen.QUICK_PHRASES -> {
                _phraseRow.value = (_phraseRow.value + 1) % 3
                _phraseCol.value = _phraseCol.value.coerceIn(0, 2)
            }
            AppScreen.SETTINGS -> {
                setScreen(AppScreen.COMMUNICATION)
            }
        }
    }

    private fun onArrowRight() {
        when (_currentScreen.value) {
            AppScreen.COMMUNICATION -> {
                _navigationZone.value = NavigationZone.KEYBOARD_GRID
                // Horizontal navigation: next column (circular wrap 0..4), preserves row
                _gridCol.value = (_gridCol.value + 1) % 5
            }
            AppScreen.QUICK_PHRASES -> {
                _phraseCol.value = (_phraseCol.value + 1) % 3
            }
            AppScreen.SETTINGS -> {
                setScreen(AppScreen.COMMUNICATION)
            }
        }
    }

    private fun onEnter() {
        when (_currentScreen.value) {
            AppScreen.COMMUNICATION -> {
                val cell = getGridCell(_gridRow.value, _gridCol.value)
                when (cell) {
                    is GridCell.Letter -> appendChar(cell.char)
                    is GridCell.Action -> {
                        when (cell.actionType) {
                            QuickActionType.SPACE -> appendSpace()
                            QuickActionType.DELETE -> deleteLastChar()
                            QuickActionType.CLEAR_ALL -> requestClearAll()
                            QuickActionType.SPEAK -> speakMessage()
                            QuickActionType.SWITCH_LAYOUT -> toggleKeyboardLayout()
                            QuickActionType.GOTO_PHRASES -> setScreen(AppScreen.QUICK_PHRASES)
                            QuickActionType.SWITCH_ZONE -> {}
                        }
                    }
                    GridCell.Empty -> {}
                }
            }
            AppScreen.QUICK_PHRASES -> {
                val index = _phraseRow.value * 3 + _phraseCol.value
                val phrases = QuickPhrasesData.items
                if (index in phrases.indices) {
                    onQuickPhraseSelected(phrases[index])
                }
            }
            AppScreen.SETTINGS -> {
                setScreen(AppScreen.COMMUNICATION)
            }
        }
    }

    fun onQuickPhraseSelected(item: QuickPhraseItem) {
        _composedText.value = item.title
        speechManager.speak(item.spokenText)
        triggerHaptic()
    }

    fun onGridCellClicked(row: Int, col: Int) {
        _navigationZone.value = NavigationZone.KEYBOARD_GRID
        _gridRow.value = row.coerceIn(0, 5)
        _gridCol.value = col.coerceIn(0, 4)
        val cell = getGridCell(row, col)
        when (cell) {
            is GridCell.Letter -> appendChar(cell.char)
            is GridCell.Action -> {
                when (cell.actionType) {
                    QuickActionType.SPACE -> appendSpace()
                    QuickActionType.DELETE -> deleteLastChar()
                    QuickActionType.CLEAR_ALL -> requestClearAll()
                    QuickActionType.SPEAK -> speakMessage()
                    QuickActionType.SWITCH_LAYOUT -> toggleKeyboardLayout()
                    QuickActionType.GOTO_PHRASES -> setScreen(AppScreen.QUICK_PHRASES)
                    QuickActionType.SWITCH_ZONE -> {}
                }
            }
            GridCell.Empty -> {}
        }
        triggerHaptic()
    }

    fun appendChar(char: Char) {
        _composedText.value += char
    }

    fun appendSpace() {
        if (_composedText.value.isNotEmpty() && !_composedText.value.endsWith(" ")) {
            _composedText.value += " "
        }
    }

    fun deleteLastChar() {
        if (_composedText.value.isNotEmpty()) {
            _composedText.value = _composedText.value.dropLast(1)
        }
    }

    fun requestClearAll() {
        if (preferences.value.confirmClearAll) {
            _showClearConfirmDialog.value = true
        } else {
            clearAllText()
        }
    }

    fun clearAllText() {
        _composedText.value = ""
        _showClearConfirmDialog.value = false
    }

    fun dismissClearConfirmDialog() {
        _showClearConfirmDialog.value = false
    }

    fun speakMessage() {
        val text = _composedText.value
        speechManager.speak(text)
    }

    fun stopSpeech() {
        speechManager.stop()
    }

    fun repeatSpeech() {
        speechManager.repeat()
    }

    // BLE Actions
    fun startScan() {
        _showScanDialog.value = true
        bleManager.startScan()
    }

    fun stopScan() {
        bleManager.stopScan()
    }

    fun connectDevice(device: BleDeviceItem) {
        _showScanDialog.value = false
        bleManager.connectToDevice(device)
    }

    fun disconnectBle() {
        bleManager.disconnect()
    }

    fun reconnectBle() {
        bleManager.reconnect()
    }

    // Calibration
    fun setEditingThresholdLeft(value: Int) {
        _editingThresholdLeft.value = value.coerceIn(0, 4095)
    }

    fun setEditingThresholdRight(value: Int) {
        _editingThresholdRight.value = value.coerceIn(0, 4095)
    }

    fun applyThresholds() {
        val left = _editingThresholdLeft.value
        val right = _editingThresholdRight.value
        viewModelScope.launch {
            bleManager.setLeftThreshold(left)
            delay(150)
            bleManager.setRightThreshold(right)
            delay(150)
            bleManager.requestStatus()
            _thresholdApplyMessage.value = "Perintah LEFT=$left dan RIGHT=$right telah dikirim ke ESP32"
            delay(3000)
            _thresholdApplyMessage.value = null
        }
    }

    fun resetThresholds() {
        viewModelScope.launch {
            bleManager.resetThresholds()
            delay(200)
            bleManager.requestStatus()
            _thresholdApplyMessage.value = "Perintah RESET telah dikirim ke ESP32"
            delay(3000)
            _thresholdApplyMessage.value = null
        }
    }

    fun requestStatus() {
        bleManager.requestStatus()
    }

    fun bleManagerSendCommand(cmd: String): Boolean {
        return bleManager.sendCommand(cmd)
    }

    // Dialog toggles
    fun setShowScanDialog(show: Boolean) {
        _showScanDialog.value = show
    }

    fun setShowDiagnosticsScreen(show: Boolean) {
        _showDiagnosticsScreen.value = show
    }

    fun setShowCalibrationGuide(show: Boolean) {
        _showCalibrationGuide.value = show
    }

    // Preferences setters
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setFontSizeScale(scale: Float) {
        viewModelScope.launch { prefsRepo.setFontSizeScale(scale) }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch { prefsRepo.setSpeechRate(rate) }
    }

    fun setTtsLanguage(lang: String) {
        viewModelScope.launch { prefsRepo.setTtsLanguage(lang) }
    }

    fun setConfirmClearAll(confirm: Boolean) {
        viewModelScope.launch { prefsRepo.setConfirmClearAll(confirm) }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setHapticFeedbackEnabled(enabled) }
    }

    fun setDebounceMs(ms: Long) {
        val clamped = ms.coerceIn(150L, 500L)
        viewModelScope.launch { prefsRepo.setDebounceMs(clamped) }
    }

    fun resetDebounceToDefault() {
        viewModelScope.launch { prefsRepo.setDebounceMs(250L) }
    }

    fun setSimulationMode(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setSimulationMode(enabled) }
    }

    private fun triggerHaptic() {
        if (!preferences.value.hapticFeedbackEnabled) return
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
        bleManager.onDestroy()
    }
}
