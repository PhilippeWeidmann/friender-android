package ch.friender.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import ch.friender.MainActivity
import ch.friender.R
import ch.friender.persistence.LocationManager


open class LocationService:Service() {
    val CHANNEL_ID = "ForegroundServiceChannel"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("service", "starting")
        LocationManager.initWithContext(this)
        LocationManager.startUpdatingLocation()
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Using your GPS...")
                .setSmallIcon(R.drawable.explore)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent):IBinder? {
        return null
    }

    private  fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        Log.d("service", "exiting")
        LocationManager.initWithContext(this)
        LocationManager.stopUpdatingLocation()
        super.onDestroy()
    }


}