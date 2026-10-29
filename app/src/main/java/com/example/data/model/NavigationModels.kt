package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppScreen {
    COMMUNICATION,
    QUICK_PHRASES,
    SETTINGS
}

enum class KeyboardLayoutMode {
    ALPHABET_AZ, // 5 columns x 6 rows sequential A-Z
    QWERTY       // QWERTY layout
}

enum class NavigationZone {
    KEYBOARD_GRID,
    TOP_ACTIONS,
    QUICK_PHRASES_GRID,
    SETTINGS_ZONE
}

sealed interface GridCell {
    data class Letter(val char: Char) : GridCell
    data class Action(val actionType: QuickActionType, val label: String) : GridCell
    object Empty : GridCell
}

enum class QuickActionType {
    SPACE,
    DELETE,
    SPEAK,
    CLEAR_ALL,
    SWITCH_LAYOUT, // Toggle between A-Z and QWERTY
    GOTO_PHRASES,  // Go to Quick Phrases screen
    SWITCH_ZONE    // Switch between keyboard and top action buttons
}

enum class ControlButtonType {
    SPEAK,
    STOP,
    REPEAT,
    SPACE,
    DELETE,
    CLEAR_ALL,
    SWITCH_LAYOUT,
    RETURN_TO_KEYBOARD
}

data class QuickPhraseItem(
    val id: String,
    val title: String,
    val spokenText: String,
    val categoryColor: Color,
    val iconName: String
)
