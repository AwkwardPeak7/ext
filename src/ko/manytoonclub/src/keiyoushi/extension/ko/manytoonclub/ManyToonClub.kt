package keiyoushi.extension.ko.manytoonclub

import keiyoushi.multisrc.madara.Madara

class ManyToonClub : Madara("ManyToonClub", "https://manytoon.club", "ko") {

    override val mangaSubString = "manhwa-raw"

    // The website does not flag the content.
    override val filterNonMangaItems = false
}
