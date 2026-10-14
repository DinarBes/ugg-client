package com.ugg.client.kit.care.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.care.domain.entities.Care
import com.ugg.client.kit.care.domain.entities.CareContact
import com.ugg.client.kit.care.domain.entities.CareTip
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggButtonStyle
import com.ugg.client.kit.core.presentation.external.components.UggIconButton
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

@Composable
internal fun CareView(
    care: Care?,
    isLoading: Boolean,
    error: Throwable?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onContact: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(UggTheme.colors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs),
        ) {
            UggIconButton(icon = UggIcons.ArrowLeft, contentDescription = "Назад", onClick = onBack)
        }

        when {
            care != null -> CareContent(care = care, onContact = onContact)

            error != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = UggTheme.spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = error.message ?: "Не удалось загрузить",
                    style = UggTheme.typography.body,
                    color = UggTheme.colors.contentSecondary,
                    textAlign = TextAlign.Center,
                )
                UggButton(text = "Повторить", onClick = onRetry, style = UggButtonStyle.Secondary, showArrow = false)
            }

            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UggTheme.colors.accent)
            }
        }
    }
}

@Composable
private fun CareContent(care: Care, onContact: () -> Unit) {
    val colors = UggTheme.colors
    val horizontal = UggTheme.spacing.screenHorizontal

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
    ) {
        item(key = "intro") {
            Column(
                modifier = Modifier.padding(horizontal = horizontal, vertical = UggTheme.spacing.s),
                verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
            ) {
                Text(text = care.kicker.uppercase(), style = UggTheme.typography.overline, color = colors.accent)
                Text(text = care.title, style = UggTheme.typography.displayXL, color = colors.content)
                if (care.readTime.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = UggIcons.Clock, contentDescription = null, tint = colors.contentSecondary, modifier = Modifier.size(14.dp))
                        Text(text = care.readTime, style = UggTheme.typography.caption, color = colors.contentSecondary)
                    }
                }
                Text(text = care.intro, style = UggTheme.typography.body, color = colors.contentSecondary)
            }
        }

        itemsIndexed(care.tips, key = { index, _ -> "tip-$index" }) { _, tip ->
            TipCard(tip, modifier = Modifier.padding(horizontal = horizontal, vertical = UggTheme.spacing.xs))
        }

        if (care.donts.isNotEmpty()) {
            item(key = "donts") {
                DontsCard(
                    title = care.dontsTitle,
                    items = care.donts,
                    modifier = Modifier.padding(horizontal = horizontal).padding(top = UggTheme.spacing.m, bottom = UggTheme.spacing.xs),
                )
            }
        }

        care.contact?.let { contact ->
            item(key = "contact") {
                ContactCard(
                    contact = contact,
                    onClick = onContact,
                    modifier = Modifier.padding(horizontal = horizontal).padding(top = UggTheme.spacing.s, bottom = UggTheme.spacing.xl),
                )
            }
        }
    }
}

@Composable
private fun TipCard(tip: CareTip, modifier: Modifier = Modifier) {
    val colors = UggTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(UggTheme.shapes.large)
            .background(colors.surfaceElevated)
            .border(1.dp, colors.outline, UggTheme.shapes.large)
            .padding(UggTheme.spacing.m),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(UggTheme.shapes.pill)
                .background(colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = tip.kind.icon(), contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = tip.title, style = UggTheme.typography.heading, color = colors.content)
            Text(text = tip.body, style = UggTheme.typography.bodySmall, color = colors.contentSecondary)
        }
    }
}

@Composable
private fun DontsCard(title: String, items: List<String>, modifier: Modifier = Modifier) {
    val colors = UggTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(UggTheme.shapes.large)
            .background(colors.surface)
            .padding(UggTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
    ) {
        Text(text = title, style = UggTheme.typography.heading, color = colors.content)
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(UggTheme.shapes.pill)
                        .background(colors.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "✕", style = UggTheme.typography.caption, color = colors.error)
                }
                Text(text = item, style = UggTheme.typography.body, color = colors.content)
            }
        }
    }
}

@Composable
private fun ContactCard(contact: CareContact, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = UggTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(UggTheme.shapes.extraLarge)
            .background(colors.surfaceInverse)
            .padding(UggTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(imageVector = UggIcons.Message, contentDescription = null, tint = colors.contentInverse, modifier = Modifier.size(24.dp))
        Text(text = contact.title, style = UggTheme.typography.displayM, color = colors.contentInverse)
        Text(text = contact.body, style = UggTheme.typography.bodySmall, color = colors.contentInverse.copy(alpha = 0.8f))
        UggButton(
            text = contact.button,
            onClick = onClick,
            style = UggButtonStyle.Light,
            modifier = Modifier.padding(top = 6.dp).fillMaxWidth(),
        )
    }
}

private fun CareTip.Kind.icon(): ImageVector = when (this) {
    CareTip.Kind.Snow -> UggIcons.Snowflake
    CareTip.Kind.Drying -> UggIcons.Clock
    CareTip.Kind.Salt -> UggIcons.Leaf
    CareTip.Kind.Cleaning -> UggIcons.Scissors
    CareTip.Kind.Protection -> UggIcons.ShieldCheck
    CareTip.Kind.Storage -> UggIcons.Package
    CareTip.Kind.Other -> UggIcons.Leaf
}
