package com.ads.assetsmanager.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ads.assetsmanager.viewmodel.GameViewModel

@Composable
fun GameNavHost(viewModel: GameViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "categories") {

        // Tela 1: Lista de Categorias
        composable("categories") {
            CategoryScreen(
                viewModel = viewModel,
                onCategoryClick = { catId ->
                    navController.navigate("entities/$catId")
                }
            )
        }

        // Tela 2: Lista de Entidades (com argumento)
        composable(
            route = "entities/{catId}",
            arguments = listOf(navArgument("catId") { type = NavType.IntType })
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getInt("catId") ?: 0
            // Chama a função para filtrar no ViewModel
            LaunchedEffect(catId) { viewModel.selectCategory(catId) }

            EntityScreen(
                viewModel, onBack = { navController.popBackStack() },
                onEntityClick = { }
            )
        }
    }
}