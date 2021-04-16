package ch.friender

import android.content.Context
import android.content.Intent
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
import com.google.zxing.WriterException

class AddFriend : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_friend, container, false)
        val width: Int = Resources.getSystem().displayMetrics.widthPixels
        val height: Int = Resources.getSystem().displayMetrics.heightPixels
        var smallerDimension = if (width < height) width else height
        smallerDimension = smallerDimension * 3 / 4
        val image = view.findViewById<ImageView>(R.id.QR_image)
        val id = activity?.getPreferences(Context.MODE_PRIVATE)?.getString("id", "")
        view.findViewById<TextView>(R.id.id_textview).text = id
        val qrgEncoder = QRGEncoder(id, null, QRGContents.Type.TEXT, smallerDimension)
        try {
            // Getting QR-Code as Bitmap
            val bitmap = qrgEncoder.bitmap
            // Setting Bitmap to ImageView
            image.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            Log.v("error", e.toString())
        }
        view.findViewById<Button>(R.id.button_qr).setOnClickListener {
            val intent = Intent(requireContext(), ScannedBarcodeActivity::class.java)
            startActivity(intent)
        }
        return view
    }

}