package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Wc
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuickPhraseItem
import com.example.data.model.QuickPhrasesData
import com.example.ui.theme.FocusHighlightBorder
import com.example.ui.theme.FocusHighlightColor

@Composable
fun QuickPhrasesScreen(
    activeRow: Int,
    activeCol: Int,
    fontSizeScale: Float,
    onPhraseSelected: (QuickPhraseItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = QuickPhrasesData.items

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("quick_phrases_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Guide header
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Papan Frasa Cepat (Blink Baris ${activeRow + 1}, Kolom ${activeCol + 1})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sentuh atau Kedipkan Enter untuk Bicara",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3x3 Grid
        for (r in 0..2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (c in 0..2) {
                    val index = r * 3 + c
                    if (index in items.indices) {
                        val item = items[index]
                        val isFocused = r == activeRow && c == activeCol

                        QuickPhraseCard(
                            item = item,
                            isFocused = isFocused,
                            fontSizeScale = fontSizeScale,
                            onClick = { onPhraseSelected(item) },
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
private fun QuickPhraseCard(
    item: QuickPhraseItem,
    isFocused: Boolean,
    fontSizeScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconVector: ImageVector = when (item.iconName) {
        "help" -> Icons.Default.PanTool
        "pain" -> Icons.Default.Healing
        "hungry" -> Icons.Default.Restaurant
        "thirsty" -> Icons.Default.LocalDrink
        "toilet" -> Icons.Default.Wc
        "stop" -> Icons.Default.NotInterested
        "no" -> Icons.Default.SentimentDissatisfied
        "yes" -> Icons.Default.SentimentSatisfiedAlt
        "more" -> Icons.Default.Favorite
        else -> Icons.Default.CheckCircle
    }

    val iconColor: Color = when (item.iconName) {
        "stop", "no" -> Color(0xFFDC2626) // Vivid red
        "yes" -> Color(0xFF16A34A)       // Vivid green
        "pain" -> Color(0xFFEA580C)      // Warm orange
        "thirsty" -> Color(0xFF0284C7)   // Water blue
        "toilet" -> Color(0xFFCA8A04)    // Gold/Yellow
        else -> Color(0xFF0F766E)        // Teal
    }

    val containerColor by animateColorAsState(
        targetValue = if (isFocused) FocusHighlightColor else item.categoryColor,
        animationSpec = tween(150),
        label = "phrase_bg"
    )

    val textColor = Color(0xFF0F172A) // Always high contrast black on pastel cards

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isFocused) 4.dp else 1.5.dp,
                color = if (isFocused) FocusHighlightBorder else Color.Black.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("phrase_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Large visual AAC symbol matching Screenshot 1
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = item.title,
                            tint = iconColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // High-contrast, easily readable phrase title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isFocused) FontWeight.Black else FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    fontSize = (16 * fontSizeScale).sp,
                    lineHeight = (20 * fontSizeScale).sp
                )
            }

            // High-contrast focused badge on top right
            if (isFocused) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "AKTIF",
                        color = FocusHighlightColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
