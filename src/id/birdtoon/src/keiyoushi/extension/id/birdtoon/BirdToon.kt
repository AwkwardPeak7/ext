package keiyoushi.extension.id.birdtoon

import keiyoushi.multisrc.madara.Madara

class BirdToon : Madara(
    "BirdToon",
    "https://birdtoon.org",
    "id",
) {
    override val mangaSubString = "komik"

    override val useLoadMoreRequest = LoadMoreStrategy.Never
}
