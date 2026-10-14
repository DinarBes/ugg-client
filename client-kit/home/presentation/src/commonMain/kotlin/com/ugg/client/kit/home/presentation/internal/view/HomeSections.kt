package com.ugg.client.kit.home.presentation.internal.view

import com.ugg.client.kit.core.presentation.external.components.UggProductImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggButtonStyle
import com.ugg.client.kit.core.presentation.external.components.UggIconButton
import com.ugg.client.kit.core.presentation.external.components.UggPhotoPlaceholder
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.home.domain.entities.CareTeaser
import com.ugg.client.kit.home.domain.entities.CraftStep
import com.ugg.client.kit.home.domain.entities.HomeHero
import com.ugg.client.kit.home.domain.entities.HomeStat
import com.ugg.client.kit.home.domain.entities.MasterQuote
import com.ugg.client.kit.home.domain.entities.Shade
import com.ugg.client.kit.home.domain.entities.StorePreview

private const val BRAND_NAME = "БАЙКАЛ"
private const val BRAND_TAGLINE = "угги ручной работы"

@Composable
internal fun HomeHeader(cartCount: Int, onOpenFavorites: () -> Unit, onOpenCart: () -> Unit) {
    val colors = UggTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = BRAND_NAME,
                style = UggTheme.typography.displayM.copy(fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = 0.32.em),
                color = colors.content,
            )
            Text(text = BRAND_TAGLINE, style = UggTheme.typography.caption, color = colors.contentSecondary)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            UggIconButton(icon = UggIcons.Heart, contentDescription = "Избранное", onClick = onOpenFavorites)
            UggIconButton(icon = UggIcons.Bag, contentDescription = "Корзина", onClick = onOpenCart, hasBadge = cartCount > 0)
        }
    }
}

@Composable
internal fun HeroSection(hero: HomeHero, onChooseModel: () -> Unit) {
    val colors = UggTheme.colors
    val typography = UggTheme.typography

    UggPhotoPlaceholder(
        modifier = Modifier
            .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.s)
            .fillMaxWidth()
            .height(480.dp)
            .clickable(onClick = onChooseModel),
        shape = UggTheme.shapes.extraLarge,
        top = colors.surfaceVariant,
        bottom = colors.surfaceInverse,
    ) {
        UggProductImage(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = UggTheme.spacing.m)
                // Leaves the lower half of the card to the copy over the dark gradient.
                .fillMaxWidth(0.82f),
            contentDescription = hero.title,
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(UggTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs),
        ) {
            Text(text = hero.kicker.uppercase(), style = typography.overline, color = colors.surfaceVariant)
            Text(text = hero.title, style = typography.displayXL, color = colors.contentInverse)
            Text(
                text = hero.description,
                style = typography.bodySmall,
                color = colors.contentInverse.copy(alpha = 0.85f),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = hero.price, style = typography.price, color = colors.contentInverse)
                    Text(
                        text = hero.leadTime,
                        style = typography.caption,
                        color = colors.contentInverse.copy(alpha = 0.7f),
                    )
                }

                UggButton(text = "Выбрать", onClick = onChooseModel, style = UggButtonStyle.Light)
            }
        }
    }
}

@Composable
internal fun StatsSection(stats: List<HomeStat>) {
    val colors = UggTheme.colors

    Row(
        modifier = Modifier
            .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.s)
            .fillMaxWidth()
            .clip(UggTheme.shapes.large)
            .background(colors.surface)
            .padding(horizontal = UggTheme.spacing.l, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stats.forEach { stat ->
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stat.value, style = UggTheme.typography.displayL, color = colors.accentStrong)
                Text(text = stat.caption, style = UggTheme.typography.caption, color = colors.contentSecondary)
            }
        }
    }
}

@Composable
internal fun ShadesSection(shades: List<Shade>, onChooseModel: () -> Unit) {
    Column(
        modifier = Modifier.padding(top = UggTheme.spacing.section, bottom = UggTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m),
    ) {
        SectionHeader(title = "Оттенки овчины", link = "Все цвета", onLink = onChooseModel)

        LazyRow(
            contentPadding = PaddingValues(horizontal = UggTheme.spacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        ) {
            items(shades, key = { it.id }) { shade ->
                ShadeCard(shade = shade, onClick = onChooseModel)
            }
        }
    }
}

@Composable
private fun ShadeCard(shade: Shade, onClick: () -> Unit) {
    val colors = UggTheme.colors
    val color = Color(shade.color)

    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs),
    ) {
        UggPhotoPlaceholder(
            modifier = Modifier.size(width = 150.dp, height = 190.dp),
            shape = UggTheme.shapes.medium,
            top = colors.surface,
            bottom = lerp(color, colors.surface, 0.75f),
        ) {
            UggProductImage(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(4.dp)
                    .fillMaxWidth(),
                color = color,
                contentDescription = shade.name,
            )
        }
        Column {
            Text(text = shade.name, style = UggTheme.typography.label, color = colors.content)
            Text(
                text = if (shade.isInStock) "в наличии" else "под заказ",
                style = UggTheme.typography.caption,
                color = if (shade.isInStock) colors.info else colors.contentSecondary,
            )
        }
    }
}

