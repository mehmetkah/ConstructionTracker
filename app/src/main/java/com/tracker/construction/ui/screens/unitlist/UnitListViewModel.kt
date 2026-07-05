package com.tracker.construction.ui.screens.unitlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.construction.data.Repository
import com.tracker.construction.data.UnitStage
import com.tracker.construction.data.entities.Floor
import com.tracker.construction.data.entities.UnitRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UnitFilter {
    ALL, PENDING_TILE, PENDING_GROUT, PENDING_LVP, PENDING_SILICONE, PENDING_QC, COMPLETED
}

class UnitListViewModel(
    private val repository: Repository,
    private val floorId: Long
) : ViewModel() {

    private val _filter = MutableStateFlow(UnitFilter.ALL)
    val filter: StateFlow<UnitFilter> = _filter

    val floor: StateFlow<Floor?> = repository.observeFloor(floorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allUnits: StateFlow<List<UnitRecord>> = repository.observeUnits(floorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredUnits: StateFlow<List<UnitRecord>> = combine(allUnits, _filter) { units, filter ->
        when (filter) {
            UnitFilter.ALL -> units
            UnitFilter.PENDING_TILE -> units.filter { !it.tileDone }
            UnitFilter.PENDING_GROUT -> units.filter { !it.groutDone }
            UnitFilter.PENDING_LVP -> units.filter { !it.lvpDone }
            UnitFilter.PENDING_SILICONE -> units.filter { !it.siliconeDone }
            UnitFilter.PENDING_QC -> units.filter { !it.qcDone }
            UnitFilter.COMPLETED -> units.filter { it.qcDone }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedCount: StateFlow<Int> = allUnits
        .combine(_filter) { units, _ -> units.count { it.qcDone } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCount: StateFlow<Int> = allUnits
        .combine(_filter) { units, _ -> units.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setFilter(f: UnitFilter) {
        _filter.value = f
    }

    fun toggleStage(unit: UnitRecord, stage: UnitStage) {
        viewModelScope.launch { repository.toggleStage(unit, stage) }
    }

    fun addUnit() {
        viewModelScope.launch {
            val f = floor.value ?: return@launch
            val next = (allUnits.value.mapNotNull { it.unitNumber.toIntOrNull() }.maxOrNull() ?: f.endUnit) + 1
            repository.addUnitToFloor(f, next.toString())
        }
    }

    fun removeLastUnit() {
        viewModelScope.launch { repository.removeLastUnit(floorId) }
    }

    fun duplicateUnit(unit: UnitRecord) {
        viewModelScope.launch { repository.duplicateUnit(unit) }
    }

    fun deleteUnit(unit: UnitRecord) {
        viewModelScope.launch { repository.deleteUnit(unit) }
    }

    fun renameUnit(unit: UnitRecord, newNumber: String) {
        if (newNumber.isBlank()) return
        viewModelScope.launch { repository.renameUnit(unit, newNumber.trim()) }
    }
}
