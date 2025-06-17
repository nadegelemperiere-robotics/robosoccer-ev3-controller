/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Ev3 message reply status
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

/* Android import */
import android.os.Parcelable

/* Kotlinx import */
import kotlinx.parcelize.Parcelize

@Parcelize                /* makes it bundle-friendly for NavComponent */
enum class Ev3Status(val code: Int) : Parcelable {
    DIRECT_REPLY_OK(0x02),
    SYSTEM_REPLY_OK(0x03),
    DIRECT_REPLY_ERROR(0x04),
    SYSTEM_REPLY_ERROR(0x05),
    INVALID_SIZE(0xDD),
    INVALID_ID(0xDE);

    companion object {
        /**
         * Convert a string ( “A”, “b”, “c”, “D” … ) to the enum.
         * @throws IllegalArgumentException if the text doesn’t match any motor.
         */
        @JvmStatic
        fun fromValue(value: Int): Ev3Status =
            Ev3Status.entries.firstOrNull { it.code == value }
                ?: throw IllegalArgumentException("Unknown EV3 status: $value")
    }
}
