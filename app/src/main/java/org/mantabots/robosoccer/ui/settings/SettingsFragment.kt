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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.SnapHelper

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
    private var mFirstMotor: Motor? = null
    private var mSecondMotor: Motor? = null

    private lateinit var mRepository: SettingsRepository
    private lateinit var mDeviceAdapter: DeviceAdapter
    private lateinit var mSaveButton: Button
    private lateinit var mModeSwitch: Switch
    private lateinit var mReferenceSwitch: Switch
    private lateinit var mLeftMotorAdapter: LeftMotorAdapter
    private lateinit var mLeftMotorSnap: SnapHelper
//    private lateinit var mRightMotorAdapter: RightMotorAdapter
//    private lateinit var mFirstMotorAdapter: FirstMotorAdapter
//    private lateinit var mSecondMotorAdapter: SecondMotorAdapter
//    private lateinit var mFirstMotorCheck: CheckBox
//    private lateinit var mSecondMotorCheck: CheckBox

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

        mLeftMotorAdapter = LeftMotorAdapter { motor -> mLeftMotor = Motor.fromString(motor) }
        mBinding.settingsMotorsLeftSelect.layoutManager = LinearLayoutManager(requireContext())
        mBinding.settingsMotorsLeftSelect.adapter = mLeftMotorAdapter
        mLeftMotorSnap = LinearSnapHelper()
        mLeftMotorSnap.attachToRecyclerView(mBinding.settingsMotorsLeftSelect)
        mLeftMotorAdapter.submitList(motors)

