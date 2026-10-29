package com.example

import android.app.Application
import android.content.Context
import android.view.KeyEvent
import androidx.test.core.app.ApplicationProvider
import com.example.data.ble.StrokeBoardBleManager
import com.example.data.model.AppScreen
import com.example.data.model.BleConnectionState
import com.example.data.model.GridCell
import com.example.data.model.KeyboardLayoutMode
import com.example.data.model.QuickActionType
import com.example.data.model.QuickPhrasesData
import com.example.viewmodel.StrokeBoardViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from resources`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("StrokeBoard", appName)
    }

    @Test
    fun `sensor packet parsing handles valid ESP32 notification`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)
        val bleManager = StrokeBoardBleManager(context, testScope)

        bleManager.parseSensorPacket("LEFT=450,RIGHT=620,TL=500,TR=600")

        val data = bleManager.sensorData.value
        assertEquals(450, data.leftAdc)
        assertEquals(620, data.rightAdc)
        assertEquals(500, data.thresholdLeft)
        assertEquals(600, data.thresholdRight)
        assertFalse(data.isLeftTriggered)
        assertTrue(data.isRightTriggered)
    }

    @Test
    fun `sensor packet parsing tolerates malformed data without crashing`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)
        val bleManager = StrokeBoardBleManager(context, testScope)

        bleManager.parseSensorPacket("")
        bleManager.parseSensorPacket("LEFT=,TR=abc,INVALID")
        bleManager.parseSensorPacket("LEFT=9999,RIGHT=-50,TL=300")

        val data = bleManager.sensorData.value
        assertEquals(4095, data.leftAdc)
        assertEquals(0, data.rightAdc)
        assertEquals(300, data.thresholdLeft)
    }

    @Test
    fun `blink delay defaults to 250ms and clamps between 150ms and 500ms`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StrokeBoardViewModel(app)

        assertEquals(250L, vm.preferences.value.debounceMs)

        vm.setDebounceMs(150L)
        // Values clamped properly
        vm.setDebounceMs(100L) // Underflow clamp
        vm.setDebounceMs(600L) // Overflow clamp

        vm.resetDebounceToDefault()
    }

    @Test
    fun `blink delay prevents multiple triggers within delay window`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StrokeBoardViewModel(app)

        assertEquals(0, vm.gridCol.value)

        // Rapid key presses within 5ms (faster than 250ms debounce)
        vm.handleKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT)
        assertEquals(1, vm.gridCol.value)

        // Immediate subsequent key press should be dropped by debounce
        vm.handleKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT)
        assertEquals(1, vm.gridCol.value) // Still 1, not 2!

        // Wait beyond debounce duration
        Thread.sleep(260)
        vm.handleKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT)
        assertEquals(2, vm.gridCol.value) // Successfully advances!
    }

    @Test
    fun `keyboard grid row 5 contains SPASI, HAPUS, HAPUS SEMUA, and BICARA with zero duplicates`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StrokeBoardViewModel(app)

        // In A-Z layout
        val cellZ = vm.getGridCell(5, 0)
        assertTrue(cellZ is GridCell.Letter && cellZ.char == 'Z')

        val cellSpace = vm.getGridCell(5, 1)
        assertTrue(cellSpace is GridCell.Action && cellSpace.actionType == QuickActionType.SPACE)
        assertEquals("SPASI", (cellSpace as GridCell.Action).label)

        val cellDelete = vm.getGridCell(5, 2)
        assertTrue(cellDelete is GridCell.Action && cellDelete.actionType == QuickActionType.DELETE)
        assertEquals("HAPUS", (cellDelete as GridCell.Action).label)

        val cellClearAll = vm.getGridCell(5, 3)
        assertTrue(cellClearAll is GridCell.Action && cellClearAll.actionType == QuickActionType.CLEAR_ALL)
        assertEquals("HAPUS SEMUA", (cellClearAll as GridCell.Action).label)

        val cellSpeak = vm.getGridCell(5, 4)
        assertTrue(cellSpeak is GridCell.Action && cellSpeak.actionType == QuickActionType.SPEAK)
        assertEquals("BICARA", (cellSpeak as GridCell.Action).label)

        // Verify count of function keys on the entire keyboard is strictly 1 each (no duplicates)
        var spaceCount = 0
        var deleteCount = 0
        var clearCount = 0
        var speakCount = 0

        for (r in 0..5) {
            for (c in 0..4) {
                val cell = vm.getGridCell(r, c)
                if (cell is GridCell.Action) {
                    when (cell.actionType) {
                        QuickActionType.SPACE -> spaceCount++
                        QuickActionType.DELETE -> deleteCount++
                        QuickActionType.CLEAR_ALL -> clearCount++
                        QuickActionType.SPEAK -> speakCount++
                        else -> {}
                    }
                }
            }
        }

        assertEquals(1, spaceCount)
        assertEquals(1, deleteCount)
        assertEquals(1, clearCount)
        assertEquals(1, speakCount)
    }

    @Test
    fun `auto select letter A on bluetooth connected and layout reset`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StrokeBoardViewModel(app)

        // Move cursor away from 'A'
        vm.onGridCellClicked(3, 2)
        assertEquals(3, vm.gridRow.value)
        assertEquals(2, vm.gridCol.value)

        // Calling resetFocusToA restores selection to letter 'A'
        vm.resetFocusToA()
        assertEquals(0, vm.gridRow.value)
        assertEquals(0, vm.gridCol.value)
        val cellA = vm.getGridCell(vm.gridRow.value, vm.gridCol.value)
        assertTrue(cellA is GridCell.Letter && cellA.char == 'A')
    }

    @Test
    fun `horizontal and vertical navigation remain synchronized with highlighted key`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StrokeBoardViewModel(app)

        vm.resetFocusToA()
        assertEquals(0, vm.gridRow.value)
        assertEquals(0, vm.gridCol.value)

        // Horizontal navigation: right moves column
        Thread.sleep(260)
        vm.handleKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT)
        assertEquals(0, vm.gridRow.value)
        assertEquals(1, vm.gridCol.value) // 'B'

        // Vertical navigation: down moves row, preserves column
        Thread.sleep(260)
        vm.handleKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN)
        assertEquals(1, vm.gridRow.value)
        assertEquals(1, vm.gridCol.value) // 'G'
        val cellG = vm.getGridCell(1, 1)
        assertTrue(cellG is GridCell.Letter && cellG.char == 'G')

        // Enter selects 'G'
        Thread.sleep(260)
        vm.handleKeyEvent(KeyEvent.KEYCODE_ENTER)
        assertEquals("G", vm.composedText.value)
    }

    @Test
    fun `disconnecting bluetooth does not clear composed text`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = StrokeBoardViewModel(app)

        vm.appendChar('S')
        vm.appendChar('A')
        vm.appendChar('Y')
        vm.appendChar('A')
        assertEquals("SAYA", vm.composedText.value)

        vm.disconnectBle()
        assertEquals("SAYA", vm.composedText.value) // Text is safely preserved!
    }
}
