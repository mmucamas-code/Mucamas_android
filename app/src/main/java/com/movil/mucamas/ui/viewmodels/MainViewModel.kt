package com.movil.mucamas.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.data.SessionManager
import com.movil.mucamas.data.SessionProvider
import com.movil.mucamas.data.model.SessionResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionProvider.get()


    // Expone el estado de la sesión como un StateFlow
    val sessionState = sessionManager.userSessionFlow
        .map { user ->
            if (user != null) SessionResult.Success(user) else SessionResult.Empty
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionResult.Loading // <--- Ahora empezamos en Loading
        )
}