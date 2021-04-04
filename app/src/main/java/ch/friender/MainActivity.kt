package ch.friender


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import im.delight.android.location.SimpleLocation

class MainActivity : FragmentActivity() {

    private var location: SimpleLocation? = null
    var lat:Double = 0.0
    var long:Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)
        bottomNavigationView.setupWithNavController(navController)

        //get ID
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val url = "http://10.0.2.2:1337/user/register"
        val requestQueue = Volley.newRequestQueue(this)
        if(sharedPreferences.getString("id", "")?.isEmpty() == true){
            val getId = JsonObjectRequest(Request.Method.POST, url, null, { res ->
                val id = res.get("data").toString()
                Log.d("ID", id)
                editor.putString("id", id)
                editor.apply()
            }, { error ->
                Log.e("error on getting id", error.toString())
            })
            requestQueue.add(getId)
        }
        else{
            Log.d("already an id", "" + sharedPreferences.getString("id", ""))
        }

        //get location
         location =  SimpleLocation(this, true,false,1000,true);
        if (!location!!.hasLocationEnabled()) {
            Log.d("here","here1")
            SimpleLocation.openSettings(this);
        }
        location!!.setListener {
            if (lat!=location!!.latitude){
                lat = location!!.latitude
                Log.d("new lat",""+location!!.latitude)

            }
            else if(long!=location!!.longitude){
                long = location!!.longitude
                Log.d("new long",""+ location!!.longitude)
            }

        }
    }

    override fun onResume(){
        Log.d("here","here3")
        location?.beginUpdates()
        lat = location!!.latitude
        long = location!!.longitude
        super.onResume()

    }
    override fun onPause() {
        Log.d("here","here4")
        location!!.endUpdates()
        lat = location!!.latitude
        long = location!!.longitude
        super.onPause()

    }


}