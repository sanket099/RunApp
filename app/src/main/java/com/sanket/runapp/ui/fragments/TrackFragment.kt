package com.sanket.runapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.sanket.runapp.R
import com.sanket.runapp.other.Constants.ACTION_PAUSE_SERVICE
import com.sanket.runapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.sanket.runapp.other.Constants.MAP_ZOOM
import com.sanket.runapp.other.Constants.PATH_COLOR
import com.sanket.runapp.other.Constants.Path_WIDTH
import com.sanket.runapp.services.Path
import com.sanket.runapp.services.Paths
import com.sanket.runapp.services.TrackService
import com.sanket.runapp.ui.view_models.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Path>()

    private var map: GoogleMap? = null //actual map object

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState) // map lifecycle

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        mapView.getMapAsync{
            map = it
            addAllPaths() //only when frag is created
        }
        subscribeToObserver()

    }

    private fun subscribeToObserver(){
        TrackService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPath()
            moveCameraToUser()
        })
    }

    private fun toggleRun(){
        if(isTracking){
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }
        else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking : Boolean){
        this.isTracking = isTracking
        if(!isTracking){
            //paused state
            btnToggleRun.text = "start"
            btnFinishRun.visibility = View.VISIBLE
        }
        else{
            btnToggleRun.text = "stop"
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser(){
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }


    private fun addAllPaths(){ // if device rotated or data lost

        for(path in pathPoints){
            val pathOptions = PolylineOptions()
                .color(PATH_COLOR)
                .width(Path_WIDTH)
                .addAll(path)
            map?.addPolyline(pathOptions)
        }

    }

    private fun addLatestPath(){ //draw path
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val secondLastCoord = pathPoints.last()[pathPoints.last().size - 2]
            val lastCoord = pathPoints.last().last()
            val pathOptions = PolylineOptions()
                .color(PATH_COLOR)
                .width(Path_WIDTH)
                .add(secondLastCoord)
                .add(lastCoord)
            map?.addPolyline(pathOptions)
        }
    }

    private fun sendCommandToService(action: String) =
            Intent(requireContext(), TrackService::class.java).also {
                it.action = action
                requireContext().startService(it)
            }


    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //cache map
        mapView?.onSaveInstanceState(outState)
    }

   /* override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }*/
}