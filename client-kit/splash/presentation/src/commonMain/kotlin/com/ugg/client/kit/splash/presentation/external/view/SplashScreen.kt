package com.ugg.client.kit.splash.presentation.external.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplashScreen(
    error: Throwable? = null,
    onRetry: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (error == null) {
            LoadingIndicator()
        } else {
            Text(text = error.message ?: "Unknown error", color = Color.Red)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}
