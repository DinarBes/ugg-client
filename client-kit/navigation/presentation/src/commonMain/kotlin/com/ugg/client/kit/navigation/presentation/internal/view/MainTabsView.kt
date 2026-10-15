package com.ugg.client.kit.navigation.presentation.internal.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.window.core.layout.WindowSizeClass
import com.ugg.client.kit.core.presentation.external.components.UggCenteredBox
import com.ugg.client.kit.core.presentation.external.components.UggNavigationRail
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
 * The app shell behind [com.ugg.client.kit.splash.domain.entities.SplashFlow.Launch]: the tabs
 * plus the content of the selected tab. A compact window gets the bottom [UggTabBar]; from the
 * medium width on, the same tabs move to a [UggNavigationRail] on the start edge. Screens pushed
 * on the root back stack cover it. The selected tab is owned by the host so those screens can
 * switch it on their way back.
 */
@Composable
internal fun MainTabsView(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onOpenStory: () -> Unit,
    onOpenCare: () -> Unit,
    onOpenCart: () -> Unit,
    onSignIn: () -> Unit,
    onOpenSize: () -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val tabs = MainTab.entries
    val items = tabs.map { UggTabItem(it.label, it.icon()) }
    val isMediumOrLarger = UggTheme.adaptiveInfo.windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val content: @Composable (PaddingValues) -> Unit = { padding ->
        TabContent(
            selected = selected,
            padding = padding,
            onSelect = onSelect,
            onOpenStory = onOpenStory,
            onOpenCare = onOpenCare,
            onOpenCart = onOpenCart,
            onSignIn = onSignIn,
            onOpenSize = onOpenSize,
            onOpenAddresses = onOpenAddresses,
            onOpenSettings = onOpenSettings,
        )
    }

    if (isMediumOrLarger) {
        Row {
            UggNavigationRail(
                items = items,
                selectedIndex = selected.ordinal,
                onSelect = { onSelect(tabs[it]) },
            )
            Scaffold(
                containerColor = UggTheme.colors.background,
                modifier = Modifier.weight(1f),
                content = content,
            )
        }
    } else {
        Scaffold(
            containerColor = UggTheme.colors.background,
            bottomBar = {
                UggTabBar(
                    items = items,
                    selectedIndex = selected.ordinal,
                    onSelect = { onSelect(tabs[it]) },
                )
            },
            content = content,
        )
    }
}

@Composable
private fun TabContent(
    selected: MainTab,
    padding: PaddingValues,
    onSelect: (MainTab) -> Unit,
    onOpenStory: () -> Unit,
    onOpenCare: () -> Unit,
    onOpenCart: () -> Unit,
    onSignIn: () -> Unit,
    onOpenSize: () -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    when (selected) {
        // Home, the model and the stores lay themselves out for wide windows.
        MainTab.Home -> HomeScreen(
            contentPadding = padding,
            onChooseModel = { onSelect(MainTab.Model) },
            onOpenStores = { onSelect(MainTab.Stores) },
            onOpenStory = onOpenStory,
            onOpenCare = onOpenCare,
            onOpenCart = onOpenCart,
        )

        MainTab.Model -> ProductScreen(
            contentPadding = padding,
            onAddToCart = onOpenCart,
        )

        MainTab.Stores -> StoresScreen(contentPadding = padding)

        // The profile is a single column: on a wide window it is held to a readable width.
        MainTab.Profile -> UggCenteredBox(maxWidth = UggTheme.spacing.readableWidth) {
            ProfileScreen(
                contentPadding = padding,
                onSignIn = onSignIn,
                onOpenSize = onOpenSize,
                onOpenSettings = onOpenSettings,
                onMenuItem = { kind ->
                    when (kind) {
                        ProfileMenuItem.Kind.Addresses -> onOpenAddresses()
                        ProfileMenuItem.Kind.Care -> onOpenCare()
                        else -> Unit
                    }
                },
            )
        }
    }
}
