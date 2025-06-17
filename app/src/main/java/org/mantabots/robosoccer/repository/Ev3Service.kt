/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 controller service
   ------------------------------------------------------- */
package org.mantabots.robosoccer.repository

/* System includes */
import java.util.UUID
import java.io.IOException

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/* Local includes */
import org.mantabots.robosoccer.model.Motor

/** Ev3Service.kt – keeps the socket alive **/
@Suppress("DEPRECATION")
class Ev3Service()  {

    private var mDevice = ""
    private var mSocket: BluetoothSocket? = null
    private var mCounter = 0
    private val mMutex = Mutex()

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

        var result = false

        if((device == mDevice) && (mSocket?.isConnected == true)) { result = true }
        else {

            result = false

            if (mSocket != null) { disconnect() }

            val address = findMacByName(device, context)

            if (hasPermission(context)) {
                val dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)
                mSocket = dev.createRfcommSocketToServiceRecord(sUuid)
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                mMutex.withLock {
                    try {
                        mSocket?.connect()
                        result = true
                        mDevice = device
                    } catch (_: IOException) {
                        result = false
                    }
                }
            }
        }

        result
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        mMutex.withLock {
            mSocket?.runCatching { close() }
            mSocket = null
            delay(150)
        }
    }


    /** Set raw power (-100…100) on one output port. */
    suspend fun motorPower(port: Motor, power: Int) =
        sendDirectNoReply(
            byteArrayOf(
                0xA4.toByte(),      // opOUTPUT_POWER (correct opcode)
                0x00,               // layer 0
                port.byte,          // port bit field
                power.toByte()      // signed power
            )
        )

    /** Read raw value from an analog sensor (example: EV3-Ultrasonic). */
    suspend fun readSensor(port: Motor): Int {
        val reply = sendDirectWithReply(
            byteArrayOf(
                0x9A.toByte(),      // opINPUT_DEVICE
                0x00,               // sub-command: GET_RAW
                port.byte,
                0x00,               // handle = 0
                0x01                // expected length = 1 byte
            )
        )
        return reply.getOrNull(5)?.toInt()?.and(0xFF)
            ?: throw IOException("Invalid reply length")
    }
    private suspend fun sendDirectNoReply(body: ByteArray) =
        send(0x80.toByte(), body, expectReply = false)

    private suspend fun sendDirectWithReply(body: ByteArray): ByteArray =
        send(0x00.toByte(), body, expectReply = true)

    /** Builds LEGO-EV3 Direct-Command frame: [lenLo lenHi counterLo counterHi type …body]. */
    private suspend fun send(
        msgType: Byte,
        body: ByteArray,
        expectReply: Boolean
    ): ByteArray = withContext(Dispatchers.IO) {

        val id      = mCounter++
        val length  = body.size + 3                    // type + 2-byte counter
        val header  = byteArrayOf(
            (length and 0xFF).toByte(),
            (length shr 8).toByte(),
            (id and 0xFF).toByte(),
            (id shr 8).toByte(),
            msgType
        )

        mSocket?.let { s ->
            s.outputStream.write(header + body)

            if (!expectReply) return@withContext ByteArray(0)

            /* ---- read reply (2-byte length prefix) ---- */
            val lenLo = s.inputStream.read()
            val lenHi = s.inputStream.read()
            if (lenLo < 0 || lenHi < 0) throw IOException("EV3 closed connection")

            val len = lenLo or (lenHi shl 8)
            ByteArray(len).also { s.inputStream.read(it) }
        } ?: throw IllegalStateException("Socket is null / not connected")
    }

}
