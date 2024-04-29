package com.volsib.repositorysearcher

import android.app.Application
import com.volsib.repositorysearcher.data.AppContainer
import com.volsib.repositorysearcher.data.AppDataContainer

class RSApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
