package com.example.data.model

data class SensorData(
    val leftAdc: Int = 0,
    val rightAdc: Int = 0,
    val thresholdLeft: Int = 500,
    val thresholdRight: Int = 600,
    val lastUpdatedMs: Long = 0L,
    val isLeftTriggered: Boolean = false,
    val isRightTriggered: Boolean = false,
    val rawPacket: String = ""
) {
    val isBothTriggered: Boolean
        get() = isLeftTriggered && isRightTriggered

    fun isStale(timeoutMs: Long = 3000L): Boolean {
        if (lastUpdatedMs == 0L) return true
        return (System.currentTimeMillis() - lastUpdatedMs) > timeoutMs
    }
}
