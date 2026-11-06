// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard

import androidx.test.core.app.ApplicationProvider
import helium314.keyboard.latin.App
import helium314.keyboard.latin.common.Links
import helium314.keyboard.latin.common.LocaleUtils.constructLocale
import helium314.keyboard.latin.utils.getKnownDictionariesForLocale
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class XLinkTest { // Without the X, SubtypeTests fail with ClassCastException. WTF?
    @Test fun knownDictionaries() {
        requireExternalLinkChecks()
        val context = ApplicationProvider.getApplicationContext<App>()
        val urls = mutableSetOf<String>()
        context.assets.open("dictionaries_in_dict_repo.txt").reader().readLines().forEach { line ->
            val locale = line.split(",").getOrNull(1)?.constructLocale() ?: return@forEach
            getKnownDictionariesForLocale(locale, context).forEach {
                urls.add(it.second)
            }
        }
        // can't check everything at once, this will trigger some rate limit
        val typeToCheck = listOf("/dictionaries_experimental/", "/emoji_cldr_signal_dictionaries/", "/dictionaries/").random()
        urls.forEach {
            if (it.contains(typeToCheck))
            checkLink(it)
        }
    }

    @Test fun readmeLinks() {
        requireExternalLinkChecks()
        val file = File("../README.md")
        val linkRegex = "(?:https?:\\/\\/.)?(?:www\\.)?[-a-zA-Z0-9@%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b(?:[-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)".toRegex()
        val links = linkRegex.findAll(file.readText())
        links.forEach {
            if (it.value.contains("heli", true))
                checkLink(it.value.trim('.'))
        }
    }

    @Test fun layoutsLinks() {
        requireExternalLinkChecks()
        val file = File("../layouts.md")
        val linkRegex = "(?:https?:\\/\\/.)?(?:www\\.)?[-a-zA-Z0-9@%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b(?:[-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)".toRegex()
        val links = linkRegex.findAll(file.readText())
        links.forEach {
            if (it.value.contains("heli", true))
                checkLink(it.value)
        }
    }

    @Test fun layoutsLinksInternal() {
        val root = File("..")
        val document = root.resolve("layouts.md").readText()
        val paths = """\]\((app/src/[^\s)#]+)(?:#[^)]*)?\)""".toRegex()
            .findAll(document).map { it.groupValues[1] }.toSet()
        assertTrue(paths.isNotEmpty(), "Expected local layout links")
        for (path in paths) {
            assertTrue(root.resolve(path).exists(), "Missing layout path: $path")
        }
    }

    @Test fun otherLinks() {
        requireExternalLinkChecks()
        listOf(Links.LICENSE, Links.LAYOUT_WIKI_URL, Links.WIKI_URL, Links.CUSTOM_LAYOUTS, Links.CUSTOM_COLORS).forEach {
            checkLink(it)
        }
    }

    private fun requireExternalLinkChecks() {
        assumeTrue("Live URL checks are opt-in", System.getenv("SATZFINK_CHECK_EXTERNAL_LINKS") == "1")
    }

    private fun checkLink(link: String) {
        if (link.contains("wiki/"))
            return checkWikiLink(link)
        val url = URL(link)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout = 5_000
        }
        connection.requestMethod = "HEAD"
        if (connection.responseCode != 200)
            println("error checking $link")
        assertEquals(200, connection.responseCode)
    }

    private fun checkWikiLink(link: String) {
        val url = URL(link)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout = 5_000
        }
        if (connection.responseCode != 200)
            println("error checking $link")
        assertEquals(200, connection.responseCode)
        val text = connection.getInputStream().reader().readText()
        if ("Create new page" in text)
            println("error checking wiki $link")
        assert("Create new page" !in text)
    }
}
