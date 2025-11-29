package com.danilloteles.appnetflixapi.model.v3

// Resposta unificada para detalhes da lista
data class ListaDetalhesResposta(
    val created_by: String,
    val description: String,
    val favorite_count: Int,
    val id: String,
    val item_count: Int,
    val iso_639_1: String,
    val name: String,
    val poster_path: String?,
    val items: List<MediaItem> // Usa a lista de MediaItem
)
