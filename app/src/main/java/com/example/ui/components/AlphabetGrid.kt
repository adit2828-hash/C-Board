package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.GridCell
import com.example.data.model.NavigationZone
import com.example.data.model.QuickActionType
import com.example.ui.theme.FocusHighlightBorder
import com.example.ui.theme.FocusHighlightColor

@Composable
fun AlphabetGrid(
    activeRow: Int,
    activeCol: Int,
    navigationZone: NavigationZone,
    fontSizeScale: Float,
    getCell: (row: Int, col: Int) -> GridCell,
    onCellClicked: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("alphabet_grid_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 6 Rows (Row 0..5), each row takes equal weight so keys are large and responsive
            for (row in 0..5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 5 Columns (Col 0..4)
                    for (col in 0..4) {
                        val cell = getCell(row, col)
                        val isCellFocused = navigationZone == NavigationZone.KEYBOARD_GRID &&
                                row == activeRow && col == activeCol
                        val isRowHighlight = navigationZone == NavigationZone.KEYBOARD_GRID &&
                                row == activeRow && col != activeCol

                        AlphabetGridTile(
                            cell = cell,
                            isFocused = isCellFocused,
                            isRowHighlighted = isRowHighlight,
                            fontSizeScale = fontSizeScale,
                            onClick = { onCellClicked(row, col) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlphabetGridTile(
    cell: GridCell,
    isFocused: Boolean,
    isRowHighlighted: Boolean,
    fontSizeScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isFocused -> FocusHighlightColor
            cell is GridCell.Action && cell.actionType == QuickActionType.CLEAR_ALL ->
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
            cell is GridCell.Action && cell.actionType == QuickActionType.SPEAK ->
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            cell is GridCell.Action && cell.actionType == QuickActionType.SPACE ->
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            cell is GridCell.Action && cell.actionType == QuickActionType.DELETE ->
                MaterialTheme.colorScheme.surfaceVariant
            isRowHighlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        animationSpec = tween(150),
        label = "tile_bg"
    )

    val contentColor = when {
        isFocused -> Color.Black
        cell is GridCell.Action && cell.actionType == QuickActionType.CLEAR_ALL ->
            MaterialTheme.colorScheme.error
        cell is GridCell.Action && cell.actionType == QuickActionType.SPEAK ->
            MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        isFocused -> FocusHighlightBorder
        isRowHighlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    }

    val borderWidth = when {
        isFocused -> 3.5.dp
        isRowHighlighted -> 1.5.dp
        else -> 1.dp
    }

    val tag = when (cell) {
        is GridCell.Letter -> "grid_cell_${cell.char}"
        is GridCell.Action -> "grid_cell_${cell.actionType.name}"
        GridCell.Empty -> "grid_cell_empty"
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .testTag(tag),
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            when (cell) {
                is GridCell.Letter -> {
                    Text(
                        text = cell.char.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isFocused) FontWeight.Black else FontWeight.Bold,
                        color = contentColor,
                        fontSize = (24 * fontSizeScale).sp
                    )
                }
                is GridCell.Action -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (cell.actionType) {
                            QuickActionType.SPACE -> {
                                Icon(
                                    imageVector = Icons.Default.SpaceBar,
                                    contentDescription = "Spasi",
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "SPASI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            }
                            QuickActionType.DELETE -> {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Hapus",
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "HAPUS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            }
                            QuickActionType.CLEAR_ALL -> {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Semua",
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "HAPUS SEMUA",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = contentColor
                                )
                            }
                            QuickActionType.SPEAK -> {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Bicara",
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "BICARA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            }
                            else -> {}
                        }
                    }
                }
                GridCell.Empty -> {}
            }

            // High contrast active badge dot on top right
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )
            }
        }
    }
}
