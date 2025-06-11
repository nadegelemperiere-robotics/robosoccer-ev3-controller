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
import android.widget.TextView

/* Androidx includes */
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/* Kotlin includes */
import kotlin.getValue

/* Kotlinx includes */
import kotlinx.coroutines.launch

/* Local includes */
import org.mantabots.robosoccer.databinding.FragmentSettingsBinding
import org.mantabots.robosoccer.repository.Ev3Service
import org.mantabots.robosoccer.repository.SettingsRepository
import org.mantabots.robosoccer.R
import org.mantabots.robosoccer.model.DriveMode
import org.mantabots.robosoccer.model.DriveReference
import org.mantabots.robosoccer.model.Settings
import org.mantabots.robosoccer.model.SharedData

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val mBinding get() = _binding!!
    private val mData: SharedData by activityViewModels()
    private var mDevice = ""
    private lateinit var mRepository: SettingsRepository
    private lateinit var mAdapter : DeviceAdapter
    private lateinit var mSwipe : SwipeRefreshLayout
    private lateinit var mSaveButton : Button
    private lateinit var mModeSwitch : Switch
    private lateinit var mReferenceSwitch : Switch

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

        mSwipe = mBinding.settingsDeviceSwipe
        mSwipe.setOnRefreshListener { refreshDevices() }

        val refreshButton : ImageButton = mBinding.settingsDeviceRefresh
        refreshButton.setOnClickListener{ refreshDevices() }

        val deviceChoices : RecyclerView = mBinding.settingsDeviceRecycler
        mAdapter = DeviceAdapter { device ->
            mDevice = device
        }
        deviceChoices.layoutManager = LinearLayoutManager(requireContext())
        deviceChoices.adapter = mAdapter
        deviceChoices.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        refreshDevices()

        mModeSwitch = mBinding.settingsMode
        mReferenceSwitch = mBinding.settingsReference

        mSaveButton = mBinding.settingsSave
        mSaveButton.setOnClickListener { save() }

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
        mSwipe.isRefreshing = true
        mAdapter.submitList(devices)
        mSwipe.isRefreshing = false

    }

    private fun save() {
        val settings = Settings(
            driveMode       = if (mModeSwitch.isChecked) DriveMode.TANK else DriveMode.ARCADE,
            driveReference  = if (mReferenceSwitch.isChecked) DriveReference.ROBOT_CENTRIC else DriveReference.FIELD_CENTRIC,
            device       = mDevice
        )
        mData.update(settings)
        // ② persist to DataStore through the repository
        viewLifecycleOwner.lifecycleScope.launch {
            mRepository.save(settings)      // suspend fun
        }
        findNavController().popBackStack()
    }

}

/* ── ListAdapter ── */
private class DeviceAdapter(
    private val click: (String) -> Unit
) : ListAdapter<String, DeviceVH>(Diff) {

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        DeviceVH(LayoutInflater.from(p.context)
            .inflate(R.layout.device_choice, p, false))

    override fun onBindViewHolder(h: DeviceVH, pos: Int) =
        h.bind(getItem(pos), click)

    private object Diff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(a: String, b: String) = a == b
        override fun areContentsTheSame(a: String, b: String) = a == b
    }
}

private class DeviceVH(view: View) : RecyclerView.ViewHolder(view) {
    private val name = view.findViewById<TextView>(R.id.device_choice)
    fun bind(d: String, click: (String) -> Unit) {
        name.text = d
        itemView.setOnClickListener { click(d) }
    }
}