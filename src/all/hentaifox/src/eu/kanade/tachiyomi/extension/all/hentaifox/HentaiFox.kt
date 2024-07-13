package eu.kanade.tachiyomi.extension.all.hentaifox

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable

class HentaiFox(
    override val lang: String,
    private val siteLang: String,
) : HttpSource() {
    override val name = "HentaiFox"
    override val baseUrl = "https://hentaifox.com"
    override val supportsLatest = lang != "all"
    override val client = network.cloudflareClient

    override fun popularMangaRequest(page: Int): Request {
        val url = "$baseUrl/".toHttpUrl().newBuilder().apply {
            if (lang != "all") {
                addPathSegments("language/$siteLang/popular/")
                if (page > 1) {
                    addPathSegments("pag/$page/")
                }
            } else {
                if (page > 1) {
                    addPathSegments("page/$page/")
                }
            }
        }.build()

        return GET(url, headers)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()

        val entries = document.select(".lc_galleries .thumb").map {
            SManga.create().apply {
                setUrlWithoutDomain(
                    it.selectFirst(".inner_thumb a")!!.absUrl("href"),
                )
                title = it.selectFirst(".g_title")!!.text()
                thumbnail_url = it.selectFirst(".inner_thumb img")?.absUrl("src")
            }
        }
        val hasNextPage = document.selectFirst(".pagination li.active + li:not(.disabled)") != null

        return MangasPage(entries, hasNextPage)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        val url = "$baseUrl/".toHttpUrl().newBuilder().apply {
            if (lang != "all") {
                addPathSegments("language/$siteLang/")
                if (page > 1) {
                    addPathSegments("pag/$page/")
                }
            } else {
                if (page > 1) {
                    addPathSegments("page/$page/")
                }
            }
        }.build()

        return GET(url, headers)
    }

    override fun latestUpdatesParse(response: Response) = popularMangaParse(response)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/search/".toHttpUrl().newBuilder().apply {
            val tags = filters.filterIsInstance<TextFilter>().flatMap { it.values }.toMutableList()
            if (query.isNotBlank()) {
                tags += query.trim().replace(spaceRegex, "+")
            }
            if (lang != "all") {
                tags += siteLang
            }
            addQueryParameter("q", tags.joinToString("+"))
            filters.filterIsInstance<SortFilter>().first().selected?.let {
                addQueryParameter("sort", it)
            }
        }.build()

        return GET(url, headers)
    }

    override fun getFilterList() = getFilters()

    override fun searchMangaParse(response: Response) = popularMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga {
        val document = response.asJsoup()

        return with(document.selectFirst(".gallery_top")!!) {
            SManga.create().apply {
                update_strategy = UpdateStrategy.ONLY_FETCH_ONCE
                status = SManga.COMPLETED
                title = selectFirst("h1")!!.text()
                thumbnail_url = selectFirst(".cover img")?.absUrl("src")
                genre = getInfo("tags")
                author = getInfo("artists")
                description = buildString {
                    listOf("parodies", "characters", "groups", "languages", "category")
                        .forEach { tag ->
                            getInfo(tag)
                                .takeIf { it.isNotBlank() }
                                ?.let { "$tag: $it\n" }
                                ?.let { append(it) }
                        }
                    document.select("span.pages").eachText().forEach {
                        if (it.isNotBlank()) {
                            append(it, "\n")
                        }
                    }
                }
            }
        }
    }

    private fun Element.getInfo(tag: String): String {
        return select("ul.$tag a").joinToString { it.ownText() }
    }

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        return Observable.just(
            listOf(
                SChapter.create().apply {
                    url = manga.url
                    name = "Chapter"
                },
            ),
        )
    }

    override fun chapterListParse(response: Response) = throw UnsupportedOperationException()

    override fun pageListParse(response: Response): List<Page> {
        val document = response.asJsoup()

        val totalPages = document.getInputIdValue("load_pages")

        val pages = document.select(".g_thumb img")

        if ((totalPages.toIntOrNull() ?: 0) > pages.size) {
            val form = FormBody.Builder()
                .add("u_id", document.getInputIdValue("gallery_id"))
                .add("g_id", document.getInputIdValue("load_id"))
                .add("img_dir", document.getInputIdValue("load_dir"))
                .add("visible_pages", pages.size.toString())
                .add("total_pages", totalPages)
                .add("type", "2")
                .build()

            val headers = headersBuilder()
                .add("X-Requested-With", "XMLHttpRequest")
                .build()

            val morePages = client.newCall(POST("$baseUrl/includes/thumbs_loader.php", headers, form))
                .execute()
                .asJsoup()
                .select("img")

            pages.addAll(morePages)
        }

        return pages.mapIndexed { idx, img ->
            Page(idx, imageUrl = img.absUrl("data-src").thumbnailToFull())
        }
    }

    private fun Document.getInputIdValue(id: String): String {
        return select("input[id=$id]").attr("value")
    }

    private fun String.thumbnailToFull(): String {
        val ext = substringAfterLast(".")
        return replace("t.$ext", ".$ext")
    }

    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException()
}
