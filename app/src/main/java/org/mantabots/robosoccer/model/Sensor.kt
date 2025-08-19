/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Robot motors options
   ------------------------------------------------------- */
package org.mantabots.robosoccer.model

/* Android import */
import android.os.Parcelable

/* Kotlinx import */
import kotlinx.parcelize.Parcelize

@Parcelize                /* makes it bundle-friendly for NavComponent */
enum class Sensor(val text:String, val command: Int, val port:Int) : Parcelable {
    ONE("1",0x01,0x10), TWO("2",0x02,0x11), THREE("3",0x04,0x12), FOUR("4",0x08,0x13);

    companion object {
        /**
         * Convert a string ( “1”, “2”, “3”, “4” … ) to the enum.
         * @throws IllegalArgumentException if the text doesn’t match any motor.
         */
        @JvmStatic
        fun fromString(text: String): Sensor =
            entries.firstOrNull { it.text.equals(text, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown motor: $text")
    }
}