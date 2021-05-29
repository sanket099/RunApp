package com.sanket.runapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dagger.hilt.DefineComponent
import javax.inject.Singleton


@Database(
    entities = [Run::class],
    version = 1,
    exportSchema = false

)
@TypeConverters(Converters::class)
abstract class RunDataBase : RoomDatabase() { //dagger will handle

    abstract fun getRunDao(): RunDao
}