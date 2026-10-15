package com.ugg.client.infrastructure.firebase.firestore.web

import kotlin.js.Promise

internal external interface JsDocumentSnapshot : JsAny {
    val id: String
}

internal external interface JsQuerySnapshot : JsAny {
    val docs: JsArray<JsDocumentSnapshot>
}

internal external interface JsTransaction : JsAny {
    fun get(reference: JsAny): Promise<JsAny?>
    fun set(reference: JsAny, data: JsAny): JsAny
}

/** A settled Promise: never rejects, so a JS error turns into a Kotlin exception we choose. */
internal external interface JsOutcome : JsAny {
    val ok: Boolean
    val value: JsAny?
    val code: String
    val message: String
}

internal fun settle(promise: Promise<JsAny?>): Promise<JsOutcome> = js(
    """promise.then(
        (v) => ({ ok: true, value: v === undefined ? null : v, code: '', message: '' }),
        (e) => ({ ok: false, value: null, code: String((e && e.code) || ''), message: String((e && e.message) || e) })
    )""",
)

internal fun errorCode(error: JsAny): String = js("String((error && error.code) || '')")

internal fun errorMessage(error: JsAny): String = js("String((error && error.message) || error)")

internal fun invokeFunction(callback: JsAny): Unit = js("callback()")

internal fun mergeOptions(merge: Boolean): JsAny = js("({ merge: merge })")

/**
 * The document's data as JSON, `null` when it does not exist. Timestamps become
 * `{"__ms": epochMillis}` and GeoPoints `{"__geo": [lat, lng]}`; a server timestamp not yet
 * confirmed reads as the local estimate, like `ServerTimestampBehavior.ESTIMATE` on Android.
 */
internal fun snapshotJson(snapshot: JsAny): String? = js(
    """(() => {
        if (!snapshot.exists()) return null;
        const convert = (v) => {
            if (v === null || v === undefined) return null;
            if (Array.isArray(v)) return v.map(convert);
            if (typeof v === 'object') {
                if (typeof v.toMillis === 'function') return { __ms: v.toMillis() };
                if (typeof v.latitude === 'number' && typeof v.longitude === 'number' && typeof v.isEqual === 'function') {
                    return { __geo: [v.latitude, v.longitude] };
                }
                const out = {};
                for (const key of Object.keys(v)) out[key] = convert(v[key]);
                return out;
            }
            return v;
        };
        return JSON.stringify(convert(snapshot.data({ serverTimestamps: 'estimate' })));
    })()""",
)

/** The reverse of [snapshotJson] for writing: `{"__serverTimestamp": true}` and `{"__geo": […]}`. */
internal fun parseForFirestore(
    json: String,
    serverTimestamp: JsAny,
    geoPoint: (Double, Double) -> JsAny,
): JsAny? = js(
    """JSON.parse(json, (key, v) => {
        if (v !== null && typeof v === 'object' && !Array.isArray(v)) {
            if (v.__serverTimestamp === true) return serverTimestamp;
            if (Array.isArray(v.__geo)) return geoPoint(v.__geo[0], v.__geo[1]);
        }
        return v;
    })""",
)
