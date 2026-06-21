package eu.kanade.tachiyomi.extension.en.allporncomic

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Response
import org.jsoup.nodes.Element

class AllPornComic : Madara("AllPornComic", "https://allporncomic.com", "en") {
    override val mangaDirectory = "porncomic"
}
