plugins { alias(kei.plugins.extension) }

extension {
    name = "Dummy Basic"
    versionCode = 1
    nsfw = false
    source {
        name = "Dummy Basic"
        lang = "en"
        baseUrl("https://dummybasic.example")
    }
}
