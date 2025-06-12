/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Adapter to manage paired EV3 display and selection
   ------------------------------------------------------- */

package org.mantabots.robosoccer.ui.settings

/* Android includes */
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/* Androidx includes */
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/* Local includes */
import org.mantabots.robosoccer.R

class DeviceAdapter(
    private val select: (String) -> Unit
) : ListAdapter<String, DeviceVH>(Diff) {

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        DeviceVH(LayoutInflater.from(p.context)
            .inflate(R.layout.string_choice, p, false))

    override fun onBindViewHolder(h: DeviceVH, pos: Int) =
        h.bind(getItem(pos))

    override fun onViewAttachedToWindow(h: DeviceVH) {
        super.onViewAttachedToWindow(h)
        h.select(select)    // callback with the row’s string
    }

    private object Diff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(a: String, b: String) = a == b
        override fun areContentsTheSame(a: String, b: String) = a == b
    }
}

class DeviceVH(view: View) : RecyclerView.ViewHolder(view) {

    private val name = view.findViewById<TextView>(R.id.string_choice)
    private var item = ""

    fun bind(d: String) {
        name.text = d
        item = d
    }

    fun select(select: (String) -> Unit) {
        select(item)
    }

}