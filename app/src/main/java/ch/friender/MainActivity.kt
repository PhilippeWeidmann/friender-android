package ch.friender

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)
        bottomNavigationView.setupWithNavController(navController)

        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val url = "http://10.0.2.2:1337/user/register"
        val requestQueue = Volley.newRequestQueue(this)
        if(sharedPreferences.getString("id","")?.isEmpty() == true){
            val getId = JsonObjectRequest(Request.Method.POST,url,null, { res ->
                val id = res.get("data").toString()
                Log.d("ID",id)
                editor.putString("id",id)
                editor.apply()
            }, { error ->
                Log.e("error on getting id", error.toString())
            })
            requestQueue.add(getId)
        }
        else{
            Log.d("already an id",""+sharedPreferences.getString("id",""))
        }

    }
}