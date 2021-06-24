package com.sanket.runapp.ui

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.sanket.runapp.BuildConfig
import com.sanket.runapp.R
import com.sanket.runapp.db.Run
import com.sanket.runapp.other.Constants
import com.sanket.runapp.other.Constants.RUN_ID
import com.sanket.runapp.other.Constants.RUN_IMAGE
import com.sanket.runapp.other.Constants.RUN_NAME
import com.sanket.runapp.other.Constants.RUN_SPEED
import com.sanket.runapp.other.Constants.RUN_TIMEINMILLIS
import com.sanket.runapp.other.TrackingUtility
import com.sanket.runapp.ui.fragments.CancelDialogFragment
import com.sanket.runapp.ui.view_models.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_zoom.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class ZoomActivity : AppCompatActivity() {

    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1.0f
    private var shareBitmap : Bitmap? = null

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom)

        //val bundle = intent.extras;

        //val run = bundle?.getParcelable<Run>(RUN_OBJECT) as Run

        val id = intent.getIntExtra(RUN_ID, -1)
        lifecycleScope.launch {
            val run = viewModel.getRunById(id)
            getData(run)

        }

        ivShare.setOnClickListener {
            if(shareBitmap != null)
                shareRec(getLocalBitmapUri(shareBitmap!!))
        }

        ivDelete.setOnClickListener {
            Timber.d("ID : $id")
                if(id!=-1){
                    showCancelTrackingDialog()

                }
        }

    }

    private fun getData(run: Run) {

        val img = run.img
        val name = run.runName
        val speed = run.avgSpeedInKMH
        val dist = run.distanceInMeters
        val time =  run.timeInMillis


        val bmp  = img
        Glide.with(this).load(bmp).into(ivZoomImg)

        //tvZoomRun.text = run.runName
        tvZoomRun.text = name

        mScaleGestureDetector = ScaleGestureDetector(this, scaleListener)
        shareBitmap = drawTextToBitmap(this, bmp!!, dist.toString(), speed.toString(), TrackingUtility.getFormattedStopWatchTime(time).toString())

        Glide.with(this).load(shareBitmap).into(ivZoomImg)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        return mScaleGestureDetector!!.onTouchEvent(event)
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor

            // Don't let the object get too small or too large.
            mScaleFactor = max(1.0f, min(mScaleFactor, 5.0f))



          //  mScaleFactor *= scaleGestureDetector.scaleFactor
            ivZoomImg.scaleX = mScaleFactor
            ivZoomImg.scaleY = mScaleFactor

           // invalidate()
            return true
        }
    }

    private fun drawTextToBitmap(mContext: Context, originalBitmap: Bitmap, distance: String, speed: String, time: String): Bitmap? {
        return try {
            val resources: Resources = mContext.resources
            val scale: Float = resources.displayMetrics.density
            var bitmap = originalBitmap
            var bitmapConfig = bitmap.config
            // set default bitmap config if none
            if (bitmapConfig == null) {
                bitmapConfig = Bitmap.Config.ARGB_8888
            }
            // resource bitmaps are immutable,
            // so we need to convert it to mutable one
            bitmap = bitmap.copy(bitmapConfig, true)
            val canvas = Canvas(bitmap)
            // new antialised Paint
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            // text color - #3D3D3D
            paint.color = Color.rgb(0, 0, 0)
            // text size in pixels
            paint.textSize = (18 * scale).toInt().toFloat()
            // text shadow
            paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY)

            // draw text to the Canvas center

            val bounds = Rect()
            val plain = ResourcesCompat.getFont(this, R.font.odibee)
            paint.typeface = plain
            paint.getTextBounds(distance, 0, distance.length, bounds)
            val x: Float = 64f
            val y: Float = (bitmap.height - 120).toFloat()

            val x2: Float = canvas.width / 2f - 120f

            val x3: Float = canvas.width - 240f


            val paintHeading = Paint(Paint.ANTI_ALIAS_FLAG)
            paintHeading.color = Color.rgb(0, 0, 0)
            paintHeading.textSize = (24 * scale).toInt().toFloat()
            // text shadow
            paintHeading.setShadowLayer(1f, 0f, 1f, Color.DKGRAY)
            val plainHeading = ResourcesCompat.getFont(this, R.font.opensansregular_semibold)
            paintHeading.typeface = plainHeading


            canvas.drawText("$distance KM", x, y, paint)
            canvas.drawText("$speed KM/H", x2, y, paint)
            canvas.drawText("$time ", x3, y, paint)
            canvas.drawText("Persist", 64f, (64f +paint.textSize), paintHeading)

            bitmap
        } catch (e: Exception) {
                Timber.d("error ${e.message}")
            null
        }
    }

    private fun getLocalBitmapUri(resource : Bitmap): Uri? {
        // Extract Bitmap from ImageView drawable
        val bmpUri : Uri
        val file: File = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png")
        val out = FileOutputStream(file)
        resource.compress(Bitmap.CompressFormat.PNG, 90, out)
        out.close()
        bmpUri = FileProvider.getUriForFile(this@ZoomActivity, BuildConfig.APPLICATION_ID.toString() + ".provider", file)
        return bmpUri
    }

   private fun shareRec(uri_share: Uri?){
        // showProgress(true);
        //.println("share_img = " + share_img.
        // trim());
        // uri_share = getLocalBitmapUri();
        if(uri_share != null){
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "image/jpeg"
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri_share)
            shareIntent.putExtra(Intent.EXTRA_TEXT,"Persist App")

            shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            // Launch sharing dialog for image

            startActivity(Intent.createChooser(shareIntent, "Share Run"))
        }
       else{
           Timber.d("Error : uri null")
        }


   }

    private fun showCancelTrackingDialog(){
        CancelDialogFragment().apply {
            setPositiveListener {
                viewModel.deleteRun(id)
                finish()
            }
        }.show(supportFragmentManager, Constants.CANCEL_DIALOG_TAG)
    }

    fun getStringImage(bmp: Bitmap?): String? {
        val baos = ByteArrayOutputStream()
        bmp?.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes: ByteArray = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }








}