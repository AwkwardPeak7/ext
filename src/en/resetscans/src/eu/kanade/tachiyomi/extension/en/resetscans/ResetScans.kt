package eu.kanade.tachiyomi.extension.en.resetscans
import keiyoushix.multisrc.madara.Madara

class ResetScans : Madara("Reset Scans", "https://reset-scans.xyz", "en") {
    override val useNewChapterEndpoint = true
    override val chapterUrlSelector = ".li__text > a"
}
