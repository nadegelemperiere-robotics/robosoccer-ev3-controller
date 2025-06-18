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
enum class Motor(val text:String, val command: Int, val port:Int) : Parcelable {
    A("A",0x01,0x10), B("B",0x02,0x11), C("C",0x04,0x12), D("D",0x08,0x13);

    companion object {
        /**
         * Convert a string ( “A”, “b”, “c”, “D” … ) to the enum.
         * @throws IllegalArgumentException if the text doesn’t match any motor.
         */
        @JvmStatic
        fun fromString(text: String): Motor =
            entries.firstOrNull { it.text.equals(text, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown motor: $text")
    }
}