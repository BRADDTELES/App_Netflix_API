package com.danilloteles.appnetflixapi.utils

sealed class LoginEvent {
    data class OpenWebView(val url: String) : LoginEvent()
    object LoginSuccess : LoginEvent()
}
