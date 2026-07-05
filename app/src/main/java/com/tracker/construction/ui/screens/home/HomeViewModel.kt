package com.tracker.construction.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.construction.data.Repository
import com.tracker.construction.data.entities.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjectSummary(
    val project: Project,
    val floorCount: Int,
    val totalUnits: Int,
    val completedUnits: Int
) {
    val percentComplete: Int
        get() = if (totalUnits == 0) 0 else (completedUnits * 100) / totalUnits
}

class HomeViewModel(private val repository: Repository) : ViewModel() {

    val summaries: StateFlow<List<ProjectSummary>> = repository.observeProjects()
        .flatMapLatest { projects ->
            if (projects.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                val perProject = projects.map { project ->
                    combine(
                        repository.observeFloors(project.id),
                        repository.observeUnitsForProject(project.id)
                    ) { floors, units ->
                        ProjectSummary(
                            project = project,
                            floorCount = floors.size,
                            totalUnits = units.size,
                            completedUnits = units.count { it.qcDone }
                        )
                    }
                }
                combine(perProject) { it.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createProject(name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createProject(name.trim())
            onCreated(id)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch { repository.deleteProject(project) }
    }

    fun renameProject(project: Project, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { repository.renameProject(project, newName.trim()) }
    }
}
