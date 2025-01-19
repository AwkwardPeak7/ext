package keiyoushi.extension.en.hentaixdickgirl

import keiyoushi.multisrc.madara.Madara
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import org.jsoup.nodes.Document

class HentaiXDickgirl : Madara("HentaiXDickgirl", "https://hentaixdickgirl.com", "en") {

    override fun mangaDetailsParse(document: Document): SManga {
        return super.mangaDetailsParse(document).apply {
            update_strategy = UpdateStrategy.ONLY_FETCH_ONCE
        }
    }
}
