package com.movil.mucamas.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.movil.mucamas.data.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlinx.coroutines.flow.catch

// Extensión para acceder a DataStore de forma sencilla
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        // Claves para guardar los datos
        private val USER_ID = stringPreferencesKey("user_id")
        private val FULL_NAME = stringPreferencesKey("full_name")
        private val ID_NUMBER = stringPreferencesKey("id_number")
        private val ROLE = stringPreferencesKey("role")
    }

    // Guarda la sesión del usuario
    suspend fun saveUserSession(session: UserSession) {
        context.dataStore.edit {
            it[USER_ID] = session.userId
            it[FULL_NAME] = session.fullName
            it[ID_NUMBER] = session.idNumber
            it[ROLE] = session.role
        }
    }

    // Borra la sesión (logout)
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    // Flujo para obtener la sesión actual
    val userSessionFlow: Flow<UserSession?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences()) // Si hay error, devuelve preferencias vacías
            } else {
                throw exception
            }
        }
        .map {
        val userId = it[USER_ID]
        val fullName = it[FULL_NAME]
        val idNumber = it[ID_NUMBER]
        val role = it[ROLE]

        if (userId != null && fullName != null && idNumber != null && role != null) {
            UserSession(userId, fullName, idNumber, role)
        } else {
            null
        }
    }
}

object SessionProvider {

    private lateinit var sessionManager: SessionManager

    fun init(context: Context) {
        if (!::sessionManager.isInitialized) {
            sessionManager = SessionManager(context.applicationContext)
        }
    }

    fun get(): SessionManager {
        check(::sessionManager.isInitialized) {
            "SessionProvider no inicializado. Llama init() en Application"
        }
        return sessionManager
    }
}

