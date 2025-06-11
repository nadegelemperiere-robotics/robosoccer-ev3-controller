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
import org.mantabots.robosoccer.model.Settings

import org.mantabots.robosoccer.proto.DriveModeProto
import org.mantabots.robosoccer.proto.DriveReferenceProto
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
            device = proto.device
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
                .build()
        }
    }
}