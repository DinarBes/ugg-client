package com.ugg.client.kit.stores.presentation.internal.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ugg.client.kit.stores.domain.entities.Store

// TODO: Yandex MapKit on iOS (YandexMapsMobile pod) once the iOS app is in the repository.
@Composable
internal actual fun StoresMap(
    stores: List<Store>,
    selectedStoreId: String?,
    onStoreTap: (String) -> Unit,
    modifier: Modifier,
) {
    StoresMapPlaceholder(message = "Карта появится в iOS-версии чуть позже", modifier = modifier)
}
