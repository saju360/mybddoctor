package com.lifeplus.healthcare.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.lifeplus.healthcare.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    placeholder: String? = null,
    supportingText: String? = null,
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isError) {
        if (isError) {
            launch {
                repeat(3) {
                    shakeOffset.animateTo(-8f, spring(stiffness = 600f))
                    shakeOffset.animateTo(8f,  spring(stiffness = 600f))
                }
                shakeOffset.animateTo(0f, spring(stiffness = 600f))
            }
        }
    }

    Column(modifier = modifier.offset(x = shakeOffset.value.dp)) {
        OutlinedTextField(
            value             = value,
            onValueChange     = onValueChange,
            label             = { Text(label) },
            placeholder       = placeholder?.let { { Text(it, color = TextHint) } },
            isError           = isError,
            enabled           = enabled,
            visualTransformation = visualTransformation,
            keyboardOptions   = keyboardOptions,
            keyboardActions   = keyboardActions,
            trailingIcon      = trailingIcon,
            leadingIcon       = leadingIcon,
            singleLine        = singleLine,
            maxLines          = if (singleLine) 1 else 5,
            shape             = MaterialTheme.shapes.medium,
            supportingText    = when {
                isError && errorMessage != null -> {
                    { Text(errorMessage, color = ErrorColor, style = MaterialTheme.typography.bodySmall) }
                }
                supportingText != null -> {
                    { Text(supportingText, color = TextHint, style = MaterialTheme.typography.bodySmall) }
                }
                else -> null
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Primary,
                unfocusedBorderColor = Color(0xFFEEEEEE),
                errorBorderColor     = ErrorColor,
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor  = Surface2Light.copy(alpha = 0.5f),
                focusedLabelColor    = Primary,
                unfocusedLabelColor  = TextSecondary,
                errorLabelColor      = ErrorColor,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                disabledTextColor    = TextHint,
                cursorColor          = Primary,
                errorCursorColor     = ErrorColor,
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DropdownField(
    label: String,
    selected: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { onExpandedChange(true) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Surface2Light,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selected,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selected.startsWith("Select")) TextHint else TextPrimary,
                        fontWeight = if (selected.startsWith("Select")) FontWeight.Normal else FontWeight.Bold
                    )
                    Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.fillMaxWidth(0.85f).background(Color.White, RoundedCornerShape(16.dp))
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, fontWeight = FontWeight.Medium) },
                        onClick = {
                            onItemSelected(item)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}
