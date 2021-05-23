package com.sanket.runapp.dependency_injection

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.sanket.runapp.db.RunDataBase
import com.sanket.runapp.other.Constants.FIRST_TIME_TOGGLE
import com.sanket.runapp.other.Constants.NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.sanket.runapp.other.Constants.RUNNING_DB_NAME
import com.sanket.runapp.other.Constants.SHARED_PREFERENCE_NAME
import com.sanket.runapp.other.Constants.WEIGHT

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

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) = app.getSharedPreferences(
        SHARED_PREFERENCE_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) = sharedPreferences.getString(NAME, "") ?: "" //with null check

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) = sharedPreferences.getFloat(WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTime(sharedPreferences: SharedPreferences) = sharedPreferences.getBoolean(
        FIRST_TIME_TOGGLE, true)


}