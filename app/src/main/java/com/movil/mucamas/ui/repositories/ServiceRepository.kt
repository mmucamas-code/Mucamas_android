package com.movil.mucamas.ui.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.movil.mucamas.ui.models.Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

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

    suspend fun getServiceByNameOnce(name: String): Service? {
        val snapshot = firestore.collection("services")
            .whereEqualTo("nombre", name)
            .limit(1)
            .get()
            .await()

        val serviceDocument = snapshot.documents.firstOrNull()
        val serviceObject = serviceDocument?.toObject(Service::class.java)
        Log.d("Session", "Service: $serviceObject")
        return serviceObject
    }

    suspend fun getServiceById(serviceId: String): Service? {
        val document = firestore.collection("services").document(serviceId).get().await()
        return document.toObject(Service::class.java)
    }
}
