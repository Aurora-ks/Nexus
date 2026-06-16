package com.nexus.game.wuwa

import java.time.Clock
import java.time.LocalDate

class ReqMonthProvider(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun currentMonth(): String {
        val month = LocalDate.now(clock).monthValue
        return month.toString().padStart(2, '0')
    }
}
