package eu.kanade.tachiyomi.extension.all.kavita

import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.UnmeteredSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import keiyoushi.annotation.Source
import keiyoushi.network.get
import keiyoushi.network.post
import keiyoushi.source.KeiSource
import keiyoushi.utils.firstInstanceOrNull
import keiyoushi.utils.getPreferencesLazy
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonElement
import keiyoushi.utils.toJsonRequestBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import okhttp3.Dns
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@Source
class Kavita(
    override val name: String,
    override val lang: String,
    override val id: Long,
) : KeiSource(),
    ConfigurableSource,
    UnmeteredSource {

    private val preferences by getPreferencesLazy()
    private val preferenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var preferenceLibraries = emptyList<LibraryDto>()
    private var loadingPreferenceLibraries = false

    private val address
        get() = preferences.getString(ADDRESS_PREF, "").orEmpty()

    override val baseUrl
        get() = preferences.getString(BASE_URL_PREF, null)
            ?.takeIf(String::isNotBlank)
            ?: address.toServerUrl()
            ?: DEFAULT_BASE_URL

    private val apiUrl
        get() = preferences.getString(API_URL_PREF, null)
            ?.takeIf(String::isNotBlank)
            ?: "$baseUrl/api"

    private val apiKey
        get() = preferences.getString(API_KEY_PREF, null)
            ?.takeIf(String::isNotBlank)
            ?: extractApiKey(address)

    private val feedLibraries
        get() = preferences.getStringSet(FEED_LIBRARIES_PREF, emptySet()).orEmpty()

    private val searchLibraries
        get() = preferences.getStringSet(SEARCH_LIBRARIES_PREF, emptySet()).orEmpty()

    private val chapterTemplate
        get() = preferences.getString(CHAPTER_TEMPLATE_PREF, CHAPTER_TEMPLATE_DEFAULT)
            .orEmpty()
            .ifBlank { CHAPTER_TEMPLATE_DEFAULT }

    private val scanlatorTemplate
        get() = preferences.getString(SCANLATOR_TEMPLATE_PREF, SCANLATOR_TEMPLATE_DEFAULT)
            .orEmpty()
            .ifBlank { SCANLATOR_TEMPLATE_DEFAULT }

    private val useAlternateCover
        get() = preferences.getBoolean(ALTERNATE_COVER_PREF, false)

    override val supportsFilterFetching = true
    override val supportsRelatedMangas = true

    override fun OkHttpClient.Builder.configureClient(): OkHttpClient.Builder = dns(Dns.SYSTEM)
        .addInterceptor(EpubImageInterceptor())

    override fun Headers.Builder.configureHeaders(): Headers.Builder = add("x-api-key", apiKey)

    override suspend fun getPopularManga(page: Int): MangasPage = getSeriesPage(
        page,
        SeriesFilterDto(
            statements = defaultFeedStatements(),
            sortOptions = SortOptionDto(AVERAGE_RATING, false),
        ),
    )

    override suspend fun getLatestUpdates(page: Int): MangasPage = getSeriesPage(
        page,
        SeriesFilterDto(
            statements = defaultFeedStatements(),
            sortOptions = SortOptionDto(LAST_CHAPTER_ADDED, false),
        ),
    )

    override suspend fun getSearchMangaList(
        page: Int,
        query: String,
        filters: FilterList,
    ): MangasPage {
        val smartFilter = filters.firstInstanceOrNull<SmartFilterFilter>()?.selected
        val decoded = smartFilter?.let {
            client.post("$apiUrl/Filter/decode", DecodeFilterDto(it.filter).toJsonRequestBody())
                .parseAs<SeriesFilterDto>()
        }
        val statements = decoded?.statements.orEmpty().toMutableList()

        if (query.isNotBlank()) {
            statements += FilterStatementDto(MATCHES, SERIES_NAME, query)
        }

        filters.firstInstanceOrNull<ReadingStatusFilter>()?.state?.let { state ->
            when (state) {
                1 -> statements += FilterStatementDto(EQUAL, READ_PROGRESS, "0")
                2 -> {
                    statements += FilterStatementDto(GREATER_THAN_EQUAL, READ_PROGRESS, "1")
                    statements += FilterStatementDto(LESS_THAN_EQUAL, READ_PROGRESS, "99")
                }
                3 -> statements += FilterStatementDto(EQUAL, READ_PROGRESS, "100")
            }
        }

        filters.firstInstanceOrNull<ReleaseYearFilter>()?.let { filter ->
            filter.minimum?.let { statements += FilterStatementDto(GREATER_THAN_EQUAL, RELEASE_YEAR, it.toString()) }
            filter.maximum?.let { statements += FilterStatementDto(LESS_THAN_EQUAL, RELEASE_YEAR, it.toString()) }
        }

        filters.firstInstanceOrNull<MinimumRatingFilter>()
            ?.state
            ?.takeIf { it > 0 }
            ?.let { statements += FilterStatementDto(GREATER_THAN_EQUAL, USER_RATING, it.toString()) }

        filters.filterIsInstance<MetadataFilterGroup>().forEach { filter ->
            filter.included.takeIf(List<String>::isNotEmpty)?.let {
                statements += FilterStatementDto(CONTAINS, filter.field, it.joinToString())
            }
            filter.excluded.takeIf(List<String>::isNotEmpty)?.let {
                statements += FilterStatementDto(NOT_CONTAINS, filter.field, it.joinToString())
            }
        }

        filters.filterIsInstance<PeopleFilterGroup>().forEach { filter ->
            filter.selectedValues.forEach { value ->
                statements += FilterStatementDto(MUST_CONTAINS, filter.field, value)
            }
        }

        filters.firstInstanceOrNull<PublicationStatusFilter>()
            ?.selectedValues
            ?.takeIf(List<String>::isNotEmpty)
            ?.let { statements += FilterStatementDto(CONTAINS, PUBLICATION_STATUS, it.joinToString()) }

        val selectedFormats = filters.firstInstanceOrNull<FormatFilter>()?.selectedValues.orEmpty()
        if (selectedFormats.isEmpty()) {
            statements += epubExclusion
        } else {
            statements += FilterStatementDto(CONTAINS, FORMATS_FIELD, selectedFormats.joinToString())
        }

        val libraryFilter = filters.filterIsInstance<MetadataFilterGroup>().firstOrNull { it.field == LIBRARIES }
        if (libraryFilter?.included.isNullOrEmpty() && libraryFilter?.excluded.isNullOrEmpty() && searchLibraries.isNotEmpty()) {
            statements += FilterStatementDto(CONTAINS, LIBRARIES, searchLibraries.joinToString())
        }

        val sort = filters.firstInstanceOrNull<SortFilter>()?.sortOption
            ?: decoded?.sortOptions
            ?: SortOptionDto(AVERAGE_RATING, false)
        val endpoint = if (filters.firstInstanceOrNull<CatalogFilter>()?.wantToRead == true) {
            "$apiUrl/want-to-read/v2"
        } else {
            "$apiUrl/Series/all-v2"
        }

        return getSeriesPage(page, SeriesFilterDto(statements, sortOptions = sort), endpoint)
    }

    private suspend fun getSeriesPage(
        page: Int,
        filter: SeriesFilterDto,
        endpoint: String = "$apiUrl/Series/all-v2",
    ): MangasPage {
        val url = endpoint.toHttpUrl().newBuilder()
            .addQueryParameter("PageNumber", page.toString())
            .addQueryParameter("PageSize", PAGE_SIZE.toString())
            .build()
        val response = client.post(url, filter.toJsonRequestBody())
        val pagination = response.header("Pagination")?.parseAs<PaginationDto>()
        val series = response.parseAs<List<SeriesDto>>()

        return MangasPage(
            series.map { it.toSManga(baseUrl, apiKey) },
            pagination?.hasNextPage ?: (series.size == PAGE_SIZE),
        )
    }

    override suspend fun getMangaByUrl(url: HttpUrl): SManga? {
        if (url.host != baseUrl.toHttpUrl().host) return null
        val index = url.pathSegments.indexOfFirst { it.equals("series", true) }
        val seriesId = url.pathSegments.getOrNull(index + 1)?.toIntOrNull() ?: return null
        return client.get("$apiUrl/Series/$seriesId")
            .parseAs<SeriesDto>()
            .toSManga(baseUrl, apiKey)
    }

    override fun getMangaUrl(manga: SManga) = manga.url

    override suspend fun fetchMangaUpdate(
        manga: SManga,
        chapters: List<SChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): SMangaUpdate = coroutineScope {
        val detailsDeferred = async { if (fetchDetails) fetchDetails(manga) else manga }
        val chaptersDeferred = async { if (fetchChapters) fetchChapters(manga) else chapters }
        SMangaUpdate(detailsDeferred.await(), chaptersDeferred.await())
    }

    private suspend fun fetchDetails(manga: SManga): SManga = coroutineScope {
        val seriesId = manga.seriesId()
        val seriesDeferred = async { client.get("$apiUrl/Series/$seriesId").parseAs<SeriesDto>() }
        val metadataDeferred = async {
            client.get("$apiUrl/Series/metadata?seriesId=$seriesId").parseAs<SeriesMetadataDto>()
        }
        val plusDeferred = async {
            runCatching {
                client.get("$apiUrl/Metadata/series-detail-plus?seriesId=$seriesId")
                    .parseAs<SeriesDetailPlusDto>()
            }.getOrNull()
        }
        val volumesDeferred = async {
            if (useAlternateCover) {
                client.get("$apiUrl/Series/volumes?seriesId=$seriesId").parseAs<List<VolumeDto>>()
            } else {
                emptyList()
            }
        }
        val librariesDeferred = async {
            if (useAlternateCover) client.get("$apiUrl/Library/libraries").parseAs<List<LibraryDto>>() else emptyList()
        }

        val series = seriesDeferred.await()
        val libraryType = librariesDeferred.await().firstOrNull { it.id == series.libraryId }?.type ?: LIBRARY_MANGA
        val cover = selectAlternateCover(volumesDeferred.await(), libraryType)
        series.toSManga(baseUrl, apiKey, metadataDeferred.await(), plusDeferred.await()?.score, cover)
    }

    private suspend fun fetchChapters(manga: SManga): List<SChapter> = coroutineScope {
        val seriesId = manga.seriesId()
        val seriesDeferred = async { client.get("$apiUrl/Series/$seriesId").parseAs<SeriesDto>() }
        val metadataDeferred = async {
            client.get("$apiUrl/Series/metadata?seriesId=$seriesId").parseAs<SeriesMetadataDto>()
        }
        val volumesDeferred = async {
            client.get("$apiUrl/Series/volumes?seriesId=$seriesId").parseAs<List<VolumeDto>>()
        }
        val librariesDeferred = async { client.get("$apiUrl/Library/libraries").parseAs<List<LibraryDto>>() }

        val series = seriesDeferred.await()
        val metadata = metadataDeferred.await()
        val libraryType = librariesDeferred.await().firstOrNull { it.id == series.libraryId }?.type ?: LIBRARY_MANGA
        val context = ChapterContext(
            libraryType = libraryType,
            libraryName = series.libraryName.orEmpty(),
            seriesName = series.name,
            isWebtoon = metadata.isWebtoon || series.libraryName.orEmpty().contains("webtoon", true),
            chapterTemplate = chapterTemplate,
            scanlatorTemplate = scanlatorTemplate,
        )
        volumesDeferred.await()
            .flatMap { it.toSChapters(context) }
            .sortedWith(compareByDescending<SChapter> { it.chapter_number }.thenByDescending { it.date_upload })
    }

    override suspend fun fetchRelatedMangaList(manga: SManga): List<SManga> = coroutineScope {
        val seriesId = manga.seriesId()
        val relatedDeferred = async {
            client.get("$apiUrl/Series/all-related?seriesId=$seriesId").parseAs<RelatedSeriesDto>().all
        }
        val recommendationsDeferred = async {
            runCatching {
                client.get("$apiUrl/Metadata/series-detail-plus?seriesId=$seriesId")
                    .parseAs<SeriesDetailPlusDto>()
                    .recommendations
                    ?.ownedSeries
                    .orEmpty()
                    .map { it.series }
            }.getOrDefault(emptyList())
        }
        (relatedDeferred.await() + recommendationsDeferred.await())
            .distinctBy { it.id }
            .filterNot { it.id == seriesId }
            .map { it.toSManga(baseUrl, apiKey) }
    }

    override fun getChapterUrl(chapter: SChapter) = "$baseUrl${chapter.url.substringBefore('?')}"

    override suspend fun getPageList(chapter: SChapter): List<Page> {
        val chapterId = chapter.chapterId()
        val chapterDto = client.get("$apiUrl/Chapter?chapterId=$chapterId").parseAs<ChapterDto>()
        if (chapterDto.format == EPUB) {
            return getEpubPageList(client, apiUrl, chapterId, chapterDto.pages, headers)
        }

        return (0 until chapterDto.pages).map { index ->
            val imageUrl = "$apiUrl/Reader/image".toHttpUrl().newBuilder()
                .addQueryParameter("chapterId", chapterId.toString())
                .addQueryParameter("page", index.toString())
                .addQueryParameter("extractPdf", "true")
                .build()
            Page(index, imageUrl = imageUrl.toString())
        }
    }

    override fun imageRequest(page: Page): Request {
        val url = page.imageUrl!!.toHttpUrl().newBuilder()
            .addQueryParameter("apiKey", apiKey)
            .build()
        return GET(url, headers)
    }

    override suspend fun fetchFilterData(): JsonElement = coroutineScope {
        val libraries = async { client.get("$apiUrl/Library/libraries").parseAs<List<LibraryDto>>() }
        val genres = async { client.get("$apiUrl/Metadata/genres").parseAs<List<NamedIdDto>>() }
        val tags = async { client.get("$apiUrl/Metadata/tags").parseAs<List<NamedIdDto>>() }
        val ageRatings = async { client.get("$apiUrl/Metadata/age-ratings").parseAs<List<NamedValueDto>>() }
        val collections = async { client.get("$apiUrl/Collection").parseAs<List<CollectionDto>>() }
        val languages = async { client.get("$apiUrl/Metadata/languages").parseAs<List<LanguageDto>>() }
        val publicationStatuses = async {
            client.get("$apiUrl/Metadata/publication-status").parseAs<List<NamedValueDto>>()
        }
        val smartFilters = async {
            client.get("$apiUrl/Filter").parseAs<List<SmartFilterDto>>().filter { it.isSeriesFilter }
        }
        fun people(role: Int) = async {
            client.get("$apiUrl/Metadata/people-by-role?role=$role").parseAs<List<PersonDto>>()
        }
        val writers = people(3)
        val pencillers = people(4)
        val inkers = people(5)
        val colorists = people(6)
        val letterers = people(7)
        val coverArtists = people(8)
        val editors = people(9)
        val publishers = people(10)
        val characters = people(11)
        val translators = people(12)

        val loadedLibraries = libraries.await()
        preferenceLibraries = loadedLibraries
        FilterDataDto(
            libraries = loadedLibraries,
            genres = genres.await(),
            tags = tags.await(),
            ageRatings = ageRatings.await(),
            collections = collections.await(),
            languages = languages.await(),
            publicationStatuses = publicationStatuses.await(),
            smartFilters = smartFilters.await(),
            people = PeopleFilterDataDto(
                writers.await(),
                pencillers.await(),
                inkers.await(),
                colorists.await(),
                letterers.await(),
                coverArtists.await(),
                editors.await(),
                publishers.await(),
                characters.await(),
                translators.await(),
            ),
        ).toJsonElement()
    }

    override fun getFilterList(data: JsonElement?): FilterList {
        val filterData = data?.parseAs<FilterDataDto>()
        val statuses = filterData?.publicationStatuses?.map { FilterValue(it.title, it.value.toString()) }
            ?: DEFAULT_STATUSES
        return FilterList(
            buildList {
                add(SortFilter())
                add(CatalogFilter())
                add(SmartFilterFilter(filterData?.smartFilters.orEmpty()))
                add(ReadingStatusFilter())
                add(ReleaseYearFilter())
                add(MinimumRatingFilter())
                add(FormatFilter())
                add(PublicationStatusFilter(statuses))
                filterData ?: return@buildList
                addMetadataGroup("Genres", GENRES, filterData.genres.map { FilterValue(it.title, it.id.toString()) })
                addMetadataGroup("Tags", TAGS, filterData.tags.map { FilterValue(it.title, it.id.toString()) })
                addMetadataGroup("Age ratings", AGE_RATING, filterData.ageRatings.map { FilterValue(it.title, it.value.toString()) })
                addMetadataGroup("Collections", COLLECTIONS, filterData.collections.map { FilterValue(it.title, it.id.toString()) })
                addMetadataGroup("Languages", LANGUAGES, filterData.languages.map { FilterValue(it.title, it.isoCode) })
                addMetadataGroup("Libraries", LIBRARIES, filterData.libraries.map { FilterValue(it.name.orEmpty(), it.id.toString()) })
                add(PeopleFilterGroup("Writers", WRITERS, filterData.people.writers))
                add(PeopleFilterGroup("Pencillers", PENCILLERS, filterData.people.pencillers))
                add(PeopleFilterGroup("Inkers", INKERS, filterData.people.inkers))
                add(PeopleFilterGroup("Colorists", COLORISTS, filterData.people.colorists))
                add(PeopleFilterGroup("Letterers", LETTERERS, filterData.people.letterers))
                add(PeopleFilterGroup("Cover artists", COVER_ARTISTS, filterData.people.coverArtists))
                add(PeopleFilterGroup("Editors", EDITORS, filterData.people.editors))
                add(PeopleFilterGroup("Publishers", PUBLISHERS, filterData.people.publishers))
                add(PeopleFilterGroup("Characters", CHARACTERS, filterData.people.characters))
                add(PeopleFilterGroup("Translators", TRANSLATORS, filterData.people.translators))
            },
        )
    }

    private fun MutableList<eu.kanade.tachiyomi.source.model.Filter<*>>.addMetadataGroup(
        name: String,
        field: Int,
        values: List<FilterValue>,
    ) {
        if (values.isNotEmpty()) add(MetadataFilterGroup(name, field, values))
    }

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        fetchPreferenceLibraries()

        EditTextPreference(screen.context).apply {
            key = ADDRESS_PREF
            title = "OPDS URL"
            summary = "Paste the full Kavita OPDS URL, including its API key."
            setDefaultValue("")
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            }
            setOnPreferenceChangeListener { preference, newValue ->
                val newAddress = newValue.toString().trim()
                val newBaseUrl = newAddress.toServerUrl() ?: return@setOnPreferenceChangeListener false
                val newApiKey = extractApiKey(newAddress)
                    .takeUnless { it == newAddress || it.isBlank() }
                    ?: return@setOnPreferenceChangeListener false
                preferences.edit()
                    .putString(BASE_URL_PREF, newBaseUrl)
                    .putString(API_URL_PREF, "$newBaseUrl/api")
                    .putString(API_KEY_PREF, newApiKey)
                    .apply()
                preference.summary = newAddress
                true
            }
        }.also(screen::addPreference)

        SwitchPreferenceCompat(screen.context).apply {
            key = ALTERNATE_COVER_PREF
            title = "Use volume or issue covers"
            summary = "Use the first unread cover, or the latest cover when everything is read."
            setDefaultValue(false)
        }.also(screen::addPreference)

        addLibraryPreference(
            screen,
            FEED_LIBRARIES_PREF,
            "Libraries in Popular and Latest",
            "Only show selected libraries in browse feeds. All libraries are used when none are selected.",
        )
        addLibraryPreference(
            screen,
            SEARCH_LIBRARIES_PREF,
            "Default search libraries",
            "Use selected libraries when no library filter is chosen. All libraries are used when none are selected.",
        )

        EditTextPreference(screen.context).apply {
            key = CHAPTER_TEMPLATE_PREF
            title = "Chapter title format"
            summary = TEMPLATE_SUMMARY
            dialogMessage = TEMPLATE_SUMMARY
            setDefaultValue(CHAPTER_TEMPLATE_DEFAULT)
        }.also(screen::addPreference)

        EditTextPreference(screen.context).apply {
            key = SCANLATOR_TEMPLATE_PREF
            title = "Chapter subtitle format"
            summary = TEMPLATE_SUMMARY
            dialogMessage = TEMPLATE_SUMMARY
            setDefaultValue(SCANLATOR_TEMPLATE_DEFAULT)
        }.also(screen::addPreference)
    }

    private fun addLibraryPreference(
        screen: PreferenceScreen,
        preferenceKey: String,
        preferenceTitle: String,
        preferenceSummary: String,
    ) {
        MultiSelectListPreference(screen.context).apply {
            key = preferenceKey
            title = preferenceTitle
            summary = if (preferenceLibraries.isEmpty()) {
                "$preferenceSummary Reopen settings to load library choices."
            } else {
                preferenceSummary
            }
            entries = preferenceLibraries.map { it.name.orEmpty() }.toTypedArray()
            entryValues = preferenceLibraries.map { it.id.toString() }.toTypedArray()
            setDefaultValue(emptySet<String>())
        }.also(screen::addPreference)
    }

    private fun fetchPreferenceLibraries() {
        if (loadingPreferenceLibraries || preferenceLibraries.isNotEmpty() || apiKey.isBlank()) return
        loadingPreferenceLibraries = true
        preferenceScope.launch {
            preferenceLibraries = runCatching {
                client.get("$apiUrl/Library/libraries").parseAs<List<LibraryDto>>()
            }.getOrDefault(emptyList())
            loadingPreferenceLibraries = false
        }
    }

    private fun defaultFeedStatements() = buildList {
        add(epubExclusion)
        feedLibraries.takeIf(Set<String>::isNotEmpty)?.let {
            add(FilterStatementDto(CONTAINS, LIBRARIES, it.joinToString()))
        }
    }

    private fun selectAlternateCover(volumes: List<VolumeDto>, libraryType: Int): String? {
        val candidates = if (libraryType == LIBRARY_COMIC || libraryType == LIBRARY_COMIC_VINE) {
            volumes.flatMap { volume ->
                volume.chapters.mapNotNull { chapter ->
                    chapter.coverImage?.takeIf(String::isNotBlank)?.let {
                        CoverCandidate(
                            "$apiUrl/Image/chapter-cover?chapterId=${chapter.id}&apiKey=$apiKey&ts=${chapter.lastModifiedUtc}",
                            chapter.pagesRead < chapter.pages,
                            chapter.minNumber,
                        )
                    }
                }
            }
        } else {
            volumes.mapNotNull { volume ->
                volume.coverImage?.takeIf(String::isNotBlank)?.let {
                    CoverCandidate(
                        "$apiUrl/Image/volume-cover?volumeId=${volume.id}&apiKey=$apiKey&ts=${volume.lastModifiedUtc}",
                        volume.pagesRead < volume.pages,
                        volume.minNumber,
                    )
                }
            }
        }
        return candidates.filter { it.unread }.minByOrNull { it.number }?.url
            ?: candidates.maxByOrNull { it.number }?.url
    }

    private fun SManga.seriesId(): Int {
        val parsed = url.toHttpUrlOrNull()
            ?: throw IOException("Invalid Kavita series URL: $url")
        val index = parsed.pathSegments.indexOfFirst { it.equals("series", true) }
        return parsed.pathSegments.getOrNull(index + 1)?.toIntOrNull()
            ?: throw IOException("Invalid Kavita series URL: $url")
    }

    private fun SChapter.chapterId(): Int = url.substringAfter("/Chapter/", "")
        .substringBefore('?')
        .toIntOrNull()
        ?: throw IOException("Invalid Kavita chapter URL: $url")

    private fun extractApiKey(value: String): String {
        val url = value.toHttpUrlOrNull() ?: return value
        val opdsIndex = url.pathSegments.indexOfFirst { it.equals("opds", true) }
        return url.queryParameter("apiKey")
            ?: url.pathSegments.getOrNull(opdsIndex + 1)
            ?: value
    }

    private fun String.toServerUrl(): String? = toHttpUrlOrNull()?.let { url ->
        val defaultPort = if (url.scheme == "https") 443 else 80
        "${url.scheme}://${url.host}${if (url.port == defaultPort) "" else ":${url.port}"}"
    }

    private val epubExclusion
        get() = FilterStatementDto(NOT_CONTAINS, FORMATS_FIELD, EPUB.toString())

    private class CoverCandidate(val url: String, val unread: Boolean, val number: Float)

    private companion object {
        const val ADDRESS_PREF = "Address"
        const val BASE_URL_PREF = "BASEURL"
        const val API_URL_PREF = "APIURL"
        const val API_KEY_PREF = "APIKEY"
        const val FEED_LIBRARIES_PREF = "allowedLibrariesFeed"
        const val SEARCH_LIBRARIES_PREF = "allowedLibrariesSearch"
        const val ALTERNATE_COVER_PREF = "last_volume_cover"
        const val CHAPTER_TEMPLATE_PREF = "chapterTitleFormat"
        const val SCANLATOR_TEMPLATE_PREF = "scanlatorFormat"
        const val CHAPTER_TEMPLATE_DEFAULT = "\$CleanTitle"
        const val SCANLATOR_TEMPLATE_DEFAULT = "\$Type"
        const val PAGE_SIZE = 20
        const val DEFAULT_BASE_URL = "http://127.0.0.1:5000"
        const val TEMPLATE_SUMMARY =
            "Variables: \$Type, \$No, \$Title, \$CleanTitle, \$Pages, \$Size, \$Volume, \$SeriesName, \$LibraryName, \$Format, \$Created, \$ReleaseDate"
        val DEFAULT_STATUSES = listOf(
            FilterValue("Ongoing", "0"),
            FilterValue("Hiatus", "1"),
            FilterValue("Completed", "2"),
            FilterValue("Cancelled", "3"),
            FilterValue("Ended", "4"),
        )
    }
}
