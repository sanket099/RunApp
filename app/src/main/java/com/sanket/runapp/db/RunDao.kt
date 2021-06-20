package com.sanket.runapp.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)


    @Query("DELETE from RUNTABLE where id = :deleteId")
    suspend fun deleteRun(deleteId : Int)
    //Sorting :  DESC most recent on top

    @Query("SELECT * FROM RUNTABLE ORDER BY timestamp DESC") //sorting by date of run
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM RUNTABLE ORDER BY timeInMillis DESC") //sorting by duration of run
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM RUNTABLE ORDER BY caloriesBurned DESC") //sorting by calories burned
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM RUNTABLE ORDER BY avgSpeedInKMH DESC") //sorting by avg speed
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM RUNTABLE ORDER BY distanceInMeters DESC") //sorting by dist covered
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    //Total values for stats

    @Query("SELECT SUM(timeInMillis) FROM RUNTABLE")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) FROM RUNTABLE")
    fun getTotalCaloriesBurned(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) FROM RUNTABLE")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKMH) FROM RUNTABLE")
    fun getTotalAvgSpeed(): LiveData<Float>

}