@Composable
internal fun CraftSection(steps: List<CraftStep>, onOpenStory: () -> Unit) {
    Column(
        modifier = Modifier.padding(top = UggTheme.spacing.section, bottom = UggTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionHeader(title = "Как рождается пара", link = "История", onLink = onOpenStory)
            Text(
                text = "Без конвейера: от шкуры до готовой пары — руками одного мастера.",
                style = UggTheme.typography.bodySmall,
                color = UggTheme.colors.contentSecondary,
                modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal),
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = UggTheme.spacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        ) {
            items(steps, key = { it.number }) { step -> CraftStepCard(step) }
        }
    }
}

@Composable
private fun CraftStepCard(step: CraftStep) {
    val colors = UggTheme.colors

    Column(modifier = Modifier.width(250.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        UggPhotoPlaceholder(
            modifier = Modifier.size(width = 250.dp, height = 180.dp),
            shape = UggTheme.shapes.medium,
        )
        Spacer(Modifier.height(4.dp))
        Text(text = step.number, style = UggTheme.typography.overline, color = colors.accent)
        Text(text = step.title, style = UggTheme.typography.heading, color = colors.content)
        Text(text = step.description, style = UggTheme.typography.bodySmall, color = colors.contentSecondary)
    }
}

@Composable
internal fun QuoteSection(quote: MasterQuote) {
    val colors = UggTheme.colors

    Column(
        modifier = Modifier
            .padding(horizontal = UggTheme.spacing.screenHorizontal)
            .padding(top = UggTheme.spacing.section, bottom = UggTheme.spacing.xs)
            .fillMaxWidth()
            .clip(UggTheme.shapes.large)
            .background(colors.surfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = UggTheme.spacing.xl, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = "«", style = UggTheme.typography.displayXL, color = colors.accent)
        Text(text = quote.text, style = UggTheme.typography.displayItalic, color = colors.content)

        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UggPhotoPlaceholder(modifier = Modifier.size(40.dp), shape = UggTheme.shapes.pill)
            Column {
                Text(text = quote.author, style = UggTheme.typography.label, color = colors.content)
                Text(text = quote.role, style = UggTheme.typography.caption, color = colors.contentSecondary)
            }
        }
    }
}

@Composable
internal fun StoresSection(stores: List<StorePreview>, onOpenStores: () -> Unit) {
    Column(
        modifier = Modifier.padding(top = UggTheme.spacing.section, bottom = UggTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m),
    ) {
        SectionHeader(title = "Приходите примерить", link = "На карте", onLink = onOpenStores)

        Row(
            modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        ) {
            stores.forEach { store ->
                StoreCard(store = store, onClick = onOpenStores, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StoreCard(store: StorePreview, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = UggTheme.colors
    val statusColor = if (store.isOpen) colors.info else colors.contentSecondary

    Column(
        modifier = modifier
            .clip(UggTheme.shapes.medium)
            .background(colors.surfaceElevated)
            .border(1.dp, colors.outline, UggTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(UggTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(UggTheme.shapes.pill)
                .background(colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = UggIcons.Pin, contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(text = store.city, style = UggTheme.typography.heading, color = colors.content)
        Text(text = store.address, style = UggTheme.typography.bodySmall, color = colors.contentSecondary)
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(6.dp)
                    .clip(UggTheme.shapes.pill)
                    .background(statusColor),
            )
            Text(text = store.hours, style = UggTheme.typography.caption, color = statusColor)
        }
    }
}

@Composable
internal fun CareSection(care: CareTeaser, onOpenCare: () -> Unit) {
    val colors = UggTheme.colors

    Row(
        modifier = Modifier
            .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.section)
            .fillMaxWidth()
            .clip(UggTheme.shapes.large)
            .background(colors.surfaceInverse)
            .clickable(onClick = onOpenCare)
            .padding(22.dp),
        horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs)) {
            Text(text = care.kicker.uppercase(), style = UggTheme.typography.overline, color = colors.accent)
            Text(text = care.title, style = UggTheme.typography.displayM, color = colors.contentInverse)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = UggIcons.Clock, contentDescription = null, tint = colors.surfaceVariant, modifier = Modifier.size(14.dp))
                Text(text = care.readTime, style = UggTheme.typography.caption, color = colors.surfaceVariant)
            }
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(UggTheme.shapes.pill)
                .background(colors.background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = UggIcons.ArrowRight, contentDescription = null, tint = colors.content, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, link: String?, onLink: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UggTheme.spacing.screenHorizontal),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(text = title, style = UggTheme.typography.displayM, color = UggTheme.colors.content)

        if (link != null) {
            Text(
                text = link,
                style = UggTheme.typography.label,
                color = UggTheme.colors.accent,
                modifier = Modifier
                    .clip(UggTheme.shapes.small)
                    .clickable(onClick = onLink)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}
