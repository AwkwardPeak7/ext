plugins {
    alias(kei.plugins.multisrc)
}

dependencies {
    api(project(":lib:cookieinterceptor"))
}

keiyoushi {
    baseVersionCode = 51
    libVersion = "1.6"

    deeplink {
        path("/.*/..*")
    }
}
