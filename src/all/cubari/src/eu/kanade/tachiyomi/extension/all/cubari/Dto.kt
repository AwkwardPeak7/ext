package eu.kanade.tachiyomi.extension.all.cubari

import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
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
        url = "/read/$source/$slug"
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
)

@Serializable(with = CubariChapterSerializer::class)
class CubariChapter(
    val title: String,
    val date: Long,
    val url: String
)

object CubariChapterSerializer : KSerializer<CubariChapter> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("CubariChapter") {
            element<String>("title")
            element<Long>("date")
        }

    override fun deserialize(decoder: Decoder): CubariChapter {
        val json = (decoder as? JsonDecoder)?.decodeJsonElement() as? JsonObject
            ?: error("Expected JsonObject")

        val title = json["title"]!!.jsonPrimitive.content

        val date = when {
            // case: release_date = { "1": 1234567 }
            "release_date" in json -> {
                val inner = json["release_date"]!!.jsonObject
                    .values.first().jsonPrimitive

                if (inner.isString) {
                    inner.content.toLong()
                } else {
                    inner.double.toLong()
                }
            }

            // case: date = "2024-12-23"
            "date" in json -> {
                val dateStr = json["date"]!!.jsonPrimitive.content
                format.tryParse(dateStr)
            }

            else -> error("No date field found")
        }

        return CubariChapter(title, date)
    }

    override fun serialize(encoder: Encoder, value: CubariChapter) {
        val jsonEncoder = encoder as? JsonEncoder ?: error("Expected JsonEncoder")
        val obj = buildJsonObject {
            put("title", value.title)
            put("date", value.date)
        }
        jsonEncoder.encodeJsonElement(obj)
    }
}

private val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

