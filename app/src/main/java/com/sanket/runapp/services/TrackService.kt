package com.sanket.runapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.sanket.runapp.R
import com.sanket.runapp.other.Constants.ACTION_PAUSE_SERVICE
import com.sanket.runapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.sanket.runapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.sanket.runapp.other.Constants.ACTION_STOP_SERVICE
import com.sanket.runapp.other.Constants.FASTEST_LOCATION_INTERVAL
import com.sanket.runapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.sanket.runapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.sanket.runapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.sanket.runapp.other.Constants.NOTIFICATION_ID
import com.sanket.runapp.other.Constants.TIMER_UPDATE_INTERVAL
import com.sanket.runapp.other.TrackingUtility
import com.sanket.runapp.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

typealias Path = MutableList<LatLng>
typealias Paths = MutableList<Path>

class TrackService : LifecycleService() { // tell live data observe function in which lifecycle state service currently is

    private var isFirstRun = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunSeconds = MutableLiveData<Long>()

    companion object {
        val timeRunMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Paths>() // list of list of coordinates
    }

    private fun initValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunSeconds.postValue(0L)
        timeRunMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        initValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer { //lifecycle service
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        //startForegroundService()
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecond = 0L

    private fun startTimer(){
        addEmptyPath()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                lapTime = System.currentTimeMillis() - timeStarted // now - time started
                timeRunMillis.postValue(timeRun + lapTime)
                if(timeRunMillis.value!! >= lastSecond + 1000L){
                    timeRunSeconds.postValue(timeRunSeconds.value!! + 1)
                    lastSecond += 1000L
                }

                delay(TIMER_UPDATE_INTERVAL)
            }
            //not tracking
            timeRun += lapTime

        }
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtility.hasLocPermissions(this)) { // if we have location permission
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL //how often will we get updates
                    fastestInterval = FASTEST_LOCATION_INTERVAL //fastest interval
                    priority = PRIORITY_HIGH_ACCURACY //accurate location results
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback) // tracking stopped
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result?.locations?.let { locations -> //if not null
                    for(location in locations) {
                        addPathPoint(location) //add location to last path
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }
    private fun addEmptyPath() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    }?: pathPoints.postValue(mutableListOf(mutableListOf())) //if null

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true) //cant be swipped away
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("RunApp")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build()) //foreground service
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also { // go to Main Activity on click notification
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW      // as notification updates frequently
        )
        notificationManager.createNotificationChannel(channel)
    }
}