package com.sanket.runapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp //dagger is compiled time DI
class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())


    }

}