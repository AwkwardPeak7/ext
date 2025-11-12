package eu.kanade.tachiyomi.extension.all.cubari

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.await
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.closeQuietly
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

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun getSeriesData(): List<RSManga> {
        val data = runInWebView(
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
                it.parseAs<String>().parseAs<WebViewLocalStorageItems>()
            },
        )

        if (data.wireClient == null) {
            return data.items
        }

        val remoteStorageUrl = data.wireClient.href.toHttpUrl()
            .newBuilder()
            .addPathSegment("cubari")
            .addPathSegment("series")
            .addPathSegment("")
            .build()

        val remoteStorageHeader = headers.newBuilder()
            .set("Authorization", "Bearer " + data.wireClient.token)
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
                coroutineScope {
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
                }
            }.awaitAll()

        putSeriesDataInWebView(remoteItems)

        return data.items + remoteItems
    }

    suspend fun tagSeries(series: CubariManga, source: String, slug: String) {
        val rsManga = RSManga(
            source = source,
            slug = slug,
            coverUrl = series.cover,
            url = "/read/$source/$slug/",
            title = series.title,
            timestamp = System.currentTimeMillis(),
            pinned = false,
        )

        putSeriesDataInWebView(listOf(rsManga))
        syncSeriesDataWithRemoteStorage(listOf(rsManga))
    }

    private suspend fun putSeriesDataInWebView(data: List<RSManga>) {
        if (data.isEmpty()) {
            return
        }

        val objects = data.associate {
            val obj = WebViewLocalStorage(
                path = "/cubari/series/${it.source}-${it.slug}",
                local = WebViewLocalStorage.Local(
                    body = it.toJsonString(json),
                ),
            ).toJsonString(json)

            "${it.source}-${it.slug}" to obj
        }.toJsonString(json)

        runInWebView(
            script = """
                (() => {
                    const objectsMap = $objects
                    const keys = Object.keys(objectsMap).reduce((acc, key) => {
                        acc[key] = true;
                        return acc;
                    }, {});

                    localStorage.setItem('proxyHistory', true);
                    localStorage.setItem('storageOnce', 1);

                    let tmp = localStorage.getItem('remotestorage:cache:nodes:/');
                    if (!tmp) {
                        localStorage.setItem(
                            'remotestorage:cache:nodes:/',
                            JSON.stringify({
                                path: '/',
                                common: { itemsMap: {} },
                                local: { itemsMap: { 'cubari/': true } }
                            })
                        );
                    }

                    tmp = localStorage.getItem('remotestorage:cache:nodes:/cubari/');
                    if (!tmp) {
                        localStorage.setItem(
                            'remotestorage:cache:nodes:/cubari/',
                            JSON.stringify({
                                path: '/cubari/',
                                common: { itemsMap: {} },
                                local: { itemsMap: { 'series/': true } }
                            })
                        );
                    }

                    let series = localStorage.getItem('remotestorage:cache:nodes:/cubari/series/');
                    if (!series) {
                        series = {
                            path: '/cubari/series/',
                            common: { itemsMap: {} },
                            local: { itemsMap: keys }
                        };
                    } else {
                        series = JSON.parse(series);
                        series.local = series.local || { itemsMap: {} };
                        series.local.itemsMap = series.local.itemsMap || {};
                        for (const key of Object.keys(keys)) {
                            series.local.itemsMap[key] = true;
                        }
                        series.common = series.common || { itemsMap: {} };
                        series.common.itemsMap = series.common.itemsMap || {};
                        for (const key of Object.keys(keys)) {
                            delete series.common.itemsMap[key];
                        }
                    }
                    localStorage.setItem('remotestorage:cache:nodes:/cubari/series/', JSON.stringify(series));

                    for (const [key, value] of Object.entries(objectsMap)) {
                        localStorage.setItem(`remotestorage:cache:nodes:/cubari/series/${"$"}{key}`, value);
                    }
                })();
            """.trimIndent(),
            callback = {
                Log.i("Cubari", "WebView localStorage updated")
            },
        )
    }

    private suspend fun syncSeriesDataWithRemoteStorage(data: List<RSManga>) {
        if (data.isEmpty()) {
            return
        }

        val wireClient = runInWebView(
            script = """
                (() => {
                    return localStorage.getItem('remotestorage:wireclient');
                })();
            """.trimIndent(),
            callback = {
                it.takeIf { it != "null" }?.parseAs<String>()?.parseAs<WireClient?>()
            },
        )
            ?: return

        val remoteStorageHeader = headers.newBuilder()
            .set("Authorization", "Bearer " + wireClient.token)
            .build()

        coroutineScope {
            data.map {
                async {
                    val url = wireClient.href.toHttpUrl().newBuilder()
                        .addPathSegment("cubari")
                        .addPathSegment("series")
                        .addPathSegment("${it.source}-${it.slug}")
                        .build()

                    val request = Request.Builder().apply {
                        url(url)
                        method(
                            method = "PUT",
                            body = it.toJsonString()
                                .toRequestBody("application/json".toMediaType()),
                        )
                        headers(remoteStorageHeader)
                    }.build()

                    client.newCall(request).await().closeQuietly()
                }
            }.awaitAll()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun <T> runInWebView(
        script: String,
        callback: (data: String) -> T,
    ) = withTimeout(10.seconds) {
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { cont ->
                val webview = WebView(context)

                fun cleanup() {
                    webview.stopLoading()
                    webview.destroy()
                }

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
                                        .onFailure {
                                            cleanup()
                                            cont.resumeWithException(it)
                                        }
                                        .onSuccess {
                                            cleanup()
                                            cont.resume(it)
                                        }
                                } else {
                                    cleanup()
                                }
                            }
                        }
                    }

                    loadDataWithBaseURL(
                        "https://cubari.moe/",
                        "",
                        "text/html",
                        "UTF-8",
                        null,
                    )
                }

                cont.invokeOnCancellation {
                    cleanup()
                }
            }
        }
    }
}
