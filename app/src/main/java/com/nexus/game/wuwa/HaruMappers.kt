package com.nexus.game.wuwa

import com.nexus.game.wuwa.model.DashboardCardModel
import com.nexus.game.wuwa.model.DashboardDetailRowModel
import com.nexus.game.wuwa.model.DashboardMetricAccent
import com.nexus.game.wuwa.model.DashboardMetricModel
import com.nexus.game.wuwa.model.HaruDailyDataDto
import com.nexus.game.wuwa.model.HaruDailyDataItemDto
import com.nexus.game.wuwa.model.WuwaAccount
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object HaruMappers {
    private val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    fun toDashboardCard(account: WuwaAccount, dto: HaruDailyDataDto): DashboardCardModel {
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

    private fun HaruDailyDataItemDto.toResourceMetric(
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

    private fun HaruDailyDataItemDto.toDetailRow(
        defaultLabel: String,
        showRemainingTime: Boolean = false,
    ): DashboardDetailRowModel {
        return DashboardDetailRowModel(
            label = displayLabel(defaultLabel),
            value = toDisplayValue(),
            caption = toCaption(includeTimer = showRemainingTime).takeIf { it != "--" },
        )
    }

    private fun HaruDailyDataItemDto.toDisplayText(defaultLabel: String): String {
        return "${displayLabel(defaultLabel)} ${toDisplayValue()}"
    }

    private fun HaruDailyDataItemDto.displayLabel(defaultLabel: String): String {
        return name?.takeIf { it.isNotBlank() }
            ?: key?.takeIf { it.isNotBlank() }
            ?: defaultLabel
    }

    private fun HaruDailyDataItemDto.toDisplayValue(): String {
        return when {
            total > 0 -> "$cur/$total"
            else -> "--/--"
        }
    }

    private fun HaruDailyDataItemDto.toCaption(includeTimer: Boolean): String {
        return when {
            includeTimer && refreshTimeStamp != null && refreshTimeStamp > 0 -> {
                "刷新剩余${refreshTimeStamp.toRemainingDaysHoursText()}"
            }
            else -> ""
        }
    }

    private fun Long.toRemainingDaysHoursText(): String {
        val now = Instant.now().epochSecond
        val diff = (this - now).coerceAtLeast(0)
        if (diff == 0L) return "0时"
        val days = diff / 86_400
        val hours = (diff % 86_400) / 3_600
        return if (days >= 1) {
            "${days}天${hours}时"
        } else {
            "${(diff + 3_599) / 3_600}时"
        }
    }
}
