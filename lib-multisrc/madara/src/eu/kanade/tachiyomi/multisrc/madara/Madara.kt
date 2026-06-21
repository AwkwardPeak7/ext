package eu.kanade.tachiyomi.multisrc.madara

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.lib.cookieinterceptor.CookieInterceptor
import keiyoushi.network.get
import keiyoushi.network.head
import keiyoushi.network.post
import keiyoushi.source.KeiSource
import keiyoushi.utils.firstInstance
import keiyoushi.utils.firstInstanceOrNull
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonElement
import keiyoushi.utils.tryParse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

abstract class Madara(
    override val name: String,
    override val baseUrl: String,
    override val lang: String,
    protected val dateFormat: SimpleDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US),
) : KeiSource() {

    override fun OkHttpClient.Builder.configureClient() = apply {
        addNetworkInterceptor(
            CookieInterceptor(
                domain = baseUrl.toHttpUrl().host,
                cookie = "wpmanga-adault" to "1",
            ),
        )
    }

    protected open val mangaDirectory: String = "manga"
    protected open val mangaGenreDirectory: String = "manga-genre"

    protected open val mangaListFetchMethod: MangaListFetchMethod = MangaListFetchMethod.AJAX_MADARA_LOAD_MORE

    protected open val filterNonMangaItems: Boolean = true

    /**
     * Determines how to fetch chapters from source
     * @see [ChapterFetchMethod]
     */
    protected open val chapterFetchMethod: ChapterFetchMethod = ChapterFetchMethod.MANGA_PAGE

    protected open val usePostIdForMangaRequests: Boolean = true

    override fun getHomeUrl(): String = "$baseUrl/$mangaDirectory/?post_type=wp-manga&m_orderby=views"

    protected open suspend fun fetchMangasFromAjax(query: String?, sort: String?, genre: String?, page: Int): MangasPage {
        val pageSize = 25
        val formBody = FormBody.Builder().apply {
            add("action", "madara_load_more")
            add("page", (page - 1).toString())
            add("template", "madara-core/content/content-archive")
            add("vars[template]", "archive")
            add("vars[post_type]", "wp-manga")
            add("vars[paged]", "1")
            add("vars[posts_per_page]", "$pageSize")
            add("vars[sidebar]", "full")
            add("vars[post_status]", "publish")
            add("vars[manga_archives_item_layout]", "big_thumbnail")
            add("vars[meta_query][relation]", "OR")

            if (filterNonMangaItems) {
                add("vars[meta_query][0][key]", "_wp_manga_chapter_type")
                add("vars[meta_query][0][value]", "manga")
            }

            when (sort) {
                MADARA_RELEVANCE_SORT, null -> {
                    // nothing
                }
                MADARA_VIEWS_SORT -> {
                    add("vars[orderby]", "meta_value_num")
                    add("vars[meta_key]", "_wp_manga_views")
                    add("vars[order]", "desc")
                }
                MADARA_RATING_SORT -> {
                    add("vars[orderby][query_avarage_reviews]", "DESC")
                    add("vars[orderby][query_total_reviews]", "DESC")
                    add("vars[meta_query][0][query_avarage_reviews][key]", "_manga_avarage_reviews")
                    add("vars[meta_query][0][query_total_reviews][key]", "_manga_total_votes")
                }
                MADARA_TRENDING_SORT -> {
                    add("vars[orderby]", "meta_value_num")
                    add("vars[meta_key]", "_wp_manga_week_views_value")
                    add("vars[order]", "desc")
                }
                MADARA_LATEST_SORT -> {
                    add("vars[orderby]", "meta_value_num")
                    add("vars[meta_key]", "_latest_update")
                    add("vars[order]", "desc")
                }
                MADARA_NEW_SORT -> {
                    add("vars[orderby]", "date")
                }
                MADARA_ALPHABET_SORT -> {
                    add("vars[orderby]", "post_title")
                    add("vars[order]", "ASC")
                }
                else -> throw IllegalArgumentException("Unknown sort: $sort")
            }

            if (query != null) {
                add("vars[s]", query.trim())
            } else if (genre != null) {
                add("vars[wp-manga-genre]", genre)
            }
        }.build()

        val xhrHeaders = headersBuilder()
            .set("X-Requested-With", "XMLHttpRequest")
            .build()

        client.post("$baseUrl/wp-admin/admin-ajax.php", xhrHeaders, formBody).use {
            val document = it.asJsoup()
            val manga = parseMangaList(document)

            return MangasPage(manga, hasNextPage = manga.size == pageSize)
        }
    }

    protected open suspend fun fetchMangasNoAjax(sort: String?, genre: String?, page: Int): MangasPage {
        val url = baseUrl.toHttpUrl().newBuilder().apply {
            if (genre != null) {
                addPathSegment(mangaGenreDirectory)
                addPathSegment(genre)
            } else {
                addPathSegment(mangaDirectory)
            }
            if (page > 1) {
                addPathSegment("page")
                addPathSegment(page.toString())
            }
            addPathSegment("")
            addQueryParameter("post_type", "wp-manga")
            when (sort) {
                MADARA_RELEVANCE_SORT, null -> {
                    // nothing
                }
                MADARA_VIEWS_SORT, MADARA_RATING_SORT, MADARA_TRENDING_SORT, MADARA_LATEST_SORT, MADARA_NEW_SORT, MADARA_ALPHABET_SORT -> {
                    addQueryParameter("m_orderby", sort)
                }
                else -> throw IllegalArgumentException("Unknown sort: $sort")
            }
        }.build()

        client.get(url).use {
            val document = it.asJsoup()
            val manga = parseMangaList(document)
            val hasNextPage = document.selectFirst("div.nav-previous, nav.navigation-ajax, a.nextpostslink") != null

            return MangasPage(manga, hasNextPage)
        }
    }

    protected open val mangaListSelector: String get() = if (filterNonMangaItems) {
        ".page-item-detail.manga div[id^=manga-item-]"
    } else {
        ".page-item-detail div[id^=manga-item-]"
    }

    protected open fun parseMangaList(document: Document): List<SManga> = document.select(mangaListSelector).map { element ->
        val urlElement = element.selectFirst("a")!!

        SManga.create().apply {
            url = element.attr("data-post-id")
            memo = buildJsonObject {
                put("slug", urlElement.absUrl("href").toHttpUrl().pathSegments[1])
            }
            title = urlElement.attr("title")
            thumbnail_url = element.selectFirst("img")?.imgAttr()
        }
    }

    protected open suspend fun fetchSearchMangasNoAjax(query: String): MangasPage {
        val url = "$baseUrl/".toHttpUrl().newBuilder()
            .addQueryParameter("s", query.trim())
            .addQueryParameter("post_type", "wp-manga")
            .build()

        client.get(url).use {
            val document = it.asJsoup()
            var manga = parseMangaList(document)
            if (manga.isEmpty()) {
                manga = parseSearchMangaList(document)
            }

            return MangasPage(manga, hasNextPage = false) // deliberately false
        }
    }

    protected open suspend fun parseSearchMangaList(document: Document): List<SManga> = coroutineScope {
        document.select("div.c-tabs-item__content").map { element ->
            async {
                val urlElement = element.selectFirst("div.post-title a")!!
                val slug = urlElement.absUrl("href").toHttpUrl().pathSegments[1]

                SManga.create().apply {
                    url = getMangaIdFromSlug(slug)
                    memo = buildJsonObject {
                        put("slug", slug)
                    }
                    title = urlElement.ownText()
                    thumbnail_url = element.selectFirst("img")?.imgAttr()
                }
            }
        }.awaitAll()
    }

    private val headProbeMutex = Mutex()
    private var headProbeSupportsShortlink: Boolean? = null

    protected suspend fun getMangaIdFromSlug(slug: String): String {
        val url = "$baseUrl/$mangaDirectory/$slug/"

        val supportsShortlink = headProbeMutex.withLock {
            if (headProbeSupportsShortlink == null) {
                try {
                    val response = client.head(url, ensureSuccess = false)
                    val link = response.header("Link")
                    response.close()

                    val match = link?.let { SHORTLINK_REGEX.find(it) }
                    headProbeSupportsShortlink = match != null

                    val p = match?.groupValues?.get(1)?.toHttpUrlOrNull()?.queryParameter("p")
                    if (p != null) return p
                } catch (_: Throwable) {
                    headProbeSupportsShortlink = false
                }
            }
            headProbeSupportsShortlink!!
        }

        if (supportsShortlink) {
            try {
                val response = client.head(url, ensureSuccess = false)
                val link = response.header("Link")
                response.close()

                val match = link?.let { SHORTLINK_REGEX.find(it) }
                val p = match?.groupValues?.get(1)?.toHttpUrlOrNull()?.queryParameter("p")
                if (p != null) return p
            } catch (_: Throwable) {
            }
        }

        val response = client.get(url)
        val document = response.asJsoup()
        response.close()

        val id = document.selectFirst("div#manga-chapters-holder")?.attr("data-id")?.takeIf { it.isNotEmpty() }
            ?: document.selectFirst("input.rating-post-id")?.attr("value")?.takeIf { it.isNotEmpty() }
            ?: document.selectFirst("a[data-post]")?.attr("data-post")?.takeIf { it.isNotEmpty() }
            ?: document.selectFirst("link[rel=shortlink][href*='?p=']")
                ?.attr("href")
                ?.takeIf { it.isNotEmpty() }
                ?.toHttpUrlOrNull()
                ?.queryParameter("p")

        return id ?: throw Exception("Failed to extract manga ID from page: $url")
    }

    override suspend fun getPopularManga(page: Int): MangasPage = when (mangaListFetchMethod) {
        MangaListFetchMethod.MANGA_LIST_PAGE -> fetchMangasNoAjax(MADARA_VIEWS_SORT, null, page)
        MangaListFetchMethod.AJAX_MADARA_LOAD_MORE -> fetchMangasFromAjax(null, MADARA_VIEWS_SORT, null, page)
    }

    override suspend fun getLatestUpdates(page: Int): MangasPage = when (mangaListFetchMethod) {
        MangaListFetchMethod.MANGA_LIST_PAGE -> fetchMangasNoAjax(MADARA_LATEST_SORT, null, page)
        MangaListFetchMethod.AJAX_MADARA_LOAD_MORE -> fetchMangasFromAjax(null, MADARA_LATEST_SORT, null, page)
    }

    override suspend fun getSearchMangaList(page: Int, query: String, filterList: FilterList): MangasPage {
        val sort = filterList.firstInstance<SortFilter>().sort
        val genre = filterList.firstInstanceOrNull<GenreFilter>()?.genre

        return when (mangaListFetchMethod) {
            MangaListFetchMethod.MANGA_LIST_PAGE -> {
                if (query.isBlank()) {
                    fetchMangasNoAjax(sort, genre, page)
                } else {
                    fetchSearchMangasNoAjax(query)
                }
            }
            MangaListFetchMethod.AJAX_MADARA_LOAD_MORE -> fetchMangasFromAjax(query.takeIf(String::isNotBlank), sort, genre, page)
        }
    }

    override suspend fun getMangaByUrl(url: HttpUrl): SManga? = null

    override val supportsFilterFetching = true

    override suspend fun fetchFilterData(): JsonElement? = client.get("$baseUrl/$mangaDirectory/").use {
        val document = it.asJsoup()

        document.select("div.genres a[href*='/$mangaGenreDirectory/']").map { element ->
            val slug = element.absUrl("href").toHttpUrl().pathSegments[1]
            val name = element.ownText()
            name to slug
        }.toJsonElement()
    }

    override fun getFilterList(data: JsonElement?): FilterList {
        val filters = buildList {
            add(SortFilter())
            data?.also {
                val genres = buildList {
                    add("" to "")
                    addAll(it.parseAs<List<Pair<String, String>>>())
                }
                add(GenreFilter(genres))
                add(Filter.Separator())
                add(Filter.Header("Genre Filter doesn't work with text search"))
            }
        }

        return FilterList(filters)
    }

    override suspend fun getMangaUpdate(
        manga: SManga,
        chapters: List<SChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): SMangaUpdate {
        val document = if (fetchDetails || chapterFetchMethod == ChapterFetchMethod.MANGA_PAGE) {
            val url = if (usePostIdForMangaRequests) {
                "$baseUrl/?p=${manga.url}"
            } else {
                getMangaUrl(manga)
            }

            client.get(url).asJsoup()
        } else {
            null
        }

        val updatedManga = document?.let { mangaDetailsParse(it) } ?: manga

        val updatedChapters = if (fetchChapters) {
            val slug = updatedManga.memo["slug"]!!.jsonPrimitive.content

            when (chapterFetchMethod) {
                ChapterFetchMethod.MANGA_PAGE -> parseChapters(document!!)
                ChapterFetchMethod.AJAX_V1 -> getChaptersAjaxV1(manga.url, slug)
                ChapterFetchMethod.AJAX_V2 -> getChaptersAjaxV2(slug)
                ChapterFetchMethod.AJAX_V2_MULTIPAGE -> getChaptersAjaxV2MultiPage(slug)
            }
        } else {
            chapters
        }

        return SMangaUpdate(updatedManga, updatedChapters)
    }

    protected open fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        with(document) {
            url = document.selectFirst("div#manga-chapters-holder")?.attr("data-id")?.takeIf { it.isNotEmpty() }
                ?: document.selectFirst("input.rating-post-id")?.attr("value")?.takeIf { it.isNotEmpty() }
                ?: document.selectFirst("a[data-post]")?.attr("data-post")?.takeIf { it.isNotEmpty() }
                ?: document.selectFirst("link[rel=shortlink][href*='?p=']")
                    ?.attr("href")
                    ?.takeIf { it.isNotEmpty() }
                    ?.toHttpUrlOrNull()
                    ?.queryParameter("p")!!

            memo = buildJsonObject {
                put("slug", document.location().toHttpUrl().pathSegments[1])
            }

            title = selectFirst(mangaDetailsSelectorTitle)!!.ownText()

            select(mangaDetailsSelectorAuthor).eachText()
                .filter { it.notUpdating() }
                .joinToString()
                .takeIf { it.isNotBlank() }
                ?.let { author = it }

            select(mangaDetailsSelectorArtist)
                .eachText()
                .filter { it.notUpdating() }
                .joinToString()
                .takeIf { it.isNotBlank() }
                ?.let { artist = it }

            selectFirst(mangaDetailsSelectorThumbnail)?.let {
                thumbnail_url = it.imgAttr()
            }

            select(mangaDetailsSelectorStatus).last()?.let {
                status = with(it.text().filter { ch -> ch.isLetterOrDigit() || ch.isWhitespace() }.trim()) {
                    when {
                        containsIn(completedStatusList) -> SManga.COMPLETED
                        containsIn(ongoingStatusList) -> SManga.ONGOING
                        containsIn(hiatusStatusList) -> SManga.ON_HIATUS
                        containsIn(canceledStatusList) -> SManga.CANCELLED
                        else -> SManga.UNKNOWN
                    }
                }
            }

            val genres = select(mangaDetailsSelectorGenre)
                .map { element -> element.text() }
                .toMutableList()

            if (mangaDetailsSelectorTag.isNotEmpty()) {
                select(mangaDetailsSelectorTag).forEach { element ->
                    if (
                        element.text().length <= 25 &&
                        element.text().contains("read", true).not() &&
                        element.text().contains(name, true).not() &&
                        element.text().contains(name.replace(" ", ""), true).not() &&
                        element.text().contains(title, true).not()
                    ) {
                        genres.add(element.text())
                    }
                }
            }

            document.selectFirst(seriesTypeSelector)?.ownText()?.let {
                if (it.isEmpty().not() && it.notUpdating() && it != "-") {
                    genres.add(it)
                }
            }

            genre = genres.distinctBy(String::lowercase).joinToString()

            description = buildString {
                select(mangaDetailsSelectorDescription).let { desc ->
                    desc.forEach {
                        append(it.wholeText().trim())
                        append("\n\n")
                    }
                }
                document.selectFirst(altNameSelector)?.ownText()?.let { altName ->
                    if (altName.isNotBlank() && altName.notUpdating()) {
                        append("Alternative Names:\n")
                        altName.split(",").forEach {
                            append("- ")
                            append(it.trim())
                            append("\n")
                        }
                    }
                }
            }.trim()
        }
    }
    protected val completedStatusList: Array<String> = arrayOf(
        "Completed",
        "Completo",
        "Completado",
        "Concluído",
        "Concluido",
        "Finalizado",
        "Achevé",
        "Terminé",
        "Hoàn Thành",
        "مكتملة",
        "مكتمل",
        "已完结",
        "Tamamlandı",
        "Đã hoàn thành",
        "Завершено",
        "Tamamlanan",
        "Complété",
    )

    protected val ongoingStatusList: Array<String> = arrayOf(
        "OnGoing", "Продолжается", "Updating", "Em Lançamento", "Em lançamento", "Em andamento",
        "Em Andamento", "En cours", "En Cours", "En cours de publication", "Ativo", "Lançando", "Đang Tiến Hành", "Còn Nữa", "Devam Ediyor",
        "Devam ediyor", "In Corso", "In Arrivo", "مستمرة", "مستمر", "En Curso", "En curso", "Emision",
        "Curso", "En marcha", "Publicandose", "Publicándose", "En emision", "连载中", "Em Lançamento", "Devam Ediyo",
        "Đang làm", "Em postagem", "Devam Eden", "Em progresso", "Em curso", "Atualizações Semanais",
    )

    protected val hiatusStatusList: Array<String> = arrayOf(
        "On Hold",
        "Pausado",
        "En espera",
        "Durduruldu",
        "Beklemede",
        "Đang chờ",
        "متوقف",
        "En Pause",
        "Заморожено",
        "En attente",
    )

    protected val canceledStatusList: Array<String> = arrayOf(
        "Canceled",
        "Cancelado",
        "İptal Edildi",
        "Güncel",
        "Đã hủy",
        "ملغي",
        "Abandonné",
        "Заброшено",
        "Annulé",
    )

    open val mangaDetailsSelectorTitle = "div.post-title h3, div.post-title h1, #manga-title > h1"
    open val mangaDetailsSelectorAuthor = "div.author-content > a, div.manga-authors > a"
    open val mangaDetailsSelectorArtist = "div.artist-content > a"
    open val mangaDetailsSelectorStatus = "div.summary-content, div.summary-heading:contains(Status) + div"
    open val mangaDetailsSelectorDescription = "div.description-summary div.summary__content, div.summary_content div.post-content_item > h5 + div, div.summary_content div.manga-excerpt"
    open val mangaDetailsSelectorThumbnail = "div.summary_image img"
    open val mangaDetailsSelectorGenre = "div.genres-content a"
    open val mangaDetailsSelectorTag = "div.tags-content a"

    open val seriesTypeSelector = ".post-content_item:contains(Type) .summary-content"
    open val altNameSelector = ".post-content_item:contains(Alt) .summary-content"
    open val updatingRegex = "Updating|Atualizando".toRegex(RegexOption.IGNORE_CASE)

    fun String.notUpdating(): Boolean = this.contains(updatingRegex).not()

    private fun String.containsIn(array: Array<String>): Boolean = this.lowercase() in array.map { it.lowercase() }

    protected open suspend fun getChaptersAjaxV1(id: String, slug: String): List<SChapter> {
        val form = FormBody.Builder()
            .add("action", "manga_get_chapters")
            .add("manga", id)
            .build()
        val xhrHeaders = headersBuilder()
            .set("Referer", "$baseUrl/$mangaDirectory/$slug")
            .set("X-Requested-With", "XMLHttpRequest")
            .build()

        client.post("$baseUrl/wp-admin/admin-ajax.php", xhrHeaders, form).use {
            return parseChapters(it.asJsoup())
        }
    }

    protected open suspend fun getChaptersAjaxV2(slug: String): List<SChapter> {
        val emptyForm = FormBody.Builder().build()
        val xhrHeaders = headersBuilder()
            .set("Referer", "$baseUrl/$mangaDirectory/$slug")
            .set("X-Requested-With", "XMLHttpRequest")
            .build()

        client.post("$baseUrl/$mangaDirectory/$slug/ajax/chapters/", xhrHeaders, emptyForm).use {
            return parseChapters(it.asJsoup())
        }
    }

    protected open suspend fun getChaptersAjaxV2MultiPage(slug: String): List<SChapter> {
        val emptyForm = FormBody.Builder().build()
        val xhrHeaders = headersBuilder()
            .set("Referer", "$baseUrl/$mangaDirectory/$slug")
            .set("X-Requested-With", "XMLHttpRequest")
            .build()

        val chapters = mutableListOf<SChapter>()
        var page = 1
        while (true) {
            client.post("$baseUrl/$mangaDirectory/$slug/ajax/chapters/?t=$page", xhrHeaders, emptyForm).use {
                val document = it.asJsoup()
                val currentPage = parseChapters(document)

                if (currentPage.isNotEmpty()) {
                    chapters.addAll(currentPage)
                    page++
                } else {
                    break
                }
            }
        }

        return chapters
    }

    protected open val chapterListSelector = "li.wp-manga-chapter"
    protected open val chapterDateSelector = "span.chapter-release-date"

    protected open fun parseChapters(document: Document): List<SChapter> = document.select(chapterListSelector).map { element ->
        val urlElement = element.selectFirst("a")!!
        val slug = urlElement.absUrl("href").toHttpUrl().pathSegments.last()

        val title = urlElement.text().substringAfter('-', "").takeIf(String::isNotBlank)
        val number = parseChapterNumberFromSlug(slug)

        SChapter.create().apply {
            url = slug
            name = buildString {
                if (title != null) {
                    if (title.contains(number.toString())) {
                        append(title)
                    } else {
                        append("Ch. ")
                        append(number)
                    }
                } else {
                    append("Ch. ")
                    append(number)
                }
            }
            number?.also { chapter_number = it }
            date_upload = element.selectFirst("img:not(.thumb)")?.attr("alt")?.let { parseRelativeDate(it) }
                ?: element.selectFirst("span a")?.attr("title")?.let { parseRelativeDate(it) }
                ?: parseChapterDate(element.selectFirst(chapterDateSelector)?.text())
        }
    }

    /**
     * 	Chapter number is first occurrence of a number in the last element of url when split with "/" (aka the slug)
     * 	e.g.
     * 	one-piece-color-jk-english/volume-20-showdown-at-alubarna/chapter-177-30-million-vs-81-million/
     * 	has chapter number 177
     * 	parasite-chromatique-french/volume-10/chapitre-062-5/
     * 	has chapter number 62.5
     *
     * 	This method takes the last part of the url aka the slug and returns the number
     */
    protected fun parseChapterNumberFromSlug(slug: String): Float? {
        val cleanSlug = slug.split('_', '/')[0]
        val matches = CHAPTER_REGEX.findAll(cleanSlug).iterator()

        if (!matches.hasNext()) return null

        val firstValue = matches.next().value.toFloat()

        return if (matches.hasNext()) {
            firstValue + (matches.next().value.toFloat() / 10f)
        } else {
            firstValue
        }
    }

    protected open fun parseChapterDate(date: String?): Long {
        date ?: return 0

        return when {
            // Handle 'yesterday' and 'today', using midnight
            WordSet("yesterday", "يوم واحد").startsWith(date) -> {
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -1) // yesterday
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            WordSet("today").startsWith(date) -> {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            WordSet("يومين").startsWith(date) -> {
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -2) // day before yesterday
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            WordSet("ago", "atrás", "önce", "قبل", "trước").endsWith(date) -> {
                parseRelativeDate(date)
            }

            WordSet("hace", "năm", "tháng", "tuần", "ngày", "giờ", "phút", "giây").startsWith(date) -> {
                parseRelativeDate(date)
            }

            // Handle "jour" with a number before it
            date.contains(Regex("""\b\d+ jour""")) -> {
                parseRelativeDate(date)
            }

            date.contains(Regex("""\d(st|nd|rd|th)""")) -> {
                // Clean date (e.g. 5th December 2019 to 5 December 2019) before parsing it
                date.split(" ").map {
                    if (it.contains(Regex("""\d\D\D"""))) {
                        it.replace(Regex("""\D"""), "")
                    } else {
                        it
                    }
                }
                    .let { dateFormat.tryParse(it.joinToString(" ")) }
            }

            else -> dateFormat.tryParse(date)
        }
    }

    // Parses dates in this form:
    // 21 horas ago
    protected open fun parseRelativeDate(date: String): Long {
        val number = Regex("""(\d+)""").find(date)?.value?.toIntOrNull() ?: return 0
        val cal = Calendar.getInstance()

        return when {
            WordSet("hari", "gün", "jour", "día", "dia", "day", "วัน", "ngày", "giorni", "أيام", "天").anyWordIn(date) -> cal.apply { add(Calendar.DAY_OF_MONTH, -number) }.timeInMillis
            WordSet("jam", "saat", "heure", "hora", "hour", "ชั่วโมง", "giờ", "ore", "ساعة", "小时").anyWordIn(date) -> cal.apply { add(Calendar.HOUR, -number) }.timeInMillis
            WordSet("menit", "dakika", "min", "minute", "minuto", "นาที", "دقائق", "phút").anyWordIn(date) -> cal.apply { add(Calendar.MINUTE, -number) }.timeInMillis
            WordSet("detik", "segundo", "second", "วินาที", "giây").anyWordIn(date) -> cal.apply { add(Calendar.SECOND, -number) }.timeInMillis
            WordSet("week", "semana", "tuần").anyWordIn(date) -> cal.apply { add(Calendar.DAY_OF_MONTH, -number * 7) }.timeInMillis
            WordSet("month", "mes", "tháng").anyWordIn(date) -> cal.apply { add(Calendar.MONTH, -number) }.timeInMillis
            WordSet("year", "año", "năm").anyWordIn(date) -> cal.apply { add(Calendar.YEAR, -number) }.timeInMillis
            else -> 0
        }
    }

    override fun getMangaUrl(manga: SManga): String {
        val slug = manga.memo["slug"]!!.jsonPrimitive.content

        return "$baseUrl/$mangaDirectory/$slug/"
    }

    override suspend fun fetchRelatedMangaList(manga: SManga): List<SManga> = emptyList()

    override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()

    override fun getChapterUrl(chapter: SChapter): String = ""

    protected open fun Element.imgAttr(): String? = when {
        hasAttr("data-src") -> absUrl("data-src")
        hasAttr("data-lazy-src") -> absUrl("data-lazy-src")
        hasAttr("data-cfsrc") -> absUrl("data-cfsrc")
        hasAttr("data-manga-src") -> absUrl("data-manga-src")
        hasAttr("data-srcset") -> attr("data-srcset").getSrcSetImage()
        hasAttr("srcset") -> attr("srcset").getSrcSetImage()
        else -> absUrl("src")
    }

    protected open fun String.getSrcSetImage(): String? = this.split(" ")
        .filter(URL_REGEX::matches)
        .maxOfOrNull(String::toString)
}

enum class MangaListFetchMethod {
    MANGA_LIST_PAGE,
    AJAX_MADARA_LOAD_MORE,
}

enum class ChapterFetchMethod {
    /**
     * All chapters are embedded directly on manga page HTML
     */
    MANGA_PAGE,

    /**
     * All chapters are fetched via **WordPress Admin AJAX endpoint**
     * `manga_get_chapters` action POST call to `/wp-admin/admin-ajax.php`
     */
    AJAX_V1,

    /**
     * All chapters are fetched via *WordPress Chapter AJAX endpoint**
     * GET call to `/manga/$slug/ajax/chapters/
     */
    AJAX_V2,

    /**
     * Chapters are fetched via paginated *WordPress Chapter AJAX endpoint**
     * multiple GET calls to `/manga/$slug/ajax/chapters/?t=$page
     */
    AJAX_V2_MULTIPAGE,
}

val URL_REGEX = """^(https?://[^\s/$.?#].\S*)$""".toRegex()
val SHORTLINK_REGEX = Regex("""<([^>]+)>;\s*rel="?shortlink"?""", RegexOption.IGNORE_CASE)
val CHAPTER_REGEX = """\d+(?:\.\d+)?""".toRegex()

/*
abstract class MadaraOld(
    override val name: String,
    override val baseUrl: String,
    final override val lang: String,
    protected val dateFormat: SimpleDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US),
) : HttpSource() {

    override val supportsLatest = true

    override fun headersBuilder() = super.headersBuilder()
        .add("Referer", "$baseUrl/")

    protected val xhrHeaders by lazy {
        headersBuilder()
            .set("X-Requested-With", "XMLHttpRequest")
            .build()
    }

    protected open val json: Json by injectLazy()

    protected val intl = Intl(
        language = lang,
        baseLanguage = "en",
        availableLanguages = setOf("en", "pt-BR", "es"),
        classLoader = this::class.java.classLoader!!,
    )

    /**
 * If enabled, will attempt to remove non-manga items in popular and latest.
 * The filter will not be used in search as the theme doesn't set the CSS class.
 * Can be disabled if the source incorrectly sets the entry types.
 */
    protected open val filterNonMangaItems = true

    /**
 * The CSS selector used to filter manga items in popular and latest
 * if `filterNonMangaItems` is set to `true`. Can be override if needed.
 * If the flag is set to `false`, it will be empty by default.
 */
    protected open val mangaEntrySelector: String by lazy {
        if (filterNonMangaItems) ".manga" else ""
    }

    /**
 * Automatically fetched genres from the source to be used in the filters.
 */
    protected open var genresList: List<Genre> = emptyList()

    /**
 * Whether genres have been fetched
 */
    private var genresFetched: Boolean = false

    /**
 * Inner variable to control how much tries the genres request was called.
 */
    private var fetchGenresAttempts: Int = 0

    /**
 * Disable it if you don't want the genres to be fetched.
 */
    protected open val fetchGenres: Boolean = true

    /**
 * The path used in the URL for the manga pages. Can be
 * changed if needed as some sites modify it to other words.
 */
    protected open val mangaSubString = "manga"

    /**
 * enable if the site use "madara_load_more" to load manga on the site
 * Typically has "load More" instead of next/previous page
 *
 * with LoadMoreStrategy.AutoDetect it tries to detect if site uses `madara_load_more`
 */
    protected open val useLoadMoreRequest = LoadMoreStrategy.AutoDetect

    enum class LoadMoreStrategy {
        AutoDetect,
        Always,
        Never,
    }

    /**
 * internal variable to save if site uses load_more or not
 */
    private var loadMoreRequestDetected = LoadMoreDetection.Pending

    private enum class LoadMoreDetection {
        Pending,
        True,
        False,
    }

    protected fun detectLoadMore(document: Document) {
        if (useLoadMoreRequest == LoadMoreStrategy.AutoDetect &&
            loadMoreRequestDetected == LoadMoreDetection.Pending
        ) {
            loadMoreRequestDetected = when (document.selectFirst("nav.navigation-ajax") != null) {
                true -> LoadMoreDetection.True
                false -> LoadMoreDetection.False
            }
        }
    }

    protected fun useLoadMoreRequest(): Boolean = when (useLoadMoreRequest) {
        LoadMoreStrategy.Always -> true
        LoadMoreStrategy.Never -> false
        else -> loadMoreRequestDetected == LoadMoreDetection.True
    }

    // Popular Manga

    override fun popularMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()

        val entries = document.select(popularMangaSelector())
            .map(::popularMangaFromElement)
        val hasNextPage = popularMangaNextPageSelector()?.let { document.selectFirst(it) } != null

        detectLoadMore(document)

        return MangasPage(entries, hasNextPage)
    }

    // exclude/filter bilibili manga from list
    protected open fun popularMangaSelector() = "div.page-item-detail:not(:has(a[href*='bilibilicomics.com']))$mangaEntrySelector , .manga__item"

    open val popularMangaUrlSelector = "div.post-title a"
    open val popularMangaUrlSelectorImg = "img"

    protected open fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()

        with(element) {
            selectFirst(popularMangaUrlSelector)!!.let {
                manga.setUrlWithoutDomain(it.attr("abs:href"))
                manga.title = it.ownText()
            }

            selectFirst(popularMangaUrlSelectorImg)?.let {
                manga.thumbnail_url = processThumbnail(imageFromElement(it), true)
            }
        }

        return manga
    }

    override fun popularMangaRequest(page: Int): Request = if (useLoadMoreRequest()) {
        loadMoreRequest(page, popular = true)
    } else {
        GET("$baseUrl/$mangaSubString/${searchPage(page)}?m_orderby=views", headers)
    }

    protected open fun popularMangaNextPageSelector(): String? = if (useLoadMoreRequest()) {
        "body:not(:has(.no-posts))"
    } else {
        "div.nav-previous, nav.navigation-ajax, a.nextpostslink"
    }

    // Related Manga
    protected open fun relatedMangaSelector() = ".related-reading-wrap"

    override fun relatedMangaListParse(response: Response): List<SManga> {
        val document = response.asJsoup()
        return document.select(relatedMangaSelector())
            .mapNotNull { manga ->
                SManga.create().apply {
                    manga.selectFirst(".widget-title a")?.let {
                        setUrlWithoutDomain(it.attr("abs:href"))
                        title = it.ownText()
                    } ?: return@mapNotNull null
                    manga.selectFirst(".widget-thumbnail img")?.let {
                        thumbnail_url = processThumbnail(imageFromElement(it), true)
                    }
                }
            }
    }

    // Latest Updates

    protected open fun latestUpdatesSelector() = popularMangaSelector()

    protected open fun latestUpdatesFromElement(element: Element): SManga {
        // Even if it's different from the popular manga's list, the relevant classes are the same
        return popularMangaFromElement(element)
    }

    override fun latestUpdatesRequest(page: Int): Request = if (useLoadMoreRequest()) {
        loadMoreRequest(page, popular = false)
    } else {
        GET("$baseUrl/$mangaSubString/${searchPage(page)}?m_orderby=latest", headers)
    }

    protected open fun latestUpdatesNextPageSelector(): String? = popularMangaNextPageSelector()

    override fun latestUpdatesParse(response: Response): MangasPage {
        val mp = popularMangaParse(response)
        val mangas = mp.mangas.distinctBy { it.url }
        return MangasPage(mangas, mp.hasNextPage)
    }

    // load more
    protected open fun loadMoreRequest(page: Int, popular: Boolean): Request {
        val formBody = FormBody.Builder().apply {
            add("action", "madara_load_more")
            add("page", (page - 1).toString())
            add("template", "madara-core/content/content-archive")
            add("vars[orderby]", "meta_value_num")
            add("vars[paged]", "1")

            if (filterNonMangaItems) {
                add("vars[meta_query][0][key]", "_wp_manga_chapter_type")
                add("vars[meta_query][0][value]", "manga")
            }

            add("vars[post_type]", "wp-manga")
            add("vars[post_status]", "publish")
            add("vars[meta_key]", if (popular) "_wp_manga_views" else "_latest_update")
            add("vars[order]", "desc")
            add("vars[sidebar]", "right")
            add("vars[manga_archives_item_layout]", "big_thumbnail")
        }.build()

        return POST("$baseUrl/wp-admin/admin-ajax.php", xhrHeaders, formBody)
    }

    // Search Manga

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        if (query.startsWith("https://")) {
            val url = query.toHttpUrl()
            if (url.host != baseUrl.toHttpUrl().host) {
                throw Exception("Unsupported url")
            }
            if (url.pathSegments.size < 2) {
                throw Exception("Unsupported url")
            }
            val slug = url.pathSegments[1]
            return fetchSearchManga(page, "$URL_SEARCH_PREFIX$slug", filters)
        }
        if (query.startsWith(URL_SEARCH_PREFIX)) {
            val mangaUrl = baseUrl.toHttpUrl().newBuilder().apply {
                addPathSegment(mangaSubString)
                addPathSegment(query.substringAfter(URL_SEARCH_PREFIX))
                addPathSegment("") // add trailing slash
            }.build()
            return client.newCall(GET(mangaUrl, headers))
                .asObservableSuccess().map { response ->
                    val manga = mangaDetailsParse(response).apply {
                        setUrlWithoutDomain(mangaUrl.toString())
                        initialized = true
                    }

                    MangasPage(listOf(manga), false)
                }
        }

        return super.fetchSearchManga(page, query, filters)
    }

    protected open fun searchPage(page: Int): String = if (page == 1) {
        ""
    } else {
        "page/$page/"
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request = if (useLoadMoreRequest()) {
        searchLoadMoreRequest(page, query, filters)
    } else {
        searchRequest(page, query, filters)
    }

    protected open fun searchRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/${searchPage(page)}".toHttpUrl().newBuilder()
        url.addQueryParameter("s", query)
        url.addQueryParameter("post_type", "wp-manga")
        filters.forEach { filter ->
            when (filter) {
                is AuthorFilter -> {
                    if (filter.state.isNotBlank()) {
                        url.addQueryParameter("author", filter.state)
                    }
                }

                is ArtistFilter -> {
                    if (filter.state.isNotBlank()) {
                        url.addQueryParameter("artist", filter.state)
                    }
                }

                is YearFilter -> {
                    if (filter.state.isNotBlank()) {
                        url.addQueryParameter("release", filter.state)
                    }
                }

                is StatusFilter -> {
                    filter.state.forEach {
                        if (it.state) {
                            url.addQueryParameter("status[]", it.id)
                        }
                    }
                }

                is OrderByFilter -> {
                    if (filter.state != 0) {
                        url.addQueryParameter("m_orderby", filter.toUriPart())
                    }
                }

                is AdultContentFilter -> {
                    url.addQueryParameter("adult", filter.toUriPart())
                }

                is GenreConditionFilter -> {
                    url.addQueryParameter("op", filter.toUriPart())
                }

                is GenreList -> {
                    filter.state
                        .filter { it.state }
                        .let { list ->
                            if (list.isNotEmpty()) {
                                list.forEach { genre -> url.addQueryParameter("genre[]", genre.id) }
                            }
                        }
                }

                else -> {}
            }
        }
        return GET(url.build(), headers)
    }

    protected open fun searchLoadMoreRequest(page: Int, query: String, filters: FilterList): Request {
        val formBody = FormBody.Builder().apply {
            add("action", "madara_load_more")
            add("page", (page - 1).toString())
            add("template", "madara-core/content/content-search")
            add("vars[paged]", "1")
            add("vars[template]", "archive")
            add("vars[sidebar]", "right")
            add("vars[post_type]", "wp-manga")
            add("vars[post_status]", "publish")
            add("vars[manga_archives_item_layout]", "big_thumbnail")

            if (filterNonMangaItems) {
                add("vars[meta_query][0][key]", "_wp_manga_chapter_type")
                add("vars[meta_query][0][value]", "manga")
            }

            add("vars[s]", query)

            var metaQueryIdx = if (filterNonMangaItems) 1 else 0
            var taxQueryIdx = 0
            val genres = filters.filterIsInstance<GenreList>().firstOrNull()?.state
                ?.filter { it.state }
                ?.map { it.id }
                .orEmpty()

            filters.forEach { filter ->
                when (filter) {
                    is AuthorFilter -> {
                        if (filter.state.isNotBlank()) {
                            add("vars[tax_query][$taxQueryIdx][taxonomy]", "wp-manga-author")
                            add("vars[tax_query][$taxQueryIdx][field]", "name")
                            add("vars[tax_query][$taxQueryIdx][terms]", filter.state)

                            taxQueryIdx++
                        }
                    }

                    is ArtistFilter -> {
                        if (filter.state.isNotBlank()) {
                            add("vars[tax_query][$taxQueryIdx][taxonomy]", "wp-manga-artist")
                            add("vars[tax_query][$taxQueryIdx][field]", "name")
                            add("vars[tax_query][$taxQueryIdx][terms]", filter.state)

                            taxQueryIdx++
                        }
                    }

                    is YearFilter -> {
                        if (filter.state.isNotBlank()) {
                            add("vars[tax_query][$taxQueryIdx][taxonomy]", "wp-manga-release")
                            add("vars[tax_query][$taxQueryIdx][field]", "name")
                            add("vars[tax_query][$taxQueryIdx][terms]", filter.state)

                            taxQueryIdx++
                        }
                    }

                    is StatusFilter -> {
                        val statuses = filter.state
                            .filter { it.state }
                            .map { it.id }

                        if (statuses.isNotEmpty()) {
                            add("vars[meta_query][$metaQueryIdx][key]", "_wp_manga_status")

                            statuses.forEachIndexed { i, slug ->
                                add("vars[meta_query][$metaQueryIdx][value][$i]", slug)
                            }

                            metaQueryIdx++
                        }
                    }

                    is OrderByFilter -> {
                        if (filter.state != 0) {
                            when (filter.toUriPart()) {
                                "latest" -> {
                                    add("vars[orderby]", "meta_value_num")
                                    add("vars[order]", "DESC")
                                    add("vars[meta_key]", "_latest_update")
                                }

                                "alphabet" -> {
                                    add("vars[orderby]", "post_title")
                                    add("vars[order]", "ASC")
                                }

                                "rating" -> {
                                    add("vars[orderby][query_average_reviews]", "DESC")
                                    add("vars[orderby][query_total_reviews]", "DESC")
                                }

                                "trending" -> {
                                    add("vars[orderby]", "meta_value_num")
                                    add("vars[meta_key]", "_wp_manga_week_views_value")
                                    add("vars[order]", "DESC")
                                }

                                "views" -> {
                                    add("vars[orderby]", "meta_value_num")
                                    add("vars[meta_key]", "_wp_manga_views")
                                    add("vars[order]", "DESC")
                                }

                                else -> {
                                    add("vars[orderby]", "date")
                                    add("vars[order]", "DESC")
                                }
                            }
                        }
                    }

                    is AdultContentFilter -> {
                        if (filter.state != 0) {
                            add("vars[meta_query][$metaQueryIdx][key]", "manga_adult_content")
                            add(
                                "vars[meta_query][$metaQueryIdx][compare]",
                                if (filter.state == 1) "not exists" else "exists",
                            )

                            metaQueryIdx++
                        }
                    }

                    is GenreConditionFilter -> {
                        if (filter.state == 1 && genres.isNotEmpty()) {
                            add("vars[tax_query][$taxQueryIdx][operation]", "AND")
                        }
                    }

                    is GenreList -> {
                        if (genres.isNotEmpty()) {
                            add("vars[tax_query][$taxQueryIdx][taxonomy]", "wp-manga-genre")
                            add("vars[tax_query][$taxQueryIdx][field]", "slug")

                            genres.forEachIndexed { i, slug ->
                                add("vars[tax_query][$taxQueryIdx][terms][$i]", slug)
                            }

                            taxQueryIdx++
                        }
                    }

                    else -> {}
                }
            }
        }.build()

        return POST("$baseUrl/wp-admin/admin-ajax.php", xhrHeaders, formBody)
    }

    protected open val statusFilterOptions: Map<String, String> =
        mapOf(
            intl["status_filter_completed"] to "end",
            intl["status_filter_ongoing"] to "on-going",
            intl["status_filter_canceled"] to "canceled",
            intl["status_filter_on_hold"] to "on-hold",
        )

    protected open val orderByFilterOptions: Map<String, String> = mapOf(
        intl["order_by_filter_relevance"] to "",
        intl["order_by_filter_latest"] to "latest",
        intl["order_by_filter_az"] to "alphabet",
        intl["order_by_filter_rating"] to "rating",
        intl["order_by_filter_trending"] to "trending",
        intl["order_by_filter_views"] to "views",
        intl["order_by_filter_new"] to "new-manga",
    )

    protected open val genreConditionFilterOptions: Map<String, String> =
        mapOf(
            intl["genre_condition_filter_or"] to "",
            intl["genre_condition_filter_and"] to "1",
        )

    protected open val adultContentFilterOptions: Map<String, String> =
        mapOf(
            intl["adult_content_filter_all"] to "",
            intl["adult_content_filter_none"] to "0",
            intl["adult_content_filter_only"] to "1",
        )

    open class UriPartFilter(displayName: String, private val vals: Array<Pair<String, String>>, state: Int = 0) : Filter.Select<String>(displayName, vals.map { it.first }.toTypedArray(), state) {
        fun toUriPart() = vals[state].second
    }

    open class Tag(name: String, val id: String) : Filter.CheckBox(name)

    protected class AuthorFilter(title: String) : Filter.Text(title)
    protected class ArtistFilter(title: String) : Filter.Text(title)
    protected class YearFilter(title: String) : Filter.Text(title)
    protected class StatusFilter(title: String, status: List<Tag>) : Filter.Group<Tag>(title, status)

    protected class OrderByFilter(title: String, options: List<Pair<String, String>>, state: Int = 0) : UriPartFilter(title, options.toTypedArray(), state)

    protected class GenreConditionFilter(title: String, options: List<Pair<String, String>>) :
        UriPartFilter(
            title,
            options.toTypedArray(),
        )

    protected class AdultContentFilter(title: String, options: List<Pair<String, String>>) :
        UriPartFilter(
            title,
            options.toTypedArray(),
        )

    protected class GenreList(title: String, genres: List<Genre>) : Filter.Group<GenreCheckBox>(title, genres.map { GenreCheckBox(it.name, it.id) })
    class GenreCheckBox(name: String, val id: String = name) : Filter.CheckBox(name)
    class Genre(val name: String, val id: String = name)

    override fun getFilterList(): FilterList {
        launchIO { fetchGenres() }

        val filters = mutableListOf(
            AuthorFilter(intl["author_filter_title"]),
            ArtistFilter(intl["artist_filter_title"]),
            YearFilter(intl["year_filter_title"]),
            StatusFilter(
                title = intl["status_filter_title"],
                status = statusFilterOptions.map { Tag(it.key, it.value) },
            ),
            OrderByFilter(
                title = intl["order_by_filter_title"],
                options = orderByFilterOptions.toList(),
                state = 0,
            ),
            AdultContentFilter(
                title = intl["adult_content_filter_title"],
                options = adultContentFilterOptions.toList(),
            ),
        )

        if (genresList.isNotEmpty()) {
            filters += listOf(
                Filter.Separator(),
                Filter.Header(intl["genre_filter_header"]),
                GenreConditionFilter(
                    title = intl["genre_condition_filter_title"],
                    options = genreConditionFilterOptions.toList(),
                ),
                GenreList(
                    title = intl["genre_filter_title"],
                    genres = genresList,
                ),
            )
        } else if (fetchGenres) {
            filters += listOf(
                Filter.Separator(),
                Filter.Header(intl["genre_missing_warning"]),
            )
        }

        return FilterList(filters)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()

        val entries = document.select(searchMangaSelector())
            .map(::searchMangaFromElement)
        val hasNextPage = searchMangaNextPageSelector()?.let { document.selectFirst(it) } != null

        detectLoadMore(document)

        return MangasPage(entries, hasNextPage)
    }

    protected open fun searchMangaSelector() = "div.c-tabs-item__content , .manga__item"

    protected open val searchMangaUrlSelector = "div.post-title a"

    protected open fun searchMangaFromElement(element: Element): SManga {
        val manga = SManga.create()

        with(element) {
            selectFirst(searchMangaUrlSelector)!!.let {
                manga.setUrlWithoutDomain(it.attr("abs:href"))
                manga.title = it.ownText()
            }
            selectFirst("img")?.let {
                manga.thumbnail_url = processThumbnail(imageFromElement(it), true)
            }
        }

        return manga
    }

    protected open fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    // Manga Details Parse

    protected val completedStatusList: Array<String> = arrayOf(
        "Completed",
        "Completo",
        "Completado",
        "Concluído",
        "Concluido",
        "Finalizado",
        "Achevé",
        "Terminé",
        "Hoàn Thành",
        "مكتملة",
        "مكتمل",
        "已完结",
        "Tamamlandı",
        "Đã hoàn thành",
        "Завершено",
        "Tamamlanan",
        "Complété",
    )

    protected val ongoingStatusList: Array<String> = arrayOf(
        "OnGoing", "Продолжается", "Updating", "Em Lançamento", "Em lançamento", "Em andamento",
        "Em Andamento", "En cours", "En Cours", "En cours de publication", "Ativo", "Lançando", "Đang Tiến Hành", "Còn Nữa", "Devam Ediyor",
        "Devam ediyor", "In Corso", "In Arrivo", "مستمرة", "مستمر", "En Curso", "En curso", "Emision",
        "Curso", "En marcha", "Publicandose", "Publicándose", "En emision", "连载中", "Em Lançamento", "Devam Ediyo",
        "Đang làm", "Em postagem", "Devam Eden", "Em progresso", "Em curso", "Atualizações Semanais",
    )

    protected val hiatusStatusList: Array<String> = arrayOf(
        "On Hold",
        "Pausado",
        "En espera",
        "Durduruldu",
        "Beklemede",
        "Đang chờ",
        "متوقف",
        "En Pause",
        "Заморожено",
        "En attente",
    )

    protected val canceledStatusList: Array<String> = arrayOf(
        "Canceled",
        "Cancelado",
        "İptal Edildi",
        "Güncel",
        "Đã hủy",
        "ملغي",
        "Abandonné",
        "Заброшено",
        "Annulé",
    )

    override fun mangaDetailsParse(response: Response): SManga = mangaDetailsParse(response.asJsoup())

    protected open fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        with(document) {
            manga.title = selectFirst(mangaDetailsSelectorTitle)!!.ownText()
            select(mangaDetailsSelectorAuthor).eachText().filter {
                it.notUpdating()
            }.joinToString().takeIf { it.isNotBlank() }?.let {
                manga.author = it
            }
            select(mangaDetailsSelectorArtist).eachText().filter {
                it.notUpdating()
            }.joinToString().takeIf { it.isNotBlank() }?.let {
                manga.artist = it
            }
            select(mangaDetailsSelectorDescription).let {
                if (it.select("p").text().isNotEmpty()) {
                    manga.description = it.select("p").joinToString(separator = "\n\n") { p ->
                        p.text().replace("<br>", "\n")
                    }
                } else {
                    manga.description = it.text()
                }
            }
            selectFirst(mangaDetailsSelectorThumbnail)?.let {
                manga.thumbnail_url = processThumbnail(imageFromElement(it))
            }
            select(mangaDetailsSelectorStatus).last()?.let {
                manga.status = with(it.text().filter { ch -> ch.isLetterOrDigit() || ch.isWhitespace() }.trim()) {
                    when {
                        containsIn(completedStatusList) -> SManga.COMPLETED
                        containsIn(ongoingStatusList) -> SManga.ONGOING
                        containsIn(hiatusStatusList) -> SManga.ON_HIATUS
                        containsIn(canceledStatusList) -> SManga.CANCELLED
                        else -> SManga.UNKNOWN
                    }
                }
            }
            val genres = select(mangaDetailsSelectorGenre)
                .mapTo(ArrayList()) { element -> element.text() }

            if (mangaDetailsSelectorTag.isNotEmpty()) {
                select(mangaDetailsSelectorTag).forEach { element ->
                    if (
                        element.text().length <= 25 &&
                        element.text().contains("read", true).not() &&
                        element.text().contains(name, true).not() &&
                        element.text().contains(name.replace(" ", ""), true).not() &&
                        element.text().contains(manga.title, true).not() &&
                        element.text().contains(altName, true).not()
                    ) {
                        genres.add(element.text())
                    }
                }
            }

            // add manga/manhwa/manhua thinggy to genre
            document.selectFirst(seriesTypeSelector)?.ownText()?.let {
                if (it.isEmpty().not() && it.notUpdating() && it != "-") {
                    genres.add(it)
                }
            }

            manga.genre = genres.distinctBy(String::lowercase).joinToString()

            // add alternative name to manga description
            document.selectFirst(altNameSelector)?.ownText()?.let {
                if (it.isBlank().not() && it.notUpdating()) {
                    manga.description = when {
                        manga.description.isNullOrBlank() -> "$altName $it"
                        else -> "${manga.description}\n\n$altName $it"
                    }
                }
            }
        }

        return manga
    }

    // Manga Details Selector
    open val mangaDetailsSelectorTitle = "div.post-title h3, div.post-title h1, #manga-title > h1"
    open val mangaDetailsSelectorAuthor = "div.author-content > a, div.manga-authors > a"
    open val mangaDetailsSelectorArtist = "div.artist-content > a"
    open val mangaDetailsSelectorStatus = "div.summary-content, div.summary-heading:contains(Status) + div"
    open val mangaDetailsSelectorDescription = "div.description-summary div.summary__content, div.summary_content div.post-content_item > h5 + div, div.summary_content div.manga-excerpt"
    open val mangaDetailsSelectorThumbnail = "div.summary_image img"
    open val mangaDetailsSelectorGenre = "div.genres-content a"
    open val mangaDetailsSelectorTag = "div.tags-content a"

    open val seriesTypeSelector = ".post-content_item:contains(Type) .summary-content"
    open val altNameSelector = ".post-content_item:contains(Alt) .summary-content"
    open val altName = intl["alt_names_heading"]
    open val updatingRegex = "Updating|Atualizando".toRegex(RegexOption.IGNORE_CASE)

    fun String.notUpdating(): Boolean = this.contains(updatingRegex).not()

    private fun String.containsIn(array: Array<String>): Boolean = this.lowercase() in array.map { it.lowercase() }

    protected open fun imageFromElement(element: Element): String? = when {
        element.hasAttr("data-src") -> element.attr("abs:data-src")
        element.hasAttr("data-lazy-src") -> element.attr("abs:data-lazy-src")
        element.hasAttr("srcset") -> element.attr("abs:srcset").getSrcSetImage()
        element.hasAttr("data-cfsrc") -> element.attr("abs:data-cfsrc")
        element.hasAttr("data-manga-src") -> element.attr("abs:data-manga-src")
        else -> element.attr("abs:src")
    }

    /**
 *  Get the best image quality available from srcset
 */
    protected open fun String.getSrcSetImage(): String? = this.split(" ")
        .filter(URL_REGEX::matches)
        .maxOfOrNull(String::toString)

    /**
 *  Apply any additional processing to the thumbnail URL if needed.
 */
    protected open fun processThumbnail(url: String?, fromSearch: Boolean = false): String? = url

    /**
 * Set it to true if the source uses the new AJAX endpoint to
 * fetch the manga chapters instead of the old admin-ajax.php one.
 */
    protected open val useNewChapterEndpoint: Boolean = false

    /**
 * Internal attribute to control if it should always use the
 * new chapter endpoint after a first check if useNewChapterEndpoint is
 * set to false. Using a separate variable to still allow the other
 * one to be overridable manually in each source.
 */
    private var oldChapterEndpointDisabled: Boolean = false

    protected open fun oldXhrChaptersRequest(mangaId: String): Request {
        val form = FormBody.Builder()
            .add("action", "manga_get_chapters")
            .add("manga", mangaId)
            .build()

        return POST("$baseUrl/wp-admin/admin-ajax.php", xhrHeaders, form)
    }

    protected open fun xhrChaptersRequest(mangaUrl: String): Request = POST("$mangaUrl/ajax/chapters", xhrHeaders)

    override fun chapterListParse(response: Response): List<SChapter> {
        val document = response.asJsoup()

        launchIO { countViews(document) }

        val chaptersWrapper = document.select("div[id^=manga-chapters-holder]")

        var chapterElements = document.select(chapterListSelector())

        if (chapterElements.isEmpty() && !chaptersWrapper.isNullOrEmpty()) {
            val mangaUrl = document.location().removeSuffix("/")
            val mangaId = chaptersWrapper.attr("data-id")

            var xhrRequest = if (useNewChapterEndpoint || oldChapterEndpointDisabled) {
                xhrChaptersRequest(mangaUrl)
            } else {
                oldXhrChaptersRequest(mangaId)
            }
            var xhrResponse = client.newCall(xhrRequest).execute()

            // Newer Madara versions throws HTTP 400 when using the old endpoint.
            if (!useNewChapterEndpoint && xhrResponse.code == 400) {
                xhrResponse.close()
                // Set it to true so following calls will be made directly to the new endpoint.
                oldChapterEndpointDisabled = true

                xhrRequest = xhrChaptersRequest(mangaUrl)
                xhrResponse = client.newCall(xhrRequest).execute()
            }

            chapterElements = xhrResponse.asJsoup().select(chapterListSelector())
            xhrResponse.close()
        }

        return chapterElements.map(::chapterFromElement)
    }

    protected open fun chapterListSelector() = "li.wp-manga-chapter"

    protected open fun chapterDateSelector() = "span.chapter-release-date"

    open val chapterUrlSelector = "a"

    // can cause some issue for some site. blocked by cloudflare when opening the chapter pages
    open val chapterUrlSuffix = "?style=list"

    protected open fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()

        with(element) {
            selectFirst(chapterUrlSelector)!!.let { urlElement ->
                chapter.url = urlElement.attr("abs:href").let {
                    it.substringBefore("?style=paged") + if (!it.endsWith(chapterUrlSuffix)) chapterUrlSuffix else ""
                }
                chapter.name = urlElement.text()
            }
            // Dates can be part of a "new" graphic or plain text
            // Added "title" alternative
            chapter.date_upload = selectFirst("img:not(.thumb)")?.attr("alt")?.let { parseRelativeDate(it) }
                ?: selectFirst("span a")?.attr("title")?.let { parseRelativeDate(it) }
                ?: parseChapterDate(selectFirst(chapterDateSelector())?.text())
        }

        return chapter
    }

    open fun parseChapterDate(date: String?): Long {
        date ?: return 0

        fun SimpleDateFormat.tryParse(string: String): Long = try {
            parse(string)?.time ?: 0
        } catch (_: ParseException) {
            0
        }

        return when {
            // Handle 'yesterday' and 'today', using midnight
            WordSet("yesterday", "يوم واحد").startsWith(date) -> {
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -1) // yesterday
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            WordSet("today").startsWith(date) -> {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            WordSet("يومين").startsWith(date) -> {
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -2) // day before yesterday
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            WordSet("ago", "atrás", "önce", "قبل", "trước").endsWith(date) -> {
                parseRelativeDate(date)
            }

            WordSet("hace", "năm", "tháng", "tuần", "ngày", "giờ", "phút", "giây").startsWith(date) -> {
                parseRelativeDate(date)
            }

            // Handle "jour" with a number before it
            date.contains(Regex("""\b\d+ jour""")) -> {
                parseRelativeDate(date)
            }

            date.contains(Regex("""\d(st|nd|rd|th)""")) -> {
                // Clean date (e.g. 5th December 2019 to 5 December 2019) before parsing it
                date.split(" ").map {
                    if (it.contains(Regex("""\d\D\D"""))) {
                        it.replace(Regex("""\D"""), "")
                    } else {
                        it
                    }
                }
                    .let { dateFormat.tryParse(it.joinToString(" ")) }
            }

            else -> dateFormat.tryParse(date)
        }
    }

    // Parses dates in this form:
    // 21 horas ago
    protected open fun parseRelativeDate(date: String): Long {
        val number = Regex("""(\d+)""").find(date)?.value?.toIntOrNull() ?: return 0
        val cal = Calendar.getInstance()

        return when {
            WordSet("hari", "gün", "jour", "día", "dia", "day", "วัน", "ngày", "giorni", "أيام", "天").anyWordIn(date) -> cal.apply { add(Calendar.DAY_OF_MONTH, -number) }.timeInMillis
            WordSet("jam", "saat", "heure", "hora", "hour", "ชั่วโมง", "giờ", "ore", "ساعة", "小时").anyWordIn(date) -> cal.apply { add(Calendar.HOUR, -number) }.timeInMillis
            WordSet("menit", "dakika", "min", "minute", "minuto", "นาที", "دقائق", "phút").anyWordIn(date) -> cal.apply { add(Calendar.MINUTE, -number) }.timeInMillis
            WordSet("detik", "segundo", "second", "วินาที", "giây").anyWordIn(date) -> cal.apply { add(Calendar.SECOND, -number) }.timeInMillis
            WordSet("week", "semana", "tuần").anyWordIn(date) -> cal.apply { add(Calendar.DAY_OF_MONTH, -number * 7) }.timeInMillis
            WordSet("month", "mes", "tháng").anyWordIn(date) -> cal.apply { add(Calendar.MONTH, -number) }.timeInMillis
            WordSet("year", "año", "năm").anyWordIn(date) -> cal.apply { add(Calendar.YEAR, -number) }.timeInMillis
            else -> 0
        }
    }

    override fun pageListRequest(chapter: SChapter): Request {
        if (chapter.url.startsWith("http")) {
            return GET(chapter.url, headers)
        }
        return super.pageListRequest(chapter)
    }

    open val pageListParseSelector = "div.page-break, li.blocks-gallery-item, .reading-content .text-left:not(:has(.blocks-gallery-item)) img"

    open val chapterProtectorSelector = "#chapter-protector-data"
    open val chapterProtectorPasswordPrefix = "wpmangaprotectornonce='"
    open val chapterProtectorDataPrefix = "chapter_data='"

    override fun pageListParse(response: Response): List<Page> = pageListParse(response.asJsoup())

    protected open fun pageListParse(document: Document): List<Page> {
        launchIO { countViews(document) }

        val chapterProtector = document.selectFirst(chapterProtectorSelector)
            ?: return document.select(pageListParseSelector).mapIndexed { index, element ->
                val imageUrl = element.selectFirst("img")?.let { imageFromElement(it) }
                Page(index, document.location(), imageUrl)
            }
        val chapterProtectorHtml = chapterProtector.attr("src")
            .takeIf { it.startsWith("data:text/javascript;base64,") }
            ?.substringAfter("data:text/javascript;base64,")
            ?.let { Base64.decode(it, Base64.DEFAULT).toString(Charsets.UTF_8) }
            ?: chapterProtector.html()
        val password = chapterProtectorHtml
            .substringAfter(chapterProtectorPasswordPrefix)
            .substringBefore("';")
        val chapterData = json.parseToJsonElement(
            chapterProtectorHtml
                .substringAfter(chapterProtectorDataPrefix)
                .substringBefore("';")
                .replace("\\/", "/"),
        ).jsonObject

        val unsaltedCiphertext = Base64.decode(chapterData["ct"]!!.jsonPrimitive.content, Base64.DEFAULT)
        val salt = chapterData["s"]!!.jsonPrimitive.content.decodeHex()
        val ciphertext = salted + salt + unsaltedCiphertext

        val rawImgArray = CryptoAES.decrypt(Base64.encodeToString(ciphertext, Base64.DEFAULT), password)
        val imgArrayString = json.parseToJsonElement(rawImgArray).jsonPrimitive.content
        val imgArray = json.parseToJsonElement(imgArrayString).jsonArray

        return imgArray.mapIndexed { idx, it ->
            Page(idx, document.location(), it.jsonPrimitive.content)
        }
    }

    override fun imageRequest(page: Page): Request = GET(page.imageUrl!!, headers.newBuilder().set("Referer", page.url).build())

    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException()

    /**
 * Set it to false if you want to disable the extension reporting the view count
 * back to the source website through admin-ajax.php.
 */
    protected open val sendViewCount: Boolean = true

    protected open fun countViewsRequest(document: Document): Request? {
        val wpMangaData = document.selectFirst("script#wp-manga-js-extra")
            ?.data() ?: return null

        val wpMangaInfo = wpMangaData
            .substringAfter("var manga = ")
            .substringBeforeLast(";")

        val wpManga = json.parseToJsonElement(wpMangaInfo).jsonObject

        if (wpManga["enable_manga_view"]?.jsonPrimitive?.content == "1") {
            val formBuilder = FormBody.Builder()
                .add("action", "manga_views")
                .add("manga", wpManga["manga_id"]!!.jsonPrimitive.content)

            if (wpManga["chapter_slug"] != null) {
                formBuilder.add("chapter", wpManga["chapter_slug"]!!.jsonPrimitive.content)
            }

            val formBody = formBuilder.build()

            val newHeaders = headersBuilder()
                .set("Referer", document.location())
                .build()

            return POST("$baseUrl/wp-admin/admin-ajax.php", newHeaders, formBody)
        }

        return null
    }

    /**
 * Send the view count request to the Madara endpoint.
 *
 * @param document The response document with the wp-manga data
 */
    protected fun countViews(document: Document) {
        if (!sendViewCount) {
            return
        }

        try {
            val request = countViewsRequest(document) ?: return
            client.newCall(request).execute().close()
        } catch (_: Exception) { }
    }

    /**
 * Fetch the genres from the source to be used in the filters.
 */
    protected fun fetchGenres() {
        if (fetchGenres && fetchGenresAttempts < 3 && !genresFetched) {
            try {
                client.newCall(genresRequest()).execute()
                    .use { parseGenres(it.asJsoup()) }
                    .also {
                        genresFetched = true
                    }
                    .takeIf { it.isNotEmpty() }
                    ?.also {
                        genresList = it
                    }
            } catch (_: Exception) {
            } finally {
                fetchGenresAttempts++
            }
        }
    }

    /**
 * The request to the search page (or another one) that have the genres list.
 */
    protected open fun genresRequest(): Request = GET("$baseUrl/?s=genre&c", headers)

    /**
 * Get the genres from the search page document.
 *
 * @param document The search page document
 */
    protected open fun parseGenres(document: Document): List<Genre> = document.selectFirst("div.checkbox-group")
        ?.select("div.checkbox")
        .orEmpty()
        .map { li ->
            Genre(
                li.selectFirst("label")!!.text(),
                li.selectFirst("input[type=checkbox]")!!.`val`(),
            )
        }

    protected val salted = "Salted__".toByteArray(Charsets.UTF_8)

    private val scope = CoroutineScope(Dispatchers.IO)

    protected fun launchIO(block: () -> Unit) = scope.launch { block() }

    companion object {
        const val URL_SEARCH_PREFIX = "slug:"
        val URL_REGEX = """^(https?://[^\s/$.?#].[^\s]*)${'$'}""".toRegex()
    }
}*/

class WordSet(private vararg val words: String) {
    fun anyWordIn(dateString: String): Boolean = words.any { dateString.contains(it, ignoreCase = true) }
    fun startsWith(dateString: String): Boolean = words.any { dateString.startsWith(it, ignoreCase = true) }
    fun endsWith(dateString: String): Boolean = words.any { dateString.endsWith(it, ignoreCase = true) }
}
