/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 devices management
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

/* System includes */
import java.util.UUID

/* Android includes */
import android.annotation.SuppressLint
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/* Androidx includes */
import androidx.core.content.ContextCompat

/* Kotlin includes */
import kotlin.collections.orEmpty

class Ev3Device {

    companion object {

        val sUuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        @SuppressLint("MissingPermission")
        fun listPaired(ctx: Context): List<String> {
            var result: List<String> = emptyList()

            if (hasPermission(ctx)) {
                result = BluetoothAdapter.getDefaultAdapter()
                    ?.bondedDevices
                    ?.filter { it.uuids?.any { u -> u.uuid == sUuid } == true }
                    ?.map { it.name }
                    .orEmpty()
            }

            return result
        }

        @SuppressLint("MissingPermission")
        fun findMacByName(target: String, context: Context): String? {

            var result: String? = ""

            if (hasPermission(context)) {
                result = BluetoothAdapter.getDefaultAdapter()
                    .bondedDevices
                    .firstOrNull { it.name.equals(target, ignoreCase = true) }
                    ?.address
            }

            return result
        }

        fun hasPermission(ctx: Context): Boolean {

            val needPermission = Build.VERSION.SDK_INT >= 31
            var permission = PackageManager.PERMISSION_DENIED
            if (needPermission) {
                permission =
                    ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT)
            }

            return !needPermission || permission == PackageManager.PERMISSION_GRANTED
        }
    }
}