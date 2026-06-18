package com.repoflow.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.repoflow.core.data.local.entity.RepositoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao {

    @Query("SELECT * FROM repositories ORDER BY lastSyncedAt DESC")
    fun getAllRepositories(): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM repositories WHERE id = :id")
    suspend fun getRepositoryById(id: Long): RepositoryEntity?

    @Query("SELECT * FROM repositories WHERE localPath IS NOT NULL")
    fun getClonedRepositories(): Flow<List<RepositoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repositories: List<RepositoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repository: RepositoryEntity)

    @Delete
    suspend fun delete(repository: RepositoryEntity)

    @Query("DELETE FROM repositories")
    suspend fun deleteAll()
}
