package com.nexus.core.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class InMemoryCookieJar(
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) : CookieJar {
    private val cookieStore = LinkedHashMap<CookieKey, Cookie>()

    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>,
    ) {
        synchronized(cookieStore) {
            purgeExpiredCookies()
            cookies.forEach { cookie ->
                val key = cookie.key()
                if (cookie.expiresAt <= nowMillis()) {
                    cookieStore.remove(key)
                } else {
                    cookieStore[key] = cookie
                }
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        synchronized(cookieStore) {
            purgeExpiredCookies()
            return cookieStore.values.filter { cookie -> cookie.matches(url) }
        }
    }

    private fun purgeExpiredCookies() {
        val currentTimeMillis = nowMillis()
        val iterator = cookieStore.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.expiresAt <= currentTimeMillis) {
                iterator.remove()
            }
        }
    }

    private fun Cookie.key(): CookieKey {
        return CookieKey(
            name = name,
            domain = domain,
            path = path,
            secure = secure,
            hostOnly = hostOnly,
        )
    }
}

private data class CookieKey(
    val name: String,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val hostOnly: Boolean,
)
