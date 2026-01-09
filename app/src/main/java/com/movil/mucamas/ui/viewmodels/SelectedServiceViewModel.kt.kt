package com.movil.mucamas.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.movil.mucamas.ui.models.Service
import com.movil.mucamas.ui.repositories.ServiceRepository


class SelectedServiceViewModel(
    private val repository: ServiceRepository = ServiceRepository()
) : ViewModel() {

    suspend fun getServiceByName(name:String): Service? {
        return repository.getServiceByNameOnce(name)
    }
}