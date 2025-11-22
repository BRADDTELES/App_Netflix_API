package com.danilloteles.appnetflixapi.view.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.danilloteles.appnetflixapi.view.screens.FilmeListScreen
import com.danilloteles.appnetflixapi.view.screens.ListForm
import com.danilloteles.appnetflixapi.view.screens.LoginScreen
import com.danilloteles.appnetflixapi.view.screens.MainActivity
import com.danilloteles.appnetflixapi.view.screens.MovieDetails
import com.danilloteles.appnetflixapi.view.screens.MyListScreen
import com.danilloteles.appnetflixapi.view.screens.MyMovieDetails
import com.danilloteles.appnetflixapi.view.screens.MySerieDetails
import com.danilloteles.appnetflixapi.view.screens.NetflixScreen
import com.danilloteles.appnetflixapi.view.screens.SerieListScreen
import com.danilloteles.appnetflixapi.view.screens.SplashScreen
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NetflixApp(
    deeplinkRequestToken: StateFlow<String?> // Adicionado como parâmetro
) {

    val navController = rememberNavController()
    val context = LocalContext.current
    // Instanciando o Preferences DataStore
    val userPreferencesRepository = remember { UserPreferencesRepository(context) }
    // Coletando a session_id
    val sessionId by userPreferencesRepository.sessionId.collectAsState(initial = null)
    val requestTokenFromDeeplink by deeplinkRequestToken.collectAsState() // Coletando o token do deep link
    Log.d("TAG-NetflixApp", "requestTokenFromDeeplink coletado: $requestTokenFromDeeplink")

    LaunchedEffect(requestTokenFromDeeplink) {
        requestTokenFromDeeplink?.let { token ->
            // Navega para a LoginScreen passando o token como argumento
            // Se já estiver na LoginScreen, evita navegar novamente, mas o LaunchedEffect na LoginScreen ainda vai reagir ao token
            navController.navigate("${AppDestination.LOGIN_SCREEN}/$token") {
                popUpTo(AppDestination.MAIN_SCREEN) { // Limpa a back stack até a MainScreen
                    inclusive = false // Não remove a MainScreen
                }
            }
            // Resetar o token na MainActivity para evitar que o LaunchedEffect seja re-acionado desnecessariamente
            MainActivity.deeplinkRequestToken.value = null
        }
    }


    NavHost(
       navController = navController,
        startDestination = AppDestination.SPLASH_SCREEN
    ) {
        composable(
            route = AppDestination.SPLASH_SCREEN
        ) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(
                        route = AppDestination.MAIN_SCREEN
                    ) {
                        popUpTo(AppDestination.SPLASH_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppDestination.MAIN_SCREEN
        ) {
            NetflixScreen(
                onMovieClick = { filme ->
                    navController.navigate(
                        route = "${AppDestination.MOVIE_DETAILS_SCREEN}/${filme.id}"
                    )
                },
                onMyListClick = {
                    // Lógica para verificar a session_id se navega para o MyListScreen ou retorna para LoginScreen
                    if (  sessionId != null && sessionId!!.isNotEmpty()  ) {
                        navController.navigate(AppDestination.MY_LIST_SCREEN)
                    } else {
                        navController.navigate(AppDestination.LOGIN_SCREEN)
                    }
                },
                onSeriesListClick = {
                    if (  sessionId != null && sessionId!!.isNotEmpty()  ) {
                        navController.navigate(AppDestination.SERIES_LIST_SCREEN)
                    } else {
                        navController.navigate(AppDestination.LOGIN_SCREEN)
                    }
                },
                onFilmesListClick = {
                    if (  sessionId != null && sessionId!!.isNotEmpty()  ) {
                        navController.navigate(AppDestination.FILMES_LIST_SCREEN)
                    } else {
                        navController.navigate(AppDestination.LOGIN_SCREEN)
                    }
                }
            )
        }

        composable(
            route = AppDestination.MOVIE_DETAILS_ROUTE,
            arguments = listOf(
                navArgument(
                    name = AppDestination.MOVIE_ID_ARG
                ) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val movieId = backStackEntry.arguments?.getInt(AppDestination.MOVIE_ID_ARG)

            if (  movieId != null  ) {
                MovieDetails(
                    movieId = movieId
                )
            } else {
                Toast.makeText(context, "Filme não encontrado!", Toast.LENGTH_LONG).show()
            }
        }

        composable(
            route = AppDestination.MY_MOVIE_DETAILS_ROUTE,
            arguments = listOf(
                navArgument(AppDestination.MOVIE_ID_ARG) {
                    type = NavType.IntType
                },
                navArgument(AppDestination.LIST_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt(AppDestination.MOVIE_ID_ARG)
            val listId = backStackEntry.arguments?.getString(AppDestination.LIST_ID_ARG)
            if (movieId != null) {
                MyMovieDetails(
                    movieId = movieId,
                    listId = listId,
                    onClick = { /* TODO: Implement navigation from MyMovieDetails if needed */ }
                )
            } else {
                Toast.makeText(context, "Filme da Minha Lista não encontrado!", Toast.LENGTH_LONG).show()
            }
        }

        composable(
            route = AppDestination.MY_SERIES_DETAILS_ROUTE,
            arguments = listOf(
                navArgument(name = AppDestination.SERIE_ID_ARG) {
                    type = NavType.IntType
                },
                navArgument(name = AppDestination.LIST_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val serieId = backStackEntry.arguments?.getInt(AppDestination.SERIE_ID_ARG)
            val listId = backStackEntry.arguments?.getString(AppDestination.LIST_ID_ARG)
            if (serieId != null) {
                MySerieDetails(
                    serieId = serieId,
                    listId = listId,
                    onClick = { /* TODO: Implement navigation from MySerieDetails if needed */ }
                )
            } else {
                Toast.makeText(context, "Série não encontrada!", Toast.LENGTH_LONG).show()
            }
        }

        composable(
            route = AppDestination.LIST_FORM_ROUTE
        ) {
            ListForm()
        }

        // Rota atualizada para LoginScreen para aceitar o request_token
        composable(
            route = "${AppDestination.LOGIN_SCREEN}/{${AppDestination.REQUEST_TOKEN_ARG}}",
            arguments = listOf(navArgument(AppDestination.REQUEST_TOKEN_ARG) {
                type = NavType.StringType
                nullable = true // O argumento é opcional
                defaultValue = null
            })
        ) { backStackEntry ->
            val requestToken = backStackEntry.arguments?.getString(AppDestination.REQUEST_TOKEN_ARG)
            Log.d("TAG-NetflixApp", "Request token extraído do nav argument para LoginScreen: $requestToken")
            LoginScreen(navController = navController, deepLinkRequestToken = requestToken)
        }

        // Rota para a LoginScreen sem o argumento (quando o usuário clica em "Minha Lista" pela primeira vez)
        composable(
            route = AppDestination.LOGIN_SCREEN
        ) {
            LoginScreen(navController = navController)
        }

        composable(
            route = AppDestination.MY_LIST_SCREEN
        ) {
            MyListScreen(
                onMovieClick = { mediaItem, listId ->
                    val route = when (mediaItem.media_type) {
                        "movie" -> "${AppDestination.MY_MOVIE_DETAILS}/${mediaItem.id}?${AppDestination.LIST_ID_ARG}=$listId"
                        "tv" -> "${AppDestination.MY_SERIES_DETAILS}/${mediaItem.id}?${AppDestination.LIST_ID_ARG}=$listId"
                        else -> null // Ou uma rota de erro/fallback
                    }
                    route?.let { navController.navigate(it) }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToListForm = { navController.navigate(AppDestination.LIST_FORM_ROUTE) }
            )
        }

        composable(
            route = AppDestination.SERIES_LIST_SCREEN
        ) {
            SerieListScreen(
                onSerieClick = { serie ->
                    navController.navigate(
                        route = "${AppDestination.MY_SERIES_DETAILS}/${serie.id}"
                    )
                }
            )
        }

        composable(
            route = AppDestination.FILMES_LIST_SCREEN
        ) {
            FilmeListScreen(
                onFilmeClick = { filme ->
                    navController.navigate(
                        route = "${AppDestination.MY_MOVIE_DETAILS}/${filme.id}"
                    )
                }
            )
        }
    }
}