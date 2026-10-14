package com.ugg.client.kit.navigation.presentation.internal.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.ugg.client.kit.core.presentation.external.components.UggTabBar
import com.ugg.client.kit.core.presentation.external.components.UggTabItem
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.home.presentation.external.view.HomeScreen
import com.ugg.client.kit.product.presentation.external.view.ProductScreen
import com.ugg.client.kit.profile.domain.entities.ProfileMenuItem
import com.ugg.client.kit.profile.presentation.external.view.ProfileScreen
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
    onOpenCart: () -> Unit,
    onSignIn: () -> Unit,
    onOpenSize: () -> Unit,
    onOpenAddresses: () -> Unit,
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
                onOpenCart = onOpenCart,
            )

            MainTab.Model -> ProductScreen(
                contentPadding = padding,
                onAddToCart = onOpenCart,
            )

            MainTab.Stores -> StoresScreen(contentPadding = padding)

            MainTab.Profile -> ProfileScreen(
                contentPadding = padding,
                onSignIn = onSignIn,
                onOpenSize = onOpenSize,
                onMenuItem = { kind ->
                    when (kind) {
                        ProfileMenuItem.Kind.Addresses -> onOpenAddresses()
                        else -> Unit
                    }
                },
            )
        }
    }
}
