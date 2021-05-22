package com.sanket.runapp.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sanket.runapp.R
import com.sanket.runapp.other.Constants.REQUEST_CODE_LOCATION
import com.sanket.runapp.other.TrackingUtility
import com.sanket.runapp.ui.view_models.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) , EasyPermissions.PermissionCallbacks{

    private val viewModel: MainViewModel by viewModels() //view model injection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requestPermissions()
        super.onViewCreated(view, savedInstanceState)

        fab.setOnClickListener{
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun requestPermissions(){ //requirecontext ensures non null
        if(TrackingUtility.hasLocPermissions(requireContext())){
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) { //if user permanently denied permissions
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult( //callback
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this) //only this fragment receives the permission result

        //Easy Permissions is wonderful!
    }
}