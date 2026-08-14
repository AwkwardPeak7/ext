package eu.kanade.tachiyomi.extension.all.kavita

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.await
import eu.kanade.tachiyomi.source.model.Page
import keiyoushi.utils.parseAs
import keiyoushi.utils.toJsonString
import keiyoushi.zip.dataRange
import keiyoushi.zip.range
import keiyoushi.zip.readEntry
import keiyoushi.zip.readZipEntry
import keiyoushi.zip.zipDirectoryAsync
import kotlinx.serialization.Serializable
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.buffer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import java.io.IOException

suspend fun getEpubPageList(
    client: OkHttpClient,
    apiUrl: String,
    chapterId: Int,
    spineCount: Int,
    headers: Headers,
): List<Page> {
    val archivePages = runCatching {
        getArchiveEpubPages(client, "$apiUrl/Download/chapter?chapterId=$chapterId", headers)
    }.getOrNull()
    if (!archivePages.isNullOrEmpty()) return archivePages

    val pages = (0 until spineCount).mapNotNull { page ->
        val html = client.newCall(GET("$apiUrl/Book/$chapterId/book-page?page=$page", headers))
            .await()
            .parseAs<String>()
        imageFromBookPage(html, apiUrl)?.let { Page(page, imageUrl = it) }
    }
    if (pages.isEmpty()) throw IOException(UNSUPPORTED_EPUB_MESSAGE)
    return pages
}

private suspend fun getArchiveEpubPages(
    client: OkHttpClient,
    archiveUrl: String,
    headers: Headers,
): List<Page> {
    val directory = client.zipDirectoryAsync(archiveUrl, headers)
    val entries = directory.entries.associateBy { it.name }
    val container = entries[CONTAINER_PATH] ?: throw IOException("Invalid EPUB: missing $CONTAINER_PATH")
    val containerXml = client.readZipEntry(archiveUrl, container, headers).buffer().use { it.readUtf8() }
    val packagePath = Jsoup.parse(containerXml, "", Parser.xmlParser())
        .selectFirst("rootfile[full-path]")
        ?.attr("full-path")
        ?.takeIf(String::isNotBlank)
        ?: throw IOException("Invalid EPUB: package document not found")
    val packageEntry = entries[packagePath] ?: throw IOException("Invalid EPUB: package document not found")
    val packageXml = client.readZipEntry(archiveUrl, packageEntry, headers).buffer().use { it.readUtf8() }
    val packageDocument = Jsoup.parse(packageXml, "", Parser.xmlParser())
    val manifest = packageDocument.select("manifest > item").associate { item ->
        item.attr("id") to ManifestItem(
            path = resolveArchivePath(packagePath, item.attr("href")),
            mediaType = item.attr("media-type"),
        )
    }
    val spine = packageDocument.select("spine > itemref")
        .filterNot { it.attr("linear").equals("no", true) }
        .mapNotNull { manifest[it.attr("idref")] }

    val imageEntries = spine.map { item ->
        if (item.mediaType.startsWith("image/")) {
            entries[item.path] to item.mediaType
        } else {
            val contentEntry = entries[item.path] ?: throw IOException("Invalid EPUB spine entry: ${item.path}")
            val content = client.readZipEntry(archiveUrl, contentEntry, headers).buffer().use { it.readUtf8() }
            val document = Jsoup.parse(content, "", Parser.xmlParser())
            val imagePath = imagePathFromDocument(document, item.path)
                ?: throw IOException(UNSUPPORTED_EPUB_MESSAGE)
            entries[imagePath] to manifest.values.firstOrNull { it.path == imagePath }?.mediaType.orEmpty()
        }
    }
    if (imageEntries.isEmpty() || imageEntries.any { it.first == null }) {
        throw IOException(UNSUPPORTED_EPUB_MESSAGE)
    }

    return imageEntries.mapIndexed { index, (entry, mediaType) ->
        val image = entry!!
        val requestData = EpubImageRequestDto(
            archiveUrl = archiveUrl,
            localHeaderOffset = image.localHeaderOffset,
            compressedSize = image.compressedSize,
            method = image.method,
            mediaType = mediaType.ifBlank { mediaTypeFromPath(image.name) },
        )
        val imageUrl = "http://127.0.0.1/".toHttpUrl().newBuilder()
            .fragment(requestData.toJsonString())
            .build()
        Page(index, imageUrl = imageUrl.toString())
    }
}

