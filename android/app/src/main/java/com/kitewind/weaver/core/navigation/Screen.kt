package com.kitewind.weaver.core.navigation

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val hillId: Int, val runId: Long) : Screen
    data class GameOver(val resultId: Long) : Screen
    data class MeadowView(val hillId: Int) : Screen
    data object KiteBox : Screen
    data class CalmMode(val runId: Long) : Screen
    data object Settings : Screen
}
