package keiyoushi.extension.all.namicomi.dto

import keiyoushi.extension.all.namicomi.NamiComiConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(NamiComiConstants.organization)
class OrganizationDto(override val attributes: OrganizationAttributesDto? = null) : EntityDto()

@Serializable
class OrganizationAttributesDto(val name: String) : AttributesDto
