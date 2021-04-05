package ch.friender


import android.os.Bundle
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import ch.friender.networking.ApiFetcher
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import im.delight.android.location.SimpleLocation

class MainActivity : FragmentActivity() {

    private var location: SimpleLocation? = null
    var lat: Double = 0.0
    var long: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ApiFetcher.initWithContext(this)

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

        //get location
        location = SimpleLocation(this, true, false, 1000, true);
        if (!location!!.hasLocationEnabled()) {
            Log.d("here", "here1")
            SimpleLocation.openSettings(this);
        }
        location?.setListener {
            if (lat != location!!.latitude) {
                lat = location!!.latitude
                Log.d("new lat", "" + location!!.latitude)

            } else if (long != location!!.longitude) {
                long = location!!.longitude
                Log.d("new long", "" + location!!.longitude)
            }

        }
    }

    override fun onResume() {
        location?.beginUpdates()
        location?.let { location ->
            lat = location.latitude
            long = location.longitude
        }
        super.onResume()

    }

    override fun onPause() {
        location?.endUpdates()
        location?.let { location ->
            lat = location.latitude
            long = location.longitude
        }
        super.onPause()

    }

}