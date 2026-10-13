package com.ugg.client.kit.home.presentation.internal.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggButtonStyle
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.home.domain.entities.Home

@Composable
internal fun HomeView(
    home: Home?,
    cartCount: Int,
    isLoading: Boolean,
    error: Throwable?,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
    onChooseModel: () -> Unit,
    onOpenStores: () -> Unit,
    onOpenStory: () -> Unit,
    onOpenCare: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenCart: () -> Unit,
) {
    when {
        home != null -> HomeContent(
            home = home,
            cartCount = cartCount,
            contentPadding = contentPadding,
            onChooseModel = onChooseModel,
            onOpenStores = onOpenStores,
            onOpenStory = onOpenStory,
            onOpenCare = onOpenCare,
            onOpenFavorites = onOpenFavorites,
            onOpenCart = onOpenCart,
        )

        error != null -> HomeError(error, onRetry, Modifier.padding(contentPadding))

        isLoading -> Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = UggTheme.colors.accent)
        }
    }
}

@Composable
private fun HomeContent(
    home: Home,
    cartCount: Int,
    contentPadding: PaddingValues,
    onChooseModel: () -> Unit,
    onOpenStores: () -> Unit,
    onOpenStory: () -> Unit,
    onOpenCare: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenCart: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection),
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + UggTheme.spacing.xxl,
        ),
    ) {
        item(key = "header") { HomeHeader(cartCount = cartCount, onOpenFavorites = onOpenFavorites, onOpenCart = onOpenCart) }
        item(key = "hero") { HeroSection(hero = home.hero, onChooseModel = onChooseModel) }

        if (home.stats.isNotEmpty()) {
            item(key = "stats") { StatsSection(stats = home.stats) }
        }
        if (home.shades.isNotEmpty()) {
            item(key = "shades") { ShadesSection(shades = home.shades, onChooseModel = onChooseModel) }
        }
        if (home.craftSteps.isNotEmpty()) {
            item(key = "craft") { CraftSection(steps = home.craftSteps, onOpenStory = onOpenStory) }
        }
        home.quote?.let { quote ->
            item(key = "quote") { QuoteSection(quote = quote) }
        }
        if (home.stores.isNotEmpty()) {
            item(key = "stores") { StoresSection(stores = home.stores, onOpenStores = onOpenStores) }
        }
        home.care?.let { care ->
            item(key = "care") { CareSection(care = care, onOpenCare = onOpenCare) }
        }
    }
}

@Composable
private fun HomeError(error: Throwable, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = UggTheme.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = error.message ?: "Не удалось загрузить",
            style = UggTheme.typography.body,
            color = UggTheme.colors.contentSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        UggButton(
            text = "Повторить",
            onClick = onRetry,
            style = UggButtonStyle.Secondary,
            showArrow = false,
        )
    }
}
