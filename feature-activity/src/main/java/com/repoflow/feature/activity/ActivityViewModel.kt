package com.repoflow.feature.activity

import androidx.lifecycle.viewModelScope
import com.repoflow.core.domain.model.ActivityItem
import com.repoflow.core.domain.model.ActivityType
import com.repoflow.core.domain.repository.ActivityRepository
import com.repoflow.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ActivityFilter(val label: String) {
    ALL("All"),
    PUSH("Push"),
    COMMIT("Commit"),
    PULL("Pull"),
    FETCH("Fetch"),
    SYNC("Sync")
}

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : BaseViewModel() {

    private val _selectedFilter = MutableStateFlow(ActivityFilter.ALL)
    val selectedFilter: StateFlow<ActivityFilter> = _selectedFilter.asStateFlow()

    private val activities: StateFlow<List<ActivityItem>> = activityRepository.getActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredActivities: StateFlow<List<ActivityItem>> = combine(
        activities, selectedFilter
    ) { allActivities, filter ->
        when (filter) {
            ActivityFilter.ALL -> allActivities
            ActivityFilter.PUSH -> allActivities.filter { it.type == ActivityType.PUSH }
            ActivityFilter.COMMIT -> allActivities.filter { it.type == ActivityType.COMMIT }
            ActivityFilter.PULL -> allActivities.filter { it.type == ActivityType.PULL }
            ActivityFilter.FETCH -> allActivities.filter { it.type == ActivityType.FETCH }
            ActivityFilter.SYNC -> allActivities.filter {
                it.type in setOf(ActivityType.PUSH, ActivityType.PULL, ActivityType.FETCH, ActivityType.CLONE)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isEmpty: StateFlow<Boolean> = combine(
        filteredActivities, selectedFilter
    ) { list, _ -> list.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setFilter(filter: ActivityFilter) {
        _selectedFilter.value = filter
    }

    fun clearActivities() {
        viewModelScope.launch {
            activityRepository.clearActivities()
        }
    }
}
