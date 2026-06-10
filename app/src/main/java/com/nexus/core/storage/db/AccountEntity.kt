package com.nexus.core.storage.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "game_id") val gameId: Int,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "role_id") val roleId: String,
    @ColumnInfo(name = "server_id") val serverId: String,
    @ColumnInfo(name = "role_name") val roleName: String,
    @ColumnInfo(name = "server_name") val serverName: String,
    @ColumnInfo(name = "local_nickname") val localNickname: String?,
    @ColumnInfo(name = "head_photo_url") val headPhotoUrl: String?,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "last_sync_at") val lastSyncAt: Long?,
    @ColumnInfo(name = "last_check_in_at") val lastCheckInAt: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
