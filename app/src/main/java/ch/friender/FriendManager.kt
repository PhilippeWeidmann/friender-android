package ch.friender

import android.content.Context
import com.google.gson.Gson
import org.json.JSONArray

class FriendManager {

    var friends = ArrayList<Friend>()

    fun addFriend(friend:Friend):Boolean{
        return if(!friends.contains(friend)){
            friends.add(friend)
            true
        }else{
            false
        }
    }

    fun initWithContext(context: Context){
        val prefs = context.getSharedPreferences("friends", Context.MODE_PRIVATE)
        for (i in 0 until JSONArray(prefs.getString("friends", "")).length()) {
            friends.add(Gson().fromJson(Gson().toJson(JSONArray(prefs.getString("friends","")).getJSONObject(i)), Friend::class.java))
        }
    }
}