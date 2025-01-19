package keiyoushi.extension.all.ehentai

import android.net.Uri

/**
 * Uri filter
 */
interface UriFilter {
    fun addToUri(builder: Uri.Builder)
}
