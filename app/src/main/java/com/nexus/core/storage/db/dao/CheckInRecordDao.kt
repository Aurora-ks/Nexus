package com.nexus.core.storage.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.core.storage.db.CheckInRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CheckInRecordEntity): Long

    @Query("SELECT * FROM checkin_records ORDER BY executed_at DESC")
    fun observeRecent(): Flow<List<CheckInRecordEntity>>
}
