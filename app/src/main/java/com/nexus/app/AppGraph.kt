package com.nexus.app

import com.nexus.core.network.KuroHeaderProvider
import com.nexus.core.network.NetworkModule
import com.nexus.core.storage.secure.InMemoryTokenStore
import com.nexus.game.wuwa.InMemoryWuwaAccountStore
import com.nexus.game.wuwa.InMemoryWuwaSnapshotStore
import com.nexus.game.wuwa.WuwaRepositoryImpl

object AppGraph {
    private val accountStore = InMemoryWuwaAccountStore()
    private val snapshotStore = InMemoryWuwaSnapshotStore()
    private val tokenStore = InMemoryTokenStore()
    private val headerProvider = KuroHeaderProvider()

    val repository: WuwaRepositoryImpl by lazy {
        WuwaRepositoryImpl(
            roleApi = NetworkModule.wuwaRoleApi,
            widgetApi = NetworkModule.wuwaWidgetApi,
            headerProvider = headerProvider,
            accountStore = accountStore,
            snapshotStore = snapshotStore,
            tokenStore = tokenStore,
        )
    }
}
