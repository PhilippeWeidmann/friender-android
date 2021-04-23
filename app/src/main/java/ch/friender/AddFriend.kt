package ch.friender

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import ch.friender.cryptography.CryptoManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.WriterException
import org.json.JSONObject

class AddFriend : Fragment() {

    private lateinit var keys:JSONObject
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
        if (arguments?.getBoolean("comesFromQR") == true) {
            if (QRData != "") {
                Log.d("QRDATA", "" + QRData)
                if (correctQR(QRData)) {
                    addFriend(QRData)
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
        userQRData.put("id",id)
        userQRData.put("publicKey",keys.get("publicKey"))
        val qrgEncoder = QRGEncoder(userQRData.toString(), null, QRGContents.Type.TEXT, smallerDimension)
        try {
            // Getting QR-Code as Bitmap
            val bitmap = qrgEncoder.bitmap
            // Setting Bitmap to ImageView
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

    private fun addFriend(data: String) {
        //TODO add to friends list
    }
}