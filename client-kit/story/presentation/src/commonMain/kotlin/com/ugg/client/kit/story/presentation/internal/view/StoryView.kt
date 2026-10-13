package com.ugg.client.kit.story.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggButtonStyle
import com.ugg.client.kit.core.presentation.external.components.UggIconButton
import com.ugg.client.kit.core.presentation.external.components.UggPhotoPlaceholder
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.story.domain.entities.Story
import com.ugg.client.kit.story.domain.entities.StoryHighlight
import com.ugg.client.kit.story.domain.entities.StoryStep

@Composable
internal fun StoryView(
    story: Story?,
    isLoading: Boolean,
    error: Throwable?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onChooseModel: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(UggTheme.colors.background)) {
        TopBar(onBack = onBack, onToggleFavorite = onToggleFavorite)

        when {
            story != null -> StoryContent(story = story, onChooseModel = onChooseModel)

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
private fun TopBar(onBack: () -> Unit, onToggleFavorite: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        UggIconButton(icon = UggIcons.ArrowLeft, contentDescription = "Назад", onClick = onBack)
        UggIconButton(icon = UggIcons.Heart, contentDescription = "В избранное", onClick = onToggleFavorite)
    }
}

@Composable
private fun StoryContent(story: Story, onChooseModel: () -> Unit) {
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
                Text(text = story.kicker.uppercase(), style = UggTheme.typography.overline, color = colors.accent)
                Text(text = story.title, style = UggTheme.typography.displayXL, color = colors.content)
                Text(text = story.intro, style = UggTheme.typography.body, color = colors.contentSecondary)
            }
        }

        item(key = "photo") {
            UggPhotoPlaceholder(
                modifier = Modifier
                    .padding(horizontal = horizontal, vertical = UggTheme.spacing.s)
                    .fillMaxWidth()
                    .height(280.dp),
            )
        }

        if (story.steps.isNotEmpty()) {
            item(key = "steps-title") {
                Text(
                    text = story.stepsTitle,
                    style = UggTheme.typography.displayM,
                    color = colors.content,
                    modifier = Modifier.padding(horizontal = horizontal).padding(top = UggTheme.spacing.m, bottom = 18.dp),
                )
            }
            itemsIndexed(story.steps, key = { _, step -> step.number }) { index, step ->
                StepRow(step = step, isLast = index == story.steps.lastIndex)
            }
        }

        story.highlight?.let { highlight ->
            item(key = "highlight") { HighlightCard(highlight) }
        }

        item(key = "cta") {
            UggButton(
                text = "Выбрать свою пару",
                onClick = onChooseModel,
                modifier = Modifier
                    .padding(horizontal = horizontal, vertical = UggTheme.spacing.xl)
                    .fillMaxWidth(),
            )
        }
    }
}

/** A timeline row: numbered dot on a rail that runs down to the next step. */
@Composable
private fun StepRow(step: StoryStep, isLast: Boolean) {
    val colors = UggTheme.colors

    Row(
        modifier = Modifier
            .padding(horizontal = UggTheme.spacing.screenHorizontal)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.m),
    ) {
        Column(modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(UggTheme.shapes.pill)
                    .background(colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = step.number, style = UggTheme.typography.caption, color = colors.accentStrong)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .weight(1f)
                        .background(colors.outline),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = UggTheme.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = step.title, style = UggTheme.typography.heading, color = colors.content)
            Text(text = step.description, style = UggTheme.typography.bodySmall, color = colors.contentSecondary)
            Spacer(Modifier.height(4.dp))
            UggPhotoPlaceholder(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = UggTheme.shapes.medium,
            )
        }
    }
}

@Composable
private fun HighlightCard(highlight: StoryHighlight) {
    val colors = UggTheme.colors

    Column(
        modifier = Modifier
            .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs)
            .fillMaxWidth()
            .clip(UggTheme.shapes.extraLarge)
            .background(Brush.verticalGradient(listOf(colors.info, lerp(colors.info, colors.surfaceInverse, 0.35f))))
            .padding(UggTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = highlight.kicker.uppercase(),
            style = UggTheme.typography.overline,
            color = colors.contentInverse.copy(alpha = 0.8f),
        )
        Text(text = highlight.title, style = UggTheme.typography.displayL, color = colors.contentInverse)
        Text(
            text = highlight.body,
            style = UggTheme.typography.bodySmall,
            color = colors.contentInverse.copy(alpha = 0.85f),
        )
    }
}
