package ch.friender

import android.app.ActionBar
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ActionMenuView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder


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
        idText.setText("Your ID : " + userId)

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.addFriend -> {
                    addFriendDialog()
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

}