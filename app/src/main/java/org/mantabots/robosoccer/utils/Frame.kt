/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Manages socket messages generic class
   ------------------------------------------------------- */
package org.mantabots.robosoccer.utils

interface Frame {
    fun size() : Int
    fun identifier(): Int
    fun payload() : ByteArray
    fun log() : String
    fun isValid() : Boolean
}