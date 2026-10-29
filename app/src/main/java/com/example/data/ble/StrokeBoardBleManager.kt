package com.example.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.model.BleConnectionState
import com.example.data.model.BleDeviceItem
import com.example.data.model.DiagnosticLog
import com.example.data.model.LogDirection
import com.example.data.model.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.UUID

@SuppressLint("MissingPermission")
class StrokeBoardBleManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "StrokeBoardBLE"
        val SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        val RX_CHAR_UUID: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        val TX_CHAR_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        const val TARGET_DEVICE_NAME = "StrokeBoard"
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var bluetoothGatt: BluetoothGatt? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var lastConnectedDevice: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected())
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BleDeviceItem>>(emptyList())
    val scannedDevices: StateFlow<List<BleDeviceItem>> = _scannedDevices.asStateFlow()

    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    private val _diagnosticLogs = MutableStateFlow<List<DiagnosticLog>>(emptyList())
    val diagnosticLogs: StateFlow<List<DiagnosticLog>> = _diagnosticLogs.asStateFlow()

    private val _lastSentCommand = MutableStateFlow<String?>(null)
    val lastSentCommand: StateFlow<String?> = _lastSentCommand.asStateFlow()

    private val _lastReceivedPacket = MutableStateFlow<String?>(null)
    val lastReceivedPacket: StateFlow<String?> = _lastReceivedPacket.asStateFlow()

    private var isScanning = false
    private val scanHandler = Handler(Looper.getMainLooper())
    private var simulationJob: Job? = null
    private var isSimulationActive = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: result.scanRecord?.deviceName ?: "Perangkat Tidak Dikenal"
            val address = device.address ?: return
            val rssi = result.rssi
            val isStrokeBoard = name.contains(TARGET_DEVICE_NAME, ignoreCase = true)

            val currentList = _scannedDevices.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.address == address }
            val item = BleDeviceItem(name = name, address = address, rssi = rssi, isStrokeBoard = isStrokeBoard)

            if (existingIndex >= 0) {
                currentList[existingIndex] = item
            } else {
                if (isStrokeBoard) {
                    currentList.add(0, item) // Put StrokeBoard on top
                } else {
                    currentList.add(item)
                }
            }
            _scannedDevices.value = currentList
        }

        override fun onScanFailed(errorCode: Int) {
            log(LogDirection.SYS_ERROR, "Pemindaian BLE gagal dengan kode: $errorCode")
            isScanning = false
            _connectionState.value = BleConnectionState.Error("Pemindaian gagal (Kode $errorCode)")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceName = gatt.device.name ?: TARGET_DEVICE_NAME
            val address = gatt.device.address
            Log.d(TAG, "onConnectionStateChange: status=$status, newState=$newState")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        log(LogDirection.SYS_INFO, "Terhubung ke $deviceName ($address), mencari GATT service...")
                        _connectionState.value = BleConnectionState.DiscoveringServices(deviceName, address)
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        log(LogDirection.SYS_INFO, "Koneksi terputus dari $deviceName")
                        cleanupGatt()
                        _connectionState.value = BleConnectionState.Disconnected("Koneksi terputus")
                    }
                }
            } else {
                log(LogDirection.SYS_ERROR, "GATT Error status: $status saat menghubungkan ke $deviceName")
                cleanupGatt()
                _connectionState.value = BleConnectionState.Error("GATT Error status: $status")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val deviceName = gatt.device.name ?: TARGET_DEVICE_NAME
            val address = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log(LogDirection.SYS_INFO, "GATT Service ditemukan (${gatt.services.size} service)")
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    rxCharacteristic = service.getCharacteristic(RX_CHAR_UUID)
                    txCharacteristic = service.getCharacteristic(TX_CHAR_UUID)

                    log(LogDirection.SYS_INFO, "Service StrokeBoard terdeteksi: RX=${rxCharacteristic != null}, TX=${txCharacteristic != null}")

                    if (txCharacteristic != null) {
                        enableTxNotification(gatt, txCharacteristic!!)
                    } else {
                        log(LogDirection.SYS_ERROR, "Karakteristik TX tidak ditemukan pada service StrokeBoard")
                    }

                    _connectionState.value = BleConnectionState.Connected(deviceName, address)
                    // Request current status
                    sendCommand("GET")
                } else {
                    log(LogDirection.SYS_ERROR, "Service StrokeBoard ($SERVICE_UUID) tidak ditemukan!")
                    _connectionState.value = BleConnectionState.Error("Service StrokeBoard tidak ditemukan pada perangkat")
                }
            } else {
                log(LogDirection.SYS_ERROR, "Gagal menemukan service, status: $status")
                _connectionState.value = BleConnectionState.Error("Gagal menemukan service GATT ($status)")
            }
        }

        @Deprecated("Deprecated for Android 13+")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic.uuid == TX_CHAR_UUID) {
                val data = characteristic.value
                handleIncomingNotification(data)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            if (characteristic.uuid == TX_CHAR_UUID) {
                handleIncomingNotification(value)
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log(LogDirection.SYS_INFO, "Perintah berhasil ditulis ke RX characteristic")
            } else {
                log(LogDirection.SYS_ERROR, "Gagal menulis perintah ke RX, status=$status")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log(LogDirection.SYS_INFO, "Notifikasi TX aktif (CCCD berhasil ditulis)")
            } else {
                log(LogDirection.SYS_ERROR, "Gagal mengaktifkan notifikasi TX, status=$status")
            }
        }
    }

    private fun enableTxNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val registered = gatt.setCharacteristicNotification(characteristic, true)
        if (!registered) {
            log(LogDirection.SYS_ERROR, "Gagal mendaftarkan notifikasi karakteristik TX ke sistem Android")
            return
        }

        val descriptor = characteristic.getDescriptor(CCCD_UUID)
        if (descriptor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }
        } else {
            log(LogDirection.SYS_ERROR, "Descriptor CCCD (0x2902) tidak ditemukan pada TX characteristic")
        }
    }

    fun handleIncomingNotification(data: ByteArray?) {
        if (data == null || data.isEmpty()) return
        val raw = String(data, StandardCharsets.UTF_8).trim()
        if (raw.isEmpty()) return

        _lastReceivedPacket.value = raw
        log(LogDirection.TX_IN, raw)
        parseSensorPacket(raw)
    }

    /**
     * Parses sensor notification format:
     * LEFT=<ADC>,RIGHT=<ADC>,TL=<thresholdLeft>,TR=<thresholdRight>
     * Example: LEFT=450,RIGHT=620,TL=500,TR=600
     */
    fun parseSensorPacket(raw: String) {
        try {
            val tokens = raw.split(",")
            var leftAdc: Int? = null
            var rightAdc: Int? = null
            var tl: Int? = null
            var tr: Int? = null

            for (token in tokens) {
                val pair = token.trim().split("=")
                if (pair.size == 2) {
                    val key = pair[0].trim().uppercase()
                    val value = pair[1].trim().toIntOrNull() ?: continue
                    when (key) {
                        "LEFT" -> leftAdc = value.coerceIn(0, 4095)
                        "RIGHT" -> rightAdc = value.coerceIn(0, 4095)
                        "TL" -> tl = value.coerceIn(0, 4095)
                        "TR" -> tr = value.coerceIn(0, 4095)
                    }
                }
            }

            val current = _sensorData.value
            val newLeftAdc = leftAdc ?: current.leftAdc
            val newRightAdc = rightAdc ?: current.rightAdc
            val newTl = tl ?: current.thresholdLeft
            val newTr = tr ?: current.thresholdRight

            // Blink detection: TCRT5000 IR reflection drops or rises depending on sensor placement.
            // On standard TCRT5000 analog AO: eyelid closed reflects less IR or more IR.
            // We check if value crosses threshold or is close to trigger
            val leftTriggered = newLeftAdc >= newTl
            val rightTriggered = newRightAdc >= newTr

            _sensorData.value = SensorData(
                leftAdc = newLeftAdc,
                rightAdc = newRightAdc,
                thresholdLeft = newTl,
                thresholdRight = newTr,
                lastUpdatedMs = System.currentTimeMillis(),
                isLeftTriggered = leftTriggered,
                isRightTriggered = rightTriggered,
                rawPacket = raw
            )
        } catch (e: Exception) {
            log(LogDirection.SYS_ERROR, "Gagal memproses data sensor: ${e.message}")
        }
    }

    fun startScan() {
        if (isSimulationActive) {
            _connectionState.value = BleConnectionState.Connected("StrokeBoard (Virtual ESP32)", "AA:BB:CC:DD:EE:FF")
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = BleConnectionState.Error("Bluetooth mati. Harap nyalakan Bluetooth.")
            log(LogDirection.SYS_ERROR, "Bluetooth tidak aktif atau tidak didukung")
            return
        }

        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            _connectionState.value = BleConnectionState.Error("BLE Scanner tidak tersedia")
            return
        }

        _scannedDevices.value = emptyList()
        _connectionState.value = BleConnectionState.Scanning
        isScanning = true
        log(LogDirection.SYS_INFO, "Memulai pemindaian BLE mencari StrokeBoard...")

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filters = listOf(
            ScanFilter.Builder().build() // Open filter to scan all and match StrokeBoard
        )

        scanner.startScan(filters, settings, scanCallback)

        // Stop scan after 10 seconds automatically
        scanHandler.postDelayed({
            if (isScanning) {
                stopScan()
                if (_connectionState.value is BleConnectionState.Scanning) {
                    _connectionState.value = BleConnectionState.Disconnected("Pemindaian selesai")
                }
            }
        }, 10000L)
    }

    fun stopScan() {
        if (!isScanning) return
        isScanning = false
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        log(LogDirection.SYS_INFO, "Pemindaian BLE dihentikan")
    }

    fun connectToDevice(deviceItem: BleDeviceItem) {
        stopScan()
        if (bluetoothAdapter == null) return

        val device = bluetoothAdapter.getRemoteDevice(deviceItem.address)
        lastConnectedDevice = device
        _connectionState.value = BleConnectionState.Connecting(deviceItem.name, deviceItem.address)
        log(LogDirection.SYS_INFO, "Menghubungkan ke ${deviceItem.name} (${deviceItem.address})...")

        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun connectToDeviceAddress(address: String) {
        stopScan()
        if (bluetoothAdapter == null) return
        val device = bluetoothAdapter.getRemoteDevice(address)
        val name = device.name ?: TARGET_DEVICE_NAME
        lastConnectedDevice = device
        _connectionState.value = BleConnectionState.Connecting(name, address)
        log(LogDirection.SYS_INFO, "Menghubungkan ke $name ($address)...")
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun reconnect() {
        lastConnectedDevice?.let { device ->
            connectToDeviceAddress(device.address)
        } ?: run {
            startScan()
        }
    }

    fun disconnect() {
        stopScan()
        bluetoothGatt?.disconnect()
        cleanupGatt()
        _connectionState.value = BleConnectionState.Disconnected("Pengguna memutuskan koneksi")
        log(LogDirection.SYS_INFO, "Koneksi diputuskan oleh pengguna")
    }

    private fun cleanupGatt() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        rxCharacteristic = null
        txCharacteristic = null
    }

    fun sendCommand(cmd: String): Boolean {
        _lastSentCommand.value = cmd
        log(LogDirection.RX_OUT, cmd)

        if (isSimulationActive) {
            handleSimulatedCommand(cmd)
            return true
        }

        val gatt = bluetoothGatt
        val rx = rxCharacteristic
        if (gatt == null || rx == null) {
            log(LogDirection.SYS_ERROR, "Gagal mengirim: GATT atau RX belum siap")
            return false
        }

        val bytes = "$cmd\n".toByteArray(StandardCharsets.UTF_8)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val res = gatt.writeCharacteristic(rx, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            res == BluetoothGatt.GATT_SUCCESS
        } else {
            @Suppress("DEPRECATION")
            rx.value = bytes
            @Suppress("DEPRECATION")
            rx.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(rx)
        }
    }

    fun setLeftThreshold(threshold: Int): Boolean {
        return sendCommand("LEFT=$threshold")
    }

    fun setRightThreshold(threshold: Int): Boolean {
        return sendCommand("RIGHT=$threshold")
    }

    fun setBothThresholds(threshold: Int): Boolean {
        return sendCommand("BOTH=$threshold")
    }

    fun requestStatus(): Boolean {
        return sendCommand("GET")
    }

    fun resetThresholds(): Boolean {
        return sendCommand("RESET")
    }

    fun log(direction: LogDirection, message: String) {
        val newLog = DiagnosticLog(direction = direction, message = message)
        val list = _diagnosticLogs.value.toMutableList()
        list.add(0, newLog)
        if (list.size > 100) list.removeAt(list.lastIndex)
        _diagnosticLogs.value = list
    }

    fun setSimulationMode(enabled: Boolean) {
        isSimulationActive = enabled
        simulationJob?.cancel()
        if (enabled) {
            log(LogDirection.SYS_INFO, "Mode Simulasi Virtual ESP32 diaktifkan")
            _connectionState.value = BleConnectionState.Connected("StrokeBoard (Virtual ESP32)", "AA:BB:CC:DD:EE:FF")
            startSimulationStream()
        } else {
            log(LogDirection.SYS_INFO, "Mode Simulasi Virtual ESP32 dimatikan")
            if (bluetoothGatt == null) {
                _connectionState.value = BleConnectionState.Disconnected()
            }
        }
    }

    private fun startSimulationStream() {
        simulationJob = scope.launch(Dispatchers.Default) {
            var simTl = 500
            var simTr = 600
            while (isActive && isSimulationActive) {
                delay(400) // Simulates ~2.5 Hz periodic updates from ESP32
                val leftJitter = (380..520).random()
                val rightJitter = (500..630).random()
                val packet = "LEFT=$leftJitter,RIGHT=$rightJitter,TL=$simTl,TR=$simTr"
                _lastReceivedPacket.value = packet
                parseSensorPacket(packet)
            }
        }
    }

    private fun handleSimulatedCommand(cmd: String) {
        when {
            cmd.startsWith("LEFT=") -> {
                val v = cmd.substringAfter("LEFT=").toIntOrNull() ?: 500
                _sensorData.value = _sensorData.value.copy(thresholdLeft = v)
                log(LogDirection.SYS_INFO, "[Simulasi] Ambang kiri diubah menjadi $v")
            }
            cmd.startsWith("RIGHT=") -> {
                val v = cmd.substringAfter("RIGHT=").toIntOrNull() ?: 600
                _sensorData.value = _sensorData.value.copy(thresholdRight = v)
                log(LogDirection.SYS_INFO, "[Simulasi] Ambang kanan diubah menjadi $v")
            }
            cmd.startsWith("BOTH=") -> {
                val v = cmd.substringAfter("BOTH=").toIntOrNull() ?: 500
                _sensorData.value = _sensorData.value.copy(thresholdLeft = v, thresholdRight = v)
                log(LogDirection.SYS_INFO, "[Simulasi] Kedua ambang diubah menjadi $v")
            }
            cmd == "RESET" -> {
                _sensorData.value = _sensorData.value.copy(thresholdLeft = 500, thresholdRight = 600)
                log(LogDirection.SYS_INFO, "[Simulasi] Ambang direset ke default (500, 600)")
            }
            cmd == "GET" -> {
                val c = _sensorData.value
                val reply = "LEFT=${c.leftAdc},RIGHT=${c.rightAdc},TL=${c.thresholdLeft},TR=${c.thresholdRight}"
                _lastReceivedPacket.value = reply
                log(LogDirection.TX_IN, "[Simulasi GET] $reply")
            }
        }
    }

    fun onDestroy() {
        stopScan()
        simulationJob?.cancel()
        disconnect()
    }
}
