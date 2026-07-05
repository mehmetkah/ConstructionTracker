package com.tracker.construction.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object UnitList : Screen("floor/{floorId}") {
        fun createRoute(floorId: Long) = "floor/$floorId"
    }
    object UnitDetail : Screen("unit/{unitId}") {
        fun createRoute(unitId: Long) = "unit/$unitId"
    }
    object Search : Screen("search")
    object Settings : Screen("settings")
    object ProjectExport : Screen("export/{projectId}") {
        fun createRoute(projectId: Long) = "export/$projectId"
    }
}
