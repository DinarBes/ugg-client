package com.ugg.client.kit.story.presentation.external

import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.story.data.useCases.MockStoryUseCase
import com.ugg.client.kit.story.domain.useCases.StoryUseCase
import com.ugg.client.kit.story.presentation.external.view.models.StoryViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance

val storyModule: DI.Module
    get() = DI.Module("story.module") {
        bind<StoryViewModel>() with factory {
            StoryViewModel(instance())
        }

        bind<StoryUseCase>() with factory {
            // PRODUCTION gets its own implementation once the content moves to Firebase.
            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> MockStoryUseCase()
                AppEnvironment.MOCK -> MockStoryUseCase()
            }
        }
    }
