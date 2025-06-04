/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 controller fragment
   ------------------------------------------------------- */

package org.mantabots.robosoccer.ui.controller

/* Android includes */
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/* Androidx includes */
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

/* Local includes */
import org.mantabots.robosoccer.databinding.FragmentControllerBinding

class ControllerFragment : Fragment() {

    private var _binding: FragmentControllerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(ControllerViewModel::class.java)

        _binding = FragmentControllerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textController
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}