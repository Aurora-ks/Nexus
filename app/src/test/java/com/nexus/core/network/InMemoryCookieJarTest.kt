package com.nexus.core.network

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryCookieJarTest {
    @Test
    fun loadForRequest_returnsCookiesForMatchingHost() {
        val cookieJar = InMemoryCookieJar(nowMillis = { 1_000L })
        val responseUrl = "https://api.example.com/login".toHttpUrl()
        val requestUrl = "https://api.example.com/user/profile".toHttpUrl()
        val cookie = Cookie.Builder()
            .name("session")
            .value("abc123")
            .domain("api.example.com")
            .path("/")
            .expiresAt(10_000L)
            .build()

        cookieJar.saveFromResponse(responseUrl, listOf(cookie))

        val cookies = cookieJar.loadForRequest(requestUrl)

        assertEquals(1, cookies.size)
        assertEquals("session", cookies.first().name)
        assertEquals("abc123", cookies.first().value)
    }

    @Test
    fun loadForRequest_skipsExpiredCookies() {
        var now = 1_000L
        val cookieJar = InMemoryCookieJar(nowMillis = { now })
        val url = "https://api.example.com/login".toHttpUrl()
        val cookie = Cookie.Builder()
            .name("session")
            .value("abc123")
            .domain("api.example.com")
            .path("/")
            .expiresAt(1_500L)
            .build()

        cookieJar.saveFromResponse(url, listOf(cookie))
        now = 2_000L

        val cookies = cookieJar.loadForRequest(url)

        assertTrue(cookies.isEmpty())
    }
}
