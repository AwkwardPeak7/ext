package eu.kanade.tachiyomi.extension.all.cubari

import android.annotation.SuppressLint
import android.app.Application
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.await
import keiyoushi.utils.parseAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RemoteStorage(
    private val client: OkHttpClient,
    private val headers: Headers,
) {
    private val context = Injekt.get<Application>()

    suspend fun getSeriesData(): List<RSManga> = coroutineScope {
        val data = runInWebView(
            loadWebPage = false,
            script = """
                (() => {
                    const prefix = "remotestorage:cache:nodes:/cubari/series/";
                    const remoteStorageItems = [];

                    for (let i = 0; i < localStorage.length; i++) {
                        const key = localStorage.key(i);
                        if (!key || key == prefix || !key.startsWith(prefix)) continue;

                        try {
                            let value = JSON.parse(localStorage.getItem(key));
                            if (value.local && value.local.body) {
                                value = JSON.parse(value.local.body);
                            } else if (value.common && value.common.body) {
                                value = JSON.parse(value.common.body);
                            }

                            remoteStorageItems.push(value);
                        } catch (e) {
                            console.error(`Error parsing value for key ${"$"}{key}:`, e);
                            continue;
                        }
                    }

                    let wireClient = localStorage.getItem('remotestorage:wireclient');
                    if (wireClient) wireClient = JSON.parse(wireClient);

                    return JSON.stringify({wireClient: wireClient, items: remoteStorageItems});
                })();
            """.trimIndent(),
            callback = {
                it.parseAs<LocalStorage>()
            },
        )

        if (data.wireClient == null) {
            return@coroutineScope data.items
        }

        val remoteStorageUrl = data.wireClient.href.toHttpUrl()
            .newBuilder()
            .addPathSegment("cubari")
            .addPathSegment("series")
            .addPathSegment("")
            .build()

        val remoteStorageHeader = headers.newBuilder()
            .set("Authorization", "Bearerer " + data.wireClient.token)
            .build()

        val remoteItems = client.newCall(GET(remoteStorageUrl, remoteStorageHeader))
            .await()
            .parseAs<RemoteStorageResponse>()
            .items
            .keys
            .filterNot {
                val source = it.substringBefore("-")
                val slug = it.substringAfter("-")

                data.items.any { item ->
                    item.source == source && item.slug == slug
                }
            }
            .map {
                async {
                    val url = data.wireClient.href.toHttpUrl()
                        .newBuilder()
                        .addPathSegment("cubari")
                        .addPathSegment("series")
                        .addPathSegment(it)
                        .build()

                    client.newCall(GET(url, remoteStorageHeader))
                        .await()
                        .parseAs<RSManga>()
                }
            }.awaitAll()

        data.items + remoteItems
    }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun <T> runInWebView(
        loadWebPage: Boolean,
        script: String,
        callback: (data: String) -> T,
    ) = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { cont ->
            val webview = WebView(context)

            webview.apply {
                with(settings) {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        view.evaluateJavascript(script) { result ->
                            if (cont.isActive) {
                                runCatching { callback(result.parseAs()) }
                                    .onFailure { cont.resumeWithException(it) }
                                    .onSuccess { cont.resume(it) }
                            }
                            webview.stopLoading()
                            webview.destroy()
                        }
                    }

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError,
                    ) {
                        if (cont.isActive) {
                            cont.resumeWithException(
                                Exception("WebView error"),
                            )
                        }
                        webview.stopLoading()
                        webview.destroy()
                    }
                }

                if (loadWebPage) {
                    loadUrl("https://cubari.moe/")
                } else {
                    loadDataWithBaseURL(
                        "https://cubari.moe/",
                        "",
                        "text/html",
                        "UTF-8",
                        null,
                    )
                }
            }

            cont.invokeOnCancellation {
                webview.stopLoading()
                webview.destroy()
            }
        }
    }
}
