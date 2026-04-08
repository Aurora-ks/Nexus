package com.nexus.game.wuwa

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReqMonthProvider(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val serverFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun fromServerTimeOrNow(nowServerTimes: String?): String {
        val month = nowServerTimes
            ?.takeIf { it.isNotBlank() }
            ?.let { LocalDateTime.parse(it, serverFormatter).monthValue }
            ?: LocalDateTime.now(clock.withZone(ZoneId.systemDefault())).monthValue
        return month.toString().padStart(2, '0')
    }
}
