/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Robot drive mode options
   ------------------------------------------------------- */
package org.mantabots.robosoccer.model

/* Android import */
import android.os.Parcelable

/* Kotlinx import */
import kotlinx.parcelize.Parcelize

@Parcelize                /* makes it bundle-friendly for NavComponent */
enum class DriveMode(val text: String) : Parcelable {
    ARCADE("Arcade"),               /* one joystick, coding for power and orientation */
    TANK("Tank");                   /* two joysticks, controlling left and right robot wheels */
}