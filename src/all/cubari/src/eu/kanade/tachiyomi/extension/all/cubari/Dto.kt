package eu.kanade.tachiyomi.extension.all.cubari

import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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
