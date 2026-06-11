package com.nexus.game.wuwa

import com.nexus.game.wuwa.model.DashboardCardModel
import com.nexus.game.wuwa.model.DashboardDetailRowModel
import com.nexus.game.wuwa.model.DashboardMetricAccent
import com.nexus.game.wuwa.model.DashboardMetricModel
import com.nexus.game.wuwa.model.DashboardProgressModel
import com.nexus.game.wuwa.model.WuwaMetricDto
import com.nexus.game.wuwa.model.WuwaWidgetDataDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object WuwaMappers {
    private val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    fun toDashboardCard(dto: WuwaWidgetDataDto): DashboardCardModel {
        return DashboardCardModel(
            title = dto.roleName,
            subtitle = dto.serverName,
            uidText = "UID ${dto.roleId}",
            energyText = dto.energyData.toEnergyText(),
            signInStatus = dto.signInTxt.ifBlank { if (dto.hasSignIn) "已签到" else "未签到" },
            updatedAtText = if (dto.serverTime > 0) {
                formatter.format(
                    Instant.ofEpochSecond(dto.serverTime).atZone(ZoneId.systemDefault()),
                )
            } else {
                "待同步"
            },
            weeklyFocus = listOfNotNull(
                dto.weeklyData.toProgressText(),
                dto.weeklyRougeData.toProgressText(),
                dto.towerData.toProgressText(),
                dto.slashTowerData.toProgressText(),
                dto.newTowerData.toProgressText(),
                dto.weeklyFrameData.toProgressText(),
            ),
            resourceMetrics = listOf(
                dto.energyData.toResourceMetric(defaultLabel = "结晶波片", includeTimer = true),
                dto.storeEnergyData.toResourceMetric(defaultLabel = "结晶单质"),
                dto.livenessData.toResourceMetric(defaultLabel = "活跃度"),
            ),
            detailRows = listOfNotNull(
                dto.weeklyData.toDetailRow(),
                dto.weeklyRougeData.toDetailRow(),
                dto.towerData.toDetailRow(showRemainingTime = true, forceProgressValue = true),
                dto.slashTowerData.toDetailRow(showRemainingTime = true, forceProgressValue = true),
                dto.newTowerData.toDetailRow(forceProgressValue = true),
                dto.weeklyFrameData.toDetailRow(forceProgressValue = true),
            ),
            progress = dto.battlePassData.toBattlePassProgressModel()
                ?: dto.weeklyData.toProgressModel(),
        )
    }

    private fun WuwaMetricDto.toEnergyText(): String {
        return "${name} ${valueOrProgress(cur, total)}"
    }

    private fun WuwaMetricDto.toProgressText(): String {
        return "$name ${valueOrProgress(cur, total)}"
    }

    private fun WuwaMetricDto.toResourceMetric(
        defaultLabel: String,
        includeTimer: Boolean = false,
    ): DashboardMetricModel {
        val label = name.ifBlank { defaultLabel }
        val metricValueRaw = value
        val displayValue = when {
            total > 0 -> "$cur/$total"
            !metricValueRaw.isNullOrBlank() -> metricValueRaw
            else -> "--/--"
        }
        val caption = when {
            !timePreDesc.isNullOrBlank() && refreshTimeStamp > 0 -> {
                "${timePreDesc}${refreshTimeStamp.toRemainingTimeText()}"
            }
            includeTimer && refreshTimeStamp > 0 -> refreshTimeStamp.toRemainingTimeText()
            !metricValueRaw.isNullOrBlank() && metricValueRaw != valueOrProgress(cur, total) -> metricValueRaw
            status != 0 -> status.toStatusText()
            else -> "--"
        }
        return DashboardMetricModel(
            label = label,
            value = displayValue,
            caption = caption,
            imageUrl = img,
            accent = if (status > 0) DashboardMetricAccent.Positive else DashboardMetricAccent.Primary,
        )
    }

    private fun WuwaMetricDto.toDetailRow(
        displayLabel: String = name,
        showRemainingTime: Boolean = false,
        forceProgressValue: Boolean = false,
    ): DashboardDetailRowModel {
        return DashboardDetailRowModel(
            label = displayLabel,
            value = when {
                forceProgressValue -> valueOrProgress(cur, total)
                !value.isNullOrBlank() && total <= 0 -> value
                else -> valueOrProgress(cur, total)
            },
            caption = if (showRemainingTime && refreshTimeStamp > 0) {
                "剩余${refreshTimeStamp.toRemainingDaysHoursText()}"
            } else {
                null
            },
            imageUrl = img,
        )
    }

    private fun WuwaMetricDto.toProgressModel(): DashboardProgressModel? {
        val title = "先约电台"
        if (total <= 0) return null
        return DashboardProgressModel(
            label = title,
            levelText = "Lv.$cur",
            value = "本周经验 ${valueOrProgress(cur, total)}",
            progress = (cur.toFloat() / total.toFloat()).coerceIn(0f, 1f),
            imageUrl = img,
        )
    }

    private fun List<WuwaMetricDto>.toBattlePassProgressModel(): DashboardProgressModel? {
        if (isEmpty()) return null
        val levelMetric = first()
        val expMetric = getOrNull(1) ?: levelMetric
        val progressValue = valueOrProgress(expMetric.cur, expMetric.total)
        val progressFraction = if (expMetric.total > 0) {
            (expMetric.cur.toFloat() / expMetric.total.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        return DashboardProgressModel(
            label = "先约电台",
            levelText = "Lv.${levelMetric.cur}",
            value = "本周经验 $progressValue",
            progress = progressFraction,
            imageUrl = levelMetric.img ?: expMetric.img,
        )
    }

    private fun valueOrProgress(cur: Int, total: Int): String {
        return if (total > 0) "$cur/$total" else "--/--"
    }

    private fun Int.toStatusText(): String {
        return when (this) {
            1 -> "进行中"
            2 -> "已完成"
            else -> "--"
        }
    }

    private fun Long.toRemainingTimeText(): String {
        val now = Instant.now().epochSecond
        val diff = (this - now).coerceAtLeast(0)
        if (diff == 0L) return "已可领取"
        val hours = diff / 3600
        val minutes = (diff % 3600) / 60
        return when {
            hours > 0 -> "${hours}时${minutes}分钟"
            minutes > 0 -> "${minutes}分钟"
            else -> "即将恢复"
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
