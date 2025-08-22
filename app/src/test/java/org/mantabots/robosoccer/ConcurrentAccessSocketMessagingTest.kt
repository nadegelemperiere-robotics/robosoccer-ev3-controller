/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Concurrent Socket Messaging tests
   ------------------------------------------------------- */
package org.mantabots.robosoccer

/* System includes */
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.InputStream
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger

/* Android includes */
import android.util.Log

/* Kotlin includes */
import kotlin.collections.joinToString
import kotlin.random.Random

/* Kotlinx includes */
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/* Junit includes */
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.runner.RunWith

/* Robolectric includes */
import org.robolectric.RobolectricTestRunner

/* Component under test */
import org.mantabots.robosoccer.utils.ConcurrentAccessSocketMessaging
import org.mantabots.robosoccer.utils.Frame
import org.mantabots.robosoccer.utils.FrameFactory

class TestFrame(): Frame {

    private var mPayload : ByteArray? = null
    private var mValid : Boolean = false

    override fun identifier(): Int { return mPayload!![1].toInt() }
    override fun size(): Int { return mPayload!![0].toInt() }
    override fun payload() : ByteArray { return mPayload!! }
    override fun log() : String { return mPayload!!.joinToString(prefix = "[", postfix = "]")}
    override fun isValid() : Boolean { return mValid }

    fun setPayload(payload: ByteArray) {
        mPayload = payload
        mValid = true
    }
}

class TestFrameFactory() : FrameFactory {

    override suspend fun fromInputStream(stream: InputStream): Frame {
        val result = TestFrame()
        val len = ConcurrentAccessSocketMessaging.readByteCooperative(stream)
        if (len != null && len.toInt() > 0) {
            val buf = ByteArray(len.toInt() + 1)
            buf[0] = len
            ConcurrentAccessSocketMessaging.readFullyCooperative(stream, buf, 1, len.toInt())
            result.setPayload(buf)
        }
        return result
    }

    override fun fromPayload(payload: ByteArray): Frame {
        val result = TestFrame()
        result.setPayload(payload)
        return result
    }

}

@RunWith(RobolectricTestRunner::class)
class ConcurrentAccessSocketMessagingTest {

    private lateinit var mInput: PipedInputStream         // what Link reads
    private lateinit var mOutput: PipedOutputStream       // we write replies here

    private lateinit var mReceiverInput: PipedInputStream         // what Link writes
    private lateinit var mReceiverOutput: PipedOutputStream      // we read commands here

    private lateinit var mScope: CoroutineScope


    @Before
    fun setUp() {
        mScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        // Link to send message from the code to the receiver
        mReceiverInput = PipedInputStream(4096)
        mOutput = PipedOutputStream(mReceiverInput)

        // Link to send answers from the receiver back to the code
        mInput = PipedInputStream(4096)
        mReceiverOutput = PipedOutputStream(mInput)

    }

    @After
    fun tearDown() {
        mScope.cancel(CancellationException("Ending test"))
        try { mInput.close() } catch (_: Throwable) {}
        try { mOutput.close() } catch (_: Throwable) {}
        try { mReceiverInput.close() } catch (_: Throwable) {}
        try { mReceiverOutput.close() } catch (_: Throwable) {}
    }


    // ---------- Tests ----------

