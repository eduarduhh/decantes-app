package com.eduarduhh.decantes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.eduarduhh.decantes.data.repository.DecantesRepository

class ViewModelFactory(
    private val repository: DecantesRepository,
    private val grupoId: Long = 0L,
    private val perfumeId: Long = 0L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repository) as T
            modelClass.isAssignableFrom(GroupListViewModel::class.java) ->
                GroupListViewModel(repository) as T
            modelClass.isAssignableFrom(GroupDetailViewModel::class.java) ->
                GroupDetailViewModel(repository, grupoId) as T
            modelClass.isAssignableFrom(PerfumeDetailViewModel::class.java) ->
                PerfumeDetailViewModel(repository, perfumeId) as T
            modelClass.isAssignableFrom(BackupViewModel::class.java) ->
                BackupViewModel(repository) as T
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
    }
}
