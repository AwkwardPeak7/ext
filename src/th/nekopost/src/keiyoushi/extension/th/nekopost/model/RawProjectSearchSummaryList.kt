package keiyoushi.extension.th.nekopost.model

import kotlinx.serialization.Serializable

@Serializable
data class RawProjectSearchSummaryList(
    val listProject: List<RawProjectSearchSummary>,
    val totalRecord: String,
)
