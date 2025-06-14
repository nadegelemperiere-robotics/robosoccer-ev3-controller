/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Adapter to manage left motor display and selection
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

class LeftMotorAdapter(
    private val select: (String) -> Unit
) : ListAdapter<String, LeftMotorVH>(Diff) {

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        LeftMotorVH(LayoutInflater.from(p.context)
            .inflate(R.layout.string_choice, p, false))

    override fun onBindViewHolder(h: LeftMotorVH, pos: Int) =
        h.bind(getItem(pos))

    override fun onViewAttachedToWindow(h: LeftMotorVH) {
        super.onViewAttachedToWindow(h)
        h.select(select)
    }

    private object Diff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(a: String, b: String) = a == b
        override fun areContentsTheSame(a: String, b: String) = a == b
    }
}

class LeftMotorVH(view: View) : RecyclerView.ViewHolder(view) {

    private val name = view.findViewById<TextView>(R.id.string_choice)
    private var item = ""

    fun bind(d: String) {
        name.text = d
        item = d
    }

    fun select(function: (String) -> Unit) {
        function(item)
    }

}