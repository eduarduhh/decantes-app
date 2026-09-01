package com.eduarduhh.decantes.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eduarduhh.decantes.data.repository.DecantesRepository
import com.eduarduhh.decantes.ui.backup.BackupScreen
import com.eduarduhh.decantes.ui.groupdetail.GroupDetailScreen
import com.eduarduhh.decantes.ui.groups.GroupListScreen
import com.eduarduhh.decantes.ui.home.HomeScreen
import com.eduarduhh.decantes.ui.perfumedetail.PerfumeDetailScreen

@Composable
fun DecantesNavGraph(
    repository: DecantesRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                repository = repository,
                onAbrirGrupos = { navController.navigate(Screen.Groups.route) },
                onAbrirBackup = { navController.navigate(Screen.Backup.route) },
                onAbrirGrupo = { grupoId ->
                    navController.navigate(Screen.GroupDetail.rota(grupoId))
                }
            )
        }
        composable(Screen.Groups.route) {
            GroupListScreen(
                repository = repository,
                onVoltar = { navController.popBackStack() },
                onAbrirGrupo = { grupoId ->
                    navController.navigate(Screen.GroupDetail.rota(grupoId))
                }
            )
        }
        composable(Screen.Backup.route) {
            BackupScreen(
                repository = repository,
                onVoltar = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("grupoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getLong("grupoId") ?: 0L
            GroupDetailScreen(
                repository = repository,
                grupoId = grupoId,
                onVoltar = { navController.popBackStack() },
                onAbrirPerfume = { perfumeId ->
                    navController.navigate(Screen.PerfumeDetail.rota(perfumeId))
                }
            )
        }
        composable(
            route = Screen.PerfumeDetail.route,
            arguments = listOf(navArgument("perfumeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val perfumeId = backStackEntry.arguments?.getLong("perfumeId") ?: 0L
            PerfumeDetailScreen(
                repository = repository,
                perfumeId = perfumeId,
                onVoltar = { navController.popBackStack() }
            )
        }
    }
}
