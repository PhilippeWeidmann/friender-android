package ch.friender

import android.content.Context
import android.util.Log
import ch.friender.cryptography.CryptoManager
import ch.friender.networking.ApiFetcher
import com.google.android.gms.common.api.Api
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.goterl.lazysodium.utils.Key

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

    fun getFriendsLocations(context: Context, completion: (ArrayList<String>?) -> Unit) {
        ApiFetcher.initWithContext(context)
        ApiFetcher.getLocations(friends, userId) { resLocations, error ->
            completion(resLocations)
            error?.let {
                Log.e("get location error", "" + error)
            }
        }

    }

    fun sendUpdatedLocation(context: Context, location: String) {
        ApiFetcher.initWithContext(context)
        ApiFetcher.sendLocation(friends, userId, location) { res, error ->
            res?.let {
                Log.d("sent location", location)
            }
            error?.let {
                Log.e("send location error", "" + error)
            }
        }
    }

    fun sendHandshake(friend: Friend, context: Context) {
        val encryptedHandshake =
            CryptoManager.encrypt("handshake", Key.fromHexString(friend.friendPublicKey), Key.fromHexString(friend.myPrivateKey))
        ApiFetcher.initWithContext(context)
        ApiFetcher.sendHandshake(friend, encryptedHandshake, userId) { res, error ->

        }
    }

    fun getHandshake(context: Context, friend: Friend, completion: (Boolean?) -> Unit) {
        ApiFetcher.initWithContext(context)
        ApiFetcher.getHandshake(friend, userId) { res, error ->
            res?.let {
                if (res == "handshake") {
                    completion(true)
                } else {
                    completion(false)
                }
            }
            error?.let {
                Log.e("error", error.toString())
                completion(false)
            }
        }
    }

}