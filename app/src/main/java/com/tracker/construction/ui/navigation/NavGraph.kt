package com.tracker.construction.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tracker.construction.ConstructionApp
import com.tracker.construction.data.ProjectFloors
import com.tracker.construction.data.Repository
import com.tracker.construction.ui.components.AppDrawer
import com.tracker.construction.ui.components.ProjectWithFloors
import com.tracker.construction.ui.screens.home.HomeScreen
import com.tracker.construction.ui.screens.home.HomeViewModel
import com.tracker.construction.ui.screens.project.ProjectDetailScreen
import com.tracker.construction.ui.screens.project.ProjectDetailViewModel
import com.tracker.construction.ui.screens.search.SearchScreen
import com.tracker.construction.ui.screens.settings.SettingsScreen
import com.tracker.construction.ui.screens.settings.SettingsViewModel
import com.tracker.construction.ui.screens.unitdetail.UnitDetailScreen
import com.tracker.construction.ui.screens.unitdetail.UnitDetailViewModel
import com.tracker.construction.ui.screens.unitlist.UnitListScreen
import com.tracker.construction.ui.screens.unitlist.UnitListViewModel
import com.tracker.construction.util.ViewModelFactory
import kotlinx.coroutines.launch

private object ProjectDetail {
    const val ROUTE = "project/{projectId}"
    fun route(id: Long) = "project/$id"
}

@Composable
fun AppNavGraph(repository: Repository, app: ConstructionApp) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val projectsWithFloors by repository.observeProjectsWithFloors().collectAsState(initial = emptyList())
    val drawerEntries = projectsWithFloors.map { ProjectWithFloors(it.project, it.floors) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                projectsWithFloors = drawerEntries,
                onHomeClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Home.route) { launchSingleTop = true }
                },
                onFloorClick = { floor ->
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.UnitList.createRoute(floor.id))
                },
                onSearchClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Search.route)
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                val vm: HomeViewModel = viewModel(factory = ViewModelFactory { HomeViewModel(repository) })
                HomeScreen(
                    viewModel = vm,
                    repository = repository,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onOpenProject = { id -> navController.navigate(ProjectDetail.route(id)) }
                )
            }

            composable(
                ProjectDetail.ROUTE,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val vm: ProjectDetailViewModel = viewModel(
                    key = "project_$projectId",
                    factory = ViewModelFactory { ProjectDetailViewModel(repository, projectId) }
                )
                ProjectDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onOpenFloor = { floorId -> navController.navigate(Screen.UnitList.createRoute(floorId)) }
                )
            }

            composable(
                Screen.UnitList.route,
                arguments = listOf(navArgument("floorId") { type = NavType.LongType })
            ) { backStackEntry ->
                val floorId = backStackEntry.arguments?.getLong("floorId") ?: 0L
                val vm: UnitListViewModel = viewModel(
                    key = "floor_$floorId",
                    factory = ViewModelFactory { UnitListViewModel(repository, floorId) }
                )
                UnitListScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onOpenUnit = { unitId -> navController.navigate(Screen.UnitDetail.createRoute(unitId)) }
                )
            }

            composable(
                Screen.UnitDetail.route,
                arguments = listOf(navArgument("unitId") { type = NavType.LongType })
            ) { backStackEntry ->
                val unitId = backStackEntry.arguments?.getLong("unitId") ?: 0L
                val vm: UnitDetailViewModel = viewModel(
                    key = "unit_$unitId",
                    factory = ViewModelFactory { UnitDetailViewModel(repository, unitId) }
                )
                UnitDetailScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onOpenUnit = { unitId ->
                        navController.navigate(Screen.UnitDetail.createRoute(unitId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel(factory = ViewModelFactory { SettingsViewModel(app.settingsStore) })
                SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }
        }
    }
}
