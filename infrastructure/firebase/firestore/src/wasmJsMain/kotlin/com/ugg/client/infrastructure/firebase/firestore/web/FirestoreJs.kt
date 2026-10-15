@file:JsModule("firebase/firestore")

package com.ugg.client.infrastructure.firebase.firestore.web

import kotlin.js.Promise

// The few functions of the Firebase JS SDK (modular API, npm `firebase`) the store uses. Values
// cross as JsAny: the shapes are read through the external interfaces in FirestoreJsTypes.kt.

internal external fun getFirestore(): JsAny

internal external fun doc(firestore: JsAny, path: String): JsAny

internal external fun collection(firestore: JsAny, path: String): JsAny

internal external fun getDoc(reference: JsAny): Promise<JsAny?>

internal external fun getDocs(query: JsAny): Promise<JsAny?>

internal external fun setDoc(reference: JsAny, data: JsAny, options: JsAny): Promise<JsAny?>

internal external fun deleteDoc(reference: JsAny): Promise<JsAny?>

/** Works for a document and for a query; returns the unsubscribe function. */
internal external fun onSnapshot(reference: JsAny, onNext: (JsAny) -> Unit, onError: (JsAny) -> Unit): JsAny

internal external fun runTransaction(firestore: JsAny, updateFunction: (JsAny) -> Promise<JsAny?>): Promise<JsAny?>

/** `query(q, a, b)` is the same as `query(query(q, a), b)`, so constraints are added one by one. */
internal external fun query(query: JsAny, constraint: JsAny): JsAny

internal external fun where(field: String, operator: String, value: JsAny?): JsAny

internal external fun orderBy(field: String, direction: String): JsAny

internal external fun limit(count: Int): JsAny

internal external fun serverTimestamp(): JsAny

internal external class GeoPoint(latitude: Double, longitude: Double) : JsAny
