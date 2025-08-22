/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Manages socket I/O with concurrent inputs and outputs
   ------------------------------------------------------- */
package org.mantabots.robosoccer.utils

/* System includes */
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.io.EOFException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CancellationException

/* Android includes */
import android.util.Log

/* Kotlin includes */
import kotlin.math.min

/* Kotlinx includes */
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class ConcurrentAccessSocketMessaging(input: InputStream, output: OutputStream, factory: FrameFactory) : Closeable {

    private var mInput: InputStream? = null
    private var mOutput: OutputStream? = null
    private var mFactory : FrameFactory? = null


    private val mMutex = Mutex()
    private var mScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mPendings = ConcurrentHashMap<Int, CompletableDeferred<ByteArray>>()
    private var mReaderJob: Job? = null
    private var mStarted = false

    init {
        mInput = input
        mOutput = output
        mFactory = factory
    }

    fun isStarted(): Boolean { return mStarted}

    fun start() {
        if (mReaderJob == null) {
            mReaderJob = mScope.launch { readLoop() }
        }
        mStarted = true
    }

    suspend fun stop() = withContext(Dispatchers.IO) {
        if(mStarted) {
            mMutex.withLock {
                mReaderJob?.cancel()
                val ex = CancellationException("Closing messaging")
                mPendings.values.forEach { it.completeExceptionally(ex) }
                mPendings.clear()
                mReaderJob?.join()
                mInput = null
                mOutput = null
                mReaderJob = null
                mStarted = false
            }
        }
    }

    override fun close() {
        // ensure this runs even if caller scope is cancelled
        runBlocking(NonCancellable) {
            try { stop() } catch (_: Throwable) { /* swallow on shutdown */ }
        }
    }

    suspend fun sendAndAwait(frame: Frame, timeoutMs: Long = 1500): Frame? {

        var result:Frame? = null

        check(mStarted) { "Messaging not started" }

        val identifier = frame.identifier()
        val wait = CompletableDeferred<ByteArray>()
        mPendings[identifier] = wait

        try {
            Log.i("ConcurrentAccessSocketMessaging:sendAndAwait","Sending message with id $identifier")

            // Serialize writes to avoid interleaving frames
            mMutex.withLock {
                mOutput!!.write(frame.payload())
                mOutput!!.flush()
            }
        } catch (t: Throwable) {
            mPendings.remove(identifier)
            throw t
        }

        try {
            withTimeout(timeoutMs) {
                wait.await()
                result = mFactory!!.fromPayload(wait.getCompleted())
            }
        } catch (_: Throwable) {
            // Clean up the pending if we timed out or were cancelled
            mPendings.remove(identifier)
        }

        Log.i("ConcurrentAccessSocketMessaging:sendAndAwait","Returning response $identifier with ${result?.log()}")
        return result
    }

    private suspend fun readLoop() {
        try {
            while (mStarted && mScope.isActive) {

                val frame = mFactory!!.fromInputStream(mInput!!)
                if(frame.isValid()) {

                    val id = frame.identifier()
                    Log.i("ConcurrentAccessSocketMessaging:readLoop","Receiving response $id")

                    // Route to waiter; drop if nobody waits (late/duplicate reply)
                    mPendings.remove(id)?.complete(frame.payload())
                }
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

    companion object {

         suspend fun readByteCooperative(stream: InputStream, pollMs: Long = 20): Byte? {
            val ctx = currentCoroutineContext()
            while (true) {
                ctx.ensureActive()
                val avail = stream.available()
                if (avail > 0) {
                    val b = stream.read()
                    return if (b == -1) null else b.toByte()
                }
                delay(pollMs) // yield so cancellation/shutdown can proceed
            }
        }

        suspend fun readFullyCooperative(
            stream: InputStream,
            dst: ByteArray,
            off: Int,
            len: Int,
            pollMs: Long = 10
        ) {
            var got = 0
            val ctx = currentCoroutineContext()
            while (got < len) {
                ctx.ensureActive()
                val avail = stream.available()
                if (avail > 0) {
                    val toRead = min(len - got, avail)
                    val r = stream.read(dst, off + got, toRead)
                    if (r == -1) throw EOFException("EOF in body")
                    got += r
                } else {
                    delay(pollMs) // release the thread; allows cancel/close to take effect
                }
            }
        }
    }

}