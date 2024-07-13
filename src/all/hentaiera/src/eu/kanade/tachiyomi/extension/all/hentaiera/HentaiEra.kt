package eu.kanade.tachiyomi.extension.all.hentaiera

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

class HentaiEra(
    override val lang: String,
    private val siteLang: String = lang,
) : HttpSource() {
    override val name = "HentaiEra"
    override val baseUrl = "https://hentaiera.com"
    override val supportsLatest = true
    override val client = network.cloudflareClient

    override fun popularMangaRequest(page: Int) =
        searchMangaRequest(page, "", SortFilter.POPULAR)

    override fun popularMangaParse(response: Response) =
        searchMangaParse(response)

    override fun latestUpdatesRequest(page: Int) =
        searchMangaRequest(page, "", SortFilter.LATEST)

    override fun latestUpdatesParse(response: Response) =
        searchMangaParse(response)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/search/".toHttpUrl().newBuilder().apply {
            filters.filterIsInstance<TextFilter>().let {
                val text = buildString {
                    if (query.isNotBlank()) {
                        append(query.trim().lowercase())
                        append(" ")
                    }
                    it.forEach {
                        if (it.state.isNotBlank()) {
                            append(it.state.trim().lowercase())
                            append(" ")
                        }
                    }
                }

                if (text.isNotBlank()) {
                    addQueryParameter("key", text)
                }
            }
            filters.filterIsInstance<SortFilter>().first().let {
                addQueryParameter(it.selected, "1")
            }
            filters.filterIsInstance<TypeFilter>().first().let {
                it.checked.forEach { type ->
                    addQueryParameter(type, "1")
                }
                it.unchecked.forEach { type ->
                    addQueryParameter(type, "0")
                }
            }
            listOf("en", "jp", "es", "fr", "kr", "de", "ru")
                .forEach {
                    addQueryParameter(
                        it,
                        if (siteLang == "all" || siteLang == it) "1" else "0",
                    )
                }
            addQueryParameter("page", page.toString())
        }.build()

        return GET(url, headers)
    }

    override fun getFilterList() = getFilters()

    override fun searchMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()

        val entries = document.select("div.thumbnail").map {
            SManga.create().apply {
                setUrlWithoutDomain(
                    it.selectFirst(".inner_thumb a")!!.absUrl("href"),
                )
                thumbnail_url = it.selectFirst(".inner_thumb img")?.absUrl("src")
                title = it.selectFirst(".gallery_title")!!.text()
            }
        }
        val hasNextPage = document.selectFirst(".pagination li.active + li:not(.disabled)") != null

        return MangasPage(entries, hasNextPage)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val document = response.asJsoup()

        return with(document.selectFirst(".gallery_first")!!) {
            SManga.create().apply {
                update_strategy = UpdateStrategy.ONLY_FETCH_ONCE
                status = SManga.COMPLETED
                title = selectFirst("h1")!!.text()
                thumbnail_url = selectFirst("img")?.absUrl("data-src")
                genre = getInfo("Tags")
                author = getInfo("Artists")
                artist = author
                description = listOf("Parodies", "Characters", "Languages", "Category")
                    .mapNotNull { tag ->
                        getInfo(tag).let { if (it.isNotEmpty()) "$tag: $it" else null }
                    }
                    .joinToString("\n", postfix = "\n") +
                    selectFirst("#pages_btn")?.text().orEmpty()
            }
        }
    }

    private fun Element.getInfo(tag: String): String {
        return select("li:has(.tags_text:contains($tag)) .tag .item_name").joinToString { it.ownText() }
    }

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        return Observable.just(
            listOf(
                SChapter.create().apply {
                    name = "Chapter"
                    url = manga.url
                },
            ),
        )
    }

    override fun chapterListParse(response: Response) = throw UnsupportedOperationException()

    override fun pageListParse(response: Response): List<Page> {
        val document = response.asJsoup()

        val totalPages = document.getInputIdValue("load_pages")

        val pages = document.select(".gthumb img")

        if ((totalPages.toIntOrNull() ?: 0) > pages.size) {
            val form = FormBody.Builder()
                .add("server", document.getInputIdValue("load_server"))
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

            val morePages = client.newCall(POST("$baseUrl/inc/thumbs_loader.php", headers, form))
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

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException()
    }
}
