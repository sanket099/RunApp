package com.sanket.runapp.other

import android.content.Context


import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.sanket.runapp.db.Run
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*


class CustomMarkerView(
    val runs: List<Run>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId) {

    override fun getOffset(): MPPointF { //position
        return MPPointF(-width / 2f, -height.toFloat()) //so that it doesnt get cut off
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e == null) {
            return
        }
        val curRunId = e.x.toInt()
        val run = runs[curRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text = "Time : ${dateFormat.format(calendar.time)}"

        val avgSpeed = "Speed : ${run.avgSpeedInKMH}km/h"
        tvAvgSpeed.text = avgSpeed

        val distanceInKm = "Distance : ${run.distanceInMeters / 1000f}km"
        tvDistance.text = distanceInKm

        tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned = "Calories Burned : ${run.caloriesBurned}kcal"
        tvCaloriesBurned.text = caloriesBurned
    }
}