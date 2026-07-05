package com.tracker.construction.ui.screens.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.construction.data.Repository
import com.tracker.construction.data.entities.Floor
import com.tracker.construction.data.entities.Project
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FloorSummary(
    val floor: Floor,
    val totalUnits: Int,
    val completedUnits: Int
) {
    val percentComplete: Int
        get() = if (totalUnits == 0) 0 else (completedUnits * 100) / totalUnits
}

class ProjectDetailViewModel(
    private val repository: Repository,
    private val projectId: Long
) : ViewModel() {

    val project: StateFlow<Project?> = repository.observeProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val floorSummaries: StateFlow<List<FloorSummary>> = repository.observeFloors(projectId)
        .flatMapLatest { floors ->
            if (floors.isEmpty()) {
                flowOf(emptyList())
            } else {
                val perFloor = floors.map { floor ->
                    repository.observeUnits(floor.id).combine(flowOf(floor)) { units, f ->
                        FloorSummary(f, units.size, units.count { it.qcDone })
                    }
                }
                combine(perFloor) { it.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalUnits: StateFlow<Int> = repository.observeUnitsForProject(projectId)
        .combine(floorSummaries) { units, _ -> units.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedUnits: StateFlow<Int> = repository.observeUnitsForProject(projectId)
        .combine(floorSummaries) { units, _ -> units.count { it.qcDone } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun createFloor(name: String, start: Int, end: Int) {
        viewModelScope.launch {
            repository.createFloorWithUnits(projectId, name, start, end)
        }
    }

    fun deleteFloor(floor: Floor) {
        viewModelScope.launch { repository.deleteFloor(floor) }
    }
}
