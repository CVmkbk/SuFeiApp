package dev.wceng.sufei.ui.navigation

import dev.wceng.sufei.ui.screens.explore.SquareType
import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
object Login

@Serializable
object Home

@Serializable
object Explore

@Serializable
object Chat

@Serializable
object Profile

@Serializable
object FavoriteList

@Serializable
data class Detail(val id: String)

@Serializable
data class Square(val type: SquareType)

@Serializable
data class PoetDetail(val id: String)