private fun imageFromBookPage(html: String, apiUrl: String): String? {
    if (html.isBlank() || html == "#document") return null
    val document = Jsoup.parse(html, apiUrl)
    val source = singleImageSource(document) ?: throw IOException(UNSUPPORTED_EPUB_MESSAGE)
    val base = apiUrl.toHttpUrl()
    val resolved = when {
        source.startsWith("//") -> "${base.scheme}:$source".toHttpUrlOrNull()
        source.toHttpUrlOrNull() != null -> source.toHttpUrl()
        else -> base.resolve(source)
    } ?: throw IOException("Invalid EPUB image URL")
    return resolved.newBuilder().removeAllQueryParameters("apiKey").build().toString()
}

private fun imagePathFromDocument(document: Document, contentPath: String): String? {
    val source = singleImageSource(document) ?: return null
    return resolveArchivePath(contentPath, source)
}

private fun singleImageSource(document: Document): String? {
    document.select("style, script, noscript").remove()
    if (document.body().text().isNotBlank()) return null
    val sources = buildList {
        document.select("img[src]").mapTo(this) { it.attr("src") }
        document.select("image").mapNotNullTo(this) {
            it.attr("href").ifBlank { it.attr("xlink:href") }.takeIf(String::isNotBlank)
        }
    }.map { it.substringBefore('#') }.filter(String::isNotBlank).distinct()
    return sources.singleOrNull()
}

private fun resolveArchivePath(basePath: String, relativePath: String): String {
    val cleanRelative = relativePath.substringBefore('#').substringBefore('?')
    val segments = (basePath.substringBeforeLast('/', "") + "/" + cleanRelative).split('/')
    val resolved = ArrayDeque<String>()
    segments.forEach { segment ->
        when (segment) {
            "", "." -> Unit
            ".." -> if (resolved.isNotEmpty()) resolved.removeLast()
            else -> resolved.addLast(segment)
        }
    }
    return resolved.joinToString("/")
}

private fun mediaTypeFromPath(path: String) = when (path.substringAfterLast('.', "").lowercase()) {
    "avif" -> "image/avif"
    "gif" -> "image/gif"
    "jxl" -> "image/jxl"
    "png" -> "image/png"
    "svg" -> "image/svg+xml"
    "webp" -> "image/webp"
    else -> "image/jpeg"
}

class EpubImageInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (originalRequest.url.host != "127.0.0.1") return chain.proceed(originalRequest)
        val data = originalRequest.url.fragment?.parseAs<EpubImageRequestDto>()
            ?: return chain.proceed(originalRequest)
        val request = originalRequest.newBuilder()
            .url(data.archiveUrl)
            .range(dataRange(data.localHeaderOffset, data.compressedSize))
            .build()
        val response = chain.proceed(request)
        if (!response.isSuccessful) return response
        val image = readEntry(response.body.source(), data.compressedSize, data.method).buffer()
        return response.newBuilder()
            .request(originalRequest)
            .body(image.asResponseBody(data.mediaType.toMediaTypeOrNull()))
            .build()
    }
}

@Serializable
private class EpubImageRequestDto(
    val archiveUrl: String,
    val localHeaderOffset: Long,
    val compressedSize: Long,
    val method: Int,
    val mediaType: String,
)

private class ManifestItem(val path: String, val mediaType: String)

private const val CONTAINER_PATH = "META-INF/container.xml"
private const val UNSUPPORTED_EPUB_MESSAGE = "Only image-based EPUBs are supported"
