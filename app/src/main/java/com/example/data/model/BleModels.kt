package com.example.data.model

sealed interface BleConnectionState {
    data class Disconnected(val reason: String? = null) : BleConnectionState
    object Scanning : BleConnectionState
    data class Connecting(val deviceName: String, val address: String) : BleConnectionState
    data class DiscoveringServices(val deviceName: String, val address: String) : BleConnectionState
    data class Connected(val deviceName: String, val address: String) : BleConnectionState
    data class Error(val message: String) : BleConnectionState
}

data class BleDeviceItem(
    val name: String,
    val address: String,
    val rssi: Int = 0,
    val isStrokeBoard: Boolean = false
)

enum class LogDirection {
    RX_OUT,    // Command sent to ESP32 RX char
    TX_IN,     // Notification received from ESP32 TX char
    HID_INPUT, // Keyboard HID event received (Arrow Down, Arrow Right, Enter)
    SYS_INFO,  // System / Connection info
    SYS_ERROR  // Error
}

data class DiagnosticLog(
    val timestamp: Long = System.currentTimeMillis(),
    val direction: LogDirection,
    val message: String
)
