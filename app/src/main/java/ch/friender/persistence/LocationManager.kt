package ch.friender.persistence

import android.content.Context
import im.delight.android.location.SimpleLocation

object LocationManager {

    private var locationManager: SimpleLocation? = null

    var currentLatitude: Double = 0.0
    var currentLongitude: Double = 0.0

    fun initWithContext(context: Context) {
        locationManager = SimpleLocation(context, true, false, 1000, true)
        if (!locationManager!!.hasLocationEnabled()) {
            SimpleLocation.openSettings(context);
        }
        locationManager!!.setListener {
            currentLatitude = locationManager!!.latitude
            currentLongitude = locationManager!!.longitude
        }
    }

    fun startUpdatingLocation() {
        locationManager?.beginUpdates()
    }

    fun stopUpdatingLocation() {
        locationManager?.endUpdates()
    }
}