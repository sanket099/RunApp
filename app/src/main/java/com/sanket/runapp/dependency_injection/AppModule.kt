package com.sanket.runapp.dependency_injection

import android.content.Context
import androidx.room.Room
import com.sanket.runapp.db.RunDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.sanket.runapp.other.Constants.RUNNING_DB_NAME

//Manual for Dagger
@Module
@InstallIn(SingletonComponent::class) //Component defines when the dependencies are created and when they are destroyed
// dagger dependencies are limited
// to the given component .If the component is destroyed, the dependencies will also get destroyed
object AppModule {

    @Singleton //so that only one instance is used throughout the app
    @Provides
    fun provideRunningDatabase(  // init db
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        RunDataBase::class.java,
        RUNNING_DB_NAME //db name
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunDataBase) = db.getRunDao()
}