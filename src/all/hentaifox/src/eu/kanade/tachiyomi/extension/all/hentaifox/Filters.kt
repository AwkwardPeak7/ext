package eu.kanade.tachiyomi.extension.all.hentaifox

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList

abstract class SelectFilter(
    name: String,
    private val options: List<Pair<String, String>>,
) : Filter.Select<String>(
    name,
    options.map { it.first }.toTypedArray(),
) {
    val selected get() = options[state].second.takeUnless { it.isBlank() }
}

class SortFilter : SelectFilter(
    "Sort by",
    listOf(
        "Latest" to "",
        "Popular" to "popular",
    ),
)

abstract class TextFilter(name: String) : Filter.Text(name) {
    val values get() = state.split(",")
        .map(String::trim)
        .filter(String::isNotBlank)
        .map {
            if (it.contains(" ")) {
                buildString {
                    append("\"")
                    append(
                        it.removeSurrounding("\"").replace(spaceRegex, "+"),
                    )
                    append("\"")
                }
            } else {
                it
            }
        }
}
val spaceRegex = Regex("""[^a-zA-Z0-9]+(?=[a-zA-Z0-9])""")

class TagsFilter : TextFilter("Tags")
class ParodiesFilter : TextFilter("Parodies")
class ArtistsFilter : TextFilter("Artists")
class CharactersFilter : TextFilter("Characters")
class GroupsFilter : TextFilter("Groups")

fun getFilters() = FilterList(
    SortFilter(),
    TagsFilter(),
    ParodiesFilter(),
    ArtistsFilter(),
    CharactersFilter(),
    GroupsFilter(),
    Filter.Header("seperate tags with comma (,)"),
)
