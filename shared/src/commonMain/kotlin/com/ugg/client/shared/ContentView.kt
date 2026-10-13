package com.ugg.client.shared

import androidx.compose.runtime.Composable
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.navigation.presentation.external.view.NavigationScreen
import org.kodein.di.compose.withDI

@Composable
fun ContentView(app: TemplateApp) = withDI(app.di) {
    UggTheme {
        NavigationScreen()
    }
}
