package com.sanket.runapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sanket.runapp.R
import com.sanket.runapp.db.Run
import com.sanket.runapp.other.Constants.ACTION_PAUSE_SERVICE
import com.sanket.runapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.sanket.runapp.other.Constants.ACTION_STOP_SERVICE
import com.sanket.runapp.other.Constants.MAP_ZOOM
import com.sanket.runapp.other.Constants.PATH_COLOR
import com.sanket.runapp.other.Constants.Path_WIDTH
import com.sanket.runapp.other.TrackingUtility
import com.sanket.runapp.services.Path
import com.sanket.runapp.services.Paths
import com.sanket.runapp.services.TrackService
import com.sanket.runapp.ui.view_models.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.coroutines.currentCoroutineContext
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Path>()

    private var map: GoogleMap? = null //actual map object

    private var curTimeInMillis = 0L

    private var menu : Menu? = null

    @set:Inject //primitive data type
    var userWeight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true) //
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState) // map lifecycle

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        btnFinishRun.setOnClickListener {
            zoomToShowTrack()
            endRunSaveToDb()
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

        TrackService.timeRunMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            tvTimer.text = formattedTime
        })
    }

    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }
        else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        if(curTimeInMillis > 0L){ //runnning
            this.menu?.getItem(0)?.isVisible = true //cancel option visible
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.cancel_tracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    private fun showCancelTrackingDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel Run ?")
            .setMessage("Are you sure")
            .setIcon(R.drawable.ic_launcher_foreground)
            .setPositiveButton("Yes"){ _, _ ->
                stopRun()
            }
            .setNegativeButton("Yes"){ dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
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
            menu?.getItem(0)?.isVisible = true
        }
    }

    private fun moveCameraToUser(){ //zoom
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

    private fun zoomToShowTrack(){ //on saving screenshot
        val bounds = LatLngBounds.builder()
        for(paths in pathPoints){

            for(pos in paths){
                bounds.include(pos)
            }

            map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds.build(),
                    mapView.width,
                    mapView.height,
                    (mapView.height * 0.05).toInt() //padding
                )
            )

        }

    }

    private fun endRunSaveToDb(){ //saving run
        map?.snapshot { bmp -> //gives bitmap
            var distanceInMeters = 0
            for(paths in pathPoints){
                distanceInMeters += TrackingUtility.calculatePathLength(paths).toInt()
            }

            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10 ) / 10f //kmph //one decimal place
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * userWeight).toInt()

            val run = Run(bmp, dateTimeStamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)

            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView), //activity view
                "Run Saved",
                Snackbar.LENGTH_LONG
            ).show()

            stopRun()

        }
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