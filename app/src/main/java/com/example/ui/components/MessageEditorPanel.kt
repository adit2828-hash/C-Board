package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.ControlButtonType
import com.example.data.model.NavigationZone
import com.example.ui.theme.FocusHighlightBorder
import com.example.ui.theme.FocusHighlightColor

@Composable
fun MessageEditorPanel(
    composedText: String,
    fontSizeScale: Float,
    isSpeaking: Boolean,
    navigationZone: NavigationZone,
    activeControlIndex: Int,
    onControlButtonClicked: (ControlButtonType) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("message_editor_panel"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Text Viewport Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp, max = 130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (composedText.isEmpty()) {
                    Text(
                        text = stringResource(R.string.placeholder_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = (16 * fontSizeScale).sp,
                        lineHeight = (22 * fontSizeScale).sp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = composedText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = (20 * fontSizeScale).sp,
                            lineHeight = (26 * fontSizeScale).sp
                        )
                        // Blinking cursor
                        Box(
                            modifier = Modifier
                                .padding(start = 2.dp, bottom = 2.dp)
                                .width(3.dp)
                                .height((22 * fontSizeScale).dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Bar (Accessible via Touch & via Eye Blinks in NavigationZone.TOP_ACTIONS)
            val isControlsZone = navigationZone == NavigationZone.TOP_ACTIONS
            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 0: Speak
                ActionPillButton(
                    label = "Bicara",
                    icon = Icons.Default.VolumeUp,
                    isFocused = isControlsZone && activeControlIndex == 0,
                    isPrimary = true,
                    onClick = { onControlButtonClicked(ControlButtonType.SPEAK) },
                    testTag = "btn_speak"
                )

                // 1: Stop
                ActionPillButton(
                    label = "Hentikan",
                    icon = Icons.Default.Stop,
                    isFocused = isControlsZone && activeControlIndex == 1,
                    isPrimary = false,
                    enabled = isSpeaking,
                    onClick = { onControlButtonClicked(ControlButtonType.STOP) },
                    testTag = "btn_stop_speech"
                )

                // 2: Repeat
                ActionPillButton(
                    label = "Ulangi",
                    icon = Icons.Default.Replay,
                    isFocused = isControlsZone && activeControlIndex == 2,
                    isPrimary = false,
                    onClick = { onControlButtonClicked(ControlButtonType.REPEAT) },
                    testTag = "btn_repeat_speech"
                )

                // 3: Space
                ActionPillButton(
                    label = "Spasi",
                    icon = Icons.Default.SpaceBar,
                    isFocused = isControlsZone && activeControlIndex == 3,
                    isPrimary = false,
                    onClick = { onControlButtonClicked(ControlButtonType.SPACE) },
                    testTag = "btn_space"
                )

                // 4: Delete
                ActionPillButton(
                    label = "Hapus",
                    icon = Icons.Default.Backspace,
                    isFocused = isControlsZone && activeControlIndex == 4,
                    isPrimary = false,
                    onClick = { onControlButtonClicked(ControlButtonType.DELETE) },
                    testTag = "btn_delete"
                )

                // 5: Clear All
                ActionPillButton(
                    label = "Hapus Semua",
                    icon = Icons.Default.ClearAll,
                    isFocused = isControlsZone && activeControlIndex == 5,
                    isPrimary = false,
                    isDanger = true,
                    onClick = { onControlButtonClicked(ControlButtonType.CLEAR_ALL) },
                    testTag = "btn_clear_all"
                )

                // 6: Return to Alphabet
                if (isControlsZone) {
                    ActionPillButton(
                        label = "Ke Huruf",
                        icon = null,
                        isFocused = activeControlIndex == 6,
                        isPrimary = true,
                        onClick = { onControlButtonClicked(ControlButtonType.RETURN_TO_KEYBOARD) },
                        testTag = "btn_return_alphabet"
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionPillButton(
    label: String,
    icon: ImageVector?,
    isFocused: Boolean,
    isPrimary: Boolean = false,
    isDanger: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    testTag: String
) {
    val containerColor = when {
        isFocused -> FocusHighlightColor
        isDanger -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        isPrimary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isFocused -> Color.Black
        isDanger -> MaterialTheme.colorScheme.error
        isPrimary -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .border(
                width = if (isFocused) 3.dp else 1.dp,
                color = if (isFocused) FocusHighlightBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag(testTag),
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                color = contentColor,
                fontWeight = if (isFocused) FontWeight.Black else FontWeight.SemiBold,
                fontSize = 13.sp
            )
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )
            }
        }
    }
}
