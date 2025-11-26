package com.danilloteles.appnetflixapi.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.repository.v4.FilmeRepositoryV4
import kotlinx.coroutines.launch

class TesteAPIV4Activity : ComponentActivity() {

    private val repositoryV4 = FilmeRepositoryV4()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            testarAPI()
        }

    }

    private suspend fun testarAPI() {
        Log.d("TMDB","TESTE INICIADO...")

        val criar = repositoryV4.createList(
            nome = "Minha Lista Kotlin",
            descricao = "Lista criada via Retrofit"
        )

        Log.d("TMDB","CRIAR LISTA -> $criar")

        if (criar is Result.Sucesso) {
            val listId = criar.data.id.toString()

            val adicionarFilme = repositoryV4.addMovie(listId, movieId = 550)
            Log.d("TMDB","ADICIONAR FILME -> $adicionarFilme")

            val detalhes = repositoryV4.getListDetails(listId)
            Log.d("TMDB","DETALHES DA LISTA -> $detalhes")

            /*val removerFilme = repositoryV4.removeMovie(listId, movieId = 550)
            Log.d("TMDB","REMOVER FILME -> $removerFilme")*/

        }
    }
}