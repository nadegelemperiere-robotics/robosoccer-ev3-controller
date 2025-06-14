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
import org.mantabots.robosoccer.model.DriveReference
import org.mantabots.robosoccer.model.Motor
import org.mantabots.robosoccer.model.Settings

import org.mantabots.robosoccer.proto.DriveModeProto
import org.mantabots.robosoccer.proto.DriveReferenceProto
import org.mantabots.robosoccer.proto.MotorProto
import org.mantabots.robosoccer.proto.SettingsProto

/* ---------- 1. Serializer ---------- */

object SettingsSerializer : Serializer<SettingsProto> {
    override val defaultValue = SettingsProto
        .newBuilder()
        .setModeValue(DriveModeProto.ARCADE.number)
        .setReferenceValue(DriveReferenceProto.ROBOT_CENTRIC.number)
        .setDevice("")
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
            driveReference = when (proto.reference) {
                DriveReferenceProto.ROBOT_CENTRIC -> DriveReference.ROBOT_CENTRIC
                DriveReferenceProto.FIELD_CENTRIC -> DriveReference.FIELD_CENTRIC
                DriveReferenceProto.UNRECOGNIZED -> DriveReference.FIELD_CENTRIC
            },
            device = proto.device,
            leftWheel = when (proto.left) {
                MotorProto.A -> Motor.A
                MotorProto.B -> Motor.B
                MotorProto.C -> Motor.C
                MotorProto.D -> Motor.D
                MotorProto.UNRECOGNIZED -> Motor.B
                MotorProto.UNDEFINED -> Motor.B
            },
            rightWheel = when (proto.right) {
                MotorProto.A -> Motor.A
                MotorProto.B -> Motor.B
                MotorProto.C -> Motor.C
                MotorProto.D -> Motor.D
                MotorProto.UNRECOGNIZED -> Motor.C
                MotorProto.UNDEFINED -> Motor.B
            },
            firstAttachment = when (proto.first) {
                MotorProto.A -> Motor.A
                MotorProto.B -> Motor.B
                MotorProto.C -> Motor.C
                MotorProto.D -> Motor.D
                MotorProto.UNDEFINED -> null
                MotorProto.UNRECOGNIZED -> null
            },
            secondAttachment = when (proto.second) {
                MotorProto.A -> Motor.A
                MotorProto.B -> Motor.B
                MotorProto.C -> Motor.C
                MotorProto.D -> Motor.D
                MotorProto.UNDEFINED -> null
                MotorProto.UNRECOGNIZED -> null
            }
        )
    }

    suspend fun save(new: Settings) {
        context.settingsStore.updateData { old ->
            old.toBuilder()
                .setModeValue(
                    if (new.driveMode == DriveMode.TANK) DriveModeProto.TANK.number
                    else DriveModeProto.ARCADE.number
                )
                .setReferenceValue(
                    if (new.driveReference == DriveReference.ROBOT_CENTRIC) DriveReferenceProto.ROBOT_CENTRIC.number
                    else DriveReferenceProto.FIELD_CENTRIC.number
                )
                .setDevice(new.device)
                .setLeftValue(
                    if (new.leftWheel == Motor.A) MotorProto.A.number
                    else if (new.leftWheel == Motor.B) MotorProto.B.number
                    else if (new.leftWheel == Motor.C) MotorProto.C.number
                    else if (new.leftWheel == Motor.D) MotorProto.D.number
                    else MotorProto.UNDEFINED.number
                )
                .setRightValue(
                    if (new.rightWheel == Motor.A) MotorProto.A.number
                    else if (new.rightWheel == Motor.B) MotorProto.B.number
                    else if (new.rightWheel == Motor.C) MotorProto.C.number
                    else if (new.rightWheel == Motor.D) MotorProto.D.number
                    else MotorProto.UNDEFINED.number
                )
                .setFirstValue(
                    if (new.firstAttachment == Motor.A) MotorProto.A.number
                    else if (new.firstAttachment == Motor.B) MotorProto.B.number
                    else if (new.firstAttachment == Motor.C) MotorProto.C.number
                    else if (new.firstAttachment == Motor.D) MotorProto.D.number
                    else MotorProto.UNDEFINED.number
                )
                .setSecondValue(
                    if (new.secondAttachment == Motor.A) MotorProto.A.number
                    else if (new.secondAttachment == Motor.B) MotorProto.B.number
                    else if (new.secondAttachment == Motor.C) MotorProto.C.number
                    else if (new.secondAttachment == Motor.D) MotorProto.D.number
                    else MotorProto.UNDEFINED.number
                )
                .build()
        }
    }
}