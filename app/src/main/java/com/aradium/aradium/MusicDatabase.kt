package com.aradium.aradium

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Song::class, Playlist::class, PlaylistSong::class], version = 1, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var instance: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(context.applicationContext, MusicDatabase::class.java, "music_database")
                    .fallbackToDestructiveMigration()
                    .build()
                instance = db
                db
            }
        }
    }
}
