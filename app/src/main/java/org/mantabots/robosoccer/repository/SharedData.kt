/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Application wide shared data
   ------------------------------------------------------- */

package org.mantabots.robosoccer.repository

/* Android includes */
import android.content.Context

/* Androidx includes */
import androidx.lifecycle.ViewModel

/* Kotlinx includes */
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/* ---- SharedData.kt ---- */
class SharedData : ViewModel() {

    private val _state = MutableStateFlow(Ev3Service())
    val state: StateFlow<Ev3Service> = _state.asStateFlow()

}

