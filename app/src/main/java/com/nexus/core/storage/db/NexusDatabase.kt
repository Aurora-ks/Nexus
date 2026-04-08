package com.nexus.core.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nexus.core.storage.db.dao.AccountDao
import com.nexus.core.storage.db.dao.CheckInRecordDao
import com.nexus.core.storage.db.dao.DailySnapshotDao

@Database(
    entities = [
        AccountEntity::class,
        DailySnapshotEntity::class,
        CheckInRecordEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun dailySnapshotDao(): DailySnapshotDao
    abstract fun checkInRecordDao(): CheckInRecordDao
}
