package com.ugg.client.infrastructure.firebase.firestore.internal

import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Bridges Firestore documents and the `@Serializable` DTOs through JSON, so the DTOs are plain
 * Kotlin and the mapping is the same on every platform.
 *
 * A [GeoPointValue] travels as `{ "latitude": …, "longitude": … }`; the document id is put under
 * `"id"` when reading and dropped when writing.
 */
internal object FirestoreJson {
    const val ID = "id"
    private const val LATITUDE = "latitude"
    private const val LONGITUDE = "longitude"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }

    fun <T> decode(serializer: KSerializer<T>, document: RawDocument): T = try {
        json.decodeFromJsonElement(serializer, toJson(document.data + (ID to document.id)))
    } catch (e: SerializationException) {
        throw FirestoreException(FirestoreException.Reason.Malformed, e)
    } catch (e: IllegalArgumentException) {
        throw FirestoreException(FirestoreException.Reason.Malformed, e)
    }

    /** The DTO as Firestore data, without the `"id"` key (the id is the document path). */
    @Suppress("UNCHECKED_CAST")
    fun <T> encode(serializer: KSerializer<T>, value: T): Map<String, Any?> =
        (fromJson(json.encodeToJsonElement(serializer, value)) as Map<String, Any?>) - ID

    fun toJson(value: Any?): JsonElement = when (value) {
        null -> JsonNull
        is String -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is Int -> JsonPrimitive(value)
        is Long -> JsonPrimitive(value)
        is Double -> JsonPrimitive(value)
        is Float -> JsonPrimitive(value)
        is GeoPointValue -> JsonObject(
            mapOf(LATITUDE to JsonPrimitive(value.latitude), LONGITUDE to JsonPrimitive(value.longitude)),
        )
        is Map<*, *> -> JsonObject(value.entries.associate { (k, v) -> k.toString() to toJson(v) })
        is List<*> -> JsonArray(value.map(::toJson))
        else -> JsonPrimitive(value.toString())
    }

    fun fromJson(element: JsonElement): Any? = when (element) {
        is JsonNull -> null
        is JsonPrimitive -> when {
            element.isString -> element.content
            else -> element.booleanOrNull ?: element.longOrNull ?: element.doubleOrNull
        }
        is JsonObject -> element.toGeoPoint() ?: element.mapValues { (_, v) -> fromJson(v) }
        is JsonArray -> element.map(::fromJson)
    }

    private fun JsonObject.toGeoPoint(): GeoPointValue? {
        if (keys != setOf(LATITUDE, LONGITUDE)) return null
        val latitude = (get(LATITUDE) as? JsonPrimitive)?.doubleOrNull ?: return null
        val longitude = (get(LONGITUDE) as? JsonPrimitive)?.doubleOrNull ?: return null
        return GeoPointValue(latitude, longitude)
    }
}

internal fun <T> RawDocument.decode(serializer: KSerializer<T>): T = FirestoreJson.decode(serializer, this)
