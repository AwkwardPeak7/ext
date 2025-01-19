package keiyoushi.extension.en.firstkissmanhua

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.madara.Madara

class FirstKissManhua : Madara(
    "First Kiss Manhua",
    "https://1stkissmanhua.net",
    "en",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val useLoadMoreRequest = LoadMoreStrategy.Never
}
