package keiyoushi.extension.en.babelwuxia

import keiyoushi.multisrc.madara.Madara

class BabelWuxia : Madara("Babel Wuxia", "https://babelwuxia.com", "en") {

    // moved from MangaThemesia
    override val versionId = 2
    override val useNewChapterEndpoint = true
    override val useLoadMoreRequest = LoadMoreStrategy.Always
}
