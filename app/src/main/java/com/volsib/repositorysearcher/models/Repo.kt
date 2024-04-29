package com.volsib.repositorysearcher.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "repos"
)
data class Repo(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val name: String?,
    val owner: Owner?,
    val description: String?,
    @SerializedName("html_url")
    val url: String?,
    @SerializedName("stargazers_count")
    val stargazersCount: Int?,
    val language: String?,
    val forks: Int?,
)