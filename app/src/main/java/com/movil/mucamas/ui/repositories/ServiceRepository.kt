package com.movil.mucamas.ui.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import com.movil.mucamas.ui.models.Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServiceRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Escucha en tiempo real los servicios que están activos.
     * Firestore se encarga de cancelar el listener cuando el Flow se cancela.
     */
    fun getActiveServicesStream(): Flow<List<Service>> {
        return firestore.collection("services")
            .whereEqualTo("activo", true) // Solo servicios activos
            .snapshots() // Flow que emite en cada cambio
            .map { snapshot ->
                // Convierte la lista de documentos al data class Service
                snapshot.toObjects<Service>()
            }
    }
}
