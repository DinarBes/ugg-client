package com.ugg.client.infrastructure.firebase.firestore

import com.ugg.client.infrastructure.firebase.firestore.internal.DocumentStore
import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.infrastructure.firebase.firestore.services.FirestoreCatalogService
import com.ugg.client.infrastructure.firebase.firestore.services.FirestoreOrderService
import com.ugg.client.infrastructure.firebase.firestore.services.FirestoreUserService
import com.ugg.client.infrastructure.firebase.firestore.services.OrderService
import com.ugg.client.infrastructure.firebase.firestore.services.UserService

/** Firestore on Android; on iOS every request fails with `Unavailable` for now. */
internal expect fun createDocumentStore(): DocumentStore

fun createCatalogService(): CatalogService = FirestoreCatalogService(createDocumentStore())
fun createUserService(): UserService = FirestoreUserService(createDocumentStore())
fun createOrderService(): OrderService = FirestoreOrderService(createDocumentStore())
