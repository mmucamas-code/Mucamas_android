package com.movil.mucamas.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.movil.mucamas.ui.models.Service
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.BufferedReader
import java.io.InputStreamReader

sealed interface AdminUiEvent {
    data class ShowError(val message: String) : AdminUiEvent
    object ServicesLoaded : AdminUiEvent
}

class AdminViewModel : ViewModel() {

    private val _eventFlow = MutableSharedFlow<AdminUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun loadServicesFromJson(uri: Uri, contentResolver: android.content.ContentResolver) {
        viewModelScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.readText()

                val listType = object : TypeToken<List<Service>>() {}.type
                val services: List<Service> = Gson().fromJson(jsonString, listType)

                saveServicesToFirestore(services)
                _eventFlow.emit(AdminUiEvent.ServicesLoaded)

            } catch (e: Exception) {
                _eventFlow.emit(AdminUiEvent.ShowError("Error al procesar el archivo: ${e.message}"))
            }
        }
    }

    private suspend fun saveServicesToFirestore(services: List<Service>) {
        val firestore = FirebaseFirestore.getInstance()
        val servicesCollection = firestore.collection("services")

        firestore.runBatch { batch ->
            services.forEach { service ->
                val docRef = servicesCollection.document()
                batch.set(docRef, service.copy(id = docRef.id))
            }
        }.await()
    }
}
