package eu.kanade.tachiyomi.extension.all.myrockmanga

import eu.kanade.tachiyomi.source.SourceFactory
import keiyoushix.multisrc.otakusanctuary.OtakuSanctuary

class MyRockMangaFactory : SourceFactory {
    override fun createSources() = listOf(
        OtakuSanctuary("MyRockManga", "https://myrockmanga.com", "all"),
        OtakuSanctuary("MyRockManga", "https://myrockmanga.com", "vi"),
        OtakuSanctuary("MyRockManga", "https://myrockmanga.com", "en"),
        OtakuSanctuary("MyRockManga", "https://myrockmanga.com", "it"),
        OtakuSanctuary("MyRockManga", "https://myrockmanga.com", "fr"),
        OtakuSanctuary("MyRockManga", "https://myrockmanga.com", "es"),
    )
}
