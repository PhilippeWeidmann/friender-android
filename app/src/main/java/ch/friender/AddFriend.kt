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
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import ch.friender.cryptography.CryptoManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.zxing.WriterException
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets


class AddFriend : Fragment() {

    private lateinit var keys: JSONObject
    private var QRData = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //crypto
        val sharedPreferencesCrypto = requireActivity().getSharedPreferences("keys", FragmentActivity.MODE_PRIVATE)
        CryptoManager.generateKeyPair(requireContext())
        keys = JSONObject("{\"secretKey\":\"\",\"publicKey\":\"\"}")
        if (sharedPreferencesCrypto.getString("keyPair", "no keys") == "no keys") {
            //a surement besoin d'être refait ?
            Log.e("no keys", "no keys were found")
        } else {
            keys = JSONObject(sharedPreferencesCrypto.getString("keyPair", "no keys"))
        }
        if (arguments?.getBoolean("fromQR") == true) {
            QRData = arguments?.getString("QRData").toString()
            if (QRData != "") {
                Log.d("QRDATA", "" + QRData)
                if (correctQR(QRData)) {
                    val newFriend = Friend(JSONObject(QRData).getString("id"), JSONObject(QRData).getString("publicKey"), keys.getString("secretKey"))
                    FriendManager().initWithContext(requireContext())
                    if (FriendManager().addFriend(newFriend)) {

                    } else {
                        displayError(1)
                    }
                } else {
                    displayError(2)
                }
            }
        }
        Log.i("crypto keys", " \npublic key: " + keys.get("publicKey") + "\nsecret key: " + keys.get("secretKey"))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_friend, container, false)

        val width: Int = Resources.getSystem().displayMetrics.widthPixels
        val height: Int = Resources.getSystem().displayMetrics.heightPixels
        var smallerDimension = if (width < height) width else height
        smallerDimension = smallerDimension * 3 / 4
        val image = view.findViewById<ImageView>(R.id.QR_image)
        val id = activity?.getPreferences(Context.MODE_PRIVATE)?.getString("id", "")
        view.findViewById<TextView>(R.id.id_textview).text = id
        val userQRData = JSONObject()
        userQRData.put("id", id)
        userQRData.put("publicKey", keys.get("publicKey"))
        val userQRDataString = Base64.encodeToString(userQRData.toString().toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
        val qrgEncoder = QRGEncoder(userQRDataString, null, QRGContents.Type.TEXT, smallerDimension)
        try {
            val bitmap = qrgEncoder.bitmap
            image.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            Log.v("error", e.toString())
        }

        view.findViewById<Button>(R.id.button_qr).setOnClickListener {
            val action = AddFriendDirections.actionAddFriend2ToQRScanner(keys.toString())
            view?.findNavController()?.navigate(action)
        }
        return view
    }

    override fun onDestroy() {
        CryptoManager.destroyKeyPair(requireContext())
        super.onDestroy()
    }

    private fun correctQR(data: String): Boolean {
        return JSONObject(data).get("id").toString().isNotEmpty() && JSONObject(data).get("publicKey").toString().isNotEmpty()
    }

    private fun displayError(case: Int) {
        if (case == 1) {
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Could not add friend")
                    .setMessage("Your are already friend with this person")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Could not add friend")
                    .setMessage("The QR scanned was not a correct Friender QR, please try again")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        }

    }
}