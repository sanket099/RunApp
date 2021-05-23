package com.sanket.runapp.dependency_injection

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.sanket.runapp.R
import com.sanket.runapp.other.Constants
import com.sanket.runapp.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped // for lifetime of service, only one instance of this client
    @Provides
    fun provideFusedLocationClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun providePendingIntent(@ApplicationContext app: Context) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also { // go to Main Activity on click notification
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(@ApplicationContext app : Context, pendingIntent: PendingIntent) = NotificationCompat.Builder(app,
        Constants.NOTIFICATION_CHANNEL_ID
    )
        .setAutoCancel(false)
        .setOngoing(true) //cant be swipped away
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("RunApp")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)
}