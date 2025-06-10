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

data class UserSettings(
    var driveMode: DriveMode,
    var driveReference: DriveReference,
    var device: String)

class Settings : ViewModel() {
    private val _state = MutableStateFlow(UserSettings(DriveMode.ARCADE, DriveReference.ROBOT_CENTRIC, ""))
    val  state: StateFlow<UserSettings> = _state.asStateFlow()

    fun update(new: UserSettings)           { _state.value = new }
    fun device(new: String)                 { _state.value.device = new}
    fun driveMode(new: DriveMode)           { _state.value.driveMode = new}
    fun driveReference(new: DriveReference) { _state.value.driveReference = new}
}

