/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 controller fragment
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ui.controller

/* Android includes */
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat

/* Androidx includes */
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

/* Local includes */
import org.mantabots.robosoccer.R
import org.mantabots.robosoccer.databinding.FragmentControllerBinding
import org.mantabots.robosoccer.model.DriveReference
import org.mantabots.robosoccer.model.DriveMode
import org.mantabots.robosoccer.model.SharedData

class ControllerFragment : Fragment() {

    private var _binding: FragmentControllerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val mBinding get() = _binding!!
    private val mParameters: SharedData by activityViewModels()

    private lateinit var mMode : ImageView
    private lateinit var mReference : ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentControllerBinding.inflate(inflater, container, false)
        val root: View = mBinding.root

        mMode = mBinding.controllerStatusModeIcon

        if (mParameters.state.value.driveMode == DriveMode.ARCADE) {
            val arcade: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arcade_white_24dp)
            mMode.setImageDrawable(arcade)
        }
        else if (mParameters.state.value.driveMode == DriveMode.TANK) {
            val tank: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_tank_white_24dp)
            mMode.setImageDrawable(tank)
        }

        mReference = mBinding.controllerStatusReferenceIcon

        if (mParameters.state.value.driveReference == DriveReference.FIELD_CENTRIC) {
            val field: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_field_white_24dp)
            mReference.setImageDrawable(field)
        }
        else if (mParameters.state.value.driveReference == DriveReference.ROBOT_CENTRIC) {
            val robot: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_robot_white_24dp)
            mReference.setImageDrawable(robot)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
//
//     override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(view) {
//
//         val groupArcade = findViewById<Group>(R.id.group_arcade)
//         val groupTank   = findViewById<Group>(R.id.group_tank)
//         val isArcade    = findViewById<Switch>(R.id.settings_drive)
//
//         groupArcade.visibility = if (arcadeMode) View.VISIBLE else View.GONE
//         groupTank.visibility   = if (arcadeMode) View.GONE    else View.VISIBLE
//
//
//        setOnLongClickListener { arcadeMode = !arcadeMode; updateUi(); true }
//
//        // Joystick callbacks
//        findViewById<JoystickView>(R.id.joystickArcade).setOnMoveListener { angle, strength ->
//            val x =  strength / 100f * cos(Math.toRadians(angle.toDouble()))
//            val y = -strength / 100f * sin(Math.toRadians(angle.toDouble()))
//            viewModel.arcadeInput(x.toFloat(), y.toFloat())
//        }
//        findViewById<JoystickView>(R.id.joystickLeft).setOnMoveListener { _, s ->
//            viewModel.tankInput(yL = -s / 100f, yR = null)
//        }
//        findViewById<JoystickView>(R.id.joystickRight).setOnMoveListener { _, s ->
//            viewModel.tankInput(yL = null, yR = -s / 100f)
//        }
//
//        // Action bite button
//        findViewById<MaterialButton>(R.id.btnAction)
//            .setOnClickListener { viewModel.onAction() }
//    }
}