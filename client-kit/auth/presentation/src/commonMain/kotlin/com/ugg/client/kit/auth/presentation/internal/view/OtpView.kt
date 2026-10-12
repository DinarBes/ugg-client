package com.ugg.client.kit.auth.presentation.internal.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.auth.presentation.external.view.models.OtpViewModel

@Composable
internal fun OtpView(
    otp: String,
    error: Throwable?,
    isLoading: Boolean,
    onOtpChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = otp,
            onValueChange = onOtpChanged,
            label = { Text("OTP Code") },
            modifier = Modifier.fillMaxWidth()
        )

        error?.let { Text(text = it.message ?: "Unknown error", color = Color.Red) }

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify")
            }
        }
    }
}
