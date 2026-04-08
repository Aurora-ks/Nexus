package com.nexus.game.wuwa

import com.nexus.game.wuwa.model.DashboardCardModel
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
            ),
        )
    }

    private fun WuwaMetricDto?.toEnergyText(): String {
        if (this == null) return "体力数据缺失"
        return "${name ?: "结晶波片"} ${cur}/${total}"
    }

    private fun WuwaMetricDto?.toProgressText(): String? {
        if (this == null) return null
        val title = name ?: return null
        return "$title ${cur}/${total}"
    }
}
