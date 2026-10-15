package com.ugg.client.kit.onboarding.presentation.internal.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggButtonStyle
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.onboarding.domain.entities.OnboardingSlide
import kotlinx.coroutines.launch

private const val BRAND_NAME = "БАЙКАЛ"
private const val BRAND_TAGLINE = "угги ручной работы"

@Composable
internal fun OnboardingView(
    slides: List<OnboardingSlide>,
    page: Int,
    error: Throwable?,
    onPageChanged: (Int) -> Unit,
    onStart: () -> Unit,
    onLogin: () -> Unit,
    onRetry: () -> Unit,
) {
    val colors = UggTheme.colors
    val spacing = UggTheme.spacing

    val pagerState = rememberPagerState(initialPage = page) { slides.size }
    val currentOnPageChanged by rememberUpdatedState(onPageChanged)
    val scope = rememberCoroutineScope()

    // Follows the pager, so a swipe to the last slide turns «Далее» into «Начать» just like a tap.
    val isLastPage by remember {
        derivedStateOf { pagerState.pageCount == 0 || pagerState.currentPage >= pagerState.pageCount - 1 }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentOnPageChanged(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Placeholder for the photo «мастер шьёт угги у окна, за окном — Байкал»: keeps the
            // light top under the status bar and the dark bottom under the white copy.
            .background(
                Brush.verticalGradient(
                    listOf(colors.surfaceVariant, colors.accentStrong, colors.surfaceInverse),
                ),
            ),
    ) {
        // The gradient fills the whole window; the copy and the buttons keep a readable width in
        // the middle of a wide one (on a phone the limit is never reached).
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = spacing.readableWidth)
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Wordmark(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.s),
            )

            if (error != null) {
                ErrorContent(
                    error = error,
                    onRetry = onRetry,
                    modifier = Modifier.weight(1f),
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) { index ->
                    SlideContent(slide = slides[index], modifier = Modifier.fillMaxSize())
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xl)
                    .padding(top = 14.dp, bottom = spacing.m),
            ) {
                PageIndicator(count = slides.size, current = pagerState.currentPage)

                Spacer(Modifier.height(spacing.l))

                UggButton(
                    text = if (isLastPage) "Начать" else "Далее",
                    onClick = {
                        if (isLastPage) {
                            onStart()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    style = UggButtonStyle.Light,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(spacing.xs))

                Text(
                    text = "Войти по номеру телефона",
                    style = UggTheme.typography.label,
                    color = colors.contentInverse.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(UggTheme.shapes.pill)
                        .clickable(onClick = onLogin)
                        .padding(vertical = spacing.s),
                )
            }
        }
    }
}

@Composable
private fun Wordmark(modifier: Modifier = Modifier) {
    val colors = UggTheme.colors

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = BRAND_NAME,
            style = UggTheme.typography.displayM.copy(
                fontSize = 30.sp,
                lineHeight = 34.sp,
                letterSpacing = 0.32.em,
            ),
            color = colors.content,
        )
        Text(
            text = BRAND_TAGLINE,
            style = UggTheme.typography.caption,
            color = colors.content.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun SlideContent(slide: OnboardingSlide, modifier: Modifier = Modifier) {
    val colors = UggTheme.colors
    val typography = UggTheme.typography

    Column(
        modifier = modifier.padding(horizontal = UggTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.Bottom),
    ) {
        Text(
            text = slide.overline.uppercase(),
            style = typography.overline,
            color = colors.surfaceVariant,
        )
        Text(
            text = slide.title,
            style = typography.displayXL,
            color = colors.contentInverse,
        )
        Text(
            text = slide.description,
            style = typography.body,
            color = colors.contentInverse.copy(alpha = 0.85f),
        )
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    val color = UggTheme.colors.contentInverse

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            val isCurrent = index == current
            val width by animateDpAsState(if (isCurrent) 24.dp else 6.dp)

            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(UggTheme.shapes.pill)
                    .background(color.copy(alpha = if (isCurrent) 1f else 0.4f)),
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = UggTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m, Alignment.Bottom),
    ) {
        Text(
            text = error.message ?: "Не удалось загрузить",
            style = UggTheme.typography.body,
            color = UggTheme.colors.contentInverse,
        )
        UggButton(
            text = "Повторить",
            onClick = onRetry,
            style = UggButtonStyle.Secondary,
            showArrow = false,
        )
    }
}
