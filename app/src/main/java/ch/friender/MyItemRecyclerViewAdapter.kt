package ch.friender

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.friender.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(private val mValues: MutableList<DummyItem?>?) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        val view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friends_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder.mItem = mValues.get(position)
        holder.mIdView.setText(mValues.get(position).id)
        holder.mContentView.setText(mValues.get(position).content)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View?) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView?
        val mContentView: TextView?
        var mItem: DummyItem? = null
        override fun toString(): String {
            return super.toString() + " '" + mContentView.getText() + "'"
        }

        init {
            mIdView = mView.findViewById<View?>(R.id.item_number) as TextView
            mContentView = mView.findViewById<View?>(R.id.content) as TextView
        }
    }
}