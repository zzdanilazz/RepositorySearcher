package com.volsib.repositorysearcher.data

import androidx.lifecycle.LiveData
import com.volsib.repositorysearcher.api.RetrofitInstance
import com.volsib.repositorysearcher.models.Repo
import okhttp3.ResponseBody
import retrofit2.Response

class ReposRepository(
    private val repoDao: RepoDao
) {
    // Remote
    suspend fun getReposByUsername(username: String, page: Int): Response<MutableList<Repo>> {
        return RetrofitInstance.api.getReposByUsername(username, page)
    }

    suspend fun downloadRepo(username: String, repoName: String): Response<ResponseBody> {
        return RetrofitInstance.api.downloadRepo(username, repoName)
    }

    // Local
    suspend fun insertRepo(repo: Repo): Long {
        return repoDao.insert(repo)
    }

    fun getAllRepos(): LiveData<List<Repo>> {
        return repoDao.getAllRepos()
    }
}