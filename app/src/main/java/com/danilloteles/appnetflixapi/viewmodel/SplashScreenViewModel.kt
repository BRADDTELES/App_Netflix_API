package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashScreenViewModel : ViewModel() {

    private val _isSplashScreenVisible : MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isSplashScreenVisible : StateFlow<Boolean> = _isSplashScreenVisible.asStateFlow()

    init {

        viewModelScope.launch {
            delay(2500)
            _isSplashScreenVisible.value = false
        }

    }

}