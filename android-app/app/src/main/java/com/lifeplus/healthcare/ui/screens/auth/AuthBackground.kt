package com.lifeplus.healthcare.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifeplus.healthcare.ui.theme.BackgroundLight
import com.lifeplus.healthcare.ui.theme.Primary

@Composable
fun AuthBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Top Right Soft Blob
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .size(400.dp)
                .background(Primary.copy(alpha = 0.1f), CircleShape)
                .blur(100.dp)
        )

        // Bottom Left Soft Blob
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-150).dp, y = 150.dp)
                .size(400.dp)
                .background(Color(0xFFE1BEE7).copy(alpha = 0.2f), CircleShape)
                .blur(100.dp)
        )

        content()
    }
}
