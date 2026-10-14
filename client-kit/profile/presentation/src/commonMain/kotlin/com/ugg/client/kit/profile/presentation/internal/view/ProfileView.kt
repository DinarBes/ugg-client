package com.ugg.client.kit.profile.presentation.internal.view

import com.ugg.client.kit.core.presentation.external.components.UggPullToRefreshBox
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggButtonStyle
import com.ugg.client.kit.core.presentation.external.components.UggIconButton
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.profile.domain.entities.Profile
import com.ugg.client.kit.profile.domain.entities.ProfileMenuItem

@Composable
internal fun ProfileView(
    profile: Profile?,
    isGuest: Boolean,
    isLoading: Boolean,
    isRefreshing: Boolean,
    error: Throwable?,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onSignIn: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenOrder: () -> Unit,
    onOpenSize: () -> Unit,
    onOpenOrders: () -> Unit,
    onMenuItem: (ProfileMenuItem.Kind) -> Unit,
) {
    val horizontal = UggTheme.spacing.screenHorizontal

    UggPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        indicatorPadding = PaddingValues(top = contentPadding.calculateTopPadding()),
    ) {
        when {
            profile != null -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            ) {
                item(key = "header") { Header(onOpenSettings = onOpenSettings) }
                item(key = "user") { UserRow(profile) }

                profile.activeOrder?.let { order ->
                    item(key = "order") {
                        ActiveOrderCard(
                            order = order,
                            onClick = onOpenOrder,
                            modifier = Modifier.padding(horizontal = horizontal, vertical = UggTheme.spacing.xs),
                        )
                    }
                }

                item(key = "quick") {
                    Row(
                        modifier = Modifier.padding(horizontal = horizontal, vertical = UggTheme.spacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        QuickCard(
                            icon = UggIcons.Ruler,
                            title = "Мой размер",
                            value = profile.size ?: "не выбран",
                            onClick = onOpenSize,
                            modifier = Modifier.weight(1f),
                        )
                        QuickCard(
                            icon = UggIcons.Package,
                            title = "Мои заказы",
                            value = profile.ordersSummary,
                            onClick = onOpenOrders,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                if (profile.menu.isNotEmpty()) {
                    item(key = "menu") {
                        Menu(
                            items = profile.menu,
                            onClick = onMenuItem,
                            modifier = Modifier.padding(horizontal = horizontal, vertical = UggTheme.spacing.xs),
                        )
                    }
                }
            }

            isGuest -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            ) {
                item(key = "header") { Header(onOpenSettings = onOpenSettings) }
                item(key = "guest") {
                    GuestCard(
                        onSignIn = onSignIn,
                        modifier = Modifier.padding(horizontal = horizontal, vertical = UggTheme.spacing.xs),
                    )
                }
                item(key = "menu") {
                    Menu(
                        items = GUEST_MENU,
                        onClick = onMenuItem,
                        modifier = Modifier.padding(horizontal = horizontal, vertical = UggTheme.spacing.xs),
                    )
                }
            }

            error != null -> Column(
                modifier = Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = horizontal),
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

            isLoading -> Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UggTheme.colors.accent)
            }
        }
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Профиль", style = UggTheme.typography.displayL, color = UggTheme.colors.content)
        UggIconButton(icon = UggIcons.Settings, contentDescription = "Настройки", onClick = onOpenSettings)
    }
}

@Composable
private fun UserRow(profile: Profile) {
    val colors = UggTheme.colors

    Row(
        modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.s),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(UggTheme.shapes.pill)
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (profile.initials.isNotEmpty()) {
                Text(text = profile.initials, style = UggTheme.typography.heading, color = colors.accentStrong)
            } else {
                Icon(imageVector = UggIcons.User, contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(24.dp))
            }
        }
        Column {
            Text(text = profile.name, style = UggTheme.typography.heading, color = colors.content)
            Text(text = profile.phone, style = UggTheme.typography.bodySmall, color = colors.contentSecondary)
        }
    }
}

/** What a guest is offered instead of the profile. */
@Composable
private fun GuestCard(onSignIn: () -> Unit, modifier: Modifier = Modifier) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.large

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .padding(UggTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(UggTheme.shapes.pill)
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = UggIcons.User, contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(24.dp))
        }
        Text(text = "Войдите по номеру телефона", style = UggTheme.typography.heading, color = colors.content)
        Text(
            text = "Чтобы следить за пошивом своей пары, хранить размер и адреса доставки.",
            style = UggTheme.typography.body,
            color = colors.contentSecondary,
        )
        UggButton(text = "Войти", onClick = onSignIn, modifier = Modifier.fillMaxWidth())
    }
}

private val GUEST_MENU = listOf(
    ProfileMenuItem(ProfileMenuItem.Kind.Care, "Уход за уггами"),
    ProfileMenuItem(ProfileMenuItem.Kind.Contact, "Написать в мастерскую"),
)

@Composable
private fun QuickCard(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = UggTheme.colors

    Column(
        modifier = modifier
            .clip(UggTheme.shapes.medium)
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(UggTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(22.dp))
        Column {
            Text(text = title, style = UggTheme.typography.label, color = colors.content)
            Text(text = value, style = UggTheme.typography.caption, color = colors.contentSecondary)
        }
    }
}

@Composable
private fun Menu(
    items: List<ProfileMenuItem>,
    onClick: (ProfileMenuItem.Kind) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.large

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceElevated)
            .border(1.dp, colors.outline, shape)
            .padding(6.dp),
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.outline)

            val isLogout = item.kind == ProfileMenuItem.Kind.Logout
            val tint = if (isLogout) colors.contentSecondary else colors.accentStrong

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(UggTheme.shapes.small)
                    .clickable { onClick(item.kind) }
                    .padding(horizontal = UggTheme.spacing.s, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(UggTheme.shapes.pill)
                        .background(colors.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = item.kind.icon(), contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = item.title,
                    style = UggTheme.typography.label,
                    color = if (isLogout) colors.contentSecondary else colors.content,
                    modifier = Modifier.weight(1f),
                )
                Icon(imageVector = UggIcons.ChevronRight, contentDescription = null, tint = colors.contentSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun ProfileMenuItem.Kind.icon() = when (this) {
    ProfileMenuItem.Kind.Addresses -> UggIcons.Pin
    ProfileMenuItem.Kind.Care -> UggIcons.Leaf
    ProfileMenuItem.Kind.Contact -> UggIcons.Message
    ProfileMenuItem.Kind.Logout -> UggIcons.Logout
}
