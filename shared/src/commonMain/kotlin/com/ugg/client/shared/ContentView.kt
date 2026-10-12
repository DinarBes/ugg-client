package com.ugg.client.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.ugg.client.kit.navigation.presentation.external.view.NavigationScreen
import org.kodein.di.compose.withDI

@Composable
fun ContentView(app: TemplateApp) = withDI(app.di) {
    MaterialTheme {
        NavigationScreen()
    }
}
