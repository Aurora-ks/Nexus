package com.nexus.core.storage.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checkin_records",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("account_id")],
)
data class CheckInRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "executed_at") val executedAt: Long,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "today_reward_json") val todayRewardJson: String,
    @ColumnInfo(name = "tomorrow_reward_json") val tomorrowRewardJson: String,
    @ColumnInfo(name = "trace_id") val traceId: String?,
    @ColumnInfo(name = "raw_json") val rawJson: String,
)
