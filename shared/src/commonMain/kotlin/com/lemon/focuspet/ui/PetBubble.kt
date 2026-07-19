package com.lemon.focuspet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PetBubble(
    text: String,
    accent: Color,
) {
    Surface(
        modifier = Modifier.offset(y = (-16).dp),
        shape = MaterialTheme.shapes.small,
        color = accent,
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
