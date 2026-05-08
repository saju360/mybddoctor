package com.lifeplus.healthcare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifeplus.healthcare.ui.theme.GlassBorder
import com.lifeplus.healthcare.ui.theme.GlassWhite
import com.lifeplus.healthcare.ui.theme.Surface2Dark
import com.lifeplus.healthcare.ui.theme.Surface3Dark

/**
 * Premium glass-morphism card with subtle gradient and border.
 * Renders a frosted-glass look on top of dark backgrounds.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColor = if (elevated) Surface3Dark else Surface2Dark
    val borderColor = if (elevated) Color(0x30FFFFFF) else GlassBorder

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        bgColor.copy(alpha = 0.95f),
                        bgColor.copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x28FFFFFF),
                        borderColor,
                        Color(0x08FFFFFF)
                    )
                ),
                shape = MaterialTheme.shapes.large
            )
    ) {
        content()
    }
}

/**
 * Danger/emergency variant — red-tinted glass card.
 */
@Composable
fun DangerGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x22FF5252),
                        Color(0x10FF5252)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                color = Color(0x40FF5252),
                shape = MaterialTheme.shapes.large
            )
    ) {
        content()
    }
}

/**
 * Success/teal variant — green-tinted glass card.
 */
@Composable
fun SuccessGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x2200E676),
                        Color(0x1000E676)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                color = Color(0x4000E676),
                shape = MaterialTheme.shapes.large
            )
    ) {
        content()
    }
}
