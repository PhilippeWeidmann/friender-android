package ch.friender

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FriendManager {

    var friends = ArrayList<Friend>()

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

    fun initWithContext(context: Context) {
        val prefs = context.getSharedPreferences("friends", Context.MODE_PRIVATE)
        val friendArrayType = object : TypeToken<ArrayList<Friend>>() {}.type
        friends = Gson().fromJson(prefs.getString("friends", "[]"), friendArrayType)
    }
}