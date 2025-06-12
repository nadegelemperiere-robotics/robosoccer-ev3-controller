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
import android.widget.ImageButton
import android.widget.Switch

/* Androidx includes */
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper

/* Kotlin includes */
import kotlin.getValue

/* Kotlinx includes */
import kotlinx.coroutines.launch

/* Local includes */
import org.mantabots.robosoccer.databinding.FragmentSettingsBinding
import org.mantabots.robosoccer.repository.Ev3Service
import org.mantabots.robosoccer.repository.SettingsRepository
import org.mantabots.robosoccer.model.DriveMode
import org.mantabots.robosoccer.model.DriveReference
import org.mantabots.robosoccer.model.Motor
import org.mantabots.robosoccer.model.Settings
import org.mantabots.robosoccer.model.SharedData

class SettingsFragment : Fragment() {


    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val mBinding get() = _binding!!
    private val mData: SharedData by activityViewModels()
    private var mDevice = ""
    private var mLeftMotor = Motor.B
    private var mRightMotor = Motor.C

    private lateinit var mRepository: SettingsRepository
    private lateinit var mDeviceAdapter : DeviceAdapter
    private lateinit var mSaveButton : Button
    private lateinit var mModeSwitch : Switch
    private lateinit var mReferenceSwitch : Switch
    private lateinit var mLeftMotorAdapter : MotorAdapter
    private lateinit var mRightMotorAdapter: MotorAdapter

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

        /* Initialize EV3 devices list */
        val refreshButton : ImageButton = mBinding.settingsDeviceRefresh
        refreshButton.setOnClickListener{ refreshDevices() }

        mDeviceAdapter = DeviceAdapter { device -> mDevice = device }
        mBinding.settingsDeviceRecycler.layoutManager = LinearLayoutManager(requireContext())
        mBinding.settingsDeviceRecycler.adapter = mDeviceAdapter
        LinearSnapHelper().attachToRecyclerView(mBinding.settingsDeviceRecycler)
        refreshDevices()

        /* Initialize motors choices list */
        val motors : List<String> = Motor.entries.map(Motor::displayName)

        mLeftMotorAdapter = MotorAdapter { motor -> mLeftMotor = Motor.fromString(motor) }
        mBinding.settingsMotorsLeftWheel.layoutManager = LinearLayoutManager(requireContext())
        mBinding.settingsMotorsLeftWheel.adapter = mLeftMotorAdapter
        LinearSnapHelper().attachToRecyclerView(mBinding.settingsMotorsLeftWheel)
        mLeftMotorAdapter.submitList(motors)

        mRightMotorAdapter = MotorAdapter { motor -> mRightMotor = Motor.fromString(motor) }
        mBinding.settingsMotorsRightWheel.layoutManager = LinearLayoutManager(requireContext())
        mBinding.settingsMotorsRightWheel.adapter = mRightMotorAdapter
        LinearSnapHelper().attachToRecyclerView(mBinding.settingsMotorsRightWheel)
        mRightMotorAdapter.submitList(motors)

        /* Configure saving */
        mSaveButton = mBinding.settingsSave
        mSaveButton.setOnClickListener { save() }

        /* Restore previous configuration */
        mModeSwitch = mBinding.settingsMode
        mReferenceSwitch = mBinding.settingsReference

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
        val ev3 = Ev3Service(ctx)
        val devices = ev3.listPaired()
        mDeviceAdapter.submitList(devices)
    }

    private fun save() {
        val settings = Settings(
            driveMode       = if (mModeSwitch.isChecked) DriveMode.TANK else DriveMode.ARCADE,
            driveReference  = if (mReferenceSwitch.isChecked) DriveReference.ROBOT_CENTRIC else DriveReference.FIELD_CENTRIC,
            device          = mDevice,
            leftWheel       = mLeftMotor,
            rightWheel      = mRightMotor
        )
        mData.update(settings)

        lifecycleScope.launch { mRepository.save(settings) }
        findNavController().popBackStack()
    }

    private fun load(settings : Settings) {

        mModeSwitch.isChecked = (settings.driveMode == DriveMode.TANK)

        mReferenceSwitch.isChecked = (settings.driveReference == DriveReference.ROBOT_CENTRIC)

        mBinding.settingsMotorsLeftWheel.smoothScrollToPosition(settings.leftWheel.ordinal)
        mBinding.settingsMotorsRightWheel.smoothScrollToPosition(settings.rightWheel.ordinal)

        val position = mDeviceAdapter.currentList.indexOf(settings.device)
        if (position >= 0) { mBinding.settingsDeviceRecycler.smoothScrollToPosition(position) }

    }

}
