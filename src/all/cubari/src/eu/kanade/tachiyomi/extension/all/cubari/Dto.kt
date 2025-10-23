package eu.kanade.tachiyomi.extension.all.cubari

import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
class LocalStorage(
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
    val coverUrl: String? = null,
    val title: String,
    val timestamp: Long,
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
