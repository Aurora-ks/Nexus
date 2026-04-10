package com.nexus.app

import android.app.Application
import com.nexus.core.network.IpAddressProvider
import com.nexus.core.network.KuroHeaderProvider
import com.nexus.core.network.NetworkModule
import com.nexus.core.storage.preferences.SharedPreferencesDeviceIdentityStore
import com.nexus.core.storage.secure.InMemoryTokenStore
import com.nexus.game.wuwa.InMemoryWuwaAccountStore
import com.nexus.game.wuwa.InMemoryWuwaSnapshotStore
import com.nexus.game.wuwa.WuwaRepositoryImpl

object AppGraph {
    private val accountStore = InMemoryWuwaAccountStore()
    private val snapshotStore = InMemoryWuwaSnapshotStore()
    private val tokenStore = InMemoryTokenStore()
    private lateinit var headerProvider: KuroHeaderProvider

    fun initialize(application: Application) {
        if (::headerProvider.isInitialized) return
        val deviceIdentityStore = SharedPreferencesDeviceIdentityStore(application)
        val ipAddressProvider = IpAddressProvider()
        headerProvider = KuroHeaderProvider(deviceIdentityStore, ipAddressProvider)
    }

    val repository: WuwaRepositoryImpl by lazy {
        check(::headerProvider.isInitialized) {
            "AppGraph must be initialized from NexusApplication before repository access."
        }
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
