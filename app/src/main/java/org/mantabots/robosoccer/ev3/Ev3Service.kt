/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 controller service
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

/* System includes */
import java.util.UUID
import java.io.IOException
import java.io.Closeable

/* Android includes */
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.Manifest
import android.annotation.SuppressLint

/* Androidx includes */
import androidx.core.content.ContextCompat

/* Kotlinx includes */
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/* Local includes */
import org.mantabots.robosoccer.model.Motor

/** Ev3Service.kt – keeps the socket alive **/
@Suppress("DEPRECATION")
class Ev3Service() : Closeable {

    private var mDevice = ""
    private var mSocket: BluetoothSocket? = null
    private var mCounter = 0
    private val mMutex = Mutex()
    private var mConnected = false

    companion object {

        private val sUuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        /** List all paired EV3 devices */
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
        private fun findMacByName(target: String, context: Context): String? {

            var result: String? = ""

            if (hasPermission(context)) {
                result = BluetoothAdapter.getDefaultAdapter()
                    .bondedDevices
                    .firstOrNull { it.name.equals(target, ignoreCase = true) }
                    ?.address
            }

            return result
        }

        private fun hasPermission(ctx: Context): Boolean {

            val needPermission = Build.VERSION.SDK_INT >= 31
            var permission = PackageManager.PERMISSION_DENIED
            if(needPermission) {
                permission = ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT)
            }

            return !needPermission || permission == PackageManager.PERMISSION_GRANTED
        }

    }

    @SuppressLint("MissingPermission")
    suspend fun connect(context: Context, device: String):Boolean = withContext(Dispatchers.IO) {

        mConnected = false

        if((device == mDevice) && (mSocket?.isConnected == true)) { mConnected = true }
        else {

            mConnected = false

            if (mSocket != null) { disconnect() }

            val address = findMacByName(device, context)

            if (hasPermission(context)) {
                val dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)
                mSocket = dev.createRfcommSocketToServiceRecord(sUuid)
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                mMutex.withLock {
                    try {
                        mSocket?.connect()
                        mConnected = true
                        mDevice = device
                    } catch (_: IOException) {
                        mConnected = false
                    }
                }
            }
        }

        val result = mConnected
        result
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        mMutex.withLock {
            if(mSocket?.isConnected == true) { mSocket?.runCatching { close() } }
            mSocket = null
            mConnected = false
        }
    }


    /** Set raw power (-100…100) on one output port. */
    suspend fun power(motor: Motor, power: Float) {

        val message = Ev3Message(mCounter,true)

        message.addCode(Ev3OpCode.OUTPUT_SPEED)
        message.addLC0(0)
        message.addLC0(motor.command.toByte())
        message.addLC1((power*100).toInt().toByte())
        message.addCode(Ev3OpCode.OUTPUT_START)
        message.addLC0(0)
        message.addLC0(motor.command.toByte())

        val reply = Ev3Reply(mCounter, send(message.get(),true))
        if(!reply.isStatusOK()) { throw IOException("Motor starting failed") }

        mCounter ++
    }

    /** Read raw value from an analog sensor (example: EV3-Ultrasonic). */
    suspend fun readSensor(motor: Motor): Int {
        val reply = send(
            byteArrayOf(
                0x9A.toByte(),      // opINPUT_DEVICE
                0x00,               // sub-command: GET_RAW
                motor.port.toByte(),
                0x00,               // handle = 0
                0x01                // expected length = 1 byte
            ), true
        )
        return reply.getOrNull(5)?.toInt()?.and(0xFF)
            ?: throw IOException("Invalid reply length")
    }

    override fun close() {
        runBlocking { disconnect() }
    }

    /** Builds LEGO-EV3 Direct-Command frame: [lenLo lenHi counterLo counterHi type …body]. */
    private suspend fun send(
        message: ByteArray,
        expectReply: Boolean
    ): ByteArray = withContext(Dispatchers.IO) {

        var result: ByteArray = byteArrayOf(0)

        if ((mSocket != null) && mConnected) {

            mSocket?.let { s ->
                s.outputStream.write(message)
                s.outputStream.flush()

                if (!expectReply) return@withContext ByteArray(0)

                /* ---- read reply (2-byte length prefix) ---- */
                val lenLo = s.inputStream.read()
                val lenHi = s.inputStream.read()
                if (lenLo < 0 || lenHi < 0) throw IOException("EV3 closed connection")

                val len = lenLo or (lenHi shl 8)
                result = ByteArray(len + 2)
                result[0] = lenLo.toByte()
                result[1] = lenHi.toByte()
                s.inputStream.read(result, 2, len)
                result
            }
        }

        result
    }

}
