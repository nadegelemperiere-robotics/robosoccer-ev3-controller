/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 bluetooth message formatting
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

/* Android includes */
import android.util.Log

/* Kotlin includes */
import kotlin.collections.copyOfRange

class Ev3Response(reply: ByteArray)
{

    var mStatus: Ev3Status = Ev3Status.DIRECT_REPLY_ERROR
    var mId: Int = -1
    var mBody: ByteArray = ByteArray(0)

    init {

        if(reply.size < 5) { mStatus = Ev3Status.INVALID_SIZE }
        else {

            val replySize = (reply[0].toInt() and 0xFF) or
                    ((reply[1].toInt() and 0xFF) shl 8)
            if(replySize != (reply.size -2)) { mStatus = Ev3Status.INVALID_SIZE }
            else {
                mId = (reply[2].toInt() and 0xFF) or ((reply[3].toInt() and 0xFF) shl 8)
                mBody = reply.copyOfRange(0, reply.size)
                mStatus = Ev3Status.fromValue(reply[4].toInt() and 0xFF)
            }

        }

    }

    constructor(id: Int) : this(byteArrayOf()) {
        mId = id
        mStatus = Ev3Status.DIRECT_REPLY_OK   // or maybe SYSTEM_REPLY_OK, depending on your semantics
        val content = mutableListOf<Byte>()
        content.add(((4) and 0x00FF).toByte())
        content.add((((4) and 0xFF00) shr 8).toByte())
        content.add(((id) and 0x00FF).toByte())
        content.add((((id) and 0xFF00) shr 8).toByte())
        content.add((Ev3Status.TIMEOUT.code and 0x00FF).toByte())
        content.add(((Ev3Status.TIMEOUT.code and 0xFF00) shr 8).toByte())
        mBody = content.toByteArray()
    }


    fun isStatusOK(): Boolean {
        return ((mStatus == Ev3Status.DIRECT_REPLY_OK) || (mStatus == Ev3Status.SYSTEM_REPLY_OK))
    }

    fun id() : Int { return mId}

    fun body() : ByteArray { return mBody }

}
