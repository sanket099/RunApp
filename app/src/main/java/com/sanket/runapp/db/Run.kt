package com.sanket.runapp.db

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "RUNTABLE")
data class Run ( //constructor

    var img: Bitmap? = null,
    var timestamp: Long = 0L, //date into milliseconds //used for sorting //when our run was
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L, //how long the run was
    var caloriesBurned: Int = 0,
    var runName: String? = ""
    ) : Parcelable {
        @PrimaryKey(autoGenerate = true) //not in constructor
        var id: Int? = null

    constructor(parcel: Parcel) : this(
            parcel.readParcelable(Bitmap::class.java.classLoader),
            parcel.readLong(),
            parcel.readFloat(),
            parcel.readInt(),
            parcel.readLong(),
            parcel.readInt(),
            parcel.readString()) {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(img, flags)
        parcel.writeLong(timestamp)
        parcel.writeFloat(avgSpeedInKMH)
        parcel.writeInt(distanceInMeters)
        parcel.writeLong(timeInMillis)
        parcel.writeInt(caloriesBurned)
        parcel.writeString(runName)
        parcel.writeValue(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Run> {
        override fun createFromParcel(parcel: Parcel): Run {
            return Run(parcel)
        }

        override fun newArray(size: Int): Array<Run?> {
            return arrayOfNulls(size)
        }
    }
}