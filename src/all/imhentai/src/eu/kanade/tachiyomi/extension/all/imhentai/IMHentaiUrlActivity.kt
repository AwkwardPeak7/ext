package eu.kanade.tachiyomi.extension.all.imhentai

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlin.system.exitProcess

/**
 * Springboard that accepts https://imhentai.xxx/gallery/xxxxxx intents and redirects them to
 * the main Tachiyomi process.
 */
class IMHentaiUrlActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pathSegments = intent?.data?.pathSegments
        if (pathSegments != null && pathSegments.size > 1) {
            val id = pathSegments[1]
            val mainIntent =
                Intent().apply {
                    action = "eu.kanade.tachiyomi.SEARCH"
                    putExtra("query", "id:$id")
                    putExtra("filter", packageName)
                }

            try {
                startActivity(mainIntent)
            } catch (e: ActivityNotFoundException) {
                Log.e("IMHentaiUrlActivity", e.toString())
            }
        } else {
            Log.e("IMHentaiUrlActivity", "could not parse uri from intent $intent")
        }

        finish()
        exitProcess(0)
    }
}
