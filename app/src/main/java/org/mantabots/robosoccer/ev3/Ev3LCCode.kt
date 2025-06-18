/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Ev3 message opcodes
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

/* Android import */
import android.os.Parcelable

/* Kotlinx import */
import kotlinx.parcelize.Parcelize

@Parcelize                /* makes it bundle-friendly for NavComponent */
enum class Ev3LCCode(val code: Int) : Parcelable {
    LC1(0x81),
    LC2(0x82),
    LCS(0x84);
}