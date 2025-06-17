package org.mantabots.robosoccer.ev3

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
/* makes it bundle-friendly for NavComponent */
enum class ConnectionState(val identifier: Int) : Parcelable {
    NONE(0),
    LISTEN(1),
    CONNECTING(2),
    CONNECTED(3)
}