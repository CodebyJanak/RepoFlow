package com.repoflow.core.data.repository

import com.repoflow.core.domain.model.ActivityItem
import com.repoflow.core.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor() : ActivityRepository {

    private val _activities = MutableStateFlow<List<ActivityItem>>(emptyList())

    override fun getActivities(): Flow<List<ActivityItem>> {
        return _activities.asStateFlow()
    }

    override suspend fun addActivity(activity: ActivityItem) {
        _activities.value = listOf(activity) + _activities.value
    }

    override suspend fun clearActivities() {
        _activities.value = emptyList()
    }

    override suspend fun getUnsyncedActivities(): List<ActivityItem> {
        return _activities.value.filter { !it.isSynced }
    }

    override suspend fun markAsSynced(activityId: String) {
        _activities.value = _activities.value.map {
            if (it.id == activityId) it.copy(isSynced = true) else it
        }
    }
}
