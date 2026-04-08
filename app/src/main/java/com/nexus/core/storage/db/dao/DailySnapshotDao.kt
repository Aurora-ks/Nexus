package com.nexus.core.storage.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.core.storage.db.DailySnapshotEntity

@Dao
interface DailySnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: DailySnapshotEntity): Long

    @Query("SELECT * FROM daily_snapshots WHERE account_id = :accountId ORDER BY fetched_at DESC LIMIT 1")
    suspend fun findLatest(accountId: Long): DailySnapshotEntity?
}
