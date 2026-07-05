package com.tracker.construction.util

import androidx.compose.ui.graphics.Color
import com.tracker.construction.data.entities.UnitRecord

/**
 * Determines a unit's status color following the supervisor's field convention:
 * Nothing -> Red, Tile -> Orange, Tile+Grout -> Yellow, Tile+Grout+LVP -> Blue,
 * + Silicone -> Purple, + QC -> Green.
 */
fun unitStatusColor(unit: UnitRecord): Color = when {
    unit.qcDone -> StatusColors.Green
    unit.siliconeDone -> StatusColors.Purple
    unit.tileDone && unit.groutDone && unit.lvpDone -> StatusColors.Blue
    unit.tileDone && unit.groutDone -> StatusColors.Yellow
    unit.tileDone -> StatusColors.Orange
    else -> StatusColors.Red
}

fun unitStatusLabel(unit: UnitRecord): String = when {
    unit.qcDone -> "QC Complete"
    unit.siliconeDone -> "Silicone Done"
    unit.tileDone && unit.groutDone && unit.lvpDone -> "Tile + Grout + LVP"
    unit.tileDone && unit.groutDone -> "Tile + Grout"
    unit.tileDone -> "Tile Only"
    else -> "Not Started"
}

object StatusColors {
    val Red = Color(0xFFE53935)
    val Orange = Color(0xFFFB8C00)
    val Yellow = Color(0xFFF9A825)
    val Blue = Color(0xFF1E88E5)
    val Purple = Color(0xFF8E24AA)
    val Green = Color(0xFF2E7D32)
}
