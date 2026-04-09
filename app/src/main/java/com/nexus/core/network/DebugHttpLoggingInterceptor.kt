package com.nexus.core.network

import android.util.Log
import java.io.EOFException
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer

private const val LOG_TAG = "HttpDebugLog"

class DebugHttpLoggingInterceptor(
    private val logger: (String) -> Unit = { message -> Log.d(LOG_TAG, message) },
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        logRequest(request.method, request.url.toString(), request.url.encodedPath, request.headers.toString(), request.body)

        return try {
            val response = chain.proceed(request)
            logResponse(response)
            response
        } catch (throwable: Throwable) {
            logger(
                buildString {
                    appendLine("HTTP FAILED")
                    appendLine("method=${request.method}")
                    appendLine("url=${request.url}")
                    appendLine("path=${request.url.encodedPath}")
                    appendLine("headers=")
                    append(request.headers.toString().ifEmpty { "<empty>" })
                    appendLine()
                    appendLine("body=${request.body.toPlainText()}")
                    append("error=${throwable::class.java.simpleName}: ${throwable.message.orEmpty()}")
                },
            )
            throw throwable
        }
    }

    private fun logRequest(
        method: String,
        url: String,
        path: String,
        headers: String,
        body: RequestBody?,
    ) {
        logger(
            buildString {
                appendLine("HTTP REQUEST")
                appendLine("method=$method")
                appendLine("url=$url")
                appendLine("path=$path")
                appendLine("headers=")
                append(headers.ifEmpty { "<empty>" })
                appendLine()
                append("body=${body.toPlainText()}")
            },
        )
    }

    private fun logResponse(response: Response) {
        logger(
            buildString {
                appendLine("HTTP RESPONSE")
                appendLine("method=${response.request.method}")
                appendLine("url=${response.request.url}")
                appendLine("path=${response.request.url.encodedPath}")
                appendLine("code=${response.code}")
                appendLine("message=${response.message}")
                appendLine("headers=")
                append(response.headers.toString().ifEmpty { "<empty>" })
                appendLine()
                append("body=${response.peekBody(Long.MAX_VALUE).string().ifEmpty { "<empty>" }}")
            },
        )
    }
}

private fun RequestBody?.toPlainText(): String {
    if (this == null) return "<empty>"
    if (isDuplex()) return "<duplex body omitted>"
    if (isOneShot()) return "<one-shot body omitted>"

    return try {
        val buffer = Buffer()
        writeTo(buffer)

        if (isProbablyUtf8(buffer)) {
            val charset = contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            buffer.readString(charset)
        } else {
            "<binary ${contentLength()}-byte body omitted>"
        }
    } catch (ioException: IOException) {
        "<failed to read body: ${ioException.message.orEmpty()}>"
    }
}

private fun isProbablyUtf8(buffer: Buffer): Boolean {
    return try {
        val prefix = Buffer()
        val byteCount = minOf(buffer.size, 64)
        buffer.copyTo(prefix, 0, byteCount)

        repeat(16) {
            if (prefix.exhausted()) return true
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                return false
            }
        }
        true
    } catch (_: EOFException) {
        false
    }
}
