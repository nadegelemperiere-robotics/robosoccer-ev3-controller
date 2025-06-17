/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 bluetooth message formatting
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

/* Androidx includes */

/* Kotlinx includes */

class Ev3Message(id: Int, reply: Boolean)
{
    val sDirectCommandReply = 0x00
    val sDirectCommandNoReply = 0x80
    val sReplySize = 4

    val mMessage = mutableListOf<Byte>()
    var mLength = 7

    init {
        mMessage.add(((mLength - 2) and 0x00FF).toByte())
        mMessage.add((((mLength - 2) and 0xFF00) shr 8).toByte())
        mMessage.add(((id - 2) and 0x00FF).toByte())
        mMessage.add((((id - 2) and 0xFF00) shr 8).toByte())
        if(reply) { mMessage.add(sDirectCommandReply.toByte()) }
        else { mMessage.add(sDirectCommandNoReply.toByte()) }
        if(reply) { mMessage.add(sReplySize.toByte()) }
        else { mMessage.add(0x00.toByte()) }
        mMessage.add(0x00.toByte())
    }

    fun addCode(value: Ev3OpCode) {
        mMessage.add(value.code.toByte())
        mLength += 1
        updateSize()

    }

    fun addLC0(value: Byte)
    {
        mMessage.add(value)
        mLength += 1
        updateSize()
    }

    fun addLC1(value: Byte)
    {
        mMessage.add(Ev3LCCode.LC1.code.toByte())
        mMessage.add(value)
        mLength += 2
        updateSize()
    }

    fun addLC2(value: Short)
    {
        mMessage.add(Ev3LCCode.LC2.code.toByte())
        mMessage.add((value.toInt() and 0x00FF).toByte())
        mMessage.add(((value.toInt() shr 8) and 0x00FF).toByte())
        mLength += 3
        updateSize()
    }

    fun addLCS(value:String) {

        val bytes = value.toByteArray(Charsets.US_ASCII)

        mMessage.add(Ev3LCCode.LCS.code.toByte())
        for (c in 0..bytes.size) {
            mMessage.add(bytes[c])
        }
        mMessage.add(0x00.toByte())

        mLength += 2 + bytes.size
        updateSize()
    }

    fun get(): ByteArray {
        return mMessage.toByteArray()
    }

    private fun updateSize() {
        mMessage[0] = (((mLength - 2) and 0x00FF).toByte())
        mMessage[1] = ((((mLength - 2) and 0xFF00) shr 8).toByte())

    }

}
