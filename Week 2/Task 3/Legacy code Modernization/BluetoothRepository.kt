package com.medtronic.mitg.shruti.bluetooth

import com.medtronic.mitg.shruti.model.BluetoothDeviceInfo
import com.medtronic.mitg.shruti.preference.SecurePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Bluetooth operations
 * Provides a clean interface for Bluetooth device management
 */
@Singleton
class BluetoothRepository @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val securePreferences: SecurePreferences
) {
    
    /**
     * Gets all paired devices
     */
    fun getPairedDevices(): Flow<Result<List<BluetoothDeviceInfo>>> {
        return bluetoothManager.getPairedDevices()
            .map { devices -> Result.success(devices) }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }
    
    /**
     * Starts device discovery and returns discovered devices
     */
    fun startDeviceDiscovery(): Flow<Result<BluetoothDeviceInfo>> {
        return bluetoothManager.startDiscovery()
            .map { device -> Result.success(device) }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }
    
    /**
     * Checks if Bluetooth is supported and enabled
     */
    fun checkBluetoothStatus(): BluetoothStatus {
        return when {
            !bluetoothManager.isBluetoothSupported() -> BluetoothStatus.NotSupported
            !bluetoothManager.isBluetoothEnabled() -> BluetoothStatus.Disabled
            !bluetoothManager.hasLocationPermission() -> BluetoothStatus.NoLocationPermission
            else -> BluetoothStatus.Ready
        }
    }
    
    /**
     * Enables Bluetooth
     */
    fun enableBluetooth(): Boolean = bluetoothManager.enableBluetooth()
    
    /**
     * Saves the selected printer device
     */
    suspend fun saveSelectedPrinter(device: BluetoothDeviceInfo) {
        securePreferences.saveBluetoothPrinter(
            name = device.name,
            address = device.address,
            deviceClass = device.deviceClass
        )
    }
    
    /**
     * Gets the saved printer device
     */
    suspend fun getSavedPrinter(): BluetoothDeviceInfo? {
        return securePreferences.getBluetoothPrinter()?.let { printer ->
            BluetoothDeviceInfo(
                name = printer.name,
                address = printer.address,
                deviceClass = printer.deviceClass,
                isPaired = true
            )
        }
    }
    
    /**
     * Checks if the given device matches the saved printer
     */
    suspend fun isSavedPrinter(device: BluetoothDeviceInfo): Boolean {
        val savedPrinter = getSavedPrinter()
        return savedPrinter?.address == device.address
    }
    
    /**
     * Clears the saved printer
     */
    suspend fun clearSavedPrinter() {
        securePreferences.clearBluetoothPrinter()
    }
}

/**
 * Represents the current Bluetooth status
 */
sealed class BluetoothStatus {
    object Ready : BluetoothStatus()
    object NotSupported : BluetoothStatus()
    object Disabled : BluetoothStatus()
    object NoLocationPermission : BluetoothStatus()
} 