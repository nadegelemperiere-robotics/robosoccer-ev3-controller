/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 controller service
   ------------------------------------------------------- */
package org.mantabots.robosoccer.repository

/* Android includes */
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.Manifest

/* Androidx includes */
import androidx.core.content.ContextCompat

/** Ev3Service.kt – keeps the socket alive **/
class Ev3Service(context: Context) {

    private val mContext = context

    /** List all paired EV3 devices */
    fun listPaired() : List<String> {

        // Android 12+ runtime permission
        var needPermission = Build.VERSION.SDK_INT >= 31
        var permission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT )

        if (needPermission && permission != PackageManager.PERMISSION_GRANTED) {
            return emptyList()  // caller should request permission first
        }
        else {
//            return BluetoothAdapter.getDefaultAdapter()
//                ?.bondedDevices
//                ?.filter { it.name?.startsWith("EV3", ignoreCase = true) == true }
//                ?.sortedBy { it.name }
//                ?.mapNotNull { it.name }
//                ?: emptyList()

            return  listOf("test1", "test2", "test3")
        }
    }

}