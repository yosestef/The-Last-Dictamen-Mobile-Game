package com.android.mobile.games.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.mobile.games.app.games.catchgame.model.CatchGameDifficulty
import com.android.mobile.games.app.games.catchgame.ui.CatchGameMenuScreen
import com.android.mobile.games.app.games.catchgame.ui.CatchGameScreen
import com.android.mobile.games.app.games.catchgame.data.ICatchGameService
import com.android.mobile.games.app.games.catchgame.data.RetrofitCatchGameService
import com.android.mobile.games.app.games.codemerge.data.MockCodeMergeGameService
import com.android.mobile.games.app.games.codemerge.engine.CodeMergeViewModel
import com.android.mobile.games.app.games.codemerge.ui.CodeMergeScreen
import com.android.mobile.games.app.games.fruitninja.model.FruitNinjaDifficulty
import com.android.mobile.games.app.games.fruitninja.ui.FruitNinjaMenuScreen
import com.android.mobile.games.app.games.fruitninja.ui.FruitNinjaScreen
import com.android.mobile.games.app.games.razarun.ui.RazaMenuScreen
import com.android.mobile.games.app.games.razarun.ui.RazaScreen
import com.android.mobile.games.app.ui.screens.MainMenuScreen

private const val DIFFICULTY_ARGUMENT = "difficulty"
private const val USERNAME_ARGUMENT = "username"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var catchGameDifficulty by remember {
        mutableStateOf(CatchGameDifficulty.EASY)
    }
    var catchGameUsername by remember {
        mutableStateOf("")
    }

    // Initialize Services (Single instances for the app)
    val codeMergeService = remember { MockCodeMergeGameService() }
    val catchGameService: ICatchGameService = remember { RetrofitCatchGameService() }

    NavHost(
        navController = navController,
        startDestination = AppRoute.MainMenu.route
    ) {
        composable(AppRoute.MainMenu.route) {
            MainMenuScreen(
                onCodeSlasherClick = {
                    navController.navigate(AppRoute.FruitNinjaMenu.route)
                },
                onLaRazaRunClick = {
                    navController.navigate(AppRoute.LaRazaRunMenu.route)
                },
                onCatchGameClick = {
                    navController.navigate(AppRoute.CatchGameMenu.route)
                },
                onCodeMergeClick = {
                    navController.navigate(AppRoute.CodeMergeGame.route)
                }
            )
        }

        // Bloque del menú de La Raza Run reparado
        composable(AppRoute.LaRazaRunMenu.route) {
            RazaMenuScreen(
                onStartGameClick = {
                    navController.navigate(AppRoute.LaRazaRunGame.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.CatchGameMenu.route) {
            CatchGameMenuScreen(
                selectedDifficulty = catchGameDifficulty,
                onDifficultySelected = { difficulty ->
                    catchGameDifficulty = difficulty
                },
                onStartGameClick = { username ->
                    catchGameUsername = username
                    navController.navigate(AppRoute.CatchGame.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                rankingLoader = { difficulty, limit ->
                    catchGameService.getRankings(difficulty = difficulty, limit = limit)
                }
            )
        }

        // Bloque del juego La Raza Run reparado
        composable(AppRoute.LaRazaRunGame.route) {
            RazaScreen(
                onBackToMenuClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.CatchGame.route) {
            CatchGameScreen(
                difficulty = catchGameDifficulty,
                username = catchGameUsername,
                gameService = catchGameService,
                onBackToMenuClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.CodeMergeGame.route) {
            val viewModel: CodeMergeViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return CodeMergeViewModel(codeMergeService) as T
                    }
                }
            )
            val state by viewModel.state.collectAsState()
            
            CodeMergeScreen(
                state = state,
                onIntent = { viewModel.handleIntent(it) },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoute.FruitNinjaMenu.route) {
            FruitNinjaMenuScreen(
                onStartGameClick = { difficulty, username ->
                    navController.navigate(
                        AppRoute.FruitNinjaGame.createRoute(
                            difficulty = difficulty.name,
                            username = username
                        )
                    )
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppRoute.FruitNinjaGame.route,
            arguments = listOf(
                navArgument(DIFFICULTY_ARGUMENT) {
                    type = NavType.StringType
                },
                navArgument(USERNAME_ARGUMENT) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val difficultyName = backStackEntry.arguments
                ?.getString(DIFFICULTY_ARGUMENT)
                ?: FruitNinjaDifficulty.CLASSIC.name

            val username = backStackEntry.arguments
                ?.getString(USERNAME_ARGUMENT)
                ?: "Anonymous"

            val difficulty = runCatching {
                FruitNinjaDifficulty.valueOf(difficultyName)
            }.getOrDefault(FruitNinjaDifficulty.CLASSIC)

            FruitNinjaScreen(
                difficulty = difficulty,
                username = username,
                onBackToMenuClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}