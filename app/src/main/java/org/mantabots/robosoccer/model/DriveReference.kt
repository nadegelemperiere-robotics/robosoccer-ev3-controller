/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Robot drive reference options
   ------------------------------------------------------- */
package org.mantabots.robosoccer.model

/* Android import */
import android.os.Parcelable

/* Kotlinx import */
import kotlinx.parcelize.Parcelize

@Parcelize                /* makes it bundle-friendly for NavComponent */
enum class DriveReference(val text:String) : Parcelable {
    ROBOT_CENTRIC("Robot centric"),               /* orientation is seen as variation relative to robot */
    FIELD_CENTRIC("Field centric");               /* orientation is seen as absolute position to reach on the field */
}