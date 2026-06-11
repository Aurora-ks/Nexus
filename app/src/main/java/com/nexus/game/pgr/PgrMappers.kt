package com.nexus.game.pgr

import com.nexus.game.pgr.model.PgrDailyDataDto
import com.nexus.game.pgr.model.PgrDailyDataItemDto
import com.nexus.game.wuwa.model.DashboardCardModel
import com.nexus.game.wuwa.model.DashboardDetailRowModel
import com.nexus.game.wuwa.model.DashboardMetricAccent
import com.nexus.game.wuwa.model.DashboardMetricModel
import com.nexus.game.wuwa.model.WuwaAccount
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object PgrMappers {
    private val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    fun toDashboardCard(account: WuwaAccount, dto: PgrDailyDataDto): DashboardCardModel {
        return DashboardCardModel(
            title = account.roleName,
            subtitle = account.serverName,
            uidText = "UID ${account.roleId}",
            energyText = dto.actionData.toDisplayText(defaultLabel = "血清"),
            signInStatus = "每日活跃 ${dto.activeData.toDisplayValue()}",
            updatedAtText = if (dto.serverTime > 0) {
                formatter.format(Instant.ofEpochSecond(dto.serverTime).atZone(ZoneId.systemDefault()))
            } else {
                "待同步"
            },
            weeklyFocus = buildList {
                add(dto.actionData.toDisplayText(defaultLabel = "血清"))
                add(dto.activeData.toDisplayText(defaultLabel = "每日活跃"))
                add(dto.dormData.toDisplayText(defaultLabel = "委托情况"))
                addAll(dto.bossData.map { it.toDisplayText(defaultLabel = it.name ?: "挑战") })
            },
            resourceMetrics = listOf(
                dto.actionData.toResourceMetric(defaultLabel = "血清", includeTimer = true),
                dto.activeData.toResourceMetric(defaultLabel = "每日活跃"),
                dto.dormData.toResourceMetric(defaultLabel = "委托情况"),
            ),
            detailRows = dto.bossData.map {
                it.toDetailRow(defaultLabel = it.name ?: "挑战", showRemainingTime = true)
            },
            progress = null,
        )
    }

    private fun PgrDailyDataItemDto.toResourceMetric(
        defaultLabel: String,
        includeTimer: Boolean = false,
    ): DashboardMetricModel {
        return DashboardMetricModel(
            label = displayLabel(defaultLabel),
            value = toDisplayValue(),
            caption = toCaption(includeTimer = includeTimer),
            accent = if (status == 2) DashboardMetricAccent.Positive else DashboardMetricAccent.Primary,
        )
    }

    private fun PgrDailyDataItemDto.toDetailRow(
        defaultLabel: String,
        showRemainingTime: Boolean = false,
    ): DashboardDetailRowModel {
        return DashboardDetailRowModel(
            label = displayLabel(defaultLabel),
            value = toDisplayValue(),
            caption = toCaption(includeTimer = showRemainingTime).takeIf { it.isNotBlank() },
        )
    }

    private fun PgrDailyDataItemDto.toDisplayText(defaultLabel: String): String {
        return "${displayLabel(defaultLabel)} ${toDisplayValue()}"
    }

    private fun PgrDailyDataItemDto.displayLabel(defaultLabel: String): String {
        return name?.takeIf { it.isNotBlank() }
            ?: key?.takeIf { it.isNotBlank() }
            ?: defaultLabel
    }

    private fun PgrDailyDataItemDto.toDisplayValue(): String {
        return when {
            total > 0 -> "$cur/$total"
            else -> "--/--"
        }
    }

    private fun PgrDailyDataItemDto.toCaption(includeTimer: Boolean): String {
        val captions = buildList {
            if (includeTimer && refreshTimeStamp != null && refreshTimeStamp > 0) {
                add("剩余${refreshTimeStamp.toRefreshTimeText()}")
            }
            expireTimeStamp?.let {
                add("到期 ${it.toRelativeTimeText()}")
            }
        }
        return captions.joinToString("\n")
    }

    private fun Long.toRefreshTimeText(): String {
        val diff = secondsUntil()
        return when {
            diff >= 604_800 -> "${diff / 604_800}周"
            diff >= 86_400 -> "${diff / 86_400}天"
            else -> {
                val hours = diff / 3_600
                val minutes = (diff % 3_600) / 60
                "${hours}小时${minutes}分钟"
            }
        }
    }

    private fun Long.toRelativeTimeText(): String {
        val diff = secondsUntil()
        return when {
            diff >= 604_800 -> "${diff.ceilDiv(604_800)}周"
            diff >= 86_400 -> "${diff.ceilDiv(86_400)}天"
            else -> "${diff.ceilDiv(3_600)}小时"
        }
    }

    private fun Long.secondsUntil(): Long {
        val now = Instant.now().epochSecond
        return (this - now).coerceAtLeast(0)
    }

    private fun Long.ceilDiv(divisor: Long): Long {
        return if (this == 0L) 0L else (this + divisor - 1) / divisor
    }
}
