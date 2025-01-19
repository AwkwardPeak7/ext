package keiyoushi.extension.en.firstkissmanhua

import keiyoushi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.interceptor.rateLimit

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
