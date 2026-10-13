package com.ugg.client.kit.core.data

import com.ugg.client.infrastructure.api.auth.services.AuthService
import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.auth.createPhoneAuthService
import com.ugg.client.infrastructure.firebase.firestore.createCatalogService
import com.ugg.client.infrastructure.firebase.firestore.createOrderService
import com.ugg.client.infrastructure.firebase.firestore.createUserService
import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.infrastructure.firebase.firestore.services.OrderService
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.core.data.scopes.TemplateCoreScope
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton

val coreDataModule get() = DI.Module("core-data") {
    bind<AuthService>() with scoped(TemplateCoreScope).singleton { AuthService(instance()) }

    // One instance for the whole app: the splash, the sign-in screens and the profile all watch
    // the same signed-in user.
    bind<PhoneAuthService>() with scoped(TemplateCoreScope).singleton { createPhoneAuthService() }

    // Firestore: the catalog and screen texts, the user's profile and addresses, the orders.
    bind<CatalogService>() with scoped(TemplateCoreScope).singleton { createCatalogService() }
    bind<UserService>() with scoped(TemplateCoreScope).singleton { createUserService() }
    bind<OrderService>() with scoped(TemplateCoreScope).singleton { createOrderService() }
}
