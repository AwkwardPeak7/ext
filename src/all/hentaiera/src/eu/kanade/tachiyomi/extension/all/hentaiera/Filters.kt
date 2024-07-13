package eu.kanade.tachiyomi.extension.all.hentaiera

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList

abstract class SelectFilter(
    name: String,
    private val options: List<Pair<String, String>>,
    defaultValue: String? = null,
) : Filter.Select<String>(
    name,
    options.map { it.first }.toTypedArray(),
    options.indexOfFirst { it.second == defaultValue }.takeIf { it != -1 } ?: 0,
) {
    val selected get() = options[state].second
}

class CheckBoxFilter(
    name: String,
    val value: String,
    checked: Boolean = false,
) : Filter.CheckBox(name, checked)

abstract class CheckBoxFilterGroup(
    name: String,
    genres: List<Pair<String, String>>,
) : Filter.Group<CheckBoxFilter>(
    name,
    genres.map { CheckBoxFilter(it.first, it.second, true) },
) {
    val checked get() = state.filter { it.state }.map { it.value }
    val unchecked get() = state.filterNot { it.state }.map { it.value }
}

class SortFilter(defaultValue: String? = null) : SelectFilter(
    "Sort by",
    listOf(
        "Popular" to "pp",
        "Latest" to "lt",
        "Downloads" to "dl",
        "Top Rated" to "tr",
    ),
    defaultValue,
) {
    companion object {
        val POPULAR = FilterList(SortFilter("pp"), TypeFilter())
        val LATEST = FilterList(SortFilter("lt"), TypeFilter())
    }
}

class TypeFilter : CheckBoxFilterGroup(
    "Types",
    listOf(
        "Manga" to "mg",
        "Doujinshi" to "dj",
        "Western" to "ws",
        "Image Set" to "is",
        "Artist CG" to "ac",
        "Game CG" to "gc",
    ),
)

abstract class TextFilter(name: String) : Filter.Text(name)
class TagsFilter : TextFilter("Tags")
class ParodiesFilter : TextFilter("Parodies")
class ArtistsFilter : TextFilter("Artists")
class CharactersFilter : TextFilter("Characters")
class GroupsFilter : TextFilter("Groups")

fun getFilters() = FilterList(
    SortFilter(),
    TypeFilter(),
    TagsFilter(),
    ParodiesFilter(),
    ArtistsFilter(),
    CharactersFilter(),
    GroupsFilter(),
)