//        mRightMotorAdapter = RightMotorAdapter { motor -> mRightMotor = Motor.fromString(motor) }
//        mBinding.settingsMotorsRightSelect.layoutManager = LinearLayoutManager(requireContext())
//        mBinding.settingsMotorsRightSelect.adapter = mRightMotorAdapter
//        LinearSnapHelper().attachToRecyclerView(mBinding.settingsMotorsRightSelect)
//        mRightMotorAdapter.submitList(motors)
//
//        mFirstMotorAdapter = FirstMotorAdapter { motor -> mFirstMotor = Motor.fromString(motor) }
//        mBinding.settingsMotorsAttachment1Select.layoutManager = LinearLayoutManager(requireContext())
//        mBinding.settingsMotorsAttachment1Select.adapter = mFirstMotorAdapter
//        LinearSnapHelper().attachToRecyclerView(mBinding.settingsMotorsAttachment1Select)
//        mFirstMotorAdapter.submitList(motors)
//
//        mFirstMotorCheck = mBinding.settingsMotorsAttachment1Checkbox
//        mFirstMotorCheck.setOnClickListener { checkFirst(mFirstMotorCheck.isChecked) }
//
//        mSecondMotorAdapter = SecondMotorAdapter { motor -> mSecondMotor = Motor.fromString(motor) }
//        mBinding.settingsMotorsAttachment2Select.layoutManager = LinearLayoutManager(requireContext())
//        mBinding.settingsMotorsAttachment2Select.adapter = mSecondMotorAdapter
//        LinearSnapHelper().attachToRecyclerView(mBinding.settingsMotorsAttachment2Select)
//        mSecondMotorAdapter.submitList(motors)
//
//        mSecondMotorCheck = mBinding.settingsMotorsAttachment2Checkbox
//        mSecondMotorCheck.setOnClickListener { checkSecond(mSecondMotorCheck.isChecked) }

        /* Configure saving */
        mSaveButton = mBinding.settingsSave
        mSaveButton.setOnClickListener { save() }

        /* Restore previous configuration */
        mModeSwitch = mBinding.settingsModeSwitch
        mReferenceSwitch = mBinding.settingsReferenceSwitch

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

    private fun checkFirst(check: Boolean) {
        if (check) {
            mBinding.settingsMotorsAttachment1Select.visibility = View.VISIBLE
            mBinding.settingsMotorsAttachment1Checkbox.isChecked = true
            mFirstMotor = Motor.A
            mBinding.settingsMotorsAttachment1Select.smoothScrollToPosition(mFirstMotor!!.ordinal)
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
            mBinding.settingsMotorsAttachment2Select.smoothScrollToPosition(mSecondMotor!!.ordinal)
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
            driveReference = if (mReferenceSwitch.isChecked) DriveReference.ROBOT_CENTRIC else DriveReference.FIELD_CENTRIC,
            device = mDevice,
            leftWheel = mLeftMotor,
            rightWheel = mRightMotor,
            firstAttachment = mFirstMotor,
            secondAttachment = mSecondMotor
        )
        mData.update(settings)

        lifecycleScope.launch { mRepository.save(settings) }
        findNavController().popBackStack()
    }

    private fun load(settings : Settings) {

        mModeSwitch.isChecked = (settings.driveMode == DriveMode.TANK)
        mReferenceSwitch.isChecked = (settings.driveReference == DriveReference.ROBOT_CENTRIC)

        mDevice = settings.device
        var position = mDeviceAdapter.currentList.indexOf(mDevice)
        if (position >= 0) { mBinding.settingsDeviceRecycler.scrollToPosition(position) }

        mLeftMotor = settings.leftWheel
        position = mLeftMotorAdapter.currentList.indexOf(mLeftMotor.displayName())
        if (position >= 0) {
            mLeftMotorSnap.attachToRecyclerView(null)

            /* 2️⃣  Jump so the target row view exists (no animation) */
            mBinding.settingsMotorsLeftSelect.scrollToPosition(position)

            /* 3️⃣  When the layout pass finishes, compute EXACT pixel delta the helper wants
                   to centre THIS very row, and perform a tiny smoothScrollBy.               */
            mBinding.settingsMotorsLeftSelect.post {
                val view  = mBinding.settingsMotorsLeftSelect.layoutManager!!.findViewByPosition(position) ?: return@post
                val delta = mLeftMotorSnap.calculateDistanceToFinalSnap(mBinding.settingsMotorsLeftSelect.layoutManager!!, view) ?: return@post
                if (delta[0] != 0 || delta[1] != 0) {
                    mBinding.settingsMotorsLeftSelect.smoothScrollBy(delta[0], delta[1])
                }

                /* 4️⃣  Re-attach helper only AFTER the micro-scroll request was queued.
                        At this point the row is mathematically centred, so helper keeps it. */
                mLeftMotorSnap.attachToRecyclerView(mBinding.settingsMotorsLeftSelect)
            }
            val lm = mBinding.settingsMotorsLeftSelect.layoutManager as RecyclerView.LayoutManager
            val scroller = CenterSmoothScroller(requireContext()).apply {
                targetPosition = position
            }
            lm.startSmoothScroll(scroller)// 3️⃣ wait until the scroll really ends, THEN re-attach the helper
            mBinding.settingsMotorsLeftSelect.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        recyclerView.removeOnScrollListener(this)   // one-shot
                        mLeftMotorSnap.attachToRecyclerView(mBinding.settingsMotorsLeftSelect)     // re-attach
                    }
                }
            })
        }
//
//        mRightMotor = settings.rightWheel
//        mBinding.settingsMotorsRightSelect.smoothScrollToPosition(settings.rightWheel.ordinal)
//
//        if(settings.firstAttachment != null) {
//            checkFirst(true)
//            mBinding.settingsMotorsAttachment1Select.smoothScrollToPosition(settings.firstAttachment!!.ordinal)
//            mFirstMotor = settings.firstAttachment
//        }
//        else {
//            checkFirst(false)
//        }
//
//        if(settings.secondAttachment != null) {
//            checkSecond(true)
//            mBinding.settingsMotorsAttachment2Select.smoothScrollToPosition(settings.secondAttachment!!.ordinal)
//            mSecondMotor = settings.secondAttachment
//        }
//        else {
//            checkSecond(false)
//        }

    }

}


/** A SmoothScroller that ends with the target view centred vertically. */
class CenterSmoothScroller(ctx: Context) : LinearSmoothScroller(ctx) {

    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        val viewCenter = (viewStart + viewEnd) / 2
        val boxCenter  = (boxStart + boxEnd) / 2
        return boxCenter - viewCenter          // move so centres coincide
    }

    /** We only care about vertical snapping here. */
    override fun getVerticalSnapPreference() = SNAP_TO_START
}