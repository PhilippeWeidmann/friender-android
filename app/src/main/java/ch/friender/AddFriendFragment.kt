package ch.friender

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import ch.friender.cryptography.CryptoManager
import com.google.zxing.WriterException
import org.json.JSONObject
import java.nio.charset.StandardCharsets


class AddFriendFragment : Fragment() {

    private lateinit var keys: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keys = JSONObject(requireArguments().getString("keys"))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_friend, container, false)

        val width: Int = Resources.getSystem().displayMetrics.widthPixels
        val height: Int = Resources.getSystem().displayMetrics.heightPixels
        var smallerDimension = if (width < height) width else height
        smallerDimension = smallerDimension * 3 / 4
        val image = view.findViewById<ImageView>(R.id.QR_image)
        val id =
            activity?.getSharedPreferences("id", Context.MODE_PRIVATE)?.getString("id", "error")
        view.findViewById<TextView>(R.id.id_textview).text = id
        val userQRData = JSONObject()
        userQRData.put("id", id)
        userQRData.put("publicKey", keys.get("publicKey"))
        val userQRDataString = Base64.encodeToString(
            userQRData.toString().toByteArray(StandardCharsets.UTF_8),
            Base64.DEFAULT
        )
        val qrgEncoder = QRGEncoder(userQRDataString, null, QRGContents.Type.TEXT, smallerDimension)
        try {
            val bitmap = qrgEncoder.bitmap
            image.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            Log.v("error", e.toString())
        }
        view.findViewById<Button>(R.id.button_qr).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val nextFrag = QRScanner()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, nextFrag)
                    .addToBackStack(null)
                    .commit()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    201
                )
            }

        }
        return view
    }

    override fun onDestroy() {
        CryptoManager.destroyKeyPair(requireContext())
        super.onDestroy()
    }

}