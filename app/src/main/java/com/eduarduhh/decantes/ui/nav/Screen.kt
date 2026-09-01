package com.eduarduhh.decantes.ui.nav

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Groups : Screen("groups")
    data object Backup : Screen("backup")
    data object GroupDetail : Screen("group_detail/{grupoId}") {
        fun rota(grupoId: Long) = "group_detail/$grupoId"
    }
    data object PerfumeDetail : Screen("perfume_detail/{perfumeId}") {
        fun rota(perfumeId: Long) = "perfume_detail/$perfumeId"
    }
}