    @Test
    fun sendAndAwait_ShouldAssociateReplyToCorrectMessageWhenOutOfOrder() = runBlocking {

        val factory = TestFrameFactory()
        val messaging = ConcurrentAccessSocketMessaging(mInput, mOutput, factory)
        messaging.start()

        // When two requests are sent concurrently
        val bodyA = byteArrayOf(0x03, 0x10, 0x11, 0x12)
        val bodyB = byteArrayOf(0x03, 0x20, 0x21, 0x22)

        val d1 = mScope.async<Frame?> {
            messaging.sendAndAwait(factory.fromPayload(bodyA), timeoutMs = 2000)
        }
        val d2 = mScope.async<Frame?> {
            messaging.sendAndAwait(factory.fromPayload(bodyB), timeoutMs = 2000)
        }

        // Capture what Link wrote so we can parse the message-ids it used
        var out1 = factory.fromInputStream(mReceiverInput) as TestFrame
        var out2 = factory.fromInputStream(mReceiverInput) as TestFrame

        // Then send replies in REVERSE order to test out-of-order routing.
        val replyForSecond = byteArrayOf(0x04, out2.identifier().toByte(), 0x02, 0x55, 0x66)
        mReceiverOutput.write(replyForSecond)
        mReceiverOutput.flush()

        val replyForFirst = byteArrayOf(0x05, out1.identifier().toByte(), 0x03, 0x77, 0x78, 0x79)
        mReceiverOutput.write(replyForFirst)
        mReceiverOutput.flush()

        val got1 = d1.await()
        val got2 = d2.await()

        assertNotNull(got1)
        assertNotNull(got2)
        Log.i("sendAndAwait_ShouldAssociateReplyToCorrectMessageWhenOutOfOrder", got1!!.log())
        Log.i("sendAndAwait_ShouldAssociateReplyToCorrectMessageWhenOutOfOrder", got2!!.log())
        assertArrayEquals(replyForSecond, (got2 as TestFrame?)!!.payload())
        assertArrayEquals(replyForFirst,  (got1 as TestFrame?)!!.payload())

        messaging.close()
    }

    @Test
    fun sendAndAwait_ShouldReceiveAnswerWhenManyConcurrentMessages() = runBlocking {

        val factory = TestFrameFactory()
        val messaging = ConcurrentAccessSocketMessaging(mInput, mOutput, factory)
        messaging.start()

        val n = 20
        var i = AtomicInteger(1)

        // Kick off N requests
        val jobs = (0 until n).map {
            mScope.async {
                val payload = Random.nextBytes(5)
                val id = i.getAndAdd(1)
                payload[0] = 0x4
                payload[1] = id.toByte()
                val resp = mScope.async<Frame?> {
                    messaging.sendAndAwait(factory.fromPayload(payload), timeoutMs = 20000)
                }
                val res = resp.await()
                assertNotNull(res)
                assertEquals(id,res!!.identifier().toInt())
            }
        }

        for (j in 0..< n) {
            var out = factory.fromInputStream(mReceiverInput) as TestFrame
            val reply = byteArrayOf(0x04, out.identifier().toByte(), 0x02, 0x55, 0x66)
            mReceiverOutput.write(reply)
            mReceiverOutput.flush()
        }

        jobs.awaitAll()
        messaging.close()
    }

    @Test
    fun sendAndAwait_ShouldTimeoutWhenNoResponseSent() = runBlocking {

        val factory = TestFrameFactory()
        val messaging = ConcurrentAccessSocketMessaging(mInput, mOutput, factory)
        messaging.start()

        // Send a request but DO NOT send a reply
        val resp = mScope.async<Frame?> {
            messaging.sendAndAwait(factory.fromPayload(byteArrayOf(0x01, 0x02)), timeoutMs = 2000)
        }

        val res = resp.await()
        assertNull(res)

        messaging.close()
    }

    @Test
    fun sendAndAwait_ShouldCloseGracefullyWhenInterruptedWhileWaiting() = runBlocking {

        val factory = TestFrameFactory()
        val messaging = ConcurrentAccessSocketMessaging(mInput, mOutput, factory)
        messaging.start()

        var resp: Deferred<Frame?>? = null
        try {
            resp = mScope.async<Frame?> {
                messaging.sendAndAwait(factory.fromPayload(byteArrayOf(0x02, 0x01)), timeoutMs = 5_000)
            }
        }
        catch (_: CancellationException) {
        }

        // Give send time to register pending
        withContext(Dispatchers.IO) { /* small yield */ }

        // Close the link -> should cancel waiter
        messaging.close()
        val res = resp!!.await()
        assertNull(res)
    }

}