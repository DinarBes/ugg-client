package com.ugg.client.kit.story.presentation.external

import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.story.data.useCases.MockStoryUseCase
import com.ugg.client.kit.story.data.useCases.SharedStoryUseCase
import com.ugg.client.kit.story.domain.useCases.StoryUseCase
import com.ugg.client.kit.story.presentation.external.view.models.StoryViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.on

val storyModule: DI.Module
    get() = DI.Module("story.module") {
        bind<StoryViewModel>() with factory {
            StoryViewModel(instance())
        }

        bind<StoryUseCase>() with factory {
            // PRODUCTION reads Firestore (CatalogService, scoped in core/data); MOCK keeps the bundled content.
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedStoryUseCase(on(core).instance())
                AppEnvironment.MOCK -> MockStoryUseCase()
            }
        }
    }
