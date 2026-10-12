package com.ugg.client.kit.core.data

import com.ugg.client.infrastructure.api.auth.services.AuthService
import com.ugg.client.kit.core.data.scopes.TemplateCoreScope
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton

val coreDataModule get() = DI.Module("core-data") {
    bind<AuthService>() with scoped(TemplateCoreScope).singleton { AuthService(instance()) }
}