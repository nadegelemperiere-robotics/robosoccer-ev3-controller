/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Settings fragment
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ui.settings

/* Android includes */
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Switch

/* Androidx includes */
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/* Material includes */
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/* Kotlinx includes */
import kotlinx.coroutines.launch

/* Local includes */
import org.mantabots.robosoccer.databinding.FragmentSettingsBinding
import org.mantabots.robosoccer.ev3.Ev3Service
import org.mantabots.robosoccer.repository.SettingsRepository
import org.mantabots.robosoccer.model.DriveMode
import org.mantabots.robosoccer.model.Motor
import org.mantabots.robosoccer.model.Settings
import org.mantabots.robosoccer.model.ValidationResult
import org.mantabots.robosoccer.ui.views.Scroller

class SettingsFragment : Fragment() {


    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val mBinding get() = _binding!!

    private var mDevice = ""
    private var mLeftMotor = Motor.B
    private var mRightMotor = Motor.C
    private var mFirstMotor: Motor? = null
    private var mSecondMotor: Motor? = null
    private var mLeftInverted = false
    private var mRightInverted = false
    private var mFirstInverted = false
    private var mSecondInverted = false

    private lateinit var mRepository: SettingsRepository
    private lateinit var mSaveButton: Button
    private lateinit var mModeSwitch: Switch
    private lateinit var mDeviceAdapter: DeviceAdapter
    private lateinit var mDeviceScroller: Scroller<DeviceVH>
    private lateinit var mLeftMotorAdapter: MotorAdapter
    private lateinit var mLeftMotorScroller: Scroller<MotorVH>
    private lateinit var mLeftMotorInvertCheck: CheckBox
    private lateinit var mRightMotorAdapter: MotorAdapter
    private lateinit var mRightMotorScroller: Scroller<MotorVH>
    private lateinit var mRightMotorInvertCheck: CheckBox
    private lateinit var mFirstMotorAdapter: MotorAdapter
    private lateinit var mFirstMotorScroller: Scroller<MotorVH>
    private lateinit var mFirstMotorInvertCheck: CheckBox
    private lateinit var mSecondMotorAdapter: MotorAdapter
    private lateinit var mSecondMotorScroller: Scroller<MotorVH>
    private lateinit var mSecondMotorInvertCheck: CheckBox
    private lateinit var mFirstMotorCheck: CheckBox
    private lateinit var mSecondMotorCheck: CheckBox

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mRepository = SettingsRepository(context.applicationContext)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = mBinding.root
        val context: Context = requireContext()

        /* Initialize EV3 devices list */
        val refreshButton : ImageButton = mBinding.settingsDeviceRefresh
        refreshButton.setOnClickListener{ refreshDevices() }

        mDeviceAdapter = DeviceAdapter { device -> mDevice = device }
        mDeviceScroller = Scroller<DeviceVH>(
            mBinding.settingsDeviceRecycler,
            mDeviceAdapter,
            { device -> mDevice = device },
            context
        )
        refreshDevices()

        /* Initialize motors choices list */
        val motors : List<String> = Motor.entries.map { it.text }

        mLeftMotorAdapter = MotorAdapter("left")
        mLeftMotorScroller = Scroller<MotorVH>(
            mBinding.settingsMotorsLeftSelect,
            mLeftMotorAdapter,
            { motor -> mLeftMotor = Motor.fromString(motor) },
            context
        )
        mLeftMotorAdapter.submitList(motors)
        mLeftMotorInvertCheck = mBinding.settingsMotorsLeftInvertedCheckbox
        mLeftMotorInvertCheck.setOnClickListener { mLeftInverted = mLeftMotorInvertCheck.isChecked }

        mRightMotorAdapter = MotorAdapter("right")
        mRightMotorScroller = Scroller<MotorVH>(
            mBinding.settingsMotorsRightSelect,
            mRightMotorAdapter,
            { motor -> mRightMotor = Motor.fromString(motor) },
            context
        )
        mRightMotorAdapter.submitList(motors)
        mRightMotorInvertCheck = mBinding.settingsMotorsRightInvertedCheckbox
        mRightMotorInvertCheck.setOnClickListener { mRightInverted = mRightMotorInvertCheck.isChecked }


        mFirstMotorAdapter = MotorAdapter("first")
        mFirstMotorScroller = Scroller<MotorVH>(
            mBinding.settingsMotorsFirstSelect,
            mFirstMotorAdapter,
            { motor -> mFirstMotor = Motor.fromString(motor) },
            context
        )
        mFirstMotorAdapter.submitList(motors)
        mFirstMotorInvertCheck = mBinding.settingsMotorsFirstInvertedCheckbox
        mFirstMotorInvertCheck.setOnClickListener { mFirstInverted = mFirstMotorInvertCheck.isChecked }


        mFirstMotorCheck = mBinding.settingsMotorsFirstCheckbox
        mFirstMotorCheck.setOnClickListener { checkFirst(mFirstMotorCheck.isChecked) }

        mSecondMotorAdapter = MotorAdapter ("second")
        mSecondMotorScroller = Scroller<MotorVH>(
            mBinding.settingsMotorsSecondSelect,
            mSecondMotorAdapter,
            { motor -> mSecondMotor = Motor.fromString(motor) },
            context
        )
        mSecondMotorAdapter.submitList(motors)
        mSecondMotorInvertCheck = mBinding.settingsMotorsSecondInvertedCheckbox
        mSecondMotorInvertCheck.setOnClickListener { mSecondInverted = mSecondMotorInvertCheck.isChecked }


        mSecondMotorCheck = mBinding.settingsMotorsSecondCheckbox
        mSecondMotorCheck.setOnClickListener { checkSecond(mSecondMotorCheck.isChecked) }

        /* Configure saving */
        mSaveButton = mBinding.settingsSave
        mSaveButton.setOnClickListener { save() }

        /* Restore previous configuration */
        mModeSwitch = mBinding.settingsModeSwitch

        lifecycleScope.launchWhenStarted {
            mRepository.settings.collect { load(it) }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshDevices() {
        val ctx = requireContext()
        val devices = Ev3Service.listPaired(ctx)
        mDeviceAdapter.submitList(devices)
    }

    private fun checkFirst(check: Boolean) {
        if (check) {
            mBinding.settingsMotorsFirstCheckbox.isChecked = true
            mBinding.settingsMotorsFirstInvertedCheckbox.visibility = View.VISIBLE
            mBinding.settingsMotorsFirstInverted.visibility = View.VISIBLE
            mBinding.settingsMotorsFirstSelect.visibility = View.VISIBLE
            mFirstMotor = Motor.A
            mFirstMotorScroller.scroll(mFirstMotor!!.ordinal)
            mFirstInverted = false
            mFirstMotorInvertCheck.isChecked = false
        }
        else {
            mBinding.settingsMotorsFirstCheckbox.isChecked = false
            mBinding.settingsMotorsFirstInvertedCheckbox.visibility = View.GONE
            mBinding.settingsMotorsFirstInverted.visibility = View.GONE
            mBinding.settingsMotorsFirstSelect.visibility = View.GONE
            mFirstMotor = null
        }
    }

    private fun checkSecond(check: Boolean) {
        if (check) {
            mBinding.settingsMotorsSecondCheckbox.isChecked = true
            mBinding.settingsMotorsSecondInvertedCheckbox.visibility = View.VISIBLE
            mBinding.settingsMotorsSecondInverted.visibility = View.VISIBLE
            mBinding.settingsMotorsSecondSelect.visibility = View.VISIBLE
            mSecondMotor = Motor.A
            mSecondMotorScroller.scroll(mSecondMotor!!.ordinal)
            mSecondInverted = false
            mSecondMotorInvertCheck.isChecked = false
        }
        else {
            mBinding.settingsMotorsSecondCheckbox.isChecked = false
            mBinding.settingsMotorsSecondInvertedCheckbox.visibility = View.GONE
            mBinding.settingsMotorsSecondInverted.visibility = View.GONE
            mBinding.settingsMotorsSecondSelect.visibility = View.GONE
            mSecondMotor = null
        }
    }

    private fun save() {
        val settings = Settings(
            driveMode = if (mModeSwitch.isChecked) DriveMode.LEVERS else DriveMode.JOYSTICK,
            device = mDevice,
            left = mLeftMotor,
            leftInverted = mLeftInverted,
            right = mRightMotor,
            rightInverted = mRightInverted,
            first = mFirstMotor,
            firstInverted = mFirstInverted,
            second = mSecondMotor,
            secondInverted = mSecondInverted
        )
        when (val res = settings.check()) {
            ValidationResult.Ok -> {
                lifecycleScope.launch { mRepository.save(settings) }
                findNavController().popBackStack()
            }
            is ValidationResult.Error -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Invalid settings")
                    .setMessage(res.message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }
    }

    private fun load(settings : Settings) {

        mModeSwitch.isChecked = (settings.driveMode == DriveMode.LEVERS)

        mDevice = settings.device
        var position = mDeviceAdapter.currentList.indexOf(mDevice)
        if (position >= 0) { mDeviceScroller.scroll(position) }

        mLeftMotor = settings.left
        mLeftMotorScroller.scroll(settings.left.ordinal)
        mLeftInverted = settings.leftInverted
        mLeftMotorInvertCheck.isChecked = settings.leftInverted

        mRightMotor = settings.right
        mRightMotorScroller.scroll(settings.right.ordinal)
        mRightInverted = settings.rightInverted
        mRightMotorInvertCheck.isChecked = settings.rightInverted

        if(settings.first != null) {
            checkFirst(true)
            mFirstMotorScroller.scroll(settings.first!!.ordinal)
            mFirstMotor = settings.first
            mFirstMotorInvertCheck.isChecked = settings.firstInverted
            mFirstInverted = settings.firstInverted
        }
        else {
            checkFirst(false)
        }

        if(settings.second != null) {
            checkSecond(true)
            mSecondMotorScroller.scroll(settings.second!!.ordinal)
            mSecondMotor = settings.second
            mSecondMotorInvertCheck.isChecked = settings.secondInverted
            mSecondInverted = settings.secondInverted
        }
        else {
            checkSecond(false)
        }

    }

}