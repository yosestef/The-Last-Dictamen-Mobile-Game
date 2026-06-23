package com.android.mobile.games.app.navigation

sealed class AppRoute(val route: String) {

    data object MainMenu : AppRoute("main_menu")

    data object CatchGameMenu : AppRoute("catch_game_menu")

    data object CatchGame : AppRoute("catch_game")

    data object CodeMergeGame : AppRoute("code_merge_game")

    data object FruitNinjaMenu : AppRoute("fruit_ninja_menu")

    data object FruitNinjaGame : AppRoute("fruit_ninja_game/{difficulty}/{username}") {
        fun createRoute(difficulty: String, username: String): String {
            return "fruit_ninja_game/$difficulty/$username"
        }
    }


    data object LaRazaRunMenu : AppRoute("la_raza_run_menu")
    data object LaRazaRunGame : AppRoute("la_raza_run_game")
}

