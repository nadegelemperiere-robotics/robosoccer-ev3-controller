/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 controller view
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ui.controller

/* Androidx includes */
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ControllerViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is controller Fragment"
    }
    val text: LiveData<String> = _text
}