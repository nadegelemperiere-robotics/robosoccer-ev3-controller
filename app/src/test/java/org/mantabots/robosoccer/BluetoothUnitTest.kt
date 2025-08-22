/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Bluetooth unit tests
   ------------------------------------------------------- */
package org.mantabots.robosoccer

/* System includes */
import java.util.UUID

/* Android includes */
import android.Manifest
import android.app.Application
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid

/* Androidx includes */
import androidx.test.core.app.ApplicationProvider

/* Junit includes */
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

/* Robolectric includes */
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowBluetoothDevice

/* Component under test */
import org.mantabots.robosoccer.utils.Bluetooth

@RunWith(RobolectricTestRunner::class)
class BluetoothUnitTest {

    /** initialize(): pre‑S should mark granted=true and not request runtime perms */
    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // pre‑S so initialize() sets granted=true
    fun initialize_ShouldGrantBluetoothAccessWhenAndroidVersionLessThanS() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val bt = Bluetooth()
        bt.initialize(activity)
        assertTrue(bt.isGranted())
    }

    /** analysePermissionsResult(): S+, one denied -> returns message and granted=false */
    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31
    fun analysePermissionsResult_ShouldNotGrantBluetoothAccessWhenPermissionDenied() {
        val bt = Bluetooth()
        val requestCode = 123
        val grantResults = intArrayOf(
            PackageManager.PERMISSION_DENIED, // BLUETOOTH_CONNECT
            PackageManager.PERMISSION_GRANTED // BLUETOOTH_SCAN
        )
        val msg = bt.analysePermissionsResult(requestCode, grantResults)
        assertTrue(msg.contains("Permission refused for"))
        assertFalse(bt.isGranted())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31
    fun analysePermissionsResult_ShouldGrantBluetoothAccessWhenPermissionGranted() {
        val bt = Bluetooth()
        val requestCode = 123
        val grantResults = intArrayOf(
            PackageManager.PERMISSION_GRANTED, // BLUETOOTH_CONNECT
            PackageManager.PERMISSION_GRANTED // BLUETOOTH_SCAN
        )
        val msg = bt.analysePermissionsResult(requestCode, grantResults)
        assertTrue(msg.isEmpty())
        assertTrue(bt.isGranted())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // S+ path executes your permission branch
    fun listDevices_ShouldFilterByUuidWhenPermissionGranted() {

        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val app: Application = ApplicationProvider.getApplicationContext<Application>()

        val shadowApp = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
        shadowApp.grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        val pmShadow = Shadows.shadowOf(app.packageManager)
        pmShadow.setSystemFeature(PackageManager.FEATURE_BLUETOOTH, true)

        val btManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter: BluetoothAdapter = btManager.adapter
            ?: error("BluetoothAdapter is null; check FEATURE_BLUETOOTH is enabled")

        val shadowAdapter = Shadows.shadowOf(adapter)

        val deviceA: BluetoothDevice =
            ShadowBluetoothDevice.newInstance("00:11:22:33:44:55").also {
                Shadows.shadowOf(it).setName("EV3-A")
                Shadows.shadowOf(it).setUuids(arrayOf(ParcelUuid(SPP)))
            }

        val deviceB: BluetoothDevice =
            ShadowBluetoothDevice.newInstance("AA:BB:CC:DD:EE:FF").also {
                Shadows.shadowOf(it).setName("Headphones")
                Shadows.shadowOf(it).setUuids(arrayOf(ParcelUuid(UUID.randomUUID())))
            }

        val deviceC: BluetoothDevice =
            ShadowBluetoothDevice.newInstance("66:77:88:99:AA:BB").also {
                Shadows.shadowOf(it).setName("EV3-B")
                Shadows.shadowOf(it).setUuids(arrayOf(ParcelUuid(SPP)))
            }

        // Install bonded devices into the shadow adapter
        shadowAdapter.setBondedDevices(setOf(deviceA, deviceB, deviceC))

        val bt = Bluetooth()
        val names = bt.listDevices(ctx, SPP).sorted()

        assertEquals(listOf("EV3-A", "EV3-B"), names)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun listDevices_ShouldReturnEmptyWhenPermissionDenied() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()

        val shadowApp = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
        shadowApp.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        val bt = Bluetooth()
        val names = bt.listDevices(ctx, SPP)
        assertTrue(names.isEmpty())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun findMacAddressByName_ShouldReturnAddressWhenFoundAndPermissionGranted() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val app: Application = ApplicationProvider.getApplicationContext<Application>()

        val shadowApp = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
        shadowApp.grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        val pmShadow = Shadows.shadowOf(app.packageManager)
        pmShadow.setSystemFeature(PackageManager.FEATURE_BLUETOOTH, true)

        val btManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter: BluetoothAdapter = btManager.adapter
            ?: error("BluetoothAdapter is null; check FEATURE_BLUETOOTH is enabled")

        val device: BluetoothDevice =
            ShadowBluetoothDevice.newInstance("00:11:22:33:44:55").also {
                Shadows.shadowOf(it).setName("EV3-X")
            }

        Shadows.shadowOf(adapter).setBondedDevices(setOf(device))

        val bt = Bluetooth()
        val mac = bt.findMacAddressByName(ctx, "ev3-x")
        assertEquals("00:11:22:33:44:55", mac)

    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun findMacAddressByName_ShouldReturnNullWhenPermissionDeniedOrNotFound() {

        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val app: Application = ApplicationProvider.getApplicationContext<Application>()

        val shadowApp = Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
        shadowApp.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        val bt = Bluetooth()
        assertNull(bt.findMacAddressByName(ctx, "anything"))

        // Now grant but with empty bonded devices
        shadowApp.grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        val pmShadow = Shadows.shadowOf(app.packageManager)
        pmShadow.setSystemFeature(PackageManager.FEATURE_BLUETOOTH, true)

        val btManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter: BluetoothAdapter = btManager.adapter
            ?: error("BluetoothAdapter is null; check FEATURE_BLUETOOTH is enabled")
        Shadows.shadowOf(adapter).setBondedDevices(emptySet())

        assertNull(bt.findMacAddressByName(ctx, "EV3"))
    }


    companion object {
        private val SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}