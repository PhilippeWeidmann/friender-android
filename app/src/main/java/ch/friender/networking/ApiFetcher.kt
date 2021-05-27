package ch.friender.networking

import android.content.Context
import android.util.Log
import ch.friender.Friend
import ch.friender.cryptography.CryptoManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.goterl.lazysodium.utils.Key
import org.json.JSONArray
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

    fun getLocations(friends: ArrayList<Friend>, userId: String, completion: (ArrayList<String>?, VolleyError?) -> Unit) {
        val locationsUrl = baseUrl + "user/" + userId + "/location/get"
        val idsJson = JSONArray()
        for (friend in friends) {
            idsJson.put(friend.id)
        }
        val jsonToSend = JSONObject().put("senders", idsJson)
        val request = JsonObjectRequest(Request.Method.POST, locationsUrl, jsonToSend, { res ->
            try {
                val locationsArray = res.getJSONArray("data")
                val decodedLocations = ArrayList<String>()
                var currentFriend: Friend? = null
                for (i in 0 until locationsArray.length()) {
                    for (friend in friends) {
                        if (friend.id == locationsArray.getJSONObject(i).getString("sender")) {
                            currentFriend = friend
                        }
                    }
                    currentFriend?.let {
                        val decodedLocation = CryptoManager.decrypt(
                            locationsArray.getJSONObject(i).getString("encryptedLocation"),
                            Key.fromHexString(it.friendPublicKey),
                            Key.fromHexString(it.myPrivateKey)
                        )
                        decodedLocations.add(decodedLocation)
                    }
                }
                completion(decodedLocations, null)
            } catch (e: JSONException) {
                Log.d("error", e.toString())
                completion(ArrayList(), null)
            }
        }, {
            completion(null, it)
        })
        requestQueue.add(request)
    }

    fun sendLocation(friends: ArrayList<Friend>, userId: String, location: String, completion: (String?, VolleyError?) -> Unit) {
        val sendLocationURL = baseUrl + "user/" + userId + "/location/send"
        val idsJson = JSONArray()
        val encryptedLocationsJson = JSONArray()
        for (friend in friends) {
            idsJson.put(friend.id)
            encryptedLocationsJson.put(
                CryptoManager.encrypt(
                    location,
                    Key.fromHexString(friend.friendPublicKey),
                    Key.fromHexString(friend.myPrivateKey)
                )
            )
        }
        val jsonToSend = JSONObject().put("receivers", idsJson)
        jsonToSend.put("encryptedLocations", encryptedLocationsJson)
        val request = JsonObjectRequest(Request.Method.POST,
            sendLocationURL,
            jsonToSend,
            { res ->
                completion(res.getString("status"), null)
            },
            { completion(null, it) })
        requestQueue.add(request)
    }

    fun sendHandshake(friend: Friend, handshake: String, userId: String, completion: (String?, VolleyError?) -> Unit) {
        val handshakeURL = baseUrl + "user/handshake/" + userId + "/" + friend.id
        val request = JsonObjectRequest(Request.Method.POST, handshakeURL, JSONObject("{\"encryptedHandshake\":$handshake}"),
            {

            },
            { completion(null, it) })
        requestQueue.add(request)
    }

    fun getHandshake(friend: Friend, userId: String, completion: (String?, VolleyError?) -> Unit) {
        val handshakeURL = baseUrl + "user/handshake/" + friend.id + "/" + userId
        val request = StringRequest(Request.Method.GET, handshakeURL,
            { res ->
                try {
                    val encodedHandshake = JSONObject(res).getString("encryptedHandshake")
                    if (encodedHandshake.isNotEmpty()) {
                        val decodedHandshake = CryptoManager.decrypt(
                            encodedHandshake,
                            Key.fromHexString(friend.friendPublicKey),
                            Key.fromHexString(friend.myPrivateKey)
                        )
                        completion(decodedHandshake, null)
                    }

                } catch (e: JSONException) {
                    Log.e("jsonError", e.toString())
                    completion("", null)
                }
            },
            { completion(null, it) })
        requestQueue.add(request)
    }
}