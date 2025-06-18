/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 bluetooth message formatting
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

class Ev3Reply(id: Int, reply: ByteArray)
{

    var mStatus: Ev3Status = Ev3Status.DIRECT_REPLY_ERROR

    init {

        if(reply.size < 5) { mStatus = Ev3Status.INVALID_SIZE }
        else {

            val replySize = (reply[0].toInt() and 0xFF) or
                    ((reply[1].toInt() and 0xFF) shl 8)
            if(replySize != (reply.size -2)) { mStatus = Ev3Status.INVALID_SIZE }
            else {
                val replyId = (reply[2].toInt() and 0xFF) or
                        ((reply[3].toInt() and 0xFF) shl 8)
                if (id != replyId) {
                    mStatus = Ev3Status.INVALID_ID
                } else {
                    mStatus = Ev3Status.fromValue(reply[4].toInt() and 0xFF)
                }
            }

        }

    }

    fun isStatusOK(): Boolean {
        return ((mStatus == Ev3Status.DIRECT_REPLY_OK) || (mStatus == Ev3Status.SYSTEM_REPLY_OK))
    }

}
