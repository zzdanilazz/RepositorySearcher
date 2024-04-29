package com.volsib.repositorysearcher.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.volsib.repositorysearcher.models.Repo

@Dao
interface RepoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repo: Repo): Long

    @Query("SELECT * FROM repos")
    fun getAllRepos(): LiveData<List<Repo>>
}