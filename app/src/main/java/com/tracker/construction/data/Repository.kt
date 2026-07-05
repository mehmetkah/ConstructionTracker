package com.tracker.construction.data

import com.tracker.construction.data.entities.Floor
import com.tracker.construction.data.entities.Project
import com.tracker.construction.data.entities.UnitRecord
import com.tracker.construction.data.entities.UnitNote
import com.tracker.construction.data.entities.UnitPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

data class ProjectFloors(val project: Project, val floors: List<Floor>)

class Repository(private val db: AppDatabase) {

    // ---------- Projects ----------
    fun observeProjects(): Flow<List<Project>> = db.projectDao().observeAllByName()

    /** Live feed of every project paired with its floors — used to drive the navigation drawer. */
    fun observeProjectsWithFloors(): Flow<List<ProjectFloors>> = observeProjects().flatMapLatest { projects ->
        if (projects.isEmpty()) {
            flowOf(emptyList())
        } else {
            val perProject = projects.map { project ->
                observeFloors(project.id).combine(flowOf(project)) { floors, p -> ProjectFloors(p, floors) }
            }
            combine(perProject) { it.toList() }
        }
    }

    suspend fun getProject(id: Long): Project? = db.projectDao().getById(id)

    fun observeProject(id: Long): Flow<Project?> = db.projectDao().observeById(id)

    suspend fun createProject(name: String): Long = db.projectDao().insert(Project(name = name))

    suspend fun deleteProject(project: Project) = db.projectDao().delete(project)

    suspend fun renameProject(project: Project, newName: String) =
        db.projectDao().update(project.copy(name = newName))

    // ---------- Floors ----------
    fun observeFloors(projectId: Long): Flow<List<Floor>> = db.floorDao().observeByProject(projectId)

    suspend fun getFloorsOnce(projectId: Long): List<Floor> = db.floorDao().getByProjectOnce(projectId)

    suspend fun getFloor(id: Long): Floor? = db.floorDao().getById(id)

    fun observeFloor(id: Long): Flow<Floor?> = db.floorDao().observeById(id)

    suspend fun deleteFloor(floor: Floor) = db.floorDao().delete(floor)

    /**
     * Creates a floor and automatically generates every unit between startUnit and endUnit
     * (inclusive), matching the number of digits typed by the supervisor (e.g. 401 -> 426).
     */
    suspend fun createFloorWithUnits(projectId: Long, name: String, startUnit: Int, endUnit: Int): Long {
        val floor = Floor(projectId = projectId, name = name, startUnit = startUnit, endUnit = endUnit)
        val floorId = db.floorDao().insert(floor)

        val range = if (startUnit <= endUnit) startUnit..endUnit else startUnit downTo endUnit
        val units = range.mapIndexed { index, number ->
            UnitRecord(
                floorId = floorId,
                projectId = projectId,
                unitNumber = number.toString(),
                sortOrder = index.toLong()
            )
        }
        db.unitDao().insertAll(units)
        return floorId
    }

    // ---------- Units ----------
    fun observeUnits(floorId: Long): Flow<List<UnitRecord>> = db.unitDao().observeByFloor(floorId)

    fun observeUnitsForProject(projectId: Long): Flow<List<UnitRecord>> = db.unitDao().observeByProject(projectId)

    suspend fun getUnitsForProjectOnce(projectId: Long): List<UnitRecord> = db.unitDao().getByProjectOnce(projectId)

    suspend fun getUnitsForFloorOnce(floorId: Long): List<UnitRecord> = db.unitDao().getByFloorOnce(floorId)

    suspend fun getUnit(id: Long): UnitRecord? = db.unitDao().getById(id)

    fun observeUnit(id: Long): Flow<UnitRecord?> = db.unitDao().observeById(id)

    suspend fun searchUnits(query: String): List<UnitRecord> = db.unitDao().searchByNumber(query)

    suspend fun updateUnit(unit: UnitRecord) = db.unitDao().update(unit)

    suspend fun deleteUnit(unit: UnitRecord) = db.unitDao().delete(unit)

    suspend fun addUnitToFloor(floor: Floor, unitNumber: String) {
        val maxOrder = db.unitDao().maxSortOrderForFloor(floor.id) ?: -1L
        db.unitDao().insert(
            UnitRecord(
                floorId = floor.id,
                projectId = floor.projectId,
                unitNumber = unitNumber,
                sortOrder = maxOrder + 1
            )
        )
    }

    suspend fun removeLastUnit(floorId: Long) {
        val units = db.unitDao().getByFloorOnce(floorId)
        units.maxByOrNull { it.sortOrder }?.let { db.unitDao().delete(it) }
    }

    suspend fun duplicateUnit(unit: UnitRecord) {
        val maxOrder = db.unitDao().maxSortOrderForFloor(unit.floorId) ?: 0L
        db.unitDao().insert(
            unit.copy(
                id = 0,
                unitNumber = unit.unitNumber + "-copy",
                sortOrder = maxOrder + 1,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun renameUnit(unit: UnitRecord, newNumber: String) =
        db.unitDao().update(unit.copy(unitNumber = newNumber))

    /** Toggles a single checkbox stage and stamps/erases its completion time; saves instantly. */
    suspend fun toggleStage(unit: UnitRecord, stage: UnitStage) {
        val now = System.currentTimeMillis()
        val updated = when (stage) {
            UnitStage.TILE -> unit.copy(tileDone = !unit.tileDone, tileAt = if (!unit.tileDone) now else null)
            UnitStage.GROUT -> unit.copy(groutDone = !unit.groutDone, groutAt = if (!unit.groutDone) now else null)
            UnitStage.LVP -> unit.copy(lvpDone = !unit.lvpDone, lvpAt = if (!unit.lvpDone) now else null)
            UnitStage.SILICONE -> unit.copy(siliconeDone = !unit.siliconeDone, siliconeAt = if (!unit.siliconeDone) now else null)
            UnitStage.QC -> unit.copy(qcDone = !unit.qcDone, qcAt = if (!unit.qcDone) now else null)
        }
        db.unitDao().update(updated)
    }

    // ---------- Notes ----------
    fun observeNotes(unitId: Long): Flow<List<UnitNote>> = db.noteDao().observeForUnit(unitId)

    suspend fun addNote(unitId: Long, text: String) = db.noteDao().insert(UnitNote(unitId = unitId, text = text))

    suspend fun deleteNote(note: UnitNote) = db.noteDao().delete(note)

    // ---------- Photos ----------
    fun observePhotos(unitId: Long): Flow<List<UnitPhoto>> = db.photoDao().observeForUnit(unitId)

    suspend fun addPhoto(unitId: Long, path: String) = db.photoDao().insert(UnitPhoto(unitId = unitId, filePath = path))

    suspend fun deletePhoto(photo: UnitPhoto) = db.photoDao().delete(photo)
}

enum class UnitStage { TILE, GROUT, LVP, SILICONE, QC }
