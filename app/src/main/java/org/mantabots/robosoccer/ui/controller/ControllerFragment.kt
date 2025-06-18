/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 controller fragment
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ui.controller

/* Java includes */
import java.io.IOException

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

/* Kotlin includes */
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.max
import kotlin.math.abs

/* Kotlinx includes */
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/* Local includes */
import org.mantabots.robosoccer.R
import org.mantabots.robosoccer.databinding.FragmentControllerBinding
import org.mantabots.robosoccer.model.DriveMode
import org.mantabots.robosoccer.model.Settings
import org.mantabots.robosoccer.model.Motor
import org.mantabots.robosoccer.repository.SettingsRepository
import org.mantabots.robosoccer.repository.SharedData
import kotlin.math.PI

class ControllerFragment : Fragment() {

    private var _binding: FragmentControllerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val mBinding get() = _binding!!

    private val mShared: SharedData by activityViewModels()

    private val sArcadeAngleRatio = 0.25

    private lateinit var mRepository: SettingsRepository
    private lateinit var mConnect: ImageView
    private lateinit var mMode: ImageView
    private lateinit var mArcade: ConstraintLayout
    private lateinit var mTank: ConstraintLayout

    private var mDevice = ""

    private var mLeftMotor: Motor? = null
    private var mLeftJob: Job? = null
    private var mRightMotor: Motor? = null
    private var mRightJob: Job? = null
    private var mFirstMotor: Motor? = null
    private var mFirstJob: Job? = null
    private var mSecondMotor: Motor? = null
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
        mConnect = mBinding.controllerStatusConnectIcon

        lifecycleScope.launchWhenStarted {
            mRepository.settings.collect { load(it) }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val left = mLeftMotor
        val right = mRightMotor
        val first = mFirstMotor
        val second = mSecondMotor

        if(left != null) {
            mLeftJob?.cancel()
            mLeftJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    mShared.state.value.power(left, 0.0f)
                } catch (_: IOException) {
                }
            }
        }
        if(right != null) {
            mRightJob?.cancel()
            mRightJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    mShared.state.value.power(right, 0.0f)
                } catch (_: IOException) {
                }
            }
        }
        if(first != null) {
            mFirstJob?.cancel()
            mFirstJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    mShared.state.value.power(first, 0.0f)
                } catch (_: IOException) {
                }
            }
        }
        if(second != null) {
            mSecondJob?.cancel()
            mSecondJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    mShared.state.value.power(second, 0.0f)
                } catch (_: IOException) {
                }
            }
        }
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun load(settings: Settings) {

        // Gather motors
        mLeftMotor = settings.leftWheel
        mRightMotor = settings.rightWheel
        mFirstMotor = settings.firstAttachment
        mSecondMotor = settings.secondAttachment
        mDevice = settings.device

        // Configure joysticks
        if (settings.driveMode == DriveMode.ARCADE) { configureArcade(mLeftMotor, mRightMotor)  }
        else if (settings.driveMode == DriveMode.TANK) { configureTank(mLeftMotor, mRightMotor) }

        if ((mSecondMotor == null) && (mFirstMotor == null)) {
            mBinding.controllerAttachments.visibility = View.GONE
        } else {
            mBinding.controllerAttachments.visibility = View.VISIBLE
        }

        configureFirstAttachment(mFirstMotor)
        configureSecondAttachment(mSecondMotor)

        // Connect to EV3
        val waiting: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_waiting_teal_24dp)
        mConnect.setImageDrawable(waiting)

        mBinding.controllerRetryButton.setOnClickListener{ deviceConnection(mDevice) }
        deviceConnection(mDevice)

    }

    private fun deviceConnection(device:String) {
        if(device != "") {

            val waiting: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_waiting_teal_24dp)
            mConnect.setImageDrawable(waiting)

            viewLifecycleOwner.lifecycleScope.launch {
                val isConnected = mShared.state.value.connect(requireContext(), device)
                if (isConnected) {
                    Toast.makeText(requireContext(), "EV3 connected!", Toast.LENGTH_SHORT).show()
                    val connected: Drawable? =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_wireless_green_24dp
                        )
                    mConnect.setImageDrawable(connected)
                } else {
                    Toast.makeText(requireContext(), "EV3 connection failed", Toast.LENGTH_LONG)
                        .show()
                    val error: Drawable? =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_wireless_red_24dp)
                    mConnect.setImageDrawable(error)
                }
            }
        }

    }

    private fun configureTank(left:Motor?, right:Motor?) {

        val icon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_tank_teal_24dp)
        mMode.setImageDrawable(icon)
        mArcade.visibility = View.GONE
        mTank.visibility = View.VISIBLE

        mBinding.controllerTankJoystickLeftText.text = left?.text
        mBinding.controllerTankJoystickRightText.text = right?.text

        if(left != null) {

            mBinding.controllerTankJoystickLeft.setOnMoveListener { angle, strength ->
                mLeftJob?.cancel()
                val rad = transformAngle(angle)
                val sign = cos(rad) / max(abs(cos(rad)),0.00001.toFloat())
                mLeftJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        mShared.state.value.power(left, (sign * strength).toFloat() / 100)
                    } catch (_: IOException) { }
                }
            }
        }

        if(right != null) {
            mBinding.controllerTankJoystickRight.setOnMoveListener { angle, strength ->
                mRightJob?.cancel()
                val rad = transformAngle(angle)
                val sign = cos(rad) / max(abs(cos(rad)),0.00001.toFloat())
                mRightJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        mShared.state.value.power(right, (sign * strength).toFloat() / 100)
                    }
                    catch(_ : IOException) {}
                }
            }
        }
    }

    private fun configureArcade(left:Motor?, right:Motor?) {

        val icon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arcade_teal_24dp)
        mMode.setImageDrawable(icon)
        mArcade.visibility = View.VISIBLE
        mTank.visibility = View.GONE

        mBinding.controllerArcadeJoystickText.text = "${left?.text} / ${right?.text}"

        mBinding.controllerArcadeJoystick.setOnMoveListener { angle, strength ->

            mLeftJob?.cancel()
            mRightJob?.cancel()
            val speeds = computeArcadeMotorSpeeds(angle, strength)
            if(left != null) {
                mLeftJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        mShared.state.value.power(left, speeds.first)
                    }
                    catch(_ : IOException) {}

                }
            }
            if(right != null) {
                mRightJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        mShared.state.value.power(right, speeds.second)
                    }
                    catch(_ : IOException) {}
                }
            }
        }
    }

    private fun configureFirstAttachment(motor: Motor?) {
        if (motor != null) {
            mBinding.controllerAttachment1.visibility = View.VISIBLE
            mBinding.controllerAttachment1JoystickText.text = motor.text

            mBinding.controllerAttachment1Joystick.setOnMoveListener { angle, strength ->
                mFirstJob?.cancel()
                val rad = transformAngle(angle)
                val sign = cos(rad) / max(abs(cos(rad)),0.00001.toFloat())
                mFirstJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        mShared.state.value.power(motor, (sign * strength).toFloat() / 100)
                    }
                    catch(_ : IOException) {}
                }
            }
        }
        else {
            mBinding.controllerAttachment1Joystick.setOnMoveListener { _, strength ->
                mFirstJob?.cancel()
            }
            mBinding.controllerAttachment1.visibility = View.GONE
        }
    }

    private fun configureSecondAttachment(motor: Motor?) {
        if (motor != null) {
            mBinding.controllerAttachment2.visibility = View.VISIBLE
            mBinding.controllerAttachment2JoystickText.text = motor.text
            mBinding.controllerAttachment2Joystick.setOnMoveListener { angle, strength ->
                mSecondJob?.cancel()
                val rad = transformAngle(angle)
                val sign = cos(rad) / max(abs(cos(rad)),0.00001.toFloat())
                mSecondJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        mShared.state.value.power(motor, (sign * strength).toFloat() / 100)
                    } catch (_: IOException) {
                    }
                }
            }
        } else {
            mBinding.controllerAttachment2Joystick.setOnMoveListener { _, strength ->
                mSecondJob?.cancel()
                mBinding.controllerAttachment2.visibility = View.GONE
            }
        }
    }

    private fun computeArcadeMotorSpeeds(angle:Int, strength:Int): Pair<Float, Float> {

        var result = Pair<Float,Float>(0.0f,0.0f)

        val rad = transformAngle(angle)
        val y =  strength.toFloat() / 100 * cos(rad)
        val x =  strength.toFloat() / 100 * sin(rad) * sArcadeAngleRatio.toFloat()
        var left  = y + x
        var right = y - x
        // scale so |power| ≤ 1
        val m = max(abs(left), abs(right))
        if (m > 1) { left /= m; right /= m }

        result= Pair<Float,Float>(left.toFloat(),right.toFloat())

        return result
    }

    private fun transformAngle(angle: Int): Float {
        return (-(angle - 90).toFloat() / 180 * PI).toFloat()
    }
}