package com.tracker.construction.ui.screens.unitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.construction.data.Repository
import com.tracker.construction.data.UnitStage
import com.tracker.construction.data.entities.UnitRecord
import com.tracker.construction.data.entities.UnitNote
import com.tracker.construction.data.entities.UnitPhoto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnitDetailViewModel(
    private val repository: Repository,
    private val unitId: Long
) : ViewModel() {

    val unit: StateFlow<UnitRecord?> = repository.observeUnit(unitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notes: StateFlow<List<UnitNote>> = repository.observeNotes(unitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photos: StateFlow<List<UnitPhoto>> = repository.observePhotos(unitId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleStage(stage: UnitStage) {
        val current = unit.value ?: return
        viewModelScope.launch { repository.toggleStage(current, stage) }
    }

    fun addNote(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { repository.addNote(unitId, text.trim()) }
    }

    fun deleteNote(note: UnitNote) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun addPhoto(path: String) {
        viewModelScope.launch { repository.addPhoto(unitId, path) }
    }

    fun deletePhoto(photo: UnitPhoto) {
        viewModelScope.launch { repository.deletePhoto(photo) }
    }
}
