package com.sanket.runapp.ui.fragments

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanket.runapp.R
import com.sanket.runapp.adapters.RunAdapter
import com.sanket.runapp.other.Constants.REQUEST_CODE_LOCATION
import com.sanket.runapp.other.Constants.RUN_DIST
import com.sanket.runapp.other.Constants.RUN_ID
import com.sanket.runapp.other.Constants.RUN_IMAGE
import com.sanket.runapp.other.Constants.RUN_NAME
import com.sanket.runapp.other.Constants.RUN_OBJECT
import com.sanket.runapp.other.Constants.RUN_SPEED
import com.sanket.runapp.other.Constants.RUN_TIMEINMILLIS
import com.sanket.runapp.other.SortType
import com.sanket.runapp.other.TrackingUtility
import com.sanket.runapp.ui.ZoomActivity
import com.sanket.runapp.ui.view_models.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_zoom.*
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min


@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) , EasyPermissions.PermissionCallbacks{

    private lateinit var runAdapter: RunAdapter

    private val viewModel: MainViewModel by viewModels() //view model injection


    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1.0f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
        setUpRecyclerView()



        mScaleGestureDetector = ScaleGestureDetector(context, scaleListener)

        when(viewModel.sortType) { //sorting runs
            SortType.DATE -> spFilter.setSelection(0) //indices from strings array
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES)
                }
            }
        }

        viewModel.runs.observe(viewLifecycleOwner, Observer {

            if(it==null || it.isEmpty()){
                tvEmpty.isVisible = true
                rvRuns.isVisible = false
                tvFilterBy.isVisible = false
                spFilter.isVisible = false
            }
            else{
                tvEmpty.isVisible = false
                rvRuns.isVisible = true
                tvFilterBy.isVisible = true
                spFilter.isVisible = true

                runAdapter.submitList(it)

            }


        }) //sorting runs


        fab.setOnClickListener{
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

        runAdapter.onArticleClicked = { it ->
           // val bundle = Bundle()
           // bundle.putParcelable("RUN_OBJECT", it)

           // startActivity(Intent(context, ZoomActivity::class.java).putExtras(bundle))

            startActivity(Intent(context, ZoomActivity::class.java)
                  //  .putExtra(RUN_IMAGE, getStringImage(it.img))
                  //  .putExtra(RUN_NAME, it.runName.toString())
                  //  .putExtra(RUN_TIMEINMILLIS, TrackingUtility.getFormattedStopWatchTime(it.timeInMillis).toString())
                 //   .putExtra(RUN_SPEED, it.avgSpeedInKMH.toString())
                    .putExtra(RUN_ID, it.id)
                 //   .putExtra(RUN_DIST, it.distanceInMeters.toString())
            )
        }
    }

    private fun setUpRecyclerView() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())

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

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        //turnGPSOn()
    }

    override fun onRequestPermissionsResult( //callback
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this) //only this fragment receives the permission result

        //Easy Permissions is wonderful!
    }



    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor

            // Don't let the object get too small or too large.
            mScaleFactor = max(0.1f, min(mScaleFactor, 5.0f))

            //  mScaleFactor *= scaleGestureDetector.scaleFactor
            ivZoomImg.scaleX = mScaleFactor
            ivZoomImg.scaleY = mScaleFactor

            // invalidate()
            return true
        }
    }

    //For encoding toString
    fun getStringImage(bmp: Bitmap?): String? {
        val baos = ByteArrayOutputStream()
        bmp?.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes: ByteArray = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    //For decoding


   // getActivity().getContentResolver().delete(uri, null, null);

    /*private fun turnGPSOn() {
        val provider: String = Settings.Secure.getString(context?.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        if (!provider.contains("gps")) { //if gps is disabled
            val poke = Intent()
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider")
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.data = Uri.parse("3")
            context?.sendBroadcast(poke)

        }
    }*/




}