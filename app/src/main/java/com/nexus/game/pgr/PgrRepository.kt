package com.nexus.game.pgr

import com.nexus.core.model.AppError
import com.nexus.core.model.GameType
import com.nexus.core.model.OperationResult
import com.nexus.core.network.KuroHeaderProvider
import com.nexus.game.kuro.KuroTokenParser
import com.nexus.game.kuro.api.KuroRoleApi
import com.nexus.game.pgr.api.PgrRoleBoxApi
import com.nexus.game.pgr.model.PgrRefreshDataEnvelopeDto
import com.nexus.game.wuwa.model.DashboardCardModel
import com.nexus.game.wuwa.model.WuwaAccount

class PgrRepository(
    private val roleApi: KuroRoleApi,
    private val roleBoxApi: PgrRoleBoxApi,
    private val headerProvider: KuroHeaderProvider,
) {
    suspend fun bindAccount(token: String, nickname: String?): OperationResult<WuwaAccount> {
        val userId = when (val parsed = KuroTokenParser.parseUserId(token)) {
            is OperationResult.Success -> parsed.value
            is OperationResult.Failure -> return parsed
        }

        val roleResponse = runCatching {
            roleApi.findUserDefaultRole(
                headers = headerProvider.nativeHeaders(token),
                queryUserId = userId,
            )
        }.getOrElse {
            return OperationResult.Failure(AppError.UnknownError(it.message ?: "绑定角色请求失败"))
        }

        if (!roleResponse.success) {
            return OperationResult.Failure(AppError.AuthError(roleResponse.msg))
        }

        val pgrRole = roleResponse.data
            ?.defaultRoleList
            ?.firstOrNull { it.gameId == GameType.PGR.gameId }
            ?: return OperationResult.Failure(AppError.ApiContractError("未找到战双帕弥什角色"))

        val accountDataResponse = runCatching {
            roleBoxApi.getAccountData(
                headers = headerProvider.webHeaders(token),
                serverId = pgrRole.serverId,
                roleId = pgrRole.roleId,
            )
        }.getOrElse {
            return OperationResult.Failure(AppError.UnknownError(it.message ?: "获取战双角色账号展示数据失败"))
        }

        if (!accountDataResponse.success) {
            return OperationResult.Failure(
                mapEnvelopeError(
                    code = accountDataResponse.code,
                    message = accountDataResponse.msg,
                    defaultMessage = "获取战双角色账号展示数据失败",
                ),
            )
        }

        val accountData = accountDataResponse.data
            ?: return OperationResult.Failure(AppError.ApiContractError("战双角色账号展示数据响应缺少 data"))

        return OperationResult.Success(
            WuwaAccount(
                gameId = GameType.PGR.gameId,
                userId = pgrRole.userId,
                roleId = accountData.roleId,
                roleName = accountData.roleName,
                serverId = pgrRole.serverId,
                serverName = accountData.serverName,
                nickname = nickname,
                headPhotoUrl = accountData.headIconUrl,
            ),
        )
    }

    suspend fun fetchDashboardCard(
        account: WuwaAccount,
        token: String,
    ): DashboardCardModel? {
        when (refreshDashboardData(account, token)) {
            is OperationResult.Success -> Unit
            is OperationResult.Failure -> return null
        }

        val response = runCatching {
            roleBoxApi.getDailyData(
                headers = headerProvider.webHeaders(token),
                serverId = account.serverId,
                roleId = account.roleId,
            )
        }.getOrNull() ?: return null

        if (!response.success) return null
        val data = response.data ?: return null
        return PgrMappers.toDashboardCard(account, data)
    }

    private suspend fun refreshDashboardData(
        account: WuwaAccount,
        token: String,
    ): OperationResult<Unit> {
        val response = runCatching {
            roleBoxApi.refreshData(
                headers = headerProvider.webHeaders(token),
                gameId = GameType.PGR.gameId.toString(),
                roleId = account.roleId,
                serverId = account.serverId,
            )
        }.getOrElse {
            return OperationResult.Failure(AppError.UnknownError(it.message ?: "刷新战双日常数据失败"))
        }

        if (!response.success) {
            return OperationResult.Failure(
                response.toAppError(defaultMessage = "刷新战双日常数据失败"),
            )
        }
        return OperationResult.Success(Unit)
    }

    private fun PgrRefreshDataEnvelopeDto.toAppError(defaultMessage: String): AppError {
        return mapEnvelopeError(code = code, message = msg, defaultMessage = defaultMessage)
    }

    private fun mapEnvelopeError(
        code: Int,
        message: String,
        defaultMessage: String,
    ): AppError {
        val normalizedMessage = message.ifBlank { defaultMessage }
        if (code == 401 || code == 403 || normalizedMessage.containsAuthHint()) {
            return AppError.AuthError(normalizedMessage)
        }
        return AppError.UnknownError(normalizedMessage)
    }

    private fun String.containsAuthHint(): Boolean {
        return contains("token", ignoreCase = true) ||
                contains("鉴权") ||
                contains("认证") ||
                contains("登录") ||
                contains("失效")
    }
}
