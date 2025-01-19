package keiyoushi.extension.en.mangamo.dto

import kotlinx.serialization.Serializable

@Serializable
class PageDto(
    val id: Int,
    val pageNumber: Int,
    val uri: String,
)
