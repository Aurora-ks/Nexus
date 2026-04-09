package com.nexus.core.network

import java.net.ServerSocket
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugHttpLoggingInterceptorTest {
    @Test
    fun logsRequestAndResponseWithFullPayloads() {
        val logs = mutableListOf<String>()
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("X-Test-Response", "ok")
                .setBody("""{"status":"ok"}"""),
        )
        server.start()

        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(DebugHttpLoggingInterceptor(logger = logs::add))
                .build()

            val request = Request.Builder()
                .url(server.url("/game/data?source=test"))
                .addHeader("X-Test-Request", "request-header")
                .post(
                    FormBody.Builder()
                        .add("roleId", "12345")
                        .add("serverId", "prod")
                        .build(),
                )
                .build()

            client.newCall(request).execute().use { response ->
                assertEquals(200, response.code)
                assertEquals("""{"status":"ok"}""", response.body?.string())
            }

            val combinedLogs = logs.joinToString(separator = "\n")
            assertTrue(combinedLogs.contains("HTTP REQUEST"))
            assertTrue(combinedLogs.contains("method=POST"))
            assertTrue(combinedLogs.contains("url=${server.url("/game/data?source=test")}"))
            assertTrue(combinedLogs.contains("path=/game/data"))
            assertTrue(combinedLogs.contains("X-Test-Request: request-header"))
            assertTrue(combinedLogs.contains("body=roleId=12345&serverId=prod"))
            assertTrue(combinedLogs.contains("HTTP RESPONSE"))
            assertTrue(combinedLogs.contains("code=200"))
            assertTrue(combinedLogs.contains("X-Test-Response: ok"))
            assertTrue(combinedLogs.contains("""body={"status":"ok"}"""))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun preservesResponseBodyForCallerAfterLogging() {
        val logs = mutableListOf<String>()
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("""{"value":"still-readable"}"""))
        server.start()

        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(DebugHttpLoggingInterceptor(logger = logs::add))
                .build()

            val request = Request.Builder()
                .url(server.url("/widget"))
                .get()
                .build()

            val body = client.newCall(request).execute().use { response ->
                response.body?.string()
            }

            assertEquals("""{"value":"still-readable"}""", body)
            assertTrue(logs.joinToString(separator = "\n").contains("""body={"value":"still-readable"}"""))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun logsFailureDetailsWhenRequestThrows() {
        val logs = mutableListOf<String>()
        val port = ServerSocket(0).use { it.localPort }
        val client = OkHttpClient.Builder()
            .addInterceptor(DebugHttpLoggingInterceptor(logger = logs::add))
            .build()

        val request = Request.Builder()
            .url("http://127.0.0.1:$port/failure")
            .get()
            .build()

        runCatching {
            client.newCall(request).execute()
        }

        val combinedLogs = logs.joinToString(separator = "\n")
        assertTrue(combinedLogs.contains("HTTP REQUEST"))
        assertTrue(combinedLogs.contains("HTTP FAILED"))
        assertTrue(combinedLogs.contains("method=GET"))
        assertTrue(combinedLogs.contains("url=http://127.0.0.1:$port/failure"))
        assertTrue(combinedLogs.contains("path=/failure"))
        assertTrue(combinedLogs.contains("error="))
    }
}
