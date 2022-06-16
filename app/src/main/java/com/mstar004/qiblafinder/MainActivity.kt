package com.mstar004.qiblafinder

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.mstar004.qiblafinder.databinding.ActivityMainBinding
import com.mstar004.sensor.Domain
import com.mstar004.sensor.LocationFinder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : AppCompatActivity() {

    var locationFinder: LocationFinder? = null
    private var domain: Domain? = null
    private var currentAzimuth = 0f
    var prefs: SharedPreferences? = null

    companion object {
        lateinit var binding: ActivityMainBinding
        private const val KAABA_LONG = 39.82573393122944
        private const val KAABA_LAT = 21.422896491347117
        private const val PERMISSION_REQUEST_CODE = 201
        private const val PREFS_KEY_QIBLA_DEGREES = "qibla_degrees"
        private const val PREFS_KEY_PERMISSION_GRANTED = "permission_granted"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        prefs = getSharedPreferences("", Context.MODE_PRIVATE)
        setupQibla()
    }

    override fun onStart() {
        super.onStart()
        if (domain != null) {
            domain!!.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (domain != null) {
            domain!!.stop()
        }
    }

    override fun onResume() {
        super.onResume()
        if (domain != null) {
            domain!!.start()
        }
    }

    override fun onStop() {
        super.onStop()
        if (domain != null) {
            domain!!.stop()
        }
    }

    private fun setupQibla() {
        val permissionGranted = getPermissionFromPrefs(PREFS_KEY_PERMISSION_GRANTED)
        if (permissionGranted) {
            getQiblaLocation()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
        domain = Domain(this)
        val cl: Domain.CompassListener = object : Domain.CompassListener {
            override fun onNewAzimuth(azimuth: Float) {
                animateQiblArrow(azimuth)
            }
        }
        domain!!.setListener(cl)
    }

    fun animateQiblArrow(azimuth: Float) {
        val qiblaDegrees = getQiblaDegreesFromPrefs(PREFS_KEY_QIBLA_DEGREES)
        val an: Animation = RotateAnimation(
            -currentAzimuth + qiblaDegrees, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        currentAzimuth = azimuth
        an.duration = 500
        an.repeatCount = 0
        an.fillAfter = true
        binding.qiblaImageview.startAnimation(an)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // permission was granted, yay! Do the
                    savePermissionToPrefs(PREFS_KEY_PERMISSION_GRANTED, true)
                    getQiblaLocation()
                } else {
                    Toast.makeText(
                        applicationContext,
                        resources.getString(R.string.toast_permission_required),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                return
            }
        }
    }

    private fun savePermissionToPrefs(key: String?, value: Boolean?) {
        val edit = prefs!!.edit()
        edit.putBoolean(key, value!!)
        edit.apply()
    }

    private fun getPermissionFromPrefs(key: String?): Boolean {
        return prefs!!.getBoolean(key, false)
    }

    private fun saveQiblaDegreesToPrefs(key: String?, value: Float?) {
        val edit = prefs!!.edit()
        edit.putFloat(key, value!!)
        edit.apply()
    }

    private fun getQiblaDegreesFromPrefs(key: String?): Float {
        return prefs!!.getFloat(key, 0f)
    }

    private fun getQiblaLocation() {
        var result = 0.0
        locationFinder = LocationFinder(this)
        if (locationFinder!!.canGetLocation()) {
            val deviceLatitude = locationFinder!!.getLatitude()
            val deviceLongitude = locationFinder!!.getLongitude()
            val kaabaLatRadians = Math.toRadians(KAABA_LAT)
            val deviceLatRadians = Math.toRadians(deviceLatitude)
            val longDiff = Math.toRadians(KAABA_LONG - deviceLongitude)
            val y = sin(longDiff) * cos(kaabaLatRadians)
            val x = cos(deviceLatRadians) * sin(kaabaLatRadians) -
                    sin(deviceLatRadians) * cos(kaabaLatRadians) * cos(longDiff)
            result = ((Math.toDegrees(atan2(y, x)) + 360) % 360)
            saveQiblaDegreesToPrefs(PREFS_KEY_QIBLA_DEGREES, result.toFloat())
        } else {
            // can't get location || GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            locationFinder!!.showSettingsAlert()
        }
    }


}