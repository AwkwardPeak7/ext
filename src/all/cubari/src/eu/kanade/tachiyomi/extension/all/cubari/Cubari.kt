package eu.kanade.tachiyomi.extension.all.cubari

import android.os.Build
import android.util.Base64
import android.util.Log
import eu.kanade.tachiyomi.AppInfo
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonString
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.brotli.BrotliInterceptor
import rx.Observable

class Cubari(override val lang: String) : HttpSource() {

    override val name = "Cubari"

    override val baseUrl = "https://cubari.moe"

    override val supportsLatest = false

    override val client = network.cloudflareClient.newBuilder()
        // fix disk cache
        .apply {
            val index = networkInterceptors().indexOfFirst { it is BrotliInterceptor }
            if (index >= 0) interceptors().add(networkInterceptors().removeAt(index))
        }
        .build()

    private val cubariHeaders = super.headersBuilder()
        .set(
            "User-Agent",
            "(Android ${Build.VERSION.RELEASE}; " +
                "${Build.MANUFACTURER} ${Build.MODEL}) " +
                "Tachiyomi/Mihon/${AppInfo.getVersionName()} (Keiyoushi) " +
                Build.ID,
        ).build()

    val remoteStorage = RemoteStorage(client, headers)

    override fun latestUpdatesRequest(page: Int) =
        throw UnsupportedOperationException()

    override fun latestUpdatesParse(response: Response) =
        throw UnsupportedOperationException()

    override fun popularMangaRequest(page: Int) =
        throw UnsupportedOperationException()

    override fun fetchPopularManga(page: Int): Observable<MangasPage> {
        val mangas = runBlocking {
            remoteStorage.getSeriesData()
                .sortedByDescending { it.timestamp }
                .map { it.toSManga() }
        }.ifEmpty {
            throw Exception(SEARCH_FALLBACK_MSG)
        }

        return Observable.just(MangasPage(mangas, false))
    }

    override fun popularMangaParse(response: Response) =
        throw UnsupportedOperationException()

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        return when {
            // handle direct links or old cubari:source/id format
            query.startsWith("https://") || query.startsWith("cubari:") -> {
                val (source, slug) = deepLinkHandler(query)
                val manga = SManga.create().apply {
                    url = "/read/$source/$slug/"
                }
                client.newCall(mangaDetailsRequest(manga))
                    .asObservableSuccess()
                    .map {
                        val data = it.parseAs<CubariManga>()
                        runBlocking {
                            remoteStorage.tagSeries(data, source, slug)
                        }
                        val manga = data.toSManga().apply {
                            url = "/read/$source/$slug/"
                        }

                        MangasPage(listOf(manga), false)
                    }
            }
            else -> {
                val mangas = runBlocking {
                    remoteStorage.getSeriesData()
                        .filter { it.title.contains(query.trim(), ignoreCase = true) }
                        .sortedByDescending { it.timestamp }
                        .map { it.toSManga() }
                }.ifEmpty {
                    throw Exception(SEARCH_FALLBACK_MSG)
                }

                Observable.just(MangasPage(mangas, false))
            }
        }
    }

    override fun getFilterList() = FilterList(
        Filter.Header("Paste a Cubari URL in the search field"),
    )

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        throw UnsupportedOperationException()
    }

    private fun deepLinkHandler(query: String): Pair<String, String> {
        return if (query.startsWith("cubari:")) { // legacy cubari:source/slug format
            val queryFragments = query.substringAfter("cubari:").split("/", limit = 2)
            queryFragments[0] to queryFragments[1]
        } else { // direct url searching
            val url = query.toHttpUrl()
            val host = url.host
            val pathSegments = url.pathSegments

            if (
                host.endsWith("imgur.com") &&
                pathSegments.size >= 2 &&
                pathSegments[0] in listOf("a", "gallery")
            ) {
                "imgur" to pathSegments[1]
            } else if (
                host.endsWith("reddit.com") &&
                pathSegments.size >= 2 &&
                pathSegments[0] == "gallery"
            ) {
                "reddit" to pathSegments[1]
            } else if (
                host == "imgchest.com" &&
                pathSegments.size >= 2 &&
                pathSegments[0] == "p"
            ) {
                "imgchest" to pathSegments[1]
            } else if (
                host.endsWith("catbox.moe") &&
                pathSegments.size >= 2 &&
                pathSegments[0] == "c"
            ) {
                "catbox" to pathSegments[1]
            } else if (
                host.endsWith("cubari.moe") &&
                pathSegments.size >= 3
            ) {
                pathSegments[1] to pathSegments[2]
            } else if (
                host.endsWith(".githubusercontent.com")
            ) {
                val src = host.substringBefore(".")
                val path = url.encodedPath

                "gist" to Base64.encodeToString("$src$path".toByteArray(), Base64.NO_PADDING)
            } else {
                throw Exception(SEARCH_FALLBACK_MSG)
            }
        }
    }

    override fun searchMangaParse(response: Response): MangasPage {
        throw UnsupportedOperationException()
    }

    override fun getMangaUrl(manga: SManga): String {
        return "$baseUrl${manga.url}"
    }

    override fun mangaDetailsRequest(manga: SManga): Request {
        val url = getMangaUrl(manga).toHttpUrl()
        val source = url.pathSegments[1]
        val slug = url.pathSegments[2]

        return GET("$baseUrl/read/api/$source/series/$slug/", cubariHeaders)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val data = response.parseAs<CubariManga>()

        return data.toSManga()
    }

    override fun chapterListRequest(manga: SManga) = mangaDetailsRequest(manga)

    @OptIn(DelicateCoroutinesApi::class)
    override fun chapterListParse(response: Response): List<SChapter> {
        val data = response.parseAs<CubariManga>()
        val (source, slug) = response.request.url.pathSegments.let {
            it[2] to it[4]
        }

        GlobalScope.launch {
            runCatching {
                remoteStorage.tagSeries(data, source, slug)
            }
        }

        val appendVolume = data.chapters.map { (_, v) -> v.volume }.toSet().size > 1

        return data.chapters.flatMap { (chapterNum, chapter) ->
            chapter.groups.map { (groupNum, chapterGroup) ->
                SChapter.create().apply {
                    scanlator = data.groups[groupNum]
                    chapter_number = chapterNum.toFloat()
                    date_upload = chapter.date
                    name = buildString {
                        if (
                            appendVolume &&
                            chapter.volume.isNotBlank() &&
                            chapter.volume.lowercase().trim() !in setOf("uncategorized", "null", "na", "")
                        ) {
                            append("Vol. ", chapter.volume, " ")
                        }
                        if (!chapter.title.orEmpty().contains(chapterNum)) {
                            append("Ch. ", chapterNum)
                        }
                        if (!chapter.title.isNullOrBlank()) {
                            if (isNotBlank()) {
                                append(" - ")
                            }
                            append(chapter.title)
                        }
                    }
                    url = if (chapterGroup.size == 1 && chapterGroup[0].startsWith("/read/api/")) {
                        chapterGroup[0]
                    } else {
                        "/read/$source/$slug/$chapterNum/$groupNum"
                    }
                }
            }
        }.sortedByDescending { it.chapter_number }
    }

    override fun pageListRequest(chapter: SChapter): Request {
        val url = "$baseUrl${chapter.url}".toHttpUrl().pathSegments
        return when {
            url[3] == "chapter" -> {
                GET("$baseUrl${chapter.url}", cubariHeaders)
            }
            else -> {
                val source = url[1]
                val slug = url[2]
                val chapNum = url[3]
                val groupNo = url[4]

                GET("$baseUrl/read/api/$source/series/$slug/#$chapNum/$groupNo", cubariHeaders)
            }
        }
    }

    override fun pageListParse(response: Response): List<Page> {
        return if (response.request.url.pathSegments[3] == "chapter") {
            directPageListParse(response)
        } else {
            seriesPageListParse(response)
        }
    }

    private fun directPageListParse(response: Response): List<Page> {
        val pages = response.parseAs<List<CubariPage>>()

        return pages.mapIndexed { i, img ->
            Page(i, imageUrl = img.src)
        }
    }

    private fun seriesPageListParse(response: Response): List<Page> {
        val (chapNum, groupNo) = response.request.url.fragment!!.split("/", limit = 2)
        val data = response.parseAs<CubariManga>()
        val pages = data.chapters[chapNum]!!.groups[groupNo]!!

        Log.d("cubari", pages.toJsonString())

        return pages.mapIndexed { i, img ->
            Page(i, imageUrl = img)
        }
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException()
    }
}

const val SEARCH_FALLBACK_MSG = "Please enter a valid Cubari URL in search"
