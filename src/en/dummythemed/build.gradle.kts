plugins { alias(kei.plugins.extension) }

extension {
    name = "Dummy Themed"
    versionCode = 1
    nsfw = false
    theme = "dummytheme"
    source {
        name = "Dummy Themed"
        lang = "en"
        baseUrl("https://dummythemed.example")
        // No explicit deeplink {} — theme's themeDeeplink paths are auto-included.
    }
}
