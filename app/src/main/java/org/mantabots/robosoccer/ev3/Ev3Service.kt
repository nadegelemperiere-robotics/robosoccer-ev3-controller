/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 controller service
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

/* System includes */
import java.io.IOException

/* Android includes */
import android.content.Context

/* Local includes */
import org.mantabots.robosoccer.model.Motor
import org.mantabots.robosoccer.model.Sensor

/** Ev3Service.kt – keeps the socket alive **/
class Ev3Service() {

    private var mMessaging : Ev3Messaging = Ev3Messaging()

    fun listPaired(ctx: Context): List<String> {
        return Ev3Device.listPaired(ctx)
    }

    suspend fun connect(context: Context, device: String) : Boolean {
        return mMessaging.connect(context, device)
    }

    fun disconnect() { mMessaging.close() }

    /** Set raw power (-100…100) on one output port. */
    suspend fun power(motor: Motor, power: Float) {

        val message = Ev3Command(true)

        message.addCode(Ev3OpCode.OUTPUT_SPEED)
        message.addLC0(0)
        message.addLC0(motor.command.toByte())
        message.addLC1((power * 100).toInt().toByte())
        message.addCode(Ev3OpCode.OUTPUT_START)
        message.addLC0(0)
        message.addLC0(motor.command.toByte())

        val response = mMessaging.sendAndAwait(message)
        if (!response.isStatusOK()) { throw IOException("Motor answer not received") }
    }

    suspend fun playSound(frequency: Int, duration: Int, level: Int) {

        val message = Ev3Command(true)

        message.addCode(Ev3OpCode.SOUND)
        message.addLC0(Ev3SoundCommand.BREAK.code.toByte())
        message.addLC1(level.toByte())
        message.addLC2(frequency.toShort())
        message.addLC2(duration.toShort())

        val response = mMessaging.sendAndAwait(message)
        if (!response.isStatusOK()) { throw IOException("Sound answer not received") }
    }

    /** Read raw value from an analog sensor (example: EV3-Ultrasonic). */
    suspend fun readSensor(sensor: Sensor) {

        val message = Ev3Command(true)

        message.addCode(Ev3OpCode.READ)
        message.addLC0(0)
        message.addLC0(sensor.command.toByte())
        message.addLC0(0)
        message.addLC0(0)
        message.addLC0(0)

        mMessaging.sendAndAwait(message)
    }

}
