package ch.friender

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.WriterException


class FriendsList : Fragment() {

    private var columnCount = 1
    private val ARG_COLUMN_COUNT = "column-count"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_friends_list_list, container, false)
        val topAppBar: MaterialToolbar = view.findViewById(R.id.topAppBar)
        val userId = activity?.getPreferences(Context.MODE_PRIVATE)?.getString("id", "")
        val idText: TextView = view.findViewById(R.id.userId)
        idText.setText("Show me my ID")

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.addFriend -> {
                    startActivity(Intent(requireContext(), ScannedBarcodeActivity::class.java))
                    true
                }
                R.id.userId -> {
                    userId?.let { showQRDialog(it) }
                    true
                }
                else -> false
            }
        }

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                //adapter = MyItemRecyclerViewAdapter2()
            }
        }
        return view
    }

    private fun addFriendDialog() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enter your friend's ID")
                .setNeutralButton("CANCEL") { dialog, which ->

                }
                .setPositiveButton("ADD") { dialog, which ->

                }
                .show()
    }

    private fun showQRDialog(id: String){

        val width: Int = Resources.getSystem().displayMetrics.widthPixels
        val height: Int = Resources.getSystem().displayMetrics.heightPixels
        var smallerDimension = if (width < height) width else height
        smallerDimension = smallerDimension * 3 / 4
        val image = ImageView(requireContext())
        val qrgEncoder = QRGEncoder(id, null, QRGContents.Type.TEXT, smallerDimension)
        try {
            // Getting QR-Code as Bitmap
            val bitmap = qrgEncoder.bitmap
            // Setting Bitmap to ImageView
            image.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            Log.v("error", e.toString())
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext()).setPositiveButton("OK", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.dismiss()
            }
        }).setView(image)
        builder.create().show()
    }

}