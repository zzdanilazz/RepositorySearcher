package com.volsib.repositorysearcher.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.volsib.repositorysearcher.models.Repo

@Database(entities = [Repo::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RepoDatabase: RoomDatabase() {

    abstract fun repoDao(): RepoDao

    companion object {
        @Volatile
        private var Instance: RepoDatabase? = null

        fun getDatabase(context: Context): RepoDatabase {
            // если значение экземпляра не равно null, возвращаем его,
            // иначе создам новый экземпляр базы данных
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, RepoDatabase::class.java, "repo_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}