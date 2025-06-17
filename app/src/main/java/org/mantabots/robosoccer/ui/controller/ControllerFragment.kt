/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 controller fragment
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ui.controller


/* Android includes */
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast

/* Androidx includes */
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

/* Kotlinx includes */
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okio.IOException

/* Local includes */
import org.mantabots.robosoccer.R
import org.mantabots.robosoccer.databinding.FragmentControllerBinding
import org.mantabots.robosoccer.model.DriveReference
import org.mantabots.robosoccer.model.DriveMode
import org.mantabots.robosoccer.model.Settings
import org.mantabots.robosoccer.repository.SettingsRepository
import org.mantabots.robosoccer.repository.SharedData

class ControllerFragment : Fragment() {

    private var _binding: FragmentControllerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val mBinding get() = _binding!!

    private val mShared: SharedData by activityViewModels()

    private lateinit var mRepository: SettingsRepository
    private lateinit var mConnect: ImageView
    private lateinit var mMode: ImageView
    private lateinit var mReference: ImageView
    private lateinit var mArcade: ConstraintLayout
    private lateinit var mTank: ConstraintLayout

    private var mLeftJob: Job? = null
    private var mRightJob: Job? = null
    private var mFirstJob: Job? = null
    private var mSecondJob: Job? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mRepository = SettingsRepository(context.applicationContext)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentControllerBinding.inflate(inflater, container, false)
        val root: View = mBinding.root

        mArcade = mBinding.controllerArcade
        mTank = mBinding.controllerTank

        mMode = mBinding.controllerStatusModeIcon
        mReference = mBinding.controllerStatusReferenceIcon
        mConnect = mBinding.controllerStatusConnectIcon

        lifecycleScope.launchWhenStarted {
            mRepository.settings.collect { load(it) }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun load(settings: Settings) {

        if (settings.driveMode == DriveMode.ARCADE) {
            val arcade: Drawable? =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_arcade_teal_24dp)
            mMode.setImageDrawable(arcade)
            mArcade.visibility = View.VISIBLE
            mTank.visibility = View.GONE
            mBinding.controllerArcadeJoystickText.text =
                "${settings.leftWheel.text} / ${settings.rightWheel.text}"

            mBinding.controllerArcadeJoystick.setOnMoveListener { _, strength ->
                mLeftJob?.cancel()
                mRightJob?.cancel()
                mLeftJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    mShared.state.value.power(settings.leftWheel, strength.toFloat() / 100)
                }
                mRightJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    mShared.state.value.power(settings.rightWheel, strength.toFloat() / 100)
                }
            }
        } else if (settings.driveMode == DriveMode.TANK) {
            val tank: Drawable? =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_tank_teal_24dp)
            mMode.setImageDrawable(tank)
            mArcade.visibility = View.GONE
            mTank.visibility = View.VISIBLE
            mBinding.controllerTankJoystickLeftText.text = settings.leftWheel.text
            mBinding.controllerTankJoystickRightText.text = settings.rightWheel.text

            mBinding.controllerTankJoystickLeft.setOnMoveListener { _, strength ->
                mLeftJob?.cancel()
                mLeftJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        mShared.state.value.power(settings.leftWheel, strength.toFloat() / 100)
                    }
                    catch(_ : IOException) {}
                }
            }

            mBinding.controllerTankJoystickRight.setOnMoveListener { _, strength ->
                mRightJob?.cancel()
                mRightJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        mShared.state.value.power(settings.rightWheel, strength.toFloat() / 100)
                    }
                    catch(_ : IOException) {}
                }
            }
        }

        if (settings.driveReference == DriveReference.FIELD_CENTRIC) {
            val field: Drawable? =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_field_teal_24dp)
            mReference.setImageDrawable(field)
        } else if (settings.driveReference == DriveReference.ROBOT_CENTRIC) {
            val robot: Drawable? =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_robot_teal_24dp)
            mReference.setImageDrawable(robot)
        }


        if ((settings.secondAttachment == null) && (settings.firstAttachment == null)) {
            mBinding.controllerAttachments.visibility = View.GONE
        } else {
            mBinding.controllerAttachments.visibility = View.VISIBLE
        }

        if (settings.firstAttachment != null) {
            mBinding.controllerAttachment1.visibility = View.VISIBLE
            mBinding.controllerAttachment1JoystickText.text =
                settings.firstAttachment!!.text
        } else {
            mBinding.controllerAttachment1.visibility = View.GONE
        }

        if (settings.secondAttachment != null) {
            mBinding.controllerAttachment2.visibility = View.VISIBLE
            mBinding.controllerAttachment2JoystickText.text =
                settings.secondAttachment!!.text
        } else {
            mBinding.controllerAttachment2.visibility = View.GONE
        }


        val waiting: Drawable? =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_waiting_teal_24dp)
        mConnect.setImageDrawable(waiting)

        viewLifecycleOwner.lifecycleScope.launch {
            val isConnected = mShared.state.value.connect(requireContext(), settings.device)
            if (isConnected) {
                Toast.makeText(requireContext(), "EV3 connected!", Toast.LENGTH_SHORT).show()
                val connected: Drawable? =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_wireless_green_24dp)
                mConnect.setImageDrawable(connected)
            } else {
                Toast.makeText(requireContext(), "EV3 connection failed", Toast.LENGTH_LONG).show()
                val error: Drawable? =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_wireless_red_24dp)
                mConnect.setImageDrawable(error)
            }

        }
    }
}