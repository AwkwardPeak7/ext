package eu.kanade.tachiyomi.extension.all.cubari

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
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
import kotlin.time.Duration.Companion.seconds

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
                it.parseAs<String>().parseAs<LocalStorage>()
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

    suspend fun tagSeries(url: String) {
        runInWebView(
            script = """
                (() => {
                   tag();
                   return true;
                })();
            """.trimIndent(),
            callback = {
                Log.i("Cubari", "in callback...")
                Thread.sleep(10.seconds.inWholeMilliseconds)
                Log.i("Cubari", "Cubari::RemoteStorage::tagSeries done")
            },
            loadWebPage = true,
            url = url,
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun <T> runInWebView(
        script: String,
        callback: (data: String) -> T,
        loadWebPage: Boolean,
        url: String = baseUrl,
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
                                runCatching { callback(result) }
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
                                Exception("WebView error when requesting: ${request.url}"),
                            )
                        }
                        webview.stopLoading()
                        webview.destroy()
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        if (consoleMessage == null) { return false }
                        val logContent = "wv: ${consoleMessage.message()} (${consoleMessage.sourceId()}, line ${consoleMessage.lineNumber()})"
                        when (consoleMessage.messageLevel()) {
                            ConsoleMessage.MessageLevel.DEBUG -> Log.d("Cubari", logContent)
                            ConsoleMessage.MessageLevel.ERROR -> Log.e("Cubari", logContent)
                            ConsoleMessage.MessageLevel.LOG -> Log.i("Cubari", logContent)
                            ConsoleMessage.MessageLevel.TIP -> Log.i("Cubari", logContent)
                            ConsoleMessage.MessageLevel.WARNING -> Log.w("Cubari", logContent)
                            else -> Log.d("Cubari", logContent)
                        }

                        return true
                    }
                }

                if (loadWebPage) {
                    loadUrl(url)
                } else {
                    loadDataWithBaseURL(
                        url,
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

private const val baseUrl = "https://cubari.moe/"
