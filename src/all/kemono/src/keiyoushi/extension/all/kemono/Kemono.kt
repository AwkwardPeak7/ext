package keiyoushi.extension.all.kemono

import keiyoushi.multisrc.kemono.Kemono

class Kemono : Kemono("Kemono", "https://kemono.su", "all") {
    override val getTypes = listOf(
        "Patreon",
        "Pixiv Fanbox",
        "Discord",
        "Fantia",
        "Afdian",
        "Boosty",
        "Gumroad",
        "SubscribeStar",
    )
}
