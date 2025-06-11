/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 controller service
   ------------------------------------------------------- */
package org.mantabots.robosoccer.repository

/* System includes */
import java.util.UUID

/* Android includes */
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.Manifest

/* Androidx includes */
import androidx.core.content.ContextCompat

/** Ev3Service.kt – keeps the socket alive **/
class Ev3Service(context: Context) {

    private val mContext = context
    private val sUuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")


    /** List all paired EV3 devices */
    fun listPaired() : List<String> {

        // Android 12+ runtime permission
        var needPermission = Build.VERSION.SDK_INT >= 31
        var permission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT )

        return if (needPermission && permission != PackageManager.PERMISSION_GRANTED) {
            emptyList()  // caller should request permission first
        } else {
            BluetoothAdapter.getDefaultAdapter()
                ?.bondedDevices
                ?.filter { it.uuids.any { it.uuid == sUuid } }
                ?.sortedBy { it.name }
                ?.mapNotNull { it.name }
                ?: emptyList()

        }
    }

}