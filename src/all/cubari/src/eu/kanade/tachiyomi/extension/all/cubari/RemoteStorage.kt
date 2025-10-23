package eu.kanade.tachiyomi.extension.all.cubari

import android.annotation.SuppressLint
import android.app.Application
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import keiyoushi.utils.parseAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object RemoteStorage {
    private val context = Injekt.get<Application>()

    suspend fun getSeriesData(): List<RSManga> {
        val data = runInWebView(
            loadWebPage = false,
            script = """
                (() => {
                  const prefix = "remotestorage:cache:nodes:/cubari/series/";
                  const remoteStorageItems = [];

                  for (let i = 0; i < localStorage.length; i++) {
                    const key = localStorage.key(i);
                    if (!key || key == prefix || !key.startsWith(prefix)) continue;

                    let value = JSON.parse(localStorage.getItem(key));
                    if (value.local && value.local.body) {
                        value = JSON.parse(value.local.body);
                    } else if (value.common && value.common.body) {
                        value = JSON.parse(value.common.body);
                    }

                    remoteStorageItems.push(value);
                  }

                  const wireClient = localStorage.getItem('remotestorage:wireclient');

                  return JSON.stringify({wireClient: wireClient, items: remoteStorageItems});
                })();
            """.trimIndent(),
            callback = {
                it.parseAs<LocalStorage>()
            },
        )

        if (data.wireClient == null) {
            return data.items
        }

        throw Exception("Not implemented")
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
