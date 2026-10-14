package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

/**
 * Material pull-to-refresh in the app's colors. The content must scroll (a LazyColumn or a
 * verticalScroll column) for the pull to reach the box.
 *
 * @param indicatorPadding moves the spinner below whatever covers the top of the box — usually the
 * status bar, i.e. the top of the screen's content padding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UggPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorPadding: PaddingValues = PaddingValues(),
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter).padding(indicatorPadding),
                containerColor = UggTheme.colors.surfaceElevated,
                color = UggTheme.colors.accent,
            )
        },
        content = content,
    )
}
