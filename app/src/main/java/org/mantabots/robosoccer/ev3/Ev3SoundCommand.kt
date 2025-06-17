/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Ev3 sound commands
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

/* Android import */
import android.os.Parcelable

/* Kotlinx import */
import kotlinx.parcelize.Parcelize

@Parcelize                /* makes it bundle-friendly for NavComponent */
enum class Ev3SoundCommand(val code: Int) : Parcelable {
    BREAK(0x01),
    PLAY(0x02),
    REPEAT(0x03);
}