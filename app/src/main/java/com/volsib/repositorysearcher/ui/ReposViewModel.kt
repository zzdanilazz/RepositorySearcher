package com.volsib.repositorysearcher.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.volsib.repositorysearcher.RSApplication
import com.volsib.repositorysearcher.data.ReposRepository
import com.volsib.repositorysearcher.models.Repo
import com.volsib.repositorysearcher.util.Resource
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.IOException


class ReposViewModel(
    app: Application,
    private val reposRepository: ReposRepository,
) : AndroidViewModel(app) {

    val repos: MutableLiveData<Resource<List<Repo>>?> = MutableLiveData()
    val downloads: MutableLiveData<Resource<String>> = MutableLiveData()

    private var reposResponse: MutableList<Repo>? = null
    private var currentPage = 1
    var isLastPage = false

    private var oldUsername: String? = null
    private var newUsername: String? = null

    private var currentRepoName: String? = null

    fun getRepos(username: String) = viewModelScope.launch {
        if (username.isNotEmpty()) {
            safeReposCall(username)
        } else {
            invalidateInput()
            repos.postValue(Resource.Error("Введите непустое значение!"))
        }
    }

    fun downloadRepo(repo: Repo) = viewModelScope.launch {
        safeDownloadCall(repo)
    }

    private fun invalidateInput() {
        reposResponse = null
        currentPage = 1
        isLastPage = false
        oldUsername = null
        newUsername = null
    }

    fun getDownloadedRepos() = reposRepository.getAllRepos()

    private suspend fun safeReposCall(username: String) {
        newUsername = username

        if (oldUsername != newUsername) {
            currentPage = 1
        }

        repos.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = reposRepository.getReposByUsername(username, currentPage)
                repos.postValue(handleReposResponse(response))
            } else {
                repos.postValue(Resource.Error("Нет подключения к Интернету!"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> repos.postValue(Resource.Error("Сбой сети, повторите попытку позже"))
                else -> {
                    repos.postValue(Resource.Error("Такого логина не существует!"))
                    t.printStackTrace()
                }
            }
        }
    }

    private suspend fun safeDownloadCall(repo: Repo) {
        downloads.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                currentRepoName = repo.name
                val response = reposRepository.downloadRepo(repo.owner?.login!!, repo.name!!)
                // Вставка скачанного репозитория в локальную бд
                reposRepository.insertRepo(repo)
                downloads.postValue(handleDownloadResponse(response))
            } else {
                downloads.postValue(Resource.Error("Нет подключения к Интернету!"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> downloads.postValue(Resource.Error("Сбой сети, повторите попытку позже"))
                else -> {
                    downloads.postValue(Resource.Error("Ошибка загрузки!"))
                    t.printStackTrace()
                }
            }
        }
    }

    private fun handleDownloadResponse(response: Response<ResponseBody>): Resource<String> {
        if (response.isSuccessful) {
            val responseBody = response.body()
            responseBody?.byteStream()?.use { input ->
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val outputFile = File(path, "$currentRepoName.zip")
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
                return Resource.Success(currentRepoName.toString())
            }
        }

        invalidateInput()
        return Resource.Error("Ошибка загрузки!")
    }

    private fun handleReposResponse(response: Response<MutableList<Repo>>) : Resource<List<Repo>> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                // Если получили первую страницу или изменили логин
                if (reposResponse == null || oldUsername != newUsername) {
                    currentPage = 1
                    oldUsername = newUsername
                    reposResponse = resultResponse
                // Добавляем данные, если еще есть следующие страницы
                } else if (!isLastPage){
                    currentPage++
                    reposResponse?.addAll(resultResponse)
                }

                // Определяем, есть ли следующая страница
                val linkHeader = response.headers()["link"]
                isLastPage = if (linkHeader != null) {
                    !linkHeader.contains("rel=\"next\"")
                } else {
                    true
                }

                return Resource.Success(reposResponse?: resultResponse)
            }
        }

        invalidateInput()
        return Resource.Error("Такого логина не существует!")
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<RSApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}