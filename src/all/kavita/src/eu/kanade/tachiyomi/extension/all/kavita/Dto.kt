package eu.kanade.tachiyomi.extension.all.kavita

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.tryParse
import keiyoushi.utils.tryParseDateTime
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant

@Serializable
class SeriesFilterDto(
    val statements: List<FilterStatementDto> = emptyList(),
    private val combination: Int = AND,
    val sortOptions: SortOptionDto? = null,
    private val entityType: Int = SERIES_ENTITY,
    private val limitTo: Int = 0,
)

@Serializable
class FilterStatementDto(
    private val comparison: Int,
    private val field: Int,
    private val value: String,
)

@Serializable
class SortOptionDto(
    private val sortField: Int,
    private val isAscending: Boolean,
)

@Serializable
class DecodeFilterDto(private val encodedFilter: String)

@Serializable
class PaginationDto(
    private val currentPage: Int,
    private val totalPages: Int,
) {
    val hasNextPage
        get() = currentPage < totalPages
}

@Serializable
class FilterDataDto(
    val libraries: List<LibraryDto> = emptyList(),
    val genres: List<NamedIdDto> = emptyList(),
    val tags: List<NamedIdDto> = emptyList(),
    val ageRatings: List<NamedValueDto> = emptyList(),
    val collections: List<CollectionDto> = emptyList(),
    val languages: List<LanguageDto> = emptyList(),
    val publicationStatuses: List<NamedValueDto> = emptyList(),
    val smartFilters: List<SmartFilterDto> = emptyList(),
    val people: PeopleFilterDataDto = PeopleFilterDataDto(),
)

@Serializable
class PeopleFilterDataDto(
    val writers: List<PersonDto> = emptyList(),
    val pencillers: List<PersonDto> = emptyList(),
    val inkers: List<PersonDto> = emptyList(),
    val colorists: List<PersonDto> = emptyList(),
    val letterers: List<PersonDto> = emptyList(),
    val coverArtists: List<PersonDto> = emptyList(),
    val editors: List<PersonDto> = emptyList(),
    val publishers: List<PersonDto> = emptyList(),
    val characters: List<PersonDto> = emptyList(),
    val translators: List<PersonDto> = emptyList(),
)

@Serializable
class SmartFilterDto(
    private val id: Int,
    val name: String,
    val filter: String,
    private val entityType: Int,
) {
    val isSeriesFilter
        get() = entityType == SERIES_ENTITY
}

@Serializable
class NamedIdDto(val id: Int, val title: String)

@Serializable
class NamedValueDto(val value: Int, val title: String)

@Serializable
class LanguageDto(val isoCode: String, val title: String)

@Serializable
class CollectionDto(val id: Int, val title: String)

@Serializable
class LibraryDto(
    val id: Int,
    val name: String? = null,
    val type: Int = LIBRARY_MANGA,
)

@Serializable
class SeriesDto(
    val id: Int,
    val name: String,
    val libraryId: Int,
    val libraryName: String? = null,
    val format: Int = UNKNOWN,
) {
    fun toSManga(
        baseUrl: String,
        apiKey: String,
        metadata: SeriesMetadataDto? = null,
        score: Int? = null,
        coverUrl: String? = null,
    ) = SManga.create().apply {
        title = name
        url = "$baseUrl/Series/$id"
        thumbnail_url = coverUrl ?: seriesCoverUrl(baseUrl, apiKey, id)
        metadata?.let {
            author = it.writers.joinToString { writer -> writer.name }.takeIf(String::isNotBlank)
            artist = it.coverArtists.joinToString { artist -> artist.name }.takeIf(String::isNotBlank)
            description = buildList {
                score?.takeIf { value -> value > 0 }?.let { value -> add("⭐ Score: $value/100") }
                it.summary?.takeIf(String::isNotBlank)?.let(::add)
            }.joinToString("\n").takeIf(String::isNotBlank)
            genre = it.groupedGenres(libraryName.orEmpty(), format)
            status = it.status
        }
    }
}

@Serializable
class SeriesMetadataDto(
    val summary: String? = null,
    val genres: List<NamedIdDto> = emptyList(),
    val tags: List<NamedIdDto> = emptyList(),
    val writers: List<PersonDto> = emptyList(),
    val coverArtists: List<PersonDto> = emptyList(),
    private val publicationStatus: Int = -1,
) {
    val status
        get() = when (publicationStatus) {
            0 -> SManga.ONGOING
            1 -> SManga.ON_HIATUS
            2 -> SManga.COMPLETED
            3 -> SManga.CANCELLED
            4 -> SManga.PUBLISHING_FINISHED
            else -> SManga.UNKNOWN
        }

    val isWebtoon
        get() = (genres.map { it.title } + tags.map { it.title })
            .any { it.contains("webtoon", true) || it.contains("long strip", true) }

    private fun groupedValues(): GroupedMetadata {
        val genreNames = genres.map { it.title }
        val tagNames = tags.map { it.title }
        val demographic = DEMOGRAPHICS.firstNotNullOfOrNull { candidate ->
            (genreNames + tagNames).firstOrNull { it.equals(candidate, true) }
        }
        val formats = FORMATS.mapNotNull { candidate ->
            (genreNames + tagNames).firstOrNull { it.equals(candidate, true) }
        }.distinct()
        val filteredGenres = genreNames.filterNot { value ->
            value.equals(demographic, true) || formats.any { it.equals(value, true) }
        }
        val filteredTags = tagNames.filterNot { value ->
            value.equals(demographic, true) ||
                formats.any { it.equals(value, true) } ||
                filteredGenres.any { it.equals(value, true) }
        }
        return GroupedMetadata(demographic, formats, filteredGenres, filteredTags)
    }

    fun groupedGenres(libraryName: String, seriesFormat: Int): String? {
        val grouped = groupedValues()
        return buildList {
            libraryName.takeIf(String::isNotBlank)?.let { add("Library:$it") }
            grouped.demographic?.let { add("Demographic:$it") }
            add("Format:${formatName(seriesFormat)}")
            grouped.formats.forEach { add("Format:$it") }
            grouped.genres.forEach { add("Genre:$it") }
            grouped.tags.forEach { add("Tag:$it") }
        }.distinct().joinToString().takeIf(String::isNotBlank)
    }
}

private class GroupedMetadata(
    val demographic: String?,
    val formats: List<String>,
    val genres: List<String>,
    val tags: List<String>,
)

@Serializable
class PersonDto(val id: Int = 0, val name: String)

@Serializable
class SeriesDetailPlusDto(
    val recommendations: RecommendationDto? = null,
    val ratings: List<RatingDto> = emptyList(),
    val series: ExternalSeriesDetailDto? = null,
) {
    val score
        get() = series?.averageScore?.takeIf { it > 0 }
            ?: ratings.firstNotNullOfOrNull { it.averageScore.takeIf { score -> score > 0 } }
}

@Serializable
class RatingDto(val averageScore: Int = 0)

@Serializable
class ExternalSeriesDetailDto(val averageScore: Int = 0)

@Serializable
class RecommendationDto(val ownedSeries: List<RecommendedSeriesDto> = emptyList())

@Serializable
class RecommendedSeriesDto(val series: SeriesDto)

@Serializable
class RelatedSeriesDto(
    private val sequels: List<SeriesDto> = emptyList(),
    private val prequels: List<SeriesDto> = emptyList(),
    private val spinOffs: List<SeriesDto> = emptyList(),
    private val adaptations: List<SeriesDto> = emptyList(),
    private val sideStories: List<SeriesDto> = emptyList(),
    private val characters: List<SeriesDto> = emptyList(),
    private val contains: List<SeriesDto> = emptyList(),
    private val others: List<SeriesDto> = emptyList(),
    private val alternativeSettings: List<SeriesDto> = emptyList(),
    private val alternativeVersions: List<SeriesDto> = emptyList(),
    private val doujinshis: List<SeriesDto> = emptyList(),
    private val parent: List<SeriesDto> = emptyList(),
    private val editions: List<SeriesDto> = emptyList(),
    private val annuals: List<SeriesDto> = emptyList(),
    private val cameos: List<SeriesDto> = emptyList(),
) {
    val all
        get() = sequels + prequels + spinOffs + adaptations + sideStories + characters + contains +
            others + alternativeSettings + alternativeVersions + doujinshis + parent + editions + annuals + cameos
}

@Serializable
class VolumeDto(
    val id: Int,
    val minNumber: Float,
    val maxNumber: Float,
    val name: String? = null,
    val pages: Int = 0,
    val pagesRead: Int = 0,
    val coverImage: String? = null,
    val lastModifiedUtc: String = "",
    val chapters: List<ChapterDto> = emptyList(),
) {
    fun toSChapters(context: ChapterContext): List<SChapter> = chapters.map { chapter ->
        chapter.toSChapter(this, context)
    }

    val number
        get() = numberRange(minNumber, maxNumber)
}

@Serializable
class ChapterDto(
    val id: Int,
    val range: String? = null,
    val number: String? = null,
    val minNumber: Float,
    val maxNumber: Float,
    private val sortOrder: Float,
    val pages: Int,
    private val title: String? = null,
    private val titleName: String? = null,
    private val releaseDate: String? = null,
    private val createdUtc: String? = null,
    val lastModifiedUtc: String = "",
    val pagesRead: Int = 0,
    val coverImage: String? = null,
    private val files: List<MangaFileDto>? = null,
    val format: Int,
) {
    fun toSChapter(volume: VolumeDto, context: ChapterContext) = SChapter.create().apply {
        val kind = chapterKind(volume, context.libraryType)
        val variables = templateVariables(volume, context, kind)
        url = "/Chapter/$id" + if (files.orEmpty().size > 1) "?split=${files.orEmpty().size}" else ""
        name = renderTemplate(context.chapterTemplate, variables).ifBlank { variables.cleanTitle }
        chapter_number = when (kind) {
            ChapterKind.SINGLE_FILE_VOLUME -> volume.minNumber / VOLUME_NUMBER_OFFSET
            ChapterKind.SPECIAL -> {
                val specialNumber = listOf(volume.minNumber, minNumber).firstOrNull { it > 0F } ?: SPECIAL_NUMBER.toFloat()
                specialNumber / SPECIAL_NUMBER_OFFSET
            }
            else -> minNumber.takeUnless { it == 0F } ?: sortOrder
        }
        date_upload = parseDate(releaseDate).takeIf { it != 0L } ?: parseDate(createdUtc)
        scanlator = renderTemplate(context.scanlatorTemplate, variables).takeIf(String::isNotBlank)
    }

    private fun chapterKind(volume: VolumeDto, libraryType: Int): ChapterKind = when {
        volume.minNumber.toInt() == SPECIAL_NUMBER || minNumber.toInt() == SPECIAL_NUMBER -> ChapterKind.SPECIAL
        volume.minNumber.toInt() == UNNUMBERED_VOLUME -> when (libraryType) {
            LIBRARY_COMIC, LIBRARY_COMIC_VINE -> ChapterKind.ISSUE
            else -> ChapterKind.CHAPTER
        }
        number == UNNUMBERED_VOLUME.toString() && volume.minNumber > 0F -> ChapterKind.SINGLE_FILE_VOLUME
        volume.minNumber > 0F -> ChapterKind.REGULAR
        libraryType == LIBRARY_COMIC || libraryType == LIBRARY_COMIC_VINE -> ChapterKind.ISSUE
        else -> ChapterKind.CHAPTER
    }

    private fun templateVariables(
        volume: VolumeDto,
        context: ChapterContext,
        kind: ChapterKind,
    ): ChapterTemplateVariables {
        val chapterNumber = numberRange(minNumber, maxNumber)
        val volumeNumber = volume.number
        val type = when (kind) {
            ChapterKind.SINGLE_FILE_VOLUME -> if (context.isWebtoon) "Season" else "Volume"
            ChapterKind.SPECIAL -> "Special"
            ChapterKind.ISSUE -> "Issue"
            else -> if (context.isWebtoon) "Episode" else "Chapter"
        }
        val suppliedTitle = listOf(titleName, title, range)
            .filterNotNull()
            .firstOrNull { it.isNotBlank() && it != UNNUMBERED_VOLUME.toString() && it != SPECIAL_NUMBER.toString() }
            .orEmpty()
        val cleanTitle = when (kind) {
            ChapterKind.SINGLE_FILE_VOLUME -> suppliedTitle.ifBlank { "$type $volumeNumber" }
            ChapterKind.SPECIAL -> suppliedTitle.ifBlank { "Special" }
            ChapterKind.ISSUE -> suppliedTitle.takeIf { it.any(Char::isLetter) } ?: "Issue #${chapterNumber.padStart(3, '0')}"
            ChapterKind.REGULAR -> suppliedTitle.ifBlank {
                if (context.isWebtoon) "Season $volumeNumber Episode $chapterNumber" else "Volume $volumeNumber Chapter $chapterNumber"
            }
            ChapterKind.CHAPTER -> suppliedTitle.ifBlank { "$type $chapterNumber" }
        }
        return ChapterTemplateVariables(
            type = type,
            number = if (kind == ChapterKind.SINGLE_FILE_VOLUME) volumeNumber else chapterNumber,
            title = suppliedTitle,
            cleanTitle = cleanTitle,
            pages = pages,
            size = files.orEmpty().sumOf { it.bytes },
            volume = volumeNumber,
            seriesName = context.seriesName,
            libraryName = context.libraryName,
            format = files.orEmpty().firstOrNull()?.extension?.trimStart('.')?.uppercase(Locale.ROOT) ?: formatName(format),
            created = createdUtc.orEmpty(),
            releaseDate = releaseDate.orEmpty(),
        )
    }
}

@Serializable
class MangaFileDto(
    val bytes: Long = 0,
    val extension: String = "",
)

class ChapterContext(
    val libraryType: Int,
    val libraryName: String,
    val seriesName: String,
    val isWebtoon: Boolean,
    val chapterTemplate: String,
    val scanlatorTemplate: String,
)

private enum class ChapterKind {
    REGULAR,
    CHAPTER,
    SINGLE_FILE_VOLUME,
    SPECIAL,
    ISSUE,
}

private class ChapterTemplateVariables(
    val type: String,
    val number: String,
    val title: String,
    val cleanTitle: String,
    val pages: Int,
    val size: Long,
    val volume: String,
    val seriesName: String,
    val libraryName: String,
    val format: String,
    val created: String,
    val releaseDate: String,
) {
    fun replacements() = mapOf(
        "Type" to type,
        "No" to number,
        "Title" to title,
        "CleanTitle" to cleanTitle,
        "Pages" to pages.takeIf { it > 0 }?.toString().orEmpty(),
        "Size" to size.takeIf { it > 0 }?.let { "%.1f MB".format(Locale.US, it / 1_048_576.0) }.orEmpty(),
        "Volume" to volume,
        "SeriesName" to seriesName,
        "LibraryName" to libraryName,
        "Format" to format,
        "Created" to created,
        "ReleaseDate" to releaseDate,
    )
}

private fun renderTemplate(template: String, variables: ChapterTemplateVariables): String {
    val escapedDollar = "\u0000"
    var result = template.replace("\\$", escapedDollar)
    variables.replacements().forEach { (key, value) ->
        result = result.replace(Regex("\\$$key(?!\\w)"), value)
    }
    return result
        .replace(Regex("\\$\\w+"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
        .replace(escapedDollar, "$")
}

private fun seriesCoverUrl(baseUrl: String, apiKey: String, seriesId: Int) = "$baseUrl/api/Image/series-cover".toHttpUrl().newBuilder()
    .addQueryParameter("seriesId", seriesId.toString())
    .addQueryParameter("apiKey", apiKey)
    .build()
    .toString()

fun formatName(format: Int) = when (format) {
    IMAGE -> "Image"
    ARCHIVE -> "Archive"
    EPUB -> "EPUB"
    PDF -> "PDF"
    else -> "Unknown"
}

private fun numberRange(min: Float, max: Float): String = when {
    max > min -> "${formatNumber(min)}-${formatNumber(max)}"
    else -> formatNumber(min)
}

private fun formatNumber(value: Float): String = if (value % 1F == 0F) value.toInt().toString() else value.toString().trimEnd('0').trimEnd('.')

private fun parseDate(value: String?): Long {
    if (value == null || value.startsWith("0001-")) return 0L
    return Instant.tryParse(value).takeIf { it != 0L }
        ?: DateTimeFormatter.ISO_LOCAL_DATE_TIME.tryParseDateTime(value, ZoneOffset.UTC)
}

private val DEMOGRAPHICS = listOf("Shounen", "Seinen", "Josei", "Shoujo", "Hentai", "Doujinshi")
private val FORMATS = listOf(
    "Long Strip",
    "4-koma",
    "4 Koma",
    "Full Color",
    "Full Colour",
    "Graphic Novel",
    "Manga",
    "Manhua",
    "Manhwa",
)

const val IMAGE = 0
const val ARCHIVE = 1
const val UNKNOWN = 2
const val EPUB = 3
const val PDF = 4

const val LIBRARY_MANGA = 0
const val LIBRARY_COMIC = 1
const val LIBRARY_COMIC_VINE = 5

const val AND = 1
const val SERIES_ENTITY = 0

const val EQUAL = 0
const val GREATER_THAN_EQUAL = 2
const val LESS_THAN_EQUAL = 4
const val CONTAINS = 5
const val MUST_CONTAINS = 6
const val MATCHES = 7
const val NOT_CONTAINS = 8

const val SERIES_NAME = 1
const val PUBLICATION_STATUS = 2
const val LANGUAGES = 3
const val AGE_RATING = 4
const val USER_RATING = 5
const val TAGS = 6
const val COLLECTIONS = 7
const val TRANSLATORS = 8
const val CHARACTERS = 9
const val PUBLISHERS = 10
const val EDITORS = 11
const val COVER_ARTISTS = 12
const val LETTERERS = 13
const val COLORISTS = 14
const val INKERS = 15
const val PENCILLERS = 16
const val WRITERS = 17
const val GENRES = 18
const val LIBRARIES = 19
const val READ_PROGRESS = 20
const val FORMATS_FIELD = 21
const val RELEASE_YEAR = 22

const val SORT_NAME = 1
const val CREATED_DATE = 2
const val LAST_MODIFIED_DATE = 3
const val LAST_CHAPTER_ADDED = 4
const val TIME_TO_READ = 5
const val RELEASE_YEAR_SORT = 6
const val READ_PROGRESS_SORT = 7
const val AVERAGE_RATING = 8
const val RANDOM = 9
const val USER_RATING_SORT = 10
const val UNREAD_CHAPTER_COUNT = 11

const val UNNUMBERED_VOLUME = -100_000
const val SPECIAL_NUMBER = 100_000
const val VOLUME_NUMBER_OFFSET = 10_000F
const val SPECIAL_NUMBER_OFFSET = 10_000_000_000F
