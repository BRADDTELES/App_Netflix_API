package com.danilloteles.appnetflixapi.view.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danilloteles.appnetflixapi.constantes.AppDestination
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.view.screens.ConteudoScreen
import com.danilloteles.appnetflixapi.view.screens.FilmeScreen
import com.danilloteles.appnetflixapi.view.screens.FormularioScreen
import com.danilloteles.appnetflixapi.view.screens.LoginScreen
import com.danilloteles.appnetflixapi.view.screens.MinhaListaScreen
import com.danilloteles.appnetflixapi.view.screens.MeuFilmeDetalhesScreen
import com.danilloteles.appnetflixapi.view.screens.MinhaSerieDetalhesScreen
import com.danilloteles.appnetflixapi.view.screens.NetflixScreen
import com.danilloteles.appnetflixapi.view.screens.PopularFilmeDetalhesScreen
import com.danilloteles.appnetflixapi.view.screens.SerieScreen
import com.danilloteles.appnetflixapi.view.screens.SplashScreen
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NetflixApp(
    deeplinkRequestToken: StateFlow<String?> // Mantido para o caso de o app ser aberto por deep link
) {

    val navController = rememberNavController()
    val context = LocalContext.current
    val userPreferencesRepository = remember { UserPreferencesRepository(context) }

    // ** LÓGICA DE AUTENTICAÇÃO CORRIGIDA **
    // Observa o accessToken da v4 para tomar decisões de navegação
    val accessTokenV4 by userPreferencesRepository.accessTokenV4.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = AppDestination.SPLASH_SCREEN
    ) {
        composable(route = AppDestination.SPLASH_SCREEN) {
            SplashScreen(onTimeout = {
                navController.navigate(AppDestination.MAIN_SCREEN) {
                    popUpTo(AppDestination.SPLASH_SCREEN) { inclusive = true }
                }
            })
        }

        composable(route = AppDestination.MAIN_SCREEN) {
            NetflixScreen(
                onMovieClick = { filme ->
                    navController.navigate("${AppDestination.MOVIE_DETAILS_SCREEN}/${filme.id}")
                },
                onMyListClick = {
                    if (!accessTokenV4.isNullOrBlank()) {
                        navController.navigate(AppDestination.MINHA_LISTA_SCREEN)
                    } else {
                        // Navega para Login, informando que o destino é a MINHA_LISTA_SCREEN
                        navController.navigate("${AppDestination.LOGIN_SCREEN}?${AppDestination.DESTINATION_ARG}=${AppDestination.MINHA_LISTA_SCREEN}")
                    }
                },
                onSeriesListClick = {
                    if (!accessTokenV4.isNullOrBlank()) {
                        navController.navigate(AppDestination.SERIES_LIST_SCREEN)
                    } else {
                        // Navega para Login, informando o destino
                        navController.navigate("${AppDestination.LOGIN_SCREEN}?${AppDestination.DESTINATION_ARG}=${AppDestination.SERIES_LIST_SCREEN}")
                    }
                },
                onFilmesListClick = {
                    if (!accessTokenV4.isNullOrBlank()) {
                        navController.navigate(AppDestination.FILMES_LIST_SCREEN)
                    } else {
                        // Navega para Login, informando o destino
                        navController.navigate("${AppDestination.LOGIN_SCREEN}?${AppDestination.DESTINATION_ARG}=${AppDestination.FILMES_LIST_SCREEN}")
                    }
                }
            )
        }

        // ** ROTA DE LOGIN UNIFICADA E CORRIGIDA **
        composable(
            route = "${AppDestination.LOGIN_SCREEN}?${AppDestination.DESTINATION_ARG}={${AppDestination.DESTINATION_ARG}}&${AppDestination.REQUEST_TOKEN_ARG}={${AppDestination.REQUEST_TOKEN_ARG}}",
            arguments = listOf(
                navArgument(AppDestination.DESTINATION_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = AppDestination.MINHA_LISTA_SCREEN // Destino padrão
                },
                navArgument(AppDestination.REQUEST_TOKEN_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val destination = backStackEntry.arguments?.getString(AppDestination.DESTINATION_ARG)
            val requestToken = backStackEntry.arguments?.getString(AppDestination.REQUEST_TOKEN_ARG)

            LoginScreen(
                navController = navController,
                deepLinkRequestToken = requestToken,
                destinationRoute = destination ?: AppDestination.MINHA_LISTA_SCREEN
            )
        }

        composable(route = AppDestination.MINHA_LISTA_SCREEN) {
            MinhaListaScreen(
                onNavigateToConteudo = { listId, listName ->
                    navController.navigate("${AppDestination.CONTEUDO_SCREEN}/$listId/$listName")
                },
                onNavigateToFormulario = { navController.navigate(AppDestination.LIST_FORM_ROUTE) }
            )
        }

        composable(route = AppDestination.SERIES_LIST_SCREEN) {
            SerieScreen(onSerieClick = { serie ->
                navController.navigate("${AppDestination.MY_SERIES_DETAILS}/${serie.id}")
            })
        }

        composable(route = AppDestination.FILMES_LIST_SCREEN) {
            FilmeScreen(onFilmeClick = { filme ->
                navController.navigate("${AppDestination.MY_MOVIE_DETAILS}/${filme.id}")
            })
        }
        
        composable(route = AppDestination.LIST_FORM_ROUTE) {
            FormularioScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = AppDestination.MOVIE_DETAILS_ROUTE,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId")
            if (movieId != null) {
                PopularFilmeDetalhesScreen(movieId = movieId)
            } else {
                Toast.makeText(context, "Filme não encontrado!", Toast.LENGTH_LONG).show()
            }
        }
        
        composable(
            route = AppDestination.CONTEUDO_ROUTE,
            arguments = listOf(
                navArgument(AppDestination.LIST_ID_ARG) { type = NavType.StringType },
                navArgument(AppDestination.MOVIE_TITLE_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString(AppDestination.LIST_ID_ARG)
            val movieTitle = backStackEntry.arguments?.getString(AppDestination.MOVIE_TITLE_ARG)

            if (listId != null && movieTitle != null) {
                ConteudoScreen(
                    listId = listId,
                    movieTitle = movieTitle,
                    onBackClick = { navController.popBackStack() },
                    onMovieClick = { movieId, listIdArg ->
                        navController.navigate(
                            "${AppDestination.MY_MOVIE_DETAILS}/$movieId?${AppDestination.LIST_ID_ARG}=$listIdArg"
                        )
                    },
                    onSerieClick = { serieId, listIdArg ->
                        navController.navigate(
                            "${AppDestination.MY_SERIES_DETAILS}/$serieId?${AppDestination.LIST_ID_ARG}=$listIdArg"
                        )
                    }
                )
            } else {
                Toast.makeText(context, "Detalhes da lista não encontrados!", Toast.LENGTH_LONG).show()
            }
        }

        composable(
            route = AppDestination.MY_MOVIE_DETAILS_ROUTE,
            arguments = listOf(
                navArgument(AppDestination.MOVIE_ID_ARG) { type = NavType.IntType },
                navArgument(AppDestination.LIST_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt(AppDestination.MOVIE_ID_ARG)
            val listId = backStackEntry.arguments?.getString(AppDestination.LIST_ID_ARG)
            if (movieId != null) {
                MeuFilmeDetalhesScreen(
                    movieId = movieId,
                    listId = listId,
                    onBackClick = { navController.popBackStack() },
                    onClick = { /* TODO */ }
                )
            }
        }

        composable(
            route = AppDestination.MY_SERIES_DETAILS_ROUTE,
            arguments = listOf(
                navArgument(AppDestination.SERIE_ID_ARG) { type = NavType.IntType },
                navArgument(AppDestination.LIST_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val serieId = backStackEntry.arguments?.getInt(AppDestination.SERIE_ID_ARG)
            val listId = backStackEntry.arguments?.getString(AppDestination.LIST_ID_ARG)
            if (serieId != null) {
                MinhaSerieDetalhesScreen(
                    serieId = serieId,
                    listId = listId,
                    onBackClick = { navController.popBackStack() },
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}