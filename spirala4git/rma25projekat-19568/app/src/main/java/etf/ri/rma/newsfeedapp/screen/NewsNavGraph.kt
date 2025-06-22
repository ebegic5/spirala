package etf.ri.rma.newsfeedapp.screen

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import etf.ri.rma.newsfeedapp.data.FilterState

@Composable
fun NewsNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "/home") {
        composable("/home") {
            NewsFeedScreen(navController)
        }
        composable("/filters") {
            FilterScreen(navController)
        }
        composable(
            "/details/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            NewsDetailsScreen(navController, newsId = id)
        }
    }
}
