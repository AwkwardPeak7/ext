package eu.kanade.tachiyomi.extension.en.mangadistrict

import eu.kanade.tachiyomi.multisrc.madara.ChapterFetchMethod
import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.multisrc.madara.MangaListFetchMethod
import eu.kanade.tachiyomi.source.model.SManga

class MangaDistrict :
    Madara(
        "Manga District",
        "https://mangadistrict.com",
        "en",
    ) {
    override val mangaListFetchMethod = MangaListFetchMethod.MANGA_LIST_PAGE
    override val mangaDirectory = "series"
    override val mangaGenreDirectory = "publication-genre"
    override val mangaListNoAjaxNextPageSelector = "div[role=navigation] span.current + a.page"
}
