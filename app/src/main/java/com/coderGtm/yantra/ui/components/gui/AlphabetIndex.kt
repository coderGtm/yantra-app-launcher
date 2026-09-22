package com.coderGtm.yantra.ui.components.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AlphabetIndex(
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()
    
    Column(
        modifier = modifier.fillMaxHeight().padding(start = 4.dp, end = 4.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        alphabet.forEach { letter ->
            Text(
                text = letter.toString(),
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.clickable { onLetterClick(letter) }
            )
        }
    }
}
