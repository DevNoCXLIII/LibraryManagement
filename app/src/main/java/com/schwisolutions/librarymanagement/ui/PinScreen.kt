package com.schwisolutions.librarymanagement.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Hardcoded 4-digit PIN for authentication.
 */
private const val CORRECT_PIN = "1234"

/**
 * PIN verification lock screen protecting app access.
 *
 * Official docs reference:
 * - developer.android.com/develop/ui/compose/state#save-ui-state
 *
 * Mechanics:
 * - Uses rememberSaveable so entered PIN survives screen rotation.
 * - Displays 4 circular dots that fill as digits are entered.
 * - Validates automatically when the 4th digit is pressed.
 * - On success, invokes onLoginSuccess() which pops the PIN screen off the backstack.
 *
 * @param onLoginSuccess Callback triggered when the correct PIN (1234) is entered.
 */
@Composable
fun PinScreen(onLoginSuccess: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    fun onDigitPress(digit: String) {
        if (pin.length < 4) {
            val newPin = pin + digit
            pin = newPin
            isError = false
            if (newPin.length == 4) {
                if (newPin == CORRECT_PIN) {
                    onLoginSuccess()
                } else {
                    isError = true
                    pin = ""
                }
            }
        }
    }

    fun onDeletePress() {
        if (pin.isNotEmpty()) {
            pin = pin.dropLast(1)
            isError = false
        }
    }

    fun onClearPress() {
        pin = ""
        isError = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Lock",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter PIN",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PIN Indicator Dots: 4 circles representing the 4 digits
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isFilled = i < pin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isError -> MaterialTheme.colorScheme.error
                                isFilled -> MaterialTheme.colorScheme.primary
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = when {
                                isError -> MaterialTheme.colorScheme.error
                                isFilled -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isError) "Incorrect PIN. Try again." else "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Keypad Grid: 3x4 layout (1-9, C, 0, Backspace)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )

            for (row in keyRows) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    for (digit in row) {
                        KeypadButton(text = digit, onClick = { onDigitPress(digit) })
                    }
                }
            }

            // Bottom Row: Clear ('C'), '0', Delete (Backspace icon)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Surface(
                    onClick = { onClearPress() },
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "C",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                KeypadButton(text = "0", onClick = { onDigitPress("0") })

                Surface(
                    onClick = { onDeletePress() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Circular keypad button for numeric entry.
 */
@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.size(72.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
