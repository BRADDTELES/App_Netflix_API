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
import com.danilloteles.appnetflixapi.constantes.Navigation
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.view.screens.ListForm
import com.danilloteles.appnetflixapi.view.screens.LoginScreen
import com.danilloteles.appnetflixapi.view.screens.MainActivity
import com.danilloteles.appnetflixapi.view.screens.MovieDetails
import com.danilloteles.appnetflixapi.view.screens.MyListScreen
import com.danilloteles.appnetflixapi.view.screens.MyMovieDetails
import com.danilloteles.appnetflixapi.view.screens.NetflixScreen
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
            navController.navigate("${Navigation.LOGIN_SCREEN}/$token") {
                popUpTo(Navigation.MAIN_SCREEN) { // Limpa a back stack até a MainScreen
                    inclusive = false // Não remove a MainScreen
                }
            }
            // Resetar o token na MainActivity para evitar que o LaunchedEffect seja re-acionado desnecessariamente
            MainActivity.deeplinkRequestToken.value = null
        }
    }


    NavHost(
       navController = navController,
        startDestination = Navigation.SPLASH_SCREEN
    ) {
        composable(
            route = Navigation.SPLASH_SCREEN
        ) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(
                        route = Navigation.MAIN_SCREEN
                    ) {
                        popUpTo(Navigation.SPLASH_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Navigation.MAIN_SCREEN
        ) {
            NetflixScreen(
                onMovieClick = { filme ->
                    navController.navigate(
                        route = "${Navigation.MOVIE_DETAILS_SCREEN}/${filme.id}"
                    )
                },
                onAddClick = {
                    navController.navigate(
                        route = Navigation.LIST_FORM_ROUTE
                    )
                },
                onMyListClick = {
                    // Lógica para verificar a session_id se navega para o MyListScreen ou retorna para LoginScreen
                    if (  sessionId != null && sessionId!!.isNotEmpty()  ) {
                        navController.navigate(Navigation.MY_LIST_SCREEN)
                    } else {
                        navController.navigate(Navigation.LOGIN_SCREEN)
                    }
                }
            )
        }

        composable(
            route = Navigation.MOVIE_DETAILS_ROUTE,
            arguments = listOf(
                navArgument(
                    name = Navigation.MOVIE_ID_ARG
                ) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val movieId = backStackEntry.arguments?.getInt(Navigation.MOVIE_ID_ARG)

            if (  movieId != null  ) {
                MovieDetails(
                    movieId = movieId
                )
            } else {
                Toast.makeText(context, "Filme não encontrado!", Toast.LENGTH_LONG).show()
            }
        }

        composable(
            route = Navigation.MY_MOVIE_DETAILS_ROUTE,
            arguments = listOf(
                navArgument(Navigation.MOVIE_ID_ARG) {
                    type = NavType.IntType
                },
                navArgument(Navigation.LIST_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt(Navigation.MOVIE_ID_ARG)
            val listId = backStackEntry.arguments?.getString(Navigation.LIST_ID_ARG)
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
            route = Navigation.LIST_FORM_ROUTE
        ) {
            ListForm()
        }

        // Rota atualizada para LoginScreen para aceitar o request_token
        composable(
            route = "${Navigation.LOGIN_SCREEN}/{${Navigation.REQUEST_TOKEN_ARG}}",
            arguments = listOf(navArgument(Navigation.REQUEST_TOKEN_ARG) {
                type = NavType.StringType
                nullable = true // O argumento é opcional
                defaultValue = null
            })
        ) { backStackEntry ->
            val requestToken = backStackEntry.arguments?.getString(Navigation.REQUEST_TOKEN_ARG)
            Log.d("TAG-NetflixApp", "Request token extraído do nav argument para LoginScreen: $requestToken")
            LoginScreen(navController = navController, deepLinkRequestToken = requestToken)
        }

        // Rota para a LoginScreen sem o argumento (quando o usuário clica em "Minha Lista" pela primeira vez)
        composable(
            route = Navigation.LOGIN_SCREEN
        ) {
            LoginScreen(navController = navController)
        }

        composable(
            route = Navigation.MY_LIST_SCREEN
        ) {
            MyListScreen(
                onMovieClick = { filme, listId ->
                    navController.navigate(
                        route = "${Navigation.MY_MOVIE_DETAILS}/${filme.id}?${Navigation.LIST_ID_ARG}=$listId"
                    )
                },
                onNavigateBack = {
                    Log.d("TAG-NetflixApp", "onNavigateBack de MyListScreen chamado. Executando popBackStack().")
                    navController.popBackStack()
                },
                onNavigateToListForm = { navController.navigate(Navigation.LIST_FORM_ROUTE) }
            )
        }
    }
}