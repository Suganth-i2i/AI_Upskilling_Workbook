package com.medtronic.mitg.shruti.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.medtronic.mitg.shruti.model.BluetoothDeviceInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modern Bluetooth manager that handles all Bluetooth operations
 * Follows SOLID principles and provides reactive streams
 */
@Singleton
class BluetoothManager @Inject constructor(
    private val context: Context
) {
    
    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    
    /**
     * Checks if Bluetooth is supported on this device
     */
    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null
    
    /**
     * Checks if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
    
    /**
     * Checks if location permission is granted (required for Bluetooth discovery)
     */
    fun hasLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    /**
     * Starts Bluetooth discovery and returns a Flow of discovered devices
     */
    fun startDiscovery(): Flow<BluetoothDeviceInfo> = callbackFlow {
        if (!isBluetoothSupported()) {
            close(BluetoothException("Bluetooth not supported on this device"))
            return@callbackFlow
        }
        
        if (!isBluetoothEnabled()) {
            close(BluetoothException("Bluetooth is not enabled"))
            return@callbackFlow
        }
        
        if (!hasLocationPermission()) {
            close(BluetoothException("Location permission required for Bluetooth discovery"))
            return@callbackFlow
        }
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        
                        device?.let { bluetoothDevice ->
                            if (bluetoothDevice.name != null) {
                                val deviceInfo = BluetoothDeviceInfo(
                                    name = bluetoothDevice.name,
                                    address = bluetoothDevice.address,
                                    deviceClass = getDeviceClass(bluetoothDevice),
                                    isPaired = bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED
                                )
                                trySend(deviceInfo)
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        close()
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        
        context.registerReceiver(receiver, filter)
        
        try {
            val success = bluetoothAdapter?.startDiscovery() ?: false
            if (!success) {
                close(BluetoothException("Failed to start Bluetooth discovery"))
            }
        } catch (e: SecurityException) {
            close(BluetoothException("Bluetooth permission denied"))
        }
        
        awaitClose {
            try {
                bluetoothAdapter?.cancelDiscovery()
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Receiver might already be unregistered
            }
        }
    }
    
    /**
     * Gets paired devices
     */
    fun getPairedDevices(): Flow<List<BluetoothDeviceInfo>> = flow {
        if (!isBluetoothSupported()) {
            throw BluetoothException("Bluetooth not supported on this device")
        }
        
        if (!isBluetoothEnabled()) {
            throw BluetoothException("Bluetooth is not enabled")
        }
        
        val pairedDevices = bluetoothAdapter?.bondedDevices?.mapNotNull { device ->
            if (device.name != null) {
                BluetoothDeviceInfo(
                    name = device.name,
                    address = device.address,
                    deviceClass = getDeviceClass(device),
                    isPaired = true
                )
            } else null
        } ?: emptyList()
        
        emit(pairedDevices)
    }
    
    /**
     * Enables Bluetooth (returns true if already enabled or successfully enabled)
     */
    fun enableBluetooth(): Boolean {
        return bluetoothAdapter?.isEnabled == true || bluetoothAdapter?.enable() == true
    }
    
    /**
     * Gets device class icon based on Bluetooth device class
     */
    private fun getDeviceClass(device: BluetoothDevice): String {
        val deviceClass = device.bluetoothClass.deviceClass
        
        return when (deviceClass) {
            0 -> DeviceClass.MISC
            in 256..280 -> DeviceClass.COMPUTER
            in 512..532 -> DeviceClass.PHONE
            768 -> DeviceClass.NETWORKING
            in 1024..1096 -> DeviceClass.AUDIO_VIDEO
            in 1280..1472 -> DeviceClass.PERIPHERAL
            1536 -> DeviceClass.IMAGING
            in 1792..1812 -> DeviceClass.WEARABLE
            in 2048..2068 -> DeviceClass.TOY
            in 2304..2332 -> DeviceClass.HEALTH
            1664 -> DeviceClass.PERIPHERAL
            7936 -> DeviceClass.UNCATEGORIZED
            else -> DeviceClass.UNCATEGORIZED
        }
    }
    
    /**
     * Device class constants
     */
    object DeviceClass {
        const val MISC = "misc"
        const val COMPUTER = "computer"
        const val PHONE = "phone"
        const val NETWORKING = "networking"
        const val AUDIO_VIDEO = "audio_video"
        const val PERIPHERAL = "peripheral"
        const val IMAGING = "imaging"
        const val WEARABLE = "wearable"
        const val TOY = "toy"
        const val HEALTH = "health"
        const val UNCATEGORIZED = "uncategorized"
    }
}

/**
 * Custom exception for Bluetooth operations
 */
class BluetoothException(message: String) : Exception(message) 