package com.ugg.client.kit.navigation.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.ugg.client.kit.auth.domain.entities.AuthFlow
import com.ugg.client.kit.auth.presentation.external.view.InitiateScreen
import com.ugg.client.kit.auth.presentation.external.view.OtpScreen
import com.ugg.client.kit.auth.presentation.external.view.RegistrationScreen
import com.ugg.client.kit.care.domain.entities.CareFlow
import com.ugg.client.kit.care.presentation.external.view.CareScreen
import com.ugg.client.kit.cart.domain.entities.CartFlow
import com.ugg.client.kit.cart.presentation.external.view.CartScreen
import com.ugg.client.kit.core.presentation.external.components.UggCenteredBox
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.navigation.domain.external.entities.NavigationEvent
import com.ugg.client.kit.navigation.presentation.internal.view.MainTab
import com.ugg.client.kit.navigation.presentation.internal.view.MainTabsView
import com.ugg.client.kit.onboarding.presentation.external.view.OnboardingScreen
import com.ugg.client.kit.profile.domain.entities.ProfileFlow
import com.ugg.client.kit.profile.presentation.external.view.AddressFormScreen
import com.ugg.client.kit.profile.presentation.external.view.AddressesScreen
import com.ugg.client.kit.profile.presentation.external.view.NameScreen
import com.ugg.client.kit.profile.presentation.external.view.SettingsScreen
import com.ugg.client.kit.profile.presentation.external.view.SizeScreen
import com.ugg.client.kit.splash.domain.entities.SplashDestination
import com.ugg.client.kit.splash.domain.entities.SplashFlow
import com.ugg.client.kit.splash.presentation.external.view.SplashScreen
import com.ugg.client.kit.splash.presentation.external.view.models.SplashViewModel
import com.ugg.client.kit.story.domain.entities.StoryFlow
import com.ugg.client.kit.story.presentation.external.view.StoryScreen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.kodein.di.compose.localDI
import org.kodein.di.compose.viewmodel.rememberViewModel
import org.kodein.di.instance

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun NavigationScreen() {
    val di = localDI()
    val navigationContext: NavigationContext by di.instance()

    // Held by the host, not by the splash entry: it decides the first destination and keeps
    // listening for authorization events long after the splash leaves the back stack.
    val splashViewModel: SplashViewModel by rememberViewModel()
    val splashState by splashViewModel.state.collectAsState()

    val config = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclass(SplashDestination.Splash::class, SplashDestination.Splash.serializer())
                subclass(SplashFlow.Onboarding::class, SplashFlow.Onboarding.serializer())
                subclass(SplashFlow.Auth::class, SplashFlow.Auth.serializer())
                subclass(SplashFlow.Launch::class, SplashFlow.Launch.serializer())
                subclass(SplashFlow.Registration::class, SplashFlow.Registration.serializer())
                subclass(AuthFlow.Otp::class, AuthFlow.Otp.serializer())
                subclass(StoryFlow.Story::class, StoryFlow.Story.serializer())
                subclass(CareFlow.Care::class, CareFlow.Care.serializer())
                subclass(CartFlow.Cart::class, CartFlow.Cart.serializer())
                subclass(ProfileFlow.Settings::class, ProfileFlow.Settings.serializer())
                subclass(ProfileFlow.Name::class, ProfileFlow.Name.serializer())
                subclass(ProfileFlow.Size::class, ProfileFlow.Size.serializer())
                subclass(ProfileFlow.Addresses::class, ProfileFlow.Addresses.serializer())
                subclass(ProfileFlow.AddressForm::class, ProfileFlow.AddressForm.serializer())
            }
        }
    }

    val backStack = rememberNavBackStack(config, SplashDestination.Splash)
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }

    LaunchedEffect(Unit) {
        navigationContext.events.collect { event ->
            when (event) {
                is NavigationEvent.OnNavigate -> {
                    with(event) { backStack.block() }
                    backStack.add(event.route)
                }
                is NavigationEvent.OnBack -> backStack.removeLastOrNull()
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<SplashDestination.Splash> {
                Readable {
                    SplashScreen(error = splashState.error, onRetry = { splashViewModel.fetch() })
                }
            }
            entry<SplashFlow.Onboarding> {
                // Full-bleed background; the screen holds its own copy to a readable width.
                OnboardingScreen(
                    onStart = { navigationContext.tryNavigate(SplashFlow.Launch) { clear() } },
                    onLogin = { navigationContext.tryNavigate(SplashFlow.Auth) { clear() } },
                )
            }
            entry<SplashFlow.Auth> {
                Readable {
                    // Sign-in is optional: back and «Продолжить без входа» both land in the app as a guest.
                    InitiateScreen(
                        onBack = { navigationContext.tryNavigate(SplashFlow.Launch) { clear() } },
                        onSkip = { navigationContext.tryNavigate(SplashFlow.Launch) { clear() } },
                    )
                }
            }
            entry<SplashFlow.Launch> {
                MainTabsView(
                    selected = selectedTab,
                    onSelect = { selectedTab = it },
                    onOpenStory = { navigationContext.tryNavigate(StoryFlow.Story) },
                    onOpenCare = { navigationContext.tryNavigate(CareFlow.Care) },
                    onOpenCart = { navigationContext.tryNavigate(CartFlow.Cart) },
                    // After the code is confirmed the splash brings the app back to Launch; the
                    // selected tab is kept, so the user lands on the profile, now signed in.
                    onSignIn = { navigationContext.tryNavigate(SplashFlow.Auth) },
                    onOpenSize = { navigationContext.tryNavigate(ProfileFlow.Size) },
                    onOpenAddresses = { navigationContext.tryNavigate(ProfileFlow.Addresses) },
                    onOpenSettings = { navigationContext.tryNavigate(ProfileFlow.Settings) },
                )
            }
            entry<StoryFlow.Story> {
                Readable {
                    StoryScreen(
                        onBack = { navigationContext.back() },
                        onChooseModel = {
                            selectedTab = MainTab.Model
                            navigationContext.back()
                        },
                    )
                }
            }
            entry<CareFlow.Care> {
                Readable {
                    // TODO: «Написать в мастерскую» once the chat with the workshop exists.
                    CareScreen(onBack = { navigationContext.back() })
                }
            }
            // The cart and the tabs lay themselves out for wide windows.
            entry<CartFlow.Cart> {
                CartScreen(
                    onBack = { navigationContext.back() },
                    onChooseModel = {
                        selectedTab = MainTab.Model
                        navigationContext.back()
                    },
                    // After the code the splash brings the app back to Launch; the cart keeps its lines.
                    onSignIn = { navigationContext.tryNavigate(SplashFlow.Auth) },
                    onOpenOrders = {
                        selectedTab = MainTab.Profile
                        navigationContext.back()
                    },
                )
            }
            entry<SplashFlow.Registration> {
                Readable {
                    // Pushed over Launch by the splash after the first sign-in; closing it pops back.
                    RegistrationScreen()
                }
            }
            entry<ProfileFlow.Settings> {
                Readable {
                    SettingsScreen(onBack = { navigationContext.back() })
                }
            }
            entry<ProfileFlow.Name> {
                Readable {
                    NameScreen(onBack = { navigationContext.back() })
                }
            }
            entry<ProfileFlow.Size> {
                Readable {
                    SizeScreen(onBack = { navigationContext.back() })
                }
            }
            entry<ProfileFlow.Addresses> {
                Readable {
                    AddressesScreen(onBack = { navigationContext.back() })
                }
            }
            entry<ProfileFlow.AddressForm> {
                Readable {
                    AddressFormScreen(addressId = it.addressId, onBack = { navigationContext.back() })
                }
            }
            entry<AuthFlow.Otp> {
                Readable {
                    OtpScreen(phone = it.phone, sessionId = it.sessionId, onBack = { navigationContext.back() })
                }
            }
        }
    )
}

/**
 * Single-column screens (forms, articles, sign-in) are held to a readable width on a wide
 * window instead of stretching across it. Only for screens on the plain app background: the
 * margins are painted in it. A screen with its own full-screen background (the onboarding)
 * limits its content itself, so the background is never cut.
 */
@Composable
private fun Readable(content: @Composable () -> Unit) =
    UggCenteredBox(maxWidth = UggTheme.spacing.readableWidth, content = content)
