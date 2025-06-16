/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Adapter to manage first attachment motor display and selection
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

class MotorAdapter(
    private val name: String
) : ListAdapter<String, MotorVH>(Diff) {

    var selected = ""

    override fun onCreateViewHolder(p: ViewGroup, v: Int): MotorVH {
        val view = MotorVH(
            LayoutInflater.from(p.context)
                .inflate(R.layout.string_choice, p, false)
        )
        return view
    }

    override fun onBindViewHolder(h: MotorVH, pos: Int) {
        h.bind(getItem(pos), name)
    }

    private object Diff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(a: String, b: String) = a == b
        override fun areContentsTheSame(a: String, b: String) = a == b
    }
}

class MotorVH(view: View) : RecyclerView.ViewHolder(view) {

    private val name = view.findViewById<TextView>(R.id.string_choice)
    internal var item = ""
    private var details = ""

    fun bind(content: String, description:String) {
        name.text = content
        name.contentDescription = description
        item = content
        details = description
    }

    fun select(function: (String) -> Unit) {
        function(item)
    }

}