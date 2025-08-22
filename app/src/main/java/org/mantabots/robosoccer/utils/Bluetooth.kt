/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Bluetooth connection manager
   ------------------------------------------------------- */
package org.mantabots.robosoccer.utils

/* System includes */
import java.util.UUID

/* Android includes */
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/* Androidx includes */
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.annotation.RequiresApi

/* Kotlin includes */
import kotlin.collections.orEmpty

class Bluetooth {

    companion object {

        private const val sRequestCode = 123

        @RequiresApi(Build.VERSION_CODES.S)
        private var sPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )

    }

    private var mGranted = false

    fun initialize(activity: Activity) {

        mGranted = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val denied = sPermissions.filter {
                ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
            }
            if (denied.isNotEmpty()) {
                ActivityCompat.requestPermissions(activity, denied.toTypedArray(), sRequestCode)
            }

        }
        else { mGranted = true }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun analysePermissionsResult(requestCode: Int, grantResults: IntArray): String
    {
        var result = ""

        if (requestCode == sRequestCode) {
            for (i in grantResults.indices) {
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    result += "Permission refused for " + sPermissions[i].toString()
                }
            }
        }

        mGranted = result.isEmpty()
        return result
    }

    fun isGranted(): Boolean { return mGranted }

    fun listDevices(context: Context, uuid: UUID) : List<String> {

        var result: List<String> = emptyList()

        if (checkPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
            result = BluetoothAdapter.getDefaultAdapter()
                ?.bondedDevices
                ?.filter { it.uuids?.any { u -> u.uuid == uuid } == true }
                ?.map { it.name }
                .orEmpty()
        }

        return result
    }

    fun findMacAddressByName(context: Context, name:String): String? {

        var result: String? = null

        if (checkPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
            result = BluetoothAdapter.getDefaultAdapter()
                .bondedDevices
                .firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?.address
        }

        return result
    }

    private fun checkPermission(ctx: Context, permission: String): Boolean {

        var result = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            result = (ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED)
        }

        return result
    }
}
