
package com.movil.mucamas.ui.viewmodels

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.movil.mucamas.ui.models.Service
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.BufferedReader
import java.io.InputStreamReader

// --- Eventos para acciones puntuales (ej. mostrar un Toast) ---
sealed interface AdminUiEvent {
    data class ShowError(val message: String) : AdminUiEvent
    data class ShowSuccess(val message: String) : AdminUiEvent
}

// --- Estado que representa lo que se ve en la UI ---
data class AdminUiState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
)

class AdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val servicesCollection = db.collection("services")

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AdminUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getServices()
    }

    // --- FUNCIONES CRUD ---

    fun getServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val snapshot = servicesCollection.get().await()
                val services = snapshot.toObjects<Service>()
                _uiState.update { it.copy(services = services, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.emit(AdminUiEvent.ShowError("Error al obtener servicios: ${e.message}"))
            }
        }
    }

    fun addService(service: Service) {
        viewModelScope.launch {
            try {
                val docRef = servicesCollection.document()
                val newService = service.copy(id = docRef.id)
                docRef.set(newService).await()
                _eventFlow.emit(AdminUiEvent.ShowSuccess("Servicio '${newService.nombre}' agregado."))
                getServices() // Refresh list
            } catch (e: Exception) {
                _eventFlow.emit(AdminUiEvent.ShowError("Error al agregar servicio: ${e.message}"))
            }
        }
    }

    fun updateService(service: Service) {
        if (service.id?.isBlank() == true) {
            viewModelScope.launch { _eventFlow.emit(AdminUiEvent.ShowError("ID de servicio inválido.")) }
            return
        }
        viewModelScope.launch {
            try {
                servicesCollection.document(service.id!!).set(service).await()
                _eventFlow.emit(AdminUiEvent.ShowSuccess("Servicio '${service.nombre}' actualizado."))
                getServices() // Refresh list
            } catch (e: Exception) {
                _eventFlow.emit(AdminUiEvent.ShowError("Error al actualizar: ${e.message}"))
            }
        }
    }

    fun deleteService(serviceId: String, serviceName: String) {
        if (serviceId.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(AdminUiEvent.ShowError("ID de servicio inválido.")) }
            return
        }
        viewModelScope.launch {
            try {
                servicesCollection.document(serviceId).delete().await()
                _eventFlow.emit(AdminUiEvent.ShowSuccess("Servicio '$serviceName' eliminado."))
                getServices() // Refresh list
            } catch (e: Exception) {
                _eventFlow.emit(AdminUiEvent.ShowError("Error al eliminar: ${e.message}"))
            }
        }
    }

    // --- FUNCIONES DE IMPORTACIÓN ---

    fun importServicesFromCSV(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val services = parseCsv(uri, contentResolver)
                saveServicesToFirestore(services)
                _eventFlow.emit(AdminUiEvent.ShowSuccess("${services.size} servicios importados con éxito."))
                getServices() // Refresh list
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.emit(AdminUiEvent.ShowError("Error al procesar CSV: ${e.message}"))
            }
        }
    }

    private fun parseCsv(uri: Uri, contentResolver: ContentResolver): List<Service> {
        val serviceList = mutableListOf<Service>()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val header = reader.readLine() // Omitir encabezado
                reader.forEachLine { line ->
                    if (line.isNotBlank()) {
                        // Intenta detectar si el separador es , o ;
                        val delimiter = if (line.contains(";")) ";" else ","
                        val tokens = line.split(delimiter)

                        if (tokens.size >= 10) { // Validar que la línea esté completa
                            val service = Service(
                                id = "",
                                nombre = tokens[0].trim(),
                                descripcion = tokens[1].trim(),
                                precio = tokens[2].trim().toLongOrNull() ?: 0L,
                                duracionMinutos = tokens[3].trim().toIntOrNull() ?: 0,
                                tipo = tokens[4].trim(),
                                categoria = tokens[5].trim(),
                                esCombo = tokens[6].trim().toBoolean(),
                                activo = tokens[7].trim().toBoolean(),
                                icono = tokens[8].trim(),
                                caracteristicas = tokens[9].trim().split("|") // Usa otro separador para la lista interna
                            )
                            serviceList.add(service)
                        }
                    }
                }
            }
        }
        return serviceList
    }

    fun loadServicesFromJson(uri: Uri, contentResolver: android.content.ContentResolver) {
        viewModelScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.readText()

                val listType = object : TypeToken<List<Service>>() {}.type
                val services: List<Service> = Gson().fromJson(jsonString, listType)

                saveServicesToFirestore(services)
                _eventFlow.emit(AdminUiEvent.ShowSuccess("${services.size} servicios importados con éxito."))
                getServices()

            } catch (e: Exception) {
                _eventFlow.emit(AdminUiEvent.ShowError("Error al procesar el archivo: ${e.message}"))
            }
        }
    }

    private suspend fun saveServicesToFirestore(services: List<Service>) {
        db.runBatch { batch ->
            services.forEach { service ->
                val docRef = servicesCollection.document()
                // Asignar el ID generado automáticamente al objeto antes de guardarlo
                batch.set(docRef, service.copy(id = docRef.id))
            }
        }.await()
    }
}
