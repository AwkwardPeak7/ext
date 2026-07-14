package eu.kanade.tachiyomi.multisrc.madara

import eu.kanade.tachiyomi.source.model.Filter

class SortFilter :
    Filter.Select<String>(
        name = "Sort",
        values = sorts.map { it.first }.toTypedArray(),
    ) {
    val sort get() = sorts[state].second
}

class GenreFilter(private val genres: List<Pair<String, String>>) :
    Filter.Select<String>(
        name = "Genre",
        values = genres.map { it.first }.toTypedArray(),
    ) {
    val genre get() = genres[state].second.takeIf(String::isNotBlank)
}

private val sorts = listOf(
    "Relevance" to MADARA_RELEVANCE_SORT,
    "Most Views" to MADARA_VIEWS_SORT,
    "Rating" to MADARA_RATING_SORT,
    "Trending" to MADARA_TRENDING_SORT,
    "Latest" to MADARA_LATEST_SORT,
    "New Manga" to MADARA_NEW_SORT,
    "A-Z" to MADARA_ALPHABET_SORT,
)

const val MADARA_RELEVANCE_SORT = "relevance"
const val MADARA_VIEWS_SORT = "views"
const val MADARA_RATING_SORT = "rating"
const val MADARA_TRENDING_SORT = "trending"
const val MADARA_LATEST_SORT = "latest"
const val MADARA_NEW_SORT = "new-manga"
const val MADARA_ALPHABET_SORT = "alphabet"
