package com.volsib.repositorysearcher

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.volsib.repositorysearcher.ui.ReposViewModel

/**
 * Предоставляет фабрику для создания экземпляра ViewModel для всего приложения
 */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ReposViewModel(rsApplication(), rsApplication().container.reposRepository)
        }
    }

    /**
     * Extension функция получает объект [Application] и возвращает экземпляр [RSApplication].
     */
    private fun CreationExtras.rsApplication(): RSApplication =
        (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RSApplication)
}