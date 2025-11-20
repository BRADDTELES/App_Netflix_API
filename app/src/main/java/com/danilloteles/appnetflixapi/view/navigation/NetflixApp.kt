package com.danilloteles.appnetflixapi.view.navigation

import android.widget.Toast
import androidx.compose.material3.Text
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
import com.danilloteles.appnetflixapi.auxiliar.getPopularMovies
import com.danilloteles.appnetflixapi.constantes.AppDestinations
import com.danilloteles.appnetflixapi.model.Movie
import com.danilloteles.appnetflixapi.utils.UserPreferencesRepository
import com.danilloteles.appnetflixapi.view.screens.ListForm
import com.danilloteles.appnetflixapi.view.screens.LoginScreen
import com.danilloteles.appnetflixapi.view.screens.MovieDetails
import com.danilloteles.appnetflixapi.view.screens.NetflixScreen
import com.danilloteles.appnetflixapi.view.screens.SplashScreen
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.LaunchedEffect // Import adicionado
import com.danilloteles.appnetflixapi.view.screens.MainActivity // Import adicionado para acessar o deeplinkRequestToken
import com.danilloteles.appnetflixapi.view.screens.MyListScreen
import android.util.Log // Import adicionado para Log

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
            navController.navigate("${AppDestinations.LOGIN_SCREEN}/$token") {
                popUpTo(AppDestinations.MAIN_SCREEN) { // Limpa a back stack até a MainScreen
                    inclusive = false // Não remove a MainScreen
                }
            }
            // Resetar o token na MainActivity para evitar que o LaunchedEffect seja re-acionado desnecessariamente
            MainActivity.deeplinkRequestToken.value = null
        }
    }


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
                onMovieClick = { filme ->
                    navController.navigate(
                        route = "${AppDestinations.MOVIE_DETAILS_SCREEN}/${filme.id}"
                    )
                },
                onAddClick = {
                    navController.navigate(
                        route = AppDestinations.LIST_FORM_ROUTE
                    )
                },
                onMyListClick = {
                    // Lógica para verificar a session_id se navega para o MyListScreen ou retorna para LoginScreen
                    if (  sessionId != null && sessionId!!.isNotEmpty()  ) {
                        navController.navigate(AppDestinations.MY_LIST_SCREEN)
                    } else {
                        navController.navigate(AppDestinations.LOGIN_SCREEN)
                    }
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

            if (  movieId != null  ) {
                MovieDetails(
                    movieId = movieId
                )
            } else {
                Toast.makeText(context, "Filme não encontrado!", Toast.LENGTH_LONG).show()
            }
        }

        composable(
            route = AppDestinations.LIST_FORM_ROUTE
        ) {
            ListForm()
        }

        // Rota atualizada para LoginScreen para aceitar o request_token
        composable(
            route = "${AppDestinations.LOGIN_SCREEN}/{${AppDestinations.REQUEST_TOKEN_ARG}}",
            arguments = listOf(navArgument(AppDestinations.REQUEST_TOKEN_ARG) {
                type = NavType.StringType
                nullable = true // O argumento é opcional
                defaultValue = null
            })
        ) { backStackEntry ->
            val requestToken = backStackEntry.arguments?.getString(AppDestinations.REQUEST_TOKEN_ARG)
            Log.d("TAG-NetflixApp", "Request token extraído do nav argument para LoginScreen: $requestToken")
            LoginScreen(navController = navController, deepLinkRequestToken = requestToken)
        }

        // Rota para a LoginScreen sem o argumento (quando o usuário clica em "Minha Lista" pela primeira vez)
        composable(
            route = AppDestinations.LOGIN_SCREEN
        ) {
            LoginScreen(navController = navController)
        }

        composable(
            route = AppDestinations.MY_LIST_SCREEN
        ) {
            MyListScreen(
                onMovieClick = { filme ->
                    navController.navigate(
                        route = "${AppDestinations.MOVIE_DETAILS_SCREEN}/${filme.id}"
                    )
                },
                onNavigateBack = {
                    Log.d("TAG-NetflixApp", "onNavigateBack de MyListScreen chamado. Executando popBackStack().")
                    navController.popBackStack()
                },
                onNavigateToListForm = { navController.navigate(AppDestinations.LIST_FORM_ROUTE) }
            )
        }
    }
}