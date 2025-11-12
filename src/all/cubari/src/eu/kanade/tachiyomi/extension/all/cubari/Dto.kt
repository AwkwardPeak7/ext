package eu.kanade.tachiyomi.extension.all.cubari

import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
class WebViewLocalStorageItems(
    val wireClient: WireClient?,
    val items: List<RSManga>,
)

/**
 * remote storage manga
 */
@Serializable
class RSManga(
    val slug: String,
    val source: String,
    val coverUrl: String,
    val url: String,
    val title: String,
    val timestamp: Long,
    val pinned: Boolean,
    val chapters: List<String> = emptyList(),
    @SerialName("@context") val context: String = "http://remotestorage.io/spec/modules/cubari/series",
) {
    fun toSManga() = SManga.create().apply {
        url = "/read/$source/$slug/"
        title = this@RSManga.title
        thumbnail_url = coverUrl
    }
}

@Serializable
class WireClient(
    val href: String,
    val token: String,
)

@Serializable
class RemoteStorageResponse(
    val items: Map<String, JsonObject>,
)

@Serializable
class WebViewLocalStorage(
    val path: String,
    val local: Local,
    val common: Map<String, String> = emptyMap(),
) {
    @Serializable
    class Local(
        val body: String,
        val contentType: String = "application/json; charset=UTF-8",
    )
}

@Serializable
class CubariManga(
    val title: String,
    val description: String,
    val author: String,
    val artist: String,
    val groups: Map<String, String>,
    val cover: String,
    val chapters: Map<String, CubariChapter>,
) {
    fun toSManga() = SManga.create().apply {
        title = this@CubariManga.title
        author = this@CubariManga.author.takeIf { it.isNotBlank() && it != "Unknown" }
        artist = this@CubariManga.artist.takeIf { it.isNotBlank() && it != "Unknown" }
        description = this@CubariManga.description.takeIf { it.isNotBlank() && it != "No description." }
            ?.substringBefore("Tags: ")
        genre = this@CubariManga.description.substringAfter("Tags: ", "")
            .takeIf { it.isNotBlank() }
        thumbnail_url = cover
        initialized = true
    }
}

@Serializable
class CubariChapter(
    val title: String? = null,
    @JsonNames("date", "release_date")
    @Serializable(with = DateTransformer::class)
    val date: Long = 0L,
    val volume: String,
    @Serializable(with = GroupTransFormer::class)
    val groups: Map<String, List<String>>,
)

object DateTransformer : JsonTransformingSerializer<Long>(Long.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return when (element) {
            is JsonPrimitive -> {
                val dateStr = element.content
                val date = format.tryParse(dateStr)
                JsonPrimitive(date)
            }

            is JsonObject -> {
                val inner = element.values.first().jsonPrimitive
                val date = if (inner.doubleOrNull != null) {
                    inner.double.toLong()
                } else if (inner.longOrNull != null) {
                    inner.long
                } else {
                    inner.content.toLong()
                }

                JsonPrimitive(date * 1000)
            }

            else -> JsonPrimitive(0L)
        }
    }
}

object GroupTransFormer : JsonTransformingSerializer<Map<String, List<String>>>(
    MapSerializer(String.serializer(), ListSerializer(String.serializer())),
) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val json = element as JsonObject

        return buildJsonObject {
            json.forEach { (k, v) ->
                val value = when (v) {
                    is JsonPrimitive -> JsonArray(listOf(v))
                    is JsonArray -> {
                        val values = v.mapNotNull {
                            when (it) {
                                is JsonObject -> it["src"]!!.jsonPrimitive
                                is JsonPrimitive -> it
                                else -> null
                            }
                        }
                        JsonArray(values)
                    }
                    else -> JsonArray(emptyList())
                }
                put(k, value)
            }
        }
    }
}

private val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

@Serializable(with = PageTransformer::class)
class CubariPage(
    val src: String,
)

object PageTransformer : KSerializer<CubariPage> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CubariPage") {
        element<String>("src")
    }

    override fun deserialize(decoder: Decoder): CubariPage {
        val src = when (val jsonElement = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonPrimitive -> jsonElement.content
            is JsonObject -> jsonElement["src"]!!.jsonPrimitive.content
            else -> throw SerializationException("Unexpected JSON type")
        }
        return CubariPage(src)
    }

    override fun serialize(encoder: Encoder, value: CubariPage) {
        encoder.encodeSerializableValue(CubariPage.serializer(), value)
    }
}
