package eu.kanade.tachiyomi.extension.zh.damaomanhua

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushix.multisrc.mccms.MCCMS
import keiyoushix.multisrc.mccms.MCCMSConfig

class DamaoManhua : MCCMS(
    "大猫漫画",
    "https://www.hanman.cyou/index.php",
    "zh",
    MCCMSConfig(useMobilePageList = true),
) {
    // Details and chapter pages are broken
    override fun getMangaUrl(manga: SManga) = baseUrl
    override fun getChapterUrl(chapter: SChapter) = baseUrl
}
