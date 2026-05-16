package eu.kanade.tachiyomi.multisrc.dummytheme

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Request
import okhttp3.Response

/**
 * No-arg HttpSource base. Concrete consumers extend this; the kei.plugins.extension
 * codegen overrides name/lang/baseUrl/id in the generated subclass. The theme also
 * declares its default deeplink paths in build.gradle.kts via the themeDeeplink {}
 * block — those flow into the per-extension GeneratedUrlActivity + manifest fragment.
 */
abstract class DummyTheme : HttpSource() {
    override val supportsLatest = false
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/popular?p=$page", headers)
    override fun popularMangaParse(response: Response): MangasPage = MangasPage(emptyList(), false)
    override fun latestUpdatesRequest(page: Int): Request = throw UnsupportedOperationException()
    override fun latestUpdatesParse(response: Response): MangasPage = throw UnsupportedOperationException()
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request = GET("$baseUrl/search?q=$query", headers)
    override fun searchMangaParse(response: Response): MangasPage = MangasPage(emptyList(), false)
    override fun mangaDetailsParse(response: Response): SManga = SManga.create()
    override fun chapterListParse(response: Response): List<SChapter> = emptyList()
    override fun pageListParse(response: Response): List<Page> = emptyList()
    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException()
}
