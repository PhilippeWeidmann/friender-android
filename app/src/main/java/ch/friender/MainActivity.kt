package ch.friender


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import ch.friender.networking.ApiFetcher
import ch.friender.persistence.LocationManager
import ch.friender.service.LocationService
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : FragmentActivity() {

    private lateinit var intentLocation: Intent
    var firstLaunch = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ApiFetcher.initWithContext(this)
        firstLaunch = true
        //foreground permission
        //TODO permissions

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)
        bottomNavigationView.setupWithNavController(navController)

        val sharedPreferences = getSharedPreferences("id", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (sharedPreferences.getString("id", "") == "") {
            ApiFetcher.registerUser { userId, error ->
                Log.d("new id", "NOK")
                userId?.let {
                    Log.d("new id", "" + userId)
                    editor.putString("id", it)
                    editor.apply()
                }

                error?.let {
                    Log.e("error on getting id", error.toString())
                }
            }
        } else {
            Log.d("already an id", "" + sharedPreferences.getString("id", ""))
        }

        if (checkPermission()) {
            LocationManager.initWithContext(this)
            intentLocation = Intent(this, LocationService::class.java)
            startService(intentLocation)
        }


    }

    fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                123
            )


            return false
        } else {
            return true
        }
    }

    override fun onResume() {
        LocationManager.startUpdatingLocation()
        super.onResume()
    }

    override fun onPause() {
        LocationManager.stopUpdatingLocation()
        super.onPause()
    }

    override fun onDestroy() {
        stopService(intentLocation)
        super.onDestroy()
    }


}