package com.repoflow.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.repoflow.core.data.local.dao.RepositoryDao
import com.repoflow.core.data.local.entity.RepositoryEntity

@Database(
    entities = [RepositoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun repositoryDao(): RepositoryDao
}
