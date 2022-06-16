package com.mstar004.sensor

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import com.mstar004.qiblafinder.R

/**
 * class that gets the location
 */
class LocationFinder(private val mContext: Context) : Service(), LocationListener {

	// for GPS status
	var isGPSEnabled = false

	// for network status
	var isNetworkEnabled = false

	// for GPS status
	var canGetLocation = false

	var location: Location? = null
	var latitude: Double? = null
	var longitude: Double? = null

	companion object {
		// The minimum distance to change Updates in meters
		private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 100 // 10 meters

		// The minimum time between updates in milliseconds
		private const val MIN_TIME_BW_UPDATES: Long = 60000 // 1 minute
	}

	init {
		getLocation()
	}

	// Declaring a Location Manager
	private var locationManager: LocationManager? = null

	@SuppressLint("MissingPermission")
	@JvmName("getTheLocation")
	fun getLocation(): Location? {
		try {
			locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

			// getting GPS status
			isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
			// getting network status
			isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				canGetLocation = true
				if (isNetworkEnabled) {
					locationManager!!.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						MIN_TIME_BW_UPDATES,
						MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
						this
					)
					Log.d("Network", "Network")
					if (locationManager != null) {
						location =
							locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
						if (location != null) {
							latitude = location!!.latitude
							longitude = location!!.longitude
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager!!.requestLocationUpdates(
							LocationManager.GPS_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
							this
						)
						Log.d("GPS Enabled", "GPS Enabled")
						if (locationManager != null) {
							location =
								locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
							if (location != null) {
								latitude = location!!.latitude
								longitude = location!!.longitude
							}
						}
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return location
	}

	/**
	 * Stop using GPS listener
	 * Calling this function will stop using GPS in your app
	 */
	@SuppressLint("MissingPermission")
	fun stopUsingGPS() {
		if (locationManager != null) {
			locationManager!!.removeUpdates(this@LocationFinder)
		}
	}

	/**
	 * Function that gets the latitude
	 */
	@JvmName("getTheLatitude")
	fun getLatitude(): Double {
		if (location != null) {
			latitude = location!!.latitude
		}
		// return latitude
		return latitude!!
	}

	/**
	 * Function that gets the longitude
	 */
	@JvmName("getTheLongitude")
	fun getLongitude(): Double {
		if (location != null) {
			longitude = location!!.longitude
		}
		// return longitude
		return longitude!!
	}

	/**
	 * Function to check GPS/wifi enabled
	 *
	 * @return boolean
	 */
	fun canGetLocation(): Boolean {
		return canGetLocation
	}

	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will launch Settings Options
	 */
	fun showSettingsAlert() {
		val alertDialog = AlertDialog.Builder(mContext)
		// Setting Dialog Title
		alertDialog.setTitle(mContext.resources.getString(R.string.gps_settings_title))
		// Setting Dialog Message
		alertDialog.setMessage(mContext.resources.getString(R.string.gps_settings_text))
		// On pressing Settings button
		alertDialog.setPositiveButton(mContext.resources.getString(R.string.settings_button_ok)) { dialog, which ->
			val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
			mContext.startActivity(intent)
		}
		// on pressing cancel button
		alertDialog.setNegativeButton(mContext.resources.getString(R.string.settings_button_cancel)) { dialog, which -> dialog.cancel() }
		// Showing Alert Message
		alertDialog.show()
	}

	override fun onLocationChanged(location: Location) {
// TODO Auto-generated method stub
	}

	override fun onProviderDisabled(provider: String) {
// TODO Auto-generated method stub
	}

	override fun onProviderEnabled(provider: String) {
// TODO Auto-generated method stub
	}

	override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
// TODO Auto-generated method stub
	}

	override fun onBind(intent: Intent): IBinder? {
// TODO Auto-generated method stub
		return null
	}

}