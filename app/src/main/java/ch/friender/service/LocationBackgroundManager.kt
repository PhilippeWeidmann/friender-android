package ch.friender.service

import android.app.*
import android.content.Intent

import android.os.IBinder
import android.util.Log



open class LocationService:Service() {

    override fun onStart(intent:Intent, startId:Int) {
        ch.friender.persistence.LocationManager.startUpdatingLocation()
    }

    override fun onBind(intent:Intent):IBinder? {
        return null
    }


    override fun onDestroy() {
        ch.friender.persistence.LocationManager.stopUpdatingLocation()
        super.onDestroy()
    }


}