/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Datastore service
   ------------------------------------------------------- */
package org.mantabots.robosoccer.repository

/* System includes */
import java.io.InputStream
import java.io.OutputStream

/* Android includes */
import android.content.Context

/* Androidx includes */
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore

/* Kotlinx includes */
import kotlinx.coroutines.flow.map

/* Local includes */
import org.mantabots.robosoccer.model.DriveMode
import org.mantabots.robosoccer.model.Motor
import org.mantabots.robosoccer.model.Settings

import org.mantabots.robosoccer.proto.DriveModeProto
import org.mantabots.robosoccer.proto.MotorProto
import org.mantabots.robosoccer.proto.SettingsProto

/* ---------- 1. Serializer ---------- */

object SettingsSerializer : Serializer<SettingsProto> {
    override val defaultValue = SettingsProto
        .newBuilder()
        .setModeValue(DriveModeProto.ARCADE.number)
        .setDevice("")
        .setLeft(MotorProto.B)
        .setRight(MotorProto.C)
        .setSecond(MotorProto.UNDEFINED)
        .setFirst(MotorProto.UNDEFINED)
        .build()!!

    override suspend fun readFrom(input: InputStream) =
        SettingsProto.parseFrom(input)!!

    override suspend fun writeTo(t: SettingsProto, output: OutputStream) =
        t.writeTo(output)
}

/* ---------- 2. DataStore property-delegate ---------- */

private val Context.settingsStore: DataStore<SettingsProto> by dataStore(
    fileName = "robosoccer_settings.pb",
    serializer = SettingsSerializer
)

/* ---------- 3. Repository ---------- */

class SettingsRepository(private val context: Context) {

    val settings = context.settingsStore.data.map { proto ->
        Settings(
            driveMode = when (proto.mode) {
                DriveModeProto.TANK -> DriveMode.TANK
                DriveModeProto.ARCADE -> DriveMode.ARCADE
                DriveModeProto.UNRECOGNIZED -> DriveMode.ARCADE
            },
            device = proto.device,
            left = when (proto.left) {
                MotorProto.A -> Motor.A
                MotorProto.B -> Motor.B
                MotorProto.C -> Motor.C
                MotorProto.D -> Motor.D
                MotorProto.UNRECOGNIZED -> Motor.B
                MotorProto.UNDEFINED -> Motor.B
            },
            leftInverted = proto.leftInverted,
            right = when (proto.right) {
                MotorProto.A -> Motor.A
                MotorProto.B -> Motor.B
                MotorProto.C -> Motor.C
                MotorProto.D -> Motor.D
                MotorProto.UNRECOGNIZED -> Motor.C
                MotorProto.UNDEFINED -> Motor.C
            },
            rightInverted = proto.rightInverted,
            first = when (proto.first) {
                MotorProto.A -> Motor.A
                MotorProto.B -> Motor.B
                MotorProto.C -> Motor.C
                MotorProto.D -> Motor.D
                MotorProto.UNDEFINED -> null
                MotorProto.UNRECOGNIZED -> null
            },
            firstInverted = proto.firstInverted,
            second = when (proto.second) {
                MotorProto.A -> Motor.A
                MotorProto.B -> Motor.B
                MotorProto.C -> Motor.C
                MotorProto.D -> Motor.D
                MotorProto.UNDEFINED -> null
                MotorProto.UNRECOGNIZED -> null
            },
            secondInverted = proto.secondInverted
        )
    }

    suspend fun save(new: Settings) {
        context.settingsStore.updateData { old ->
            old.toBuilder()
                .setModeValue(
                    if (new.driveMode == DriveMode.TANK) DriveModeProto.TANK.number
                    else DriveModeProto.ARCADE.number
                )
                .setDevice(new.device)
                .setLeftValue(
                    if (new.left == Motor.A) MotorProto.A.number
                    else if (new.left == Motor.B) MotorProto.B.number
                    else if (new.left == Motor.C) MotorProto.C.number
                    else if (new.left == Motor.D) MotorProto.D.number
                    else MotorProto.UNDEFINED.number
                )
                .setLeftInverted(new.leftInverted)
                .setRightValue(
                    if (new.right == Motor.A) MotorProto.A.number
                    else if (new.right == Motor.B) MotorProto.B.number
                    else if (new.right == Motor.C) MotorProto.C.number
                    else if (new.right == Motor.D) MotorProto.D.number
                    else MotorProto.UNDEFINED.number
                )
                .setRightInverted(new.rightInverted)
                .setFirstValue(
                    if (new.first == Motor.A) MotorProto.A.number
                    else if (new.first == Motor.B) MotorProto.B.number
                    else if (new.first == Motor.C) MotorProto.C.number
                    else if (new.first == Motor.D) MotorProto.D.number
                    else MotorProto.UNDEFINED.number
                )
                .setFirstInverted(new.firstInverted)
                .setSecondValue(
                    if (new.second == Motor.A) MotorProto.A.number
                    else if (new.second == Motor.B) MotorProto.B.number
                    else if (new.second == Motor.C) MotorProto.C.number
                    else if (new.second == Motor.D) MotorProto.D.number
                    else MotorProto.UNDEFINED.number
                )
                .setSecondInverted(new.secondInverted)
                .build()
        }
    }
}