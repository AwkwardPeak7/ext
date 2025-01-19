package keiyoushi.extension.all.xkcd

import keiyoushi.extension.all.xkcd.translations.XkcdES
import keiyoushi.extension.all.xkcd.translations.XkcdFR
import keiyoushi.extension.all.xkcd.translations.XkcdRU
import keiyoushi.extension.all.xkcd.translations.XkcdZH
import eu.kanade.tachiyomi.source.SourceFactory

class XkcdFactory : SourceFactory {
    override fun createSources() = listOf(
        Xkcd("https://xkcd.com", "en"),
        XkcdES(),
        XkcdZH(),
        XkcdFR(),
        XkcdRU(),
    )
}
