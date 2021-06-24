package com.sanket.runapp.repositories

import com.sanket.runapp.db.Run
import com.sanket.runapp.db.RunDao
import javax.inject.Inject

class MainRepo  @Inject constructor(

    val runDao: RunDao

){ //to connect dao to view models

    suspend fun insertRun(run : Run) = runDao.insertRun(run)

    suspend fun deleteRun(id: Int) = runDao.deleteRun(id)

    suspend  fun getRunById(id: Int) : Run = runDao.getRunById(id)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate() //no suspend as live data is async by default

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()
}