package com.sanket.runapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RunDataBase : RoomDatabase() { //dagger will handle 

    abstract fun getFunDao(): RunDao
}