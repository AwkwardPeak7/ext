package keiyoushi.utils

import kotlinx.serialization.json.*

operator fun JsonElement?.get(key: String): JsonElement? =
    this?.jsonObjectOrNull?.get(key)

operator fun JsonElement?.get(index: Int): JsonElement? =
    this?.jsonArrayOrNull?.getOrNull(index)

val JsonElement?.jsonObjectOrNull: JsonObject?  get() = this as? JsonObject
val JsonElement?.jsonArrayOrNull:  JsonArray?   get() = this as? JsonArray
val JsonElement?.jsonPrimitiveOrNull: JsonPrimitive? get() = this as? JsonPrimitive

val JsonElement?.string: String?
    get() = (this as? JsonPrimitive)?.takeIf { it.isString }?.content

val JsonElement?.content: String?
    get() = (this as? JsonPrimitive)?.content

val JsonElement?.int:     Int?     get() = (this as? JsonPrimitive)?.intOrNull
val JsonElement?.long:    Long?    get() = (this as? JsonPrimitive)?.longOrNull
val JsonElement?.double:  Double?  get() = (this as? JsonPrimitive)?.doubleOrNull
val JsonElement?.float:   Float?   get() = (this as? JsonPrimitive)?.floatOrNull
val JsonElement?.boolean: Boolean? get() = (this as? JsonPrimitive)?.booleanOrNull
val JsonElement?.isNull:  Boolean  get() = this == null || this is JsonNull

fun JsonElement?.path(vararg segments: String): JsonElement? {
    var current: JsonElement? = this
    for (segment in segments) {
        current = when (current) {
            null -> return null
            is JsonArray if segment.all { it.isDigit() } -> current[segment.toInt()]
            else -> current[segment]
        }
    }
    return current
}

fun JsonElement?.dotPath(dotSeparated: String): JsonElement? =
    path(*dotSeparated.split(".").toTypedArray())

fun JsonElement?.forEachElement(block: (JsonElement) -> Unit) =
    this?.jsonArrayOrNull?.forEach(block)

fun <T> JsonElement?.mapElements(transform: (JsonElement) -> T): List<T> =
    this?.jsonArrayOrNull?.map(transform) ?: emptyList()

fun JsonElement?.filterElements(predicate: (JsonElement) -> Boolean): List<JsonElement> =
    this?.jsonArrayOrNull?.filter(predicate) ?: emptyList()

val JsonElement?.strings: List<String>
    get() = this?.jsonArrayOrNull?.mapNotNull { it.string } ?: emptyList()

val JsonElement?.ints: List<Int>
    get() = this?.jsonArrayOrNull?.mapNotNull { it.int } ?: emptyList()

val JsonElement?.keys: Set<String>
    get() = this?.jsonObjectOrNull?.keys ?: emptySet()

fun JsonElement?.containsKey(key: String): Boolean =
    this?.jsonObjectOrNull?.containsKey(key) == true

val JsonElement?.entries: Set<Map.Entry<String, JsonElement>>
    get() = this?.jsonObjectOrNull?.entries ?: emptySet()

fun JsonObject.put(key: String, value: Any?): JsonObject =
    JsonObject(toMutableMap().also { it[key] = value.toDynamicJsonElement() })

fun JsonObject.remove(key: String): JsonObject =
    JsonObject(toMutableMap().also { it.remove(key) })

operator fun JsonArray.plus(element: Any?): JsonArray =
    JsonArray(toMutableList().also { it.add(element.toDynamicJsonElement()) })

fun jsonObjectOf(vararg pairs: Pair<String, Any?>): JsonObject =
    JsonObject(pairs.associate { (k, v) -> k to v.toDynamicJsonElement() })

fun jsonArrayOf(vararg elements: Any?): JsonArray =
    JsonArray(elements.map { it.toDynamicJsonElement() })

private fun Any?.toDynamicJsonElement(): JsonElement = when (this) {
    null            -> JsonNull
    is JsonElement  -> this
    is String       -> JsonPrimitive(this)
    is Boolean      -> JsonPrimitive(this)
    is Number       -> JsonPrimitive(this)
    is Map<*, *>    -> JsonObject(entries.associate { (k, v) -> k.toString() to v.toDynamicJsonElement() })
    is Iterable<*>  -> JsonArray(map { it.toDynamicJsonElement() })
    is Array<*>     -> JsonArray(map { it.toDynamicJsonElement() })
    else -> {
        if (this::class.java.isArray) {
            val length = java.lang.reflect.Array.getLength(this)
            JsonArray((0 until length).map { java.lang.reflect.Array.get(this, it).toDynamicJsonElement() })
        } else {
            JsonPrimitive(toString())
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> JsonElement?.typed(): T? {
    val prim = (this as? JsonPrimitive) ?: return null
    return when (T::class) {
        String::class       -> prim.takeIf { it.isString }?.content as T?
        Boolean::class      -> prim.booleanOrNull as T?
        Int::class          -> prim.intOrNull as T?
        Long::class         -> prim.longOrNull as T?
        Float::class        -> prim.floatOrNull as T?
        Double::class       -> prim.doubleOrNull as T?
        Number::class       -> prim.doubleOrNull as T?
        JsonPrimitive::class -> prim as T
        else                -> null
    }
}

inline fun <reified T> JsonObject.get(key: String): T? =
    get(key)?.typed<T>()

inline fun <reified T> JsonArray.get(index: Int): T? =
    getOrNull(index)?.typed<T>()
