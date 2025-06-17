/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Music note enum
   ------------------------------------------------------- */
package org.mantabots.robosoccer.model

/* Android import */
import android.os.Parcelable

/* Kotlinx import */
import kotlinx.parcelize.Parcelize

@Parcelize                /* makes it bundle-friendly for NavComponent */
enum class Note(val byte1: Byte, val byte2: Byte) : Parcelable {
    C(0x06, 0x01),
    CSHARP(0x15, 0x01),
    D(0x26, 0x01),
    DSHARP(0x37, 0x01),
    E(0x4A, 0x01),
    F(0x5D, 0x01),
    G(0x88.toByte(), 0x01),
    GSHARP(0x9F.toByte(), 0x01),
    A (0xB8.toByte(), 0x01),
    ASHARP(0xD2.toByte(), 0x01),
    B(0xEE.toByte(), 0x01);
}
