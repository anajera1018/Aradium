package com.aradium.aradium

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home) // Style: tab name and icon (same for lines below)
    object Library : Screen("library", "Library", Icons.Filled.LibraryMusic)
    object Upload : Screen("upload", "Upload", Icons.Filled.UploadFile)
    object Search : Screen("search", "Search", Icons.Filled.Search)
    object General : Screen("general", "General", Icons.Filled.Settings)
}

val bottomItems = listOf(Screen.Home, Screen.Library, Screen.Upload, Screen.Search, Screen.General)

@Composable
fun MainScreen(viewModel: MusicViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val songs by viewModel.songs.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val currentSong = songs?.find { it.id == playerState.currentSongId }

    Scaffold(
        bottomBar = {
            Column {
                if (currentSong != null && currentRoute != "player") {
                    MiniPlayer(currentSong, playerState, viewModel) {
                        navController.navigate("player")
                    }
                }
                NavigationBar { // Style: bottom navigation bar (colors from Theme.kt)
                    bottomItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.label) }, // Style: bottom bar icon (set icons in Screen above)
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding) // Layout: space around element
        ) {
            composable(Screen.Home.route) { HomeScreen(viewModel, navController) }
            composable(Screen.Library.route) { LibraryScreen(viewModel, navController) }
            composable(Screen.Upload.route) { UploadScreen(viewModel, navController) }
            composable(Screen.Search.route) { SearchScreen(viewModel, navController) }
            composable(Screen.General.route) { GeneralScreen(viewModel) }
            composable("player") { PlayerScreen(viewModel, navController) }
            composable(
                "edit/{songId}",
                arguments = listOf(navArgument("songId") { type = NavType.LongType })
            ) { entry ->
                val songId = entry.arguments?.getLong("songId") ?: 0L
                EditSongScreen(viewModel, navController, songId)
            }
            composable(
                "playlist/{playlistId}",
                arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
            ) { entry ->
                val playlistId = entry.arguments?.getLong("playlistId") ?: 0L
                PlaylistScreen(viewModel, navController, playlistId)
            }
        }
    }
}
