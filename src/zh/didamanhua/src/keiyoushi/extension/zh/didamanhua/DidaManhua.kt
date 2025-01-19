package keiyoushi.extension.zh.didamanhua

import keiyoushi.multisrc.mccms.MCCMS
import keiyoushi.multisrc.mccms.MCCMSConfig
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga

class DidaManhua : MCCMS(
    "嘀嗒漫画",
    "https://www.didamanhua.com/index.php",
    "zh",
    MCCMSConfig(useMobilePageList = true),
) {
    // Details and chapter pages are broken
    override fun getMangaUrl(manga: SManga) = baseUrl
    override fun getChapterUrl(chapter: SChapter) = baseUrl
}
