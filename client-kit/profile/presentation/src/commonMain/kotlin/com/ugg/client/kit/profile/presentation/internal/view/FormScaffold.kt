package com.ugg.client.kit.profile.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggButtonStyle
import com.ugg.client.kit.core.presentation.external.components.UggIconButton
import com.ugg.client.kit.core.presentation.external.components.UggPullToRefreshBox
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

/**
 * The frame of the profile's forms: back and title on top, scrolling content, actions pinned at
 * the bottom above the keyboard. While [isLoading] the content is a spinner; a [loadError] shows
 * with «Повторить» instead of the content. With [onRefresh] the content can be pulled down to refresh.
 */
@Composable
internal fun FormScaffold(
    title: String,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    loadError: Throwable? = null,
    onRetry: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = UggTheme.colors
    val horizontal = UggTheme.spacing.screenHorizontal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            // safeDrawing includes the keyboard: the bottom button stays above it.
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontal, vertical = UggTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UggIconButton(icon = UggIcons.ArrowLeft, contentDescription = "Назад", onClick = onBack)
            Text(text = title, style = UggTheme.typography.heading, color = colors.content)
        }

        when {
            loadError != null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = horizontal),
                verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = loadError.message ?: "Не удалось загрузить",
                    style = UggTheme.typography.body,
                    color = colors.contentSecondary,
                    textAlign = TextAlign.Center,
                )
                UggButton(text = "Повторить", onClick = onRetry, style = UggButtonStyle.Secondary, showArrow = false)
            }

            isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accent)
            }

            else -> {
                val scrolling: @Composable () -> Unit = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = horizontal, vertical = UggTheme.spacing.m),
                        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m),
                        content = content,
                    )
                }

                if (onRefresh != null) {
                    UggPullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) { scrolling() }
                } else {
                    Box(Modifier.weight(1f).fillMaxWidth()) { scrolling() }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = horizontal, vertical = UggTheme.spacing.s),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
                    content = bottom,
                )
            }
        }
    }
}

/** A save error under the content, in the error color. */
@Composable
internal fun SaveError(error: Throwable?) {
    if (error == null) return
    Text(
        text = error.message ?: "Не получилось сохранить. Попробуйте ещё раз.",
        style = UggTheme.typography.bodySmall,
        color = UggTheme.colors.error,
    )
}
