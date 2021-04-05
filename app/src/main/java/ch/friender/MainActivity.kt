package ch.friender


import android.os.Bundle
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import ch.friender.networking.ApiFetcher
import ch.friender.persistence.LocationManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import im.delight.android.location.SimpleLocation

class MainActivity : FragmentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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