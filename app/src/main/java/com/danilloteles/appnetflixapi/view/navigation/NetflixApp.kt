package com.danilloteles.appnetflixapi.view.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danilloteles.appnetflixapi.auxiliar.getPopularMovies
import com.danilloteles.appnetflixapi.constantes.AppDestinations
import com.danilloteles.appnetflixapi.model.Movie
import com.danilloteles.appnetflixapi.view.screens.MovieDetails
import com.danilloteles.appnetflixapi.view.screens.NetflixScreen
import com.danilloteles.appnetflixapi.view.screens.SplashScreen

@Composable
fun NetflixApp() {

    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
       navController = navController,
        startDestination = AppDestinations.SPLASH_SCREEN
    ) {
        composable(
            route = AppDestinations.SPLASH_SCREEN
        ) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(
                        route = AppDestinations.MAIN_SCREEN
                    ) {
                        popUpTo(AppDestinations.SPLASH_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppDestinations.MAIN_SCREEN
        ) {
            NetflixScreen(
                onMovieClick = { movie ->
                    navController.navigate(
                        route = "${AppDestinations.MOVIE_DETAILS_SCREEN}/${movie.id}"
                    )
                }
            )
        }

        composable(
            route = AppDestinations.MOVIE_DETAILS_ROUTE,
            arguments = listOf(
                navArgument(
                    name = AppDestinations.MOVIE_ID_ARG
                ) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val movieId = backStackEntry.arguments?.getInt(AppDestinations.MOVIE_ID_ARG)

            val movies = getPopularMovies()
            val movie = movies.firstOrNull{ it.id == movieId }

            if (  movie != null  ) {
                MovieDetails(movie = movie)
            } else {
                Toast.makeText(context, "Filme não encontrado!", Toast.LENGTH_LONG).show()
            }

        }
    }
}