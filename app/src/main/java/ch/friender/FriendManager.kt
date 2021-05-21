package ch.friender

import android.content.Context
import android.util.Log
import ch.friender.networking.ApiFetcher
import com.google.android.gms.common.api.Api
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FriendManager {

    var friends = ArrayList<Friend>()
    var userId = ""

    fun initWithContext(context: Context) {
        val prefs = context.getSharedPreferences("friends", Context.MODE_PRIVATE)
        val friendArrayType = object : TypeToken<ArrayList<Friend>>() {}.type
        friends = Gson().fromJson(prefs.getString("friends", "[]"), friendArrayType)
        userId =
            context.getSharedPreferences("id", Context.MODE_PRIVATE).getString("id", "").toString()
    }

    fun addFriend(friend: Friend, context: Context): Boolean {
        return if (!friends.contains(friend)) {
            friends.add(friend)
            val prefs = context.getSharedPreferences("friends", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("friends", Gson().toJson(friends))
            editor.apply()
            true
        } else {
            false
        }
    }

    fun getFriendsLocations(context: Context): ArrayList<String> {
        val locations = ArrayList<String>()
        for (friend in friends) {
            ApiFetcher.initWithContext(context)
            ApiFetcher.getLocation(friend, userId) { location, error ->
                location?.let {
                    if (location!="") {
                        Log.d("location", " -> $location")
                        locations.add(location)
                    }
                }
                error?.let {
                    Log.e("get location error", "" + error)
                }
            }
        }
        return locations
    }

    fun sendUpdatedLocation(context: Context, location: String) {
        for (friend in friends) {
            ApiFetcher.initWithContext(context)
            ApiFetcher.sendLocation(friend, userId, location) { res, error ->
                res?.let {
                    Log.d("sent location", location)
                }
                error?.let {
                    Log.e("send location error", "" + error)
                }
            }
        }
    }

}