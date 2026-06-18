package com.repoflow.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM repositories WHERE isFavorite = 1")
    fun getFavoriteRepositories(): Flow<List<RepositoryEntity>>

    @Query(
        "SELECT * FROM repositories WHERE " +
        "name LIKE '%' || :query || '%' OR " +
        "fullName LIKE '%' || :query || '%' OR " +
        "description LIKE '%' || :query || '%' " +
        "ORDER BY lastSyncedAt DESC"
    )
    fun searchRepositories(query: String): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM repositories ORDER BY stars DESC")
    fun getAllRepositoriesSortedByStars(): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM repositories ORDER BY name ASC")
    fun getAllRepositoriesSortedByName(): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM repositories ORDER BY forks DESC")
    fun getAllRepositoriesSortedByForks(): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM repositories WHERE isFavorite = 1 ORDER BY stars DESC")
    fun getFavoriteRepositoriesSortedByStars(): Flow<List<RepositoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repositories: List<RepositoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repository: RepositoryEntity)

    @Update
    suspend fun update(repository: RepositoryEntity)

    @Query("UPDATE repositories SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM repositories")
    suspend fun deleteAll()
}
