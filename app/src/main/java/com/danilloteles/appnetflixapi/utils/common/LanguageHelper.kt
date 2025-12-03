package com.danilloteles.appnetflixapi.utils.common

object LanguageHelper {
    fun getDisplayTitle(title: String?, originalTitle: String?): String {
        return when {
            !title.isNullOrBlank() && title != originalTitle -> title  // Traduzido
            !originalTitle.isNullOrBlank() -> originalTitle            // Original
            else -> "Título Indisponível"
        }
    }

    fun getDisplayName(name: String?, originalName: String?): String {
        return when {
            !name.isNullOrBlank() && name != originalName -> name      // Traduzido
            !originalName.isNullOrBlank() -> originalName              // Original
            else -> "Nome Indisponível"
        }
    }

    fun getDisplayOverview(overview: String?): String {
        return when {
            !overview.isNullOrBlank() -> overview
            else -> "Sinopse em português não disponível."
        }
    }

    fun isTranslated(title: String?, originalTitle: String?): Boolean {
        return !title.isNullOrBlank() &&
                !originalTitle.isNullOrBlank() &&
                title != originalTitle
    }

    fun isNameTranslated(name: String?, originalName: String?): Boolean {
        return !name.isNullOrBlank() &&
                !originalName.isNullOrBlank() &&
                name != originalName
    }

    // UNIVERSAL - Funciona para filmes E séries
    fun getDisplayTitleOrName(
        title: String? = null,           // Para filmes
        originalTitle: String? = null,   // Para filmes
        name: String? = null,            // Para séries
        originalName: String? = null     // Para séries
    ): String {
        return when {
            // Se for série (tem name)
            name != null || originalName != null -> {
                getDisplayName(name, originalName)
            }
            // Se for filme (tem title)
            title != null || originalTitle != null -> {
                getDisplayTitle(title, originalTitle)
            }
            else -> "Título Indisponível"
        }
    }

    // UNIVERSAL - Funciona para filmes E séries
    fun isContentTranslated(
        title: String? = null,           // Para filmes
        originalTitle: String? = null,   // Para filmes
        name: String? = null,            // Para séries
        originalName: String? = null     // Para séries
    ): Boolean {
        return when {
            // Se for série
            name != null || originalName != null -> {
                isNameTranslated(name, originalName)
            }
            // Se for filme
            title != null || originalTitle != null -> {
                isTranslated(title, originalTitle)
            }
            else -> false
        }
    }

    // Obter título original para exibição
    fun getOriginalTitleOrName(
        originalTitle: String? = null,   // Para filmes
        originalName: String? = null     // Para séries
    ): String? {
        return originalName ?: originalTitle
    }
}