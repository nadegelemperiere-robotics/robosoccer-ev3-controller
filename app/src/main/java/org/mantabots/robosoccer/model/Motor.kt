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
enum class Motor : Parcelable {
    A,
    B,
    C,
    D;

    /* Helper for human-readable labels */
    fun displayName() = when (this) {
        A -> "A"
        B -> "B"
        C -> "C"
        D -> "D"
    }

    companion object {
        /**
         * Convert a string ( “A”, “b”, “c”, “D” … ) to the enum.
         * @throws IllegalArgumentException if the text doesn’t match any motor.
         */
        @JvmStatic
        fun fromString(text: String): Motor =
            entries.firstOrNull { it.name.equals(text, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown motor: $text")
    }
}