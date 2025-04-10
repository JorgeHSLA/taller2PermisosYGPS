package com.example.taller2permisosygps.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String){
    object ParteCam: Screen("ParteCamera")
    object ParteMap: Screen("ParteMapa")
    object ScreenSelector: Screen("ScreenPrincipal")

}

@Composable
fun NavegationStack(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.ScreenSelector.route){
        composable(route = Screen.ScreenSelector.route){ SelectorScreen(navController = navController)}
        composable(route = Screen.ParteCam.route){ CamaraScreen(navController = navController)}
        composable(route = Screen.ParteMap.route){ MapaScreen(navController = navController)}


    }
}