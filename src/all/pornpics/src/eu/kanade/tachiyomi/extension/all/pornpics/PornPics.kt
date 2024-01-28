package eu.kanade.tachiyomi.extension.all.pornpics

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import rx.Observable
import uy.kohesive.injekt.injectLazy

class PornPics : HttpSource() {

    override val name = "Porn Pics"

    override val lang = "all"

    override val baseUrl = "https://www.pornpics.com"

    override val supportsLatest = true

    private val json: Json by injectLazy()

    override val client = network.cloudflareClient

    override fun headersBuilder() = super.headersBuilder()
        .set("Referer", "$baseUrl/")

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/popular/?limit=$limit&offset=${(page - 1) * limit}", headers)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val entries = if (response.header("content-type")?.contains("application/json", true) == true) {
            parseJsonResponse(response)
        } else {
            parseHtmlResponse(response)
        }

        return MangasPage(entries, entries.size >= limit)
    }

    private fun parseHtmlResponse(response: Response): List<SManga> {
        val document = response.asJsoup()

        return document.select("#tiles > li > a").map { element ->
            SManga.create().apply {
                setUrlWithoutDomain(element.absUrl("href"))
                element.selectFirst("img")!!.also {
                    title = it.attr("alt")
                    thumbnail_url = it.absUrl("data-src")
                }
            }
        }
    }

    @Serializable
    data class Gallery(
        @SerialName("g_url") val url: String,
        @SerialName("t_url") val cover: String,
        @SerialName("desc") val title: String,
    )

    private fun parseJsonResponse(response: Response): List<SManga> {
        val data = response.parseAs<List<Gallery>>()

        return data.map {
            SManga.create().apply {
                setUrlWithoutDomain(it.url)
                title = it.title
                thumbnail_url = it.cover
            }
        }
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/recent/?limit=$limit&offset=${(page - 1) * limit}", headers)
    }

    override fun latestUpdatesParse(response: Response) = popularMangaParse(response)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/search/srch.php".toHttpUrl().newBuilder()
            .addQueryParameter("q", query.trim())
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("offset", ((page - 1) * limit).toString())
            .build()

        return GET(url, headers)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val entries = parseJsonResponse(response)

        return MangasPage(entries, entries.size >= limit)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val document = response.asJsoup()

        return SManga.create().apply {
            title = document.select(".title-section > h1").text()
            author = document.getInfo("channel")
            artist = document.getInfo("model")
            genre = document.getInfo("categor", "tag")
            description = document.select(".gallery-info span:contains(stat) + div").text()
            status = SManga.COMPLETED
            update_strategy = UpdateStrategy.ONLY_FETCH_ONCE
        }
    }

    private fun Document.getInfo(vararg search: String): String {
        return search.flatMap { searchTerm ->
            select(".gallery-info span:contains($searchTerm) + div > a, .gallery-info span:contains($searchTerm) + a")
                .eachText()
                .map(String::trim)
                .filter(String::isNotEmpty)
        }.joinToString()
    }

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        return Observable.just(
            listOf(
                SChapter.create().apply {
                    url = manga.url
                    name = "Gallery"
                },
            ),
        )
    }

    override fun pageListParse(response: Response): List<Page> {
        val document = response.asJsoup()

        return document.select("#tiles img").mapIndexed { idx, img ->
            Page(idx, "", img.absUrl("data-src"))
        }
    }

    private inline fun <reified T> Response.parseAs(): T = use {
        json.decodeFromString(it.body.string())
    }

    companion object {
        private const val limit = 20
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        throw UnsupportedOperationException()
    }
    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException()
    }
}
