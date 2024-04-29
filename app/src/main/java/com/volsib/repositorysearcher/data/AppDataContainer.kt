package com.volsib.repositorysearcher.data

import android.content.Context

class AppDataContainer(private val context: Context) : AppContainer {
    override val reposRepository: ReposRepository by lazy {
        ReposRepository(RepoDatabase.getDatabase(context).repoDao())
    }
}