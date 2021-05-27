package ch.friender

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.navigation.findNavController
import ch.friender.cryptography.CryptoManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.zxing.WriterException
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets


class AddFriendActivity : FragmentActivity() {

    private var keys = JSONObject("{\"secretKey\":\"\",\"publicKey\":\"\"}")
    private lateinit var QRData: String
    var alreadyScanned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)
        //crypto
        val sharedPreferencesCrypto = getSharedPreferences("keys", FragmentActivity.MODE_PRIVATE)
        CryptoManager.generateKeyPair(this)
        if (sharedPreferencesCrypto.getString("keyPair", "no keys") == "no keys") {
            displayError(
                "Error could not get key pair",
                "make sure you are connected to internet and reboot the app"
            )
            Log.e("no keys", "no keys were found")
        } else {
            keys = JSONObject(sharedPreferencesCrypto.getString("keyPair", "no keys"))
        }
        if (savedInstanceState == null) {
            val keysToFragment = Bundle()
            keysToFragment.putString("keys", keys.toString())
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<AddFriendFragment>(R.id.fragment_container_view, args = keysToFragment)
            }
        }
        Log.i(
            "crypto keys",
            " \npublic key: " + keys.get("publicKey") + "\nsecret key: " + keys.get("secretKey")
        )
    }

    fun addFriendFromQR() {
        Log.d("-------------", keys.getString("secretKey"))
        val newFriend = Friend(
            JSONObject(QRData).getString("id"),
            JSONObject(QRData).getString("publicKey"),
            keys.getString("secretKey")
        )
        FriendManager.initWithContext(this)
        FriendManager.getHandshake(this, newFriend) { res ->
            if (res == true) {
                if (!FriendManager.addFriend(newFriend, this)) {
                    displayError("Already friend", "You are already friend with this person")
                }
            } else {
                displayError("Error", "You and your friend must scan your QRs mutually before adding each other")
            }
        }
    }

    fun sendHandshake(qrdata: String) {
        if (qrdata != "") {
            Log.d("QRDATA", "" + qrdata)
            supportFragmentManager.popBackStack()
            if (correctQR(qrdata)) {
                QRData = qrdata
                val newFriend = Friend(
                    JSONObject(qrdata).getString("id"),
                    JSONObject(qrdata).getString("publicKey"),
                    keys.getString("secretKey")
                )
                FriendManager.initWithContext(this)
                FriendManager.sendHandshake(newFriend, this)
                alreadyScanned = true
            } else {
                displayError("Wrong QR code", "The scanned QR was not a Friender QR")
            }
        }
    }


    override fun onDestroy() {
        CryptoManager.destroyKeyPair(this)
        super.onDestroy()
    }

    private fun correctQR(data: String): Boolean {
        return JSONObject(data).get("id").toString()
            .isNotEmpty() && JSONObject(data).get("publicKey").toString().isNotEmpty()
    }

    private fun displayError(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}