package com.repoflow.core.domain.repository

import com.repoflow.core.domain.model.ActivityItem
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getActivities(): Flow<List<ActivityItem>>
    suspend fun addActivity(activity: ActivityItem)
    suspend fun clearActivities()
    suspend fun getUnsyncedActivities(): List<ActivityItem>
    suspend fun markAsSynced(activityId: String)
}
