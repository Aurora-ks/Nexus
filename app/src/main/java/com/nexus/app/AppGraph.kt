package com.nexus.app

import android.app.Application
import androidx.room.Room
import com.nexus.core.network.IpAddressProvider
import com.nexus.core.network.KuroHeaderProvider
import com.nexus.core.network.NetworkModule
import com.nexus.core.storage.db.NexusDatabase
import com.nexus.core.storage.preferences.SharedPreferencesDeviceIdentityStore
import com.nexus.core.storage.secure.EncryptedTokenStore
import com.nexus.core.storage.secure.TokenStore
import com.nexus.game.wuwa.InMemoryWuwaSnapshotStore
import com.nexus.game.wuwa.RoomWuwaAccountStore
import com.nexus.game.wuwa.WuwaRepositoryImpl
import com.nexus.game.wuwa.WuwaAccountStore

object AppGraph {
    private const val DATABASE_NAME = "nexus.db"

    private val snapshotStore = InMemoryWuwaSnapshotStore()
    private lateinit var headerProvider: KuroHeaderProvider
    private lateinit var tokenStore: TokenStore
    private lateinit var database: NexusDatabase
    private lateinit var accountStore: WuwaAccountStore

    fun initialize(application: Application) {
        if (::headerProvider.isInitialized) return
        val deviceIdentityStore = SharedPreferencesDeviceIdentityStore(application)
        val ipAddressProvider = IpAddressProvider()
        headerProvider = KuroHeaderProvider(application, deviceIdentityStore, ipAddressProvider)
        tokenStore = EncryptedTokenStore(application)
        database = Room.databaseBuilder(
            application,
            NexusDatabase::class.java,
            DATABASE_NAME,
        ).addMigrations(NexusDatabase.Migration1To2).build()
        accountStore = RoomWuwaAccountStore(database.accountDao())
    }

    val repository: WuwaRepositoryImpl by lazy {
        check(::headerProvider.isInitialized) {
            "AppGraph must be initialized from NexusApplication before repository access."
        }
        WuwaRepositoryImpl(
            roleApi = NetworkModule.wuwaRoleApi,
            widgetApi = NetworkModule.wuwaWidgetApi,
            akiBoxApi = NetworkModule.wuwaAkiBoxApi,
            headerProvider = headerProvider,
            accountStore = accountStore,
            snapshotStore = snapshotStore,
            tokenStore = tokenStore,
        )
    }
}
