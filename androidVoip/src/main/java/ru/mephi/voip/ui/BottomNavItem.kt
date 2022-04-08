package ru.mephi.voip.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(var title: String, var icon: ImageVector, var screen_route: String) {
    object Caller : BottomNavItem("Звонки", Icons.Default.Call, "caller")
    object Catalog : BottomNavItem("Каталог", Icons.Default.Home, "catalog")
    object Profile : BottomNavItem("Профиль", Icons.Default.AccountCircle, "profile")
}