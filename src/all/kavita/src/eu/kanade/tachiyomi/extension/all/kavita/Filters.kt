package eu.kanade.tachiyomi.extension.all.kavita

import eu.kanade.tachiyomi.source.model.Filter

class SortFilter :
    Filter.Sort(
        "Sort",
        SORTS.map { it.first }.toTypedArray(),
        Selection(7, false),
    ) {
    val sortOption
        get() = SortOptionDto(SORTS[state!!.index].second, state!!.ascending)
}

class CatalogFilter : Filter.Select<String>("Catalog", arrayOf("All series", "Want to Read")) {
    val wantToRead
        get() = state == 1
}

class SmartFilterFilter(private val filters: List<SmartFilterDto>) : Filter.Select<String>("Smart Filter", (listOf("None") + filters.map { it.name }).toTypedArray()) {
    val selected
        get() = filters.getOrNull(state - 1)
}

class ReadingStatusFilter : Filter.Select<String>("Reading status", arrayOf("Any", "Unread", "In progress", "Read"))

class TextValueFilter(name: String) : Filter.Text(name)

class ReleaseYearFilter :
    Filter.Group<TextValueFilter>(
        "Release year",
        listOf(TextValueFilter("Minimum"), TextValueFilter("Maximum")),
    ) {
    val minimum
        get() = state[0].state.toIntOrNull()

    val maximum
        get() = state[1].state.toIntOrNull()
}

class MinimumRatingFilter :
    Filter.Select<String>(
        "Minimum rating",
        arrayOf("Any", "1 star", "2 stars", "3 stars", "4 stars", "5 stars"),
    )

class ValueOption(name: String, val value: String) : Filter.TriState(name)

class MetadataFilterGroup(
    name: String,
    val field: Int,
    values: List<FilterValue>,
) : Filter.Group<ValueOption>(name, values.map { ValueOption(it.label, it.value) }) {
    val included
        get() = state.filter { it.state == Filter.TriState.STATE_INCLUDE }.map { it.value }

    val excluded
        get() = state.filter { it.state == Filter.TriState.STATE_EXCLUDE }.map { it.value }
}

class FormatOption(name: String, val value: Int) : Filter.CheckBox(name)

class FormatFilter :
    Filter.Group<FormatOption>(
        "Formats",
        listOf(
            FormatOption("Image", IMAGE),
            FormatOption("Archive", ARCHIVE),
            FormatOption("EPUB (image-based only)", EPUB),
            FormatOption("PDF", PDF),
            FormatOption("Unknown", UNKNOWN),
        ),
    ) {
    val selectedValues
        get() = state.filter { it.state }.map { it.value }
}

class PublicationStatusOption(name: String, val value: String) : Filter.CheckBox(name)

class PublicationStatusFilter(statuses: List<FilterValue>) :
    Filter.Group<PublicationStatusOption>(
        "Publication status",
        statuses.map { PublicationStatusOption(it.label, it.value) },
    ) {
    val selectedValues
        get() = state.filter { it.state }.map { it.value }
}

class PersonOption(person: PersonDto) : Filter.CheckBox(person.name) {
    val value = person.id.toString()
}

class PeopleFilterGroup(
    name: String,
    val field: Int,
    people: List<PersonDto>,
) : Filter.Group<PersonOption>(name, people.map(::PersonOption)) {
    val selectedValues
        get() = state.filter { it.state }.map { it.value }
}

class FilterValue(val label: String, val value: String)

private val SORTS = listOf(
    "Title" to SORT_NAME,
    "Date added" to CREATED_DATE,
    "Last modified" to LAST_MODIFIED_DATE,
    "Last chapter added" to LAST_CHAPTER_ADDED,
    "Time to read" to TIME_TO_READ,
    "Release year" to RELEASE_YEAR_SORT,
    "Read progress" to READ_PROGRESS_SORT,
    "Average rating" to AVERAGE_RATING,
    "Random" to RANDOM,
    "User rating" to USER_RATING_SORT,
    "Unread chapters" to UNREAD_CHAPTER_COUNT,
)
