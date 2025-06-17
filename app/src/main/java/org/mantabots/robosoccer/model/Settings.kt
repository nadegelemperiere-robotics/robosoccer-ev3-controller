/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Settings shared data
   ------------------------------------------------------- */
package org.mantabots.robosoccer.model

/* Androidx includes */

/* Kotlinx includes */


sealed interface ValidationResult {
    object Ok : ValidationResult
    data class Error(val message: String) : ValidationResult
}

data class Settings(
    var driveMode: DriveMode,
    var driveReference: DriveReference,
    var device: String,
    var leftWheel: Motor,
    var rightWheel: Motor,
    var firstAttachment: Motor?,
    var secondAttachment: Motor?
)
{
    fun check(): ValidationResult {

        /* ① device must be chosen */
        if (device.isBlank()) {  return ValidationResult.Error("Please select an EV3 device.")  }

        /* ② motors must be unique (ignore null attachments) */
        val motors = listOfNotNull(
            leftWheel,
            rightWheel,
            firstAttachment,
            secondAttachment
        )

        val dupes = motors.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        if (dupes.isNotEmpty()) {
            val list = dupes.joinToString { it.text }
            return ValidationResult.Error("Motor(s) $list are selected twice.")
        }

        return ValidationResult.Ok
    }
}

