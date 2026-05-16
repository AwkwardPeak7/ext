plugins { alias(kei.plugins.extension) }

extension {
    name = "Dummy Deeplink"
    versionCode = 1
    nsfw = false
    source {
        name = "Dummy Deeplink"
        lang = "en"
        baseUrl("https://dummydeeplink.example")
        deeplink {
            path("/manga/..*")
            path("/read/..*/chapter-..*")
        }
    }
}
