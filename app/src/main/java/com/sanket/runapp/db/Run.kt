package com.sanket.runapp.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "RUNTABLE")
data class Run ( //constructor

    var img: Bitmap? = null,
    var timestamp: Long = 0L, //date into milliseconds //used for sorting //when our run was
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L, //how long the run was
    var caloriesBurned: Int = 0,
    var runName: String = ""
    ) {
        @PrimaryKey(autoGenerate = true) //not in constructor
        var id: Int? = null
    }