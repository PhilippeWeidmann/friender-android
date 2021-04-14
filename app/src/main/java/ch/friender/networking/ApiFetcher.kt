package ch.friender.networking

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

object ApiFetcher {

    private const val baseUrl = "http://10.0.2.2:1337/"

    lateinit var requestQueue: RequestQueue

    fun initWithContext(context: Context) {
        requestQueue = Volley.newRequestQueue(context)
    }

    fun registerUser(completion: (String?, VolleyError?) -> Unit) {
        val registerUrl = baseUrl + "user/register"
        val request = JsonObjectRequest(Request.Method.POST, registerUrl, null, { res ->
            completion(res.getJSONObject("data").getString("id"), null)
        }, { error ->
            completion(null, error)
        })
        requestQueue.add(request)
    }

}