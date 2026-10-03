package com.aradium.aradium.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Library : Screen("library", "Library", Icons.Filled.LibraryMusic)
    object Upload : Screen("upload", "Upload", Icons.Filled.UploadFile)
    object Search : Screen("search", "Search", Icons.Filled.Search)
    object General : Screen("general", "General", Icons.Filled.Settings)

    companion object {
        val bottomBarItems = listOf(Home, Library, Upload, Search, General)
    }
}