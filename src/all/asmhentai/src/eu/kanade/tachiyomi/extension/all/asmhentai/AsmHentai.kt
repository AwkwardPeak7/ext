package eu.kanade.tachiyomi.extension.all.asmhentai

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.Filter
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

class AsmHentai(
    override val lang: String,
    private val siteLang: String,
) : HttpSource() {
    override val name = "AsmHentai"
    override val baseUrl = "https://asmhentai.com"
    override val supportsLatest = false
    override val client = network.cloudflareClient

    override fun popularMangaRequest(page: Int): Request {
        val url = baseUrl.toHttpUrl().newBuilder().apply {
            if (siteLang != "all") {
                addPathSegments("language/$siteLang/")
            }
            if (page > 1) {
                addQueryParameter("page", page.toString())
            }
        }.build()

        return GET(url, headers)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()

        return MangasPage(
            mangas = document.select(".preview_item").map {
                mangaFromElement(it)
            },
            hasNextPage = document.hasNextPage(),
        )
    }

    private fun mangaFromElement(element: Element): SManga {
        return SManga.create().apply {
            setUrlWithoutDomain(
                element.selectFirst(".image a")!!.absUrl("href"),
            )
            thumbnail_url = element.selectFirst(".image img")?.absUrl("src")
            title = element.selectFirst(".caption")!!.text()
        }
    }

    private fun Document.hasNextPage(): Boolean {
        return selectFirst(".pagination li.active + li:not(.disabled)") != null
    }

    override fun latestUpdatesRequest(page: Int) = throw UnsupportedOperationException()
    override fun latestUpdatesParse(response: Response) = throw UnsupportedOperationException()

    // any space except after a comma (we're going to replace spaces only between words)
    private val spaceRegex = Regex("""(?<!,)\s+""")

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val tags = (filters.last() as TagFilter).state

        val q = when {
            tags.isBlank() -> query
            query.isBlank() -> tags
            else -> "$query,$tags"
        }.replace(spaceRegex, "+")

        val url = baseUrl.toHttpUrl().newBuilder().apply {
            addPathSegments("search/")
            addEncodedQueryParameter("q", q)
            if (page > 1) addQueryParameter("page", page.toString())
        }

        return GET(url.build(), headers)
    }

    override fun getFilterList(): FilterList = FilterList(
        Filter.Header("Separate tags with commas (,)"),
        TagFilter(),
    )

    class TagFilter : Filter.Text("Tags")

    override fun searchMangaParse(response: Response): MangasPage {
        if (siteLang == "all") {
            return popularMangaParse(response)
        }

        val document = response.asJsoup()

        val entries = document.select(
            ".preview_item:has(a:has(.flag)[href\$=/language/$siteLang/])",
        ).map {
            mangaFromElement(it)
        }.ifEmpty {
            // workaround to not get "no results found"
            document.selectFirst(".preview_item")?.let {
                listOf(mangaFromElement(it))
            }.orEmpty()
        }

        return MangasPage(entries, document.hasNextPage())
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val document = response.asJsoup()
        return SManga.create().apply {
            update_strategy = UpdateStrategy.ONLY_FETCH_ONCE
            status = SManga.COMPLETED
            document.select(".book_page").first()!!.let { element ->
                thumbnail_url = element.select(".cover img").attr("abs:src")
                title = element.select("h1").text()
                genre = element.get("Tags")
                artist = element.get("Artists")
                author = artist
                description = listOf("Parodies", "Groups", "Languages", "Category")
                    .mapNotNull { tag ->
                        element.get(tag).let { if (it.isNotEmpty()) "$tag: $it" else null }
                    }
                    .joinToString("\n", postfix = "\n") +
                    element.select(".pages h3").text() +
                    element.select("h1 + h2").text()
                        .let { altTitle -> if (altTitle.isNotEmpty()) "\nAlternate Title: $altTitle" else "" }
            }
        }
    }

    private fun Element.get(tag: String): String {
        return select(".tags:contains($tag) .tag").joinToString { it.ownText() }
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

    private fun String.full(): String {
        val fType = substringAfterLast("t")
        return replace("t$fType", fType)
    }

    private fun Document.inputIdValueOf(string: String): String {
        return select("input[id=$string]").attr("value")
    }

    override fun pageListParse(response: Response): List<Page> {
        val document = response.asJsoup()

        val thumbUrls = document.select(".preview_thumb img")
            .map { it.attr("abs:data-src") }
            .toMutableList()

        // input only exists if pages > 10 and have to make a request to get the other thumbnails
        val totalPages = document.inputIdValueOf("t_pages")

        if (totalPages.isNotEmpty()) {
            val token = document.select("[name=csrf-token]").attr("content")

            val form = FormBody.Builder()
                .add("_token", token)
                .add("id", document.inputIdValueOf("load_id"))
                .add("dir", document.inputIdValueOf("load_dir"))
                .add("visible_pages", "10")
                .add("t_pages", totalPages)
                .add("type", "2") // 1 would be "more", 2 is "all remaining"
                .build()

            val xhrHeaders = headersBuilder()
                .add("X-Requested-With", "XMLHttpRequest")
                .build()

            client.newCall(POST("$baseUrl/inc/thumbs_loader.php", xhrHeaders, form))
                .execute()
                .asJsoup()
                .select("img")
                .mapTo(thumbUrls) { it.attr("abs:data-src") }
        }
        return thumbUrls.mapIndexed { i, url -> Page(i, "", url.full()) }
    }

    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException()
}
