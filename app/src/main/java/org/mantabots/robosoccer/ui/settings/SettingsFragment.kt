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

    private lateinit var mRepository: SettingsRepository
    private lateinit var mSaveButton: Button
    private lateinit var mModeSwitch: Switch
    private lateinit var mDeviceAdapter: DeviceAdapter
    private lateinit var mDeviceScroller: Scroller<DeviceVH>
    private lateinit var mLeftMotorAdapter: MotorAdapter
    private lateinit var mLeftMotorScroller: Scroller<MotorVH>
    private lateinit var mRightMotorAdapter: MotorAdapter
    private lateinit var mRightMotorScroller: Scroller<MotorVH>
    private lateinit var mFirstMotorAdapter: MotorAdapter
    private lateinit var mFirstMotorScroller: Scroller<MotorVH>
    private lateinit var mSecondMotorAdapter: MotorAdapter
    private lateinit var mSecondMotorScroller: Scroller<MotorVH>
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


        mRightMotorAdapter = MotorAdapter("right")
        mRightMotorScroller = Scroller<MotorVH>(
            mBinding.settingsMotorsRightSelect,
            mRightMotorAdapter,
            { motor -> mRightMotor = Motor.fromString(motor) },
            context
        )
        mRightMotorAdapter.submitList(motors)

        mFirstMotorAdapter = MotorAdapter("first")
        mFirstMotorScroller = Scroller<MotorVH>(
            mBinding.settingsMotorsAttachment1Select,
            mFirstMotorAdapter,
            { motor -> mFirstMotor = Motor.fromString(motor) },
            context
        )
        mFirstMotorAdapter.submitList(motors)

        mFirstMotorCheck = mBinding.settingsMotorsAttachment1Checkbox
        mFirstMotorCheck.setOnClickListener { checkFirst(mFirstMotorCheck.isChecked) }

        mSecondMotorAdapter = MotorAdapter ("second")
        mSecondMotorScroller = Scroller<MotorVH>(
            mBinding.settingsMotorsAttachment2Select,
            mSecondMotorAdapter,
            { motor -> mSecondMotor = Motor.fromString(motor) },
            context
        )
        mSecondMotorAdapter.submitList(motors)

        mSecondMotorCheck = mBinding.settingsMotorsAttachment2Checkbox
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
            mBinding.settingsMotorsAttachment1Select.visibility = View.VISIBLE
            mBinding.settingsMotorsAttachment1Checkbox.isChecked = true
            mFirstMotor = Motor.A
            mFirstMotorScroller.scroll(mFirstMotor!!.ordinal)
        }
        else {
            mBinding.settingsMotorsAttachment1Checkbox.isChecked = false
            mBinding.settingsMotorsAttachment1Select.visibility = View.GONE
            mFirstMotor = null
        }
    }

    private fun checkSecond(check: Boolean) {
        if (check) {
            mBinding.settingsMotorsAttachment2Checkbox.isChecked = true
            mBinding.settingsMotorsAttachment2Select.visibility = View.VISIBLE
            mSecondMotor = Motor.A
            mSecondMotorScroller.scroll(mSecondMotor!!.ordinal)
        }
        else {
            mBinding.settingsMotorsAttachment2Checkbox.isChecked = false
            mBinding.settingsMotorsAttachment2Select.visibility = View.GONE
            mSecondMotor = null
        }
    }

    private fun save() {
        val settings = Settings(
            driveMode = if (mModeSwitch.isChecked) DriveMode.TANK else DriveMode.ARCADE,
            device = mDevice,
            leftWheel = mLeftMotor,
            rightWheel = mRightMotor,
            firstAttachment = mFirstMotor,
            secondAttachment = mSecondMotor
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

        mModeSwitch.isChecked = (settings.driveMode == DriveMode.TANK)

        mDevice = settings.device
        var position = mDeviceAdapter.currentList.indexOf(mDevice)
        if (position >= 0) { mDeviceScroller.scroll(position) }

        mLeftMotor = settings.leftWheel
        mLeftMotorScroller.scroll(settings.leftWheel.ordinal)

        mRightMotor = settings.rightWheel
        mRightMotorScroller.scroll(settings.rightWheel.ordinal)

        if(settings.firstAttachment != null) {
            checkFirst(true)
            mFirstMotorScroller.scroll(settings.firstAttachment!!.ordinal)
            mFirstMotor = settings.firstAttachment
        }
        else {
            checkFirst(false)
        }

        if(settings.secondAttachment != null) {
            checkSecond(true)
            mSecondMotorScroller.scroll(settings.secondAttachment!!.ordinal)
            mSecondMotor = settings.secondAttachment
        }
        else {
            checkSecond(false)
        }

    }

}