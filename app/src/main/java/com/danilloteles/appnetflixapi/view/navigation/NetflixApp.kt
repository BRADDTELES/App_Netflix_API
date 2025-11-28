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
import com.danilloteles.appnetflixapi.view.screens.ConteudoScreen
import com.danilloteles.appnetflixapi.view.screens.FilmeScreen
import com.danilloteles.appnetflixapi.view.screens.FormularioScreen
import com.danilloteles.appnetflixapi.view.screens.LoginScreen
import com.danilloteles.appnetflixapi.view.screens.LoginScreenV4
import com.danilloteles.appnetflixapi.view.screens.MainActivity
import com.danilloteles.appnetflixapi.view.screens.MinhaListaScreen
import com.danilloteles.appnetflixapi.view.screens.PopularFilmeDetalhesScreen
import com.danilloteles.appnetflixapi.view.screens.MeuFilmeDetalhesScreen
import com.danilloteles.appnetflixapi.view.screens.MinhaSerieDetalhesScreen
import com.danilloteles.appnetflixapi.view.screens.NetflixScreen
import com.danilloteles.appnetflixapi.view.screens.SerieScreen
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
            Log.d("TAG-NetflixApp", "Token recebido do deep link: $token")

            // IMPORTANTE: Só navegar se o token for válido (não vazio e não for TEST_TOKEN de debug)
            if (token.isNotEmpty() && token.length > 50) { // JWT tokens são longos
                Log.d("TAG-NetflixApp", "Token válido detectado, navegando para LoginScreen")

                // Navega para a LoginScreen passando o token como argumento
                navController.navigate("${AppDestination.LOGIN_SCREEN}/$token") {
                    popUpTo(AppDestination.MAIN_SCREEN) {
                        inclusive = false
                    }
                }

                // Resetar o token na MainActivity
                MainActivity.deeplinkRequestToken.value = null
            } else {
                Log.w("TAG-NetflixApp", "Token inválido ou vazio, ignorando navegação")
                // Limpar o token inválido
                MainActivity.deeplinkRequestToken.value = null
            }
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
                        navController.navigate(AppDestination.MINHA_LISTA_SCREEN)
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
                PopularFilmeDetalhesScreen(
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
                MeuFilmeDetalhesScreen(
                    movieId = movieId,
                    listId = listId,
                    onBackClick = { navController.popBackStack() },
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
                MinhaSerieDetalhesScreen(
                    serieId = serieId,
                    listId = listId,
                    onBackClick = { navController.popBackStack() },
                    onClick = { /* TODO: Implement navigation from MySerieDetails if needed */ }
                )
            } else {
                Toast.makeText(context, "Série não encontrada!", Toast.LENGTH_LONG).show()
            }
        }

        composable(
            route = AppDestination.LIST_FORM_ROUTE
        ) {
            FormularioScreen(
                onNavigateBack = { navController.popBackStack() }
            )
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
            route = AppDestination.MINHA_LISTA_SCREEN
        ) {
            MinhaListaScreen(
                onNavigateToConteudo = { listId, listName ->
                    navController.navigate(
                        route = "${AppDestination.CONTEUDO_SCREEN}/$listId/$listName"
                    )
                },
                onNavigateToFormulario = { navController.navigate(AppDestination.LIST_FORM_ROUTE) }
            )
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
                            route = "${AppDestination.MY_MOVIE_DETAILS}/$movieId?${AppDestination.LIST_ID_ARG}=$listIdArg"
                        )
                    },
                    onSerieClick = { serieId, listIdArg ->
                        navController.navigate(
                            route = "${AppDestination.MY_SERIES_DETAILS}/$serieId?${AppDestination.LIST_ID_ARG}=$listIdArg"
                        )
                    }
                )
            } else {
                Toast.makeText(context, "Detalhes da lista não encontrados!", Toast.LENGTH_LONG).show()
            }
        }

        composable(
            route = AppDestination.SERIES_LIST_SCREEN
        ) {
            SerieScreen(
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
            FilmeScreen(
                onFilmeClick = { filme ->
                    navController.navigate(
                        route = "${AppDestination.MY_MOVIE_DETAILS}/${filme.id}"
                    )
                }
            )
        }

        // Substitua apenas a parte das rotas de Login no seu NetflixApp.kt

// Rota com argumento (quando vem do deep link)
        composable(
            route = "${AppDestination.LOGIN_SCREEN}/{${AppDestination.REQUEST_TOKEN_ARG}}",
            arguments = listOf(
                navArgument(AppDestination.REQUEST_TOKEN_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val requestToken = backStackEntry.arguments?.getString(AppDestination.REQUEST_TOKEN_ARG)
            Log.d("NetflixApp", "Rota com argumento - Token: $requestToken")

            // Use a nova LoginScreenV4
            LoginScreenV4(
                navController = navController,
                deepLinkRequestToken = requestToken
            )
        }

// Rota sem argumento (primeira vez que o usuário acessa)
        composable(route = AppDestination.LOGIN_SCREEN) {
            Log.d("NetflixApp", "Rota sem argumento - Login inicial")

            // Use a nova LoginScreenV4
            LoginScreenV4(navController = navController)
        }
    }
}