package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.repository.ShopRepository

class SouqApplication : Application() {
    lateinit var database: AppDatabase
        private set
        
    lateinit var repository: ShopRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "souq_database"
        ).fallbackToDestructiveMigration().build()
        
        repository = ShopRepository(database.shopDao())
    }
}
