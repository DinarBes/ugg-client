package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

/**
 * Confirmation in the app's style: a title, a short explanation and two full-width buttons —
 * the action on top, «cancel» under it. Tapping outside or «back» dismisses it.
 */
@Composable
fun UggAlertDialog(
    title: String,
    text: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = UggTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(UggTheme.shapes.large)
                .background(colors.surfaceElevated)
                .padding(UggTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        ) {
            Text(
                text = title,
                style = UggTheme.typography.heading,
                color = colors.content,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = text,
                style = UggTheme.typography.body,
                color = colors.contentSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            UggButton(
                text = confirmText,
                onClick = onConfirm,
                showArrow = false,
                modifier = Modifier.fillMaxWidth(),
            )
            UggButton(
                text = dismissText,
                onClick = onDismiss,
                style = UggButtonStyle.Secondary,
                showArrow = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
