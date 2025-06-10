/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Main EV3 controller application
   ------------------------------------------------------- */
package org.mantabots.robosoccer

/* Android includes */
import android.Manifest
import android.os.Bundle
import android.os.Build
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager

/* Material includes */
import com.google.android.material.bottomnavigation.BottomNavigationView

/* Androidx includes */
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

/* Local includes */
import org.mantabots.robosoccer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mRequestCode = 123
    private val mPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.checkAndRequestBluetoothPermissions()

        val navView: BottomNavigationView = binding.navView

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHost.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_controller, R.id.navigation_settings)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        /* ── lock/unlock orientation per destination ─────────────────── */
        navController.addOnDestinationChangedListener { _, dest, _ ->
            requestedOrientation = when (dest.id) {
                R.id.navigation_controller ->  // joystick / drive screen
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                R.id.navigation_settings ->   // settings pane
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                else ->                       // any other future tab
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    private fun checkAndRequestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val denied = mPermissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (denied.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, denied.toTypedArray(), mRequestCode)
            }
        }
    }

}