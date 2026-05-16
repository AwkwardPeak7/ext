plugins { alias(kei.plugins.extension) }

extension {
    name = "Dummy Same-Lang"
    versionCode = 1
    nsfw = false
    // One site, three content categories — all in the same language.
    source {
        name = "Dummy (Webtoon)"
        lang = "en"
        baseUrl("https://dummysamelang.example")
    }
    source {
        name = "Dummy (Comic)"
        lang = "en"
        baseUrl("https://dummysamelang.example")
    }
    source {
        name = "Dummy (Photo)"
        lang = "en"
        baseUrl("https://dummysamelang.example")
    }
}
