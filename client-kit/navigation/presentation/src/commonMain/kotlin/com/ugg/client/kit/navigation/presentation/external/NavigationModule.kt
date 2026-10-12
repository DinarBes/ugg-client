package com.ugg.client.kit.navigation.presentation.external

import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val navigationModule: DI.Module
    get() = DI.Module("navigation.module") {
        bind<NavigationContext>() with singleton {
            NavigationContext()
        }
    }
