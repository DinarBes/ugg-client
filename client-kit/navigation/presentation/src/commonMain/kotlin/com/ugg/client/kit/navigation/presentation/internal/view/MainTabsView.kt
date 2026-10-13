package com.ugg.client.kit.navigation.presentation.internal.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ugg.client.kit.core.presentation.external.components.UggTabBar
import com.ugg.client.kit.core.presentation.external.components.UggTabItem
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.home.presentation.external.view.HomeScreen
import com.ugg.client.kit.product.presentation.external.view.ProductScreen
import com.ugg.client.kit.stores.presentation.external.view.StoresScreen

internal enum class MainTab(val label: String, val icon: () -> ImageVector) {
    Home("Главная", { UggIcons.Home }),
    Model("Модель", { UggIcons.Boot }),
    Stores("Точки", { UggIcons.Pin }),
    Profile("Профиль", { UggIcons.User }),
}

/**
 * The app shell behind [com.ugg.client.kit.splash.domain.entities.SplashFlow.Launch]: tab bar plus
 * the content of the selected tab. Screens pushed on the root back stack cover it, tab bar
 * included. The selected tab is owned by the host so those screens can switch it on their way back.
 */
@Composable
internal fun MainTabsView(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onOpenStory: () -> Unit,
) {
    val tabs = MainTab.entries

    Scaffold(
        containerColor = UggTheme.colors.background,
        bottomBar = {
            UggTabBar(
                items = tabs.map { UggTabItem(it.label, it.icon()) },
                selectedIndex = selected.ordinal,
                onSelect = { onSelect(tabs[it]) },
            )
        },
    ) { padding ->
        when (selected) {
            MainTab.Home -> HomeScreen(
                contentPadding = padding,
                onChooseModel = { onSelect(MainTab.Model) },
                onOpenStores = { onSelect(MainTab.Stores) },
                onOpenStory = onOpenStory,
            )

            MainTab.Model -> ProductScreen(
                contentPadding = padding,
                onAddToCart = {},
            )

            MainTab.Stores -> StoresScreen(contentPadding = padding)

            else -> TabPlaceholder(tab = selected, contentPadding = padding)
        }
    }
}

@Composable
private fun TabPlaceholder(tab: MainTab, contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = UggTheme.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = tab.label, style = UggTheme.typography.displayL, color = UggTheme.colors.content)
        Text(
            text = "Экран скоро появится",
            style = UggTheme.typography.body,
            color = UggTheme.colors.contentSecondary,
        )
    }
}
