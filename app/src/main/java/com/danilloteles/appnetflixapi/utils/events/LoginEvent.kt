package com.danilloteles.appnetflixapi.utils.events

sealed class LoginEvent {
    data class OpenWebView(val url: String) : LoginEvent()
    object LoginSuccess : LoginEvent()
}
