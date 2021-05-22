package com.sanket.runapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.sanket.runapp.R
import com.sanket.runapp.ui.view_models.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null //actual map object

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState) // map lifecycle

        mapView.getMapAsync{
            map = it
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