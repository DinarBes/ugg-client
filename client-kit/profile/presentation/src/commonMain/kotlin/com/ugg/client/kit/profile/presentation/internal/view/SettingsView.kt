package com.ugg.client.kit.profile.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.profile.domain.states.SettingsState

@Composable
internal fun SettingsView(
    state: SettingsState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onEditName: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val colors = UggTheme.colors
    val account = state.account

    FormScaffold(
        title = "Настройки",
        onBack = onBack,
        isLoading = state.isLoading && account == null,
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        bottom = {
            Text(text = "Байкал · угги ручной работы", style = UggTheme.typography.caption, color = colors.contentSecondary)
            if (state.version.isNotEmpty()) {
                Text(text = state.version, style = UggTheme.typography.caption, color = colors.contentSecondary.copy(alpha = 0.7f))
            }
        },
    ) {
        if (account == null) {
            Text(
                text = "Войдите по номеру телефона, чтобы указать имя и управлять аккаунтом.",
                style = UggTheme.typography.body,
                color = colors.contentSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = UggTheme.spacing.xl),
            )
        } else {
            Section(title = "Профиль") {
                SettingsRow(title = "Имя", value = account.name.ifEmpty { "Не указано" }, onClick = onEditName)
                Divider()
                SettingsRow(title = "Телефон", value = account.phone, caption = "Номер — ваш вход в приложение, сменить его нельзя")
            }

            Section(title = "Аккаунт") {
                SettingsRow(title = "Выйти", onClick = onSignOut, icon = UggIcons.Logout)
                Divider()
                SettingsRow(
                    title = if (state.isDeleting) "Удаляем…" else "Удалить аккаунт",
                    onClick = onDeleteAccount.takeIf { !state.isDeleting },
                    titleColor = colors.error,
                    caption = "Сотрём профиль, размер и адреса",
                )
            }
        }

        SaveError(state.error)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.large

    Column(verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs)) {
        Text(text = title.uppercase(), style = UggTheme.typography.overline, color = colors.accent)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surfaceElevated)
                .border(1.dp, colors.outline, shape)
                .padding(6.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    value: String? = null,
    caption: String? = null,
    onClick: (() -> Unit)? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    titleColor: Color = UggTheme.colors.content,
) {
    val colors = UggTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(UggTheme.shapes.small)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = UggTheme.spacing.s, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let { Icon(imageVector = it, contentDescription = null, tint = colors.contentSecondary, modifier = Modifier.size(18.dp)) }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = UggTheme.typography.label, color = titleColor)
            caption?.let { Text(text = it, style = UggTheme.typography.caption, color = colors.contentSecondary) }
        }
        value?.let { Text(text = it, style = UggTheme.typography.body, color = colors.contentSecondary, textAlign = TextAlign.End) }
        if (onClick != null) {
            Icon(imageVector = UggIcons.ChevronRight, contentDescription = null, tint = colors.contentSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun Divider() = HorizontalDivider(thickness = 1.dp, color = UggTheme.colors.outline)
