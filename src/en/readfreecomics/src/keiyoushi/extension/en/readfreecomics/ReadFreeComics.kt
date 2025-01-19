package keiyoushi.extension.en.readfreecomics

import keiyoushi.multisrc.madara.Madara

class ReadFreeComics : Madara("ReadFreeComics", "https://readfreecomics.com", "en") {
    override val useNewChapterEndpoint = true
}
