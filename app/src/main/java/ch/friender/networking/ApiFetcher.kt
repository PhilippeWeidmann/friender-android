package ch.friender.networking

import android.content.Context
import android.util.Log
import ch.friender.Friend
import ch.friender.cryptography.CryptoManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonParseException
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import org.json.JSONException
import org.json.JSONObject

object ApiFetcher {

    private const val baseUrl = "http://192.168.1.129:1337/"
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

    fun getLocation(friend: Friend, userId: String, completion: (String?, VolleyError?) -> Unit) {
        val locationURL = baseUrl + "location/" + friend.id + "/" + userId
        val request = StringRequest(Request.Method.GET, locationURL,
            { res ->
                try {
                    val location =
                        JSONObject(res).getJSONObject("data").getString("encryptedLocation")
                    if (location.isNotEmpty()) {
                        val decodedLocation = CryptoManager.decrypt(
                            location,
                            Key.fromHexString(friend.friendPublicKey),
                            Key.fromHexString(friend.myPrivateKey)
                        )
                        completion(decodedLocation, null)
                    }
                    completion("", null)
                } catch (e: JSONException) {
                    Log.d("error", e.toString())
                    completion("", null)
                }
            },
            { completion(null, it) })
        requestQueue.add(request)
    }

    fun sendLocation(
        friend: Friend,
        userId: String,
        location: String,
        completion: (String?, VolleyError?) -> Unit
    ) {
        val sendLocationURL = baseUrl + "location/" + userId + "/" + friend.id
        val encodedLocation = CryptoManager.encrypt(
            location,
            Key.fromHexString(friend.friendPublicKey),
            Key.fromHexString(friend.myPrivateKey)
        )
        if (encodedLocation != "") {
            val request = JsonObjectRequest(Request.Method.POST,
                sendLocationURL,
                JSONObject("{\"encryptedLocation\":$encodedLocation}"),
                { res ->
                    completion(res.getString("status"), null)
                },
                { completion(null, it) })
            requestQueue.add(request)
        }
    }
}