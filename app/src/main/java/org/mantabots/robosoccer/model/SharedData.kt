/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Settings shared data
   ------------------------------------------------------- */
package org.mantabots.robosoccer.model

/* Androidx includes */
import androidx.lifecycle.ViewModel

/* Kotlinx includes */
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


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
            val list = dupes.joinToString { it.displayName() }
            return ValidationResult.Error("Motor(s) $list are selected twice.")
        }

        return ValidationResult.Ok
    }
}

class SharedData : ViewModel() {
    private val _state = MutableStateFlow(Settings(DriveMode.ARCADE, DriveReference.FIELD_CENTRIC, "", Motor.B, Motor.C, null, null))
    val state: StateFlow<Settings> = _state.asStateFlow()



    fun update(new: Settings)               { _state.value = new }
    fun device(new: String)                 { _state.value.device = new}
    fun driveMode(new: DriveMode)           { _state.value.driveMode = new}
    fun driveReference(new: DriveReference) { _state.value.driveReference = new}
    fun leftWheel(new: Motor)               { _state.value.leftWheel = new}
    fun rightWheel(new: Motor)              { _state.value.rightWheel = new}
}

