package com.ugg.client.kit.stores.presentation.internal.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggPhotoPlaceholder
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.stores.domain.entities.Store

/**
 * Map with a marker per store. Android draws Yandex MapKit; iOS shows [StoresMapPlaceholder]
 * until the iOS app gets the MapKit pod.
 */
@Composable
internal expect fun StoresMap(
    stores: List<Store>,
    selectedStoreId: String?,
    onStoreTap: (String) -> Unit,
    modifier: Modifier = Modifier,
)

/** Shown where no real map is available: on iOS for now, and on Android without an API key. */
@Composable
internal fun StoresMapPlaceholder(message: String, modifier: Modifier = Modifier) {
    val colors = UggTheme.colors

    UggPhotoPlaceholder(
        modifier = modifier,
        top = colors.surface,
        bottom = colors.surfaceVariant,
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(UggTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = UggIcons.Pin, contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(28.dp))
            }
            Text(
                text = message,
                style = UggTheme.typography.bodySmall,
                color = colors.contentSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
