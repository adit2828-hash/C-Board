package com.example.data.model

import androidx.compose.ui.graphics.Color

object QuickPhrasesData {
    val items = listOf(
        // Row 0
        QuickPhraseItem(
            id = "help",
            title = "Tolong Panggil Keluarga",
            spokenText = "Tolong panggilkan keluarga saya, saya membutuhkan bantuan.",
            categoryColor = Color(0xFFC7EBD1), // Light green
            iconName = "help"
        ),
        QuickPhraseItem(
            id = "pain",
            title = "Saya Merasa Sakit",
            spokenText = "Saya merasa sakit dan tidak nyaman.",
            categoryColor = Color(0xFFBAE6FD), // Light blue
            iconName = "pain"
        ),
        QuickPhraseItem(
            id = "hungry",
            title = "Saya Lapar",
            spokenText = "Saya merasa lapar, saya ingin makan.",
            categoryColor = Color(0xFFBAE6FD), // Light blue
            iconName = "hungry"
        ),
        // Row 1
        QuickPhraseItem(
            id = "thirsty",
            title = "Saya Ingin Minum",
            spokenText = "Saya haus, tolong berikan saya minum.",
            categoryColor = Color(0xFFBAE6FD), // Light blue
            iconName = "thirsty"
        ),
        QuickPhraseItem(
            id = "toilet",
            title = "Saya Ingin ke Toilet",
            spokenText = "Saya ingin pergi ke toilet sekarang.",
            categoryColor = Color(0xFFFEF08A), // Warm yellow
            iconName = "toilet"
        ),
        QuickPhraseItem(
            id = "stop",
            title = "Berhenti / Stop",
            spokenText = "Tolong berhenti dulu.",
            categoryColor = Color(0xFFC7EBD1), // Light green
            iconName = "stop"
        ),
        // Row 2
        QuickPhraseItem(
            id = "no",
            title = "Tidak",
            spokenText = "Tidak.",
            categoryColor = Color(0xFFFBCFE8), // Pink
            iconName = "no"
        ),
        QuickPhraseItem(
            id = "yes",
            title = "Ya",
            spokenText = "Ya.",
            categoryColor = Color(0xFFFBCFE8), // Pink
            iconName = "yes"
        ),
        QuickPhraseItem(
            id = "more",
            title = "Terima Kasih / Lebih Banyak",
            spokenText = "Terima kasih banyak.",
            categoryColor = Color(0xFFBAE6FD), // Light blue
            iconName = "more"
        )
    )
}
