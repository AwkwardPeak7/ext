package keiyoushi.extension.all.mangadex.dto

import keiyoushi.extension.all.mangadex.MDConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(MDConstants.scanlationGroup)
data class ScanlationGroupDto(override val attributes: ScanlationGroupAttributes? = null) : EntityDto()

@Serializable
data class ScanlationGroupAttributes(val name: String) : AttributesDto()
