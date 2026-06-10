package com.nexus.core.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nexus.core.storage.db.dao.AccountDao
import com.nexus.core.storage.db.dao.CheckInRecordDao
import com.nexus.core.storage.db.dao.DailySnapshotDao

@Database(
    entities = [
        AccountEntity::class,
        DailySnapshotEntity::class,
        CheckInRecordEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun dailySnapshotDao(): DailySnapshotDao
    abstract fun checkInRecordDao(): CheckInRecordDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN head_photo_url TEXT")
            }
        }
    }
}
