/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Ev3 messaging management
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

/* System includes */
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/* Android includes */
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log

/* Kotlinx includes */
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Ev3Messaging() : Closeable {

    private var mDevice = ""
    private var mSocket: BluetoothSocket? = null
    private val mConnectMutex = Mutex()
    private val mOutputMutex = Mutex()
    private var mConnected = false
    private var mScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mPendings = ConcurrentHashMap<Int, CompletableDeferred<ByteArray>>()
    private val mNextId = AtomicInteger(1) // 0..65535 (EV3 uses 2-byte counter)
    private var mReaderJob: Job? = null

    @SuppressLint("MissingPermission")
    suspend fun connect(context: Context, device: String): Boolean = withContext(Dispatchers.IO) {

        mConnected = false

        if ((device == mDevice) && (mSocket?.isConnected == true)) {
            mConnected = true
        } else {

            mConnected = false

            if (mSocket != null) {
                disconnect()
            }

            val address = Ev3Device.findMacByName(device,context)

            if (Ev3Device.hasPermission(context)) {

                val dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)
                mSocket = dev.createRfcommSocketToServiceRecord(Ev3Device.sUuid)
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                mConnectMutex.withLock {
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

        if (mConnected) {
            if (mReaderJob == null) {
                mReaderJob = mScope.launch { readLoop() }
            }
        }

        val result = mConnected
        result
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        mConnectMutex.withLock {
            if (mSocket?.isConnected == true) {
                mSocket?.runCatching { close() }
            }
            mReaderJob?.cancelAndJoin()
            val ex = CancellationException("EV3 link closed")
            mPendings.values.forEach { it.completeExceptionally(ex) }
            mPendings.clear()
            mSocket = null
            mReaderJob = null
            mConnected = false
        }
    }

    override fun close() {
        // ensure this runs even if caller scope is cancelled
        runBlocking(NonCancellable) {
            try { disconnect() } catch (_: Throwable) { /* swallow on shutdown */ }
        }
    }

    suspend fun sendAndAwait(command: Ev3Command, timeoutMs: Long = 1500): Ev3Response {

        check(mConnected) { "EV3 link is closed" }

        val id = nextMessageId()
        command.addId(id)
        val wait = CompletableDeferred<ByteArray>()
        mPendings[id] = wait
        var result = Ev3Response(id)

        try {
            Log.i("Sending message","Sending message with id ${id}")

            // Serialize writes to avoid interleaving frames
            mOutputMutex.withLock {
                mSocket?.outputStream?.write(command.get())
                mSocket?.outputStream?.flush()
            }
        } catch (t: Throwable) {
            mPendings.remove(id)
            throw t
        }

        try {
            withTimeout(timeoutMs) {
                wait.await()
                result = Ev3Response(wait.getCompleted())
            }

        } catch (t: Throwable) {
            // Clean up the pending if we timed out or were cancelled
            mPendings.remove(id)
        }

        Log.i("Returning response","Returning message ${result.id()} with ${result.body().joinToString(prefix = "[", postfix = "]")}")
        return result
    }

    private fun readLoop() {
        try {
            while (mConnected && mScope.isActive) {

                /* ---- read reply (2-byte length prefix) ---- */
                val lenLo = mSocket!!.inputStream.read()
                val lenHi = mSocket!!.inputStream.read()
                if (lenLo < 0 || lenHi < 0) throw IOException("EV3 closed connection")

                val len = lenLo or (lenHi shl 8)
                val result = ByteArray(len + 2)
                result[0] = lenLo.toByte()
                result[1] = lenHi.toByte()
                mSocket!!.inputStream.read(result, 2, len)
                val response = Ev3Response(result)


                Log.i("Receiving message","Receiving message with id ${response.id()}")

                // Route to waiter; drop if nobody waits (late/duplicate reply)
                mPendings.remove(response.id())?.complete(response.body())
            }
        } catch (_: CancellationException) {
            // normal shutdown
        } catch (t: Throwable) {
            // fail all waiters on unexpected I/O error
            val ex = t
            mPendings.entries.forEach { (_, d) -> d.completeExceptionally(ex) }
            mPendings.clear()
        }
    }

    private fun nextMessageId(): Int {
        while (true) {
            val cur = mNextId.get()
            var next = (cur + 1) and 0xFFFF
            if (next == 0) next = 1  // skip 0

            if (mNextId.compareAndSet(cur, next)) {
                // return the value we just reserved
                return cur and 0xFFFF
            }
        }
    }


}