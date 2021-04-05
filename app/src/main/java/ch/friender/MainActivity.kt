package ch.friender


import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import ch.friender.networking.ApiFetcher
import ch.friender.persistence.LocationManager
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : FragmentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //foreground permission
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1234)
        ApiFetcher.initWithContext(this)
        LocationManager.initWithContext(this)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)
        bottomNavigationView.setupWithNavController(navController)

        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (sharedPreferences.getInt("id", -1) == -1) {
            ApiFetcher.registerUser { userId, error ->

                userId?.let {
                    editor.putInt("id", it)
                    editor.apply()
                }

                error?.let {
                    Log.e("error on getting id", error.toString())
                }
            }
        } else {
            Log.d("already an id", "" + sharedPreferences.getInt("id", -1))
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

}