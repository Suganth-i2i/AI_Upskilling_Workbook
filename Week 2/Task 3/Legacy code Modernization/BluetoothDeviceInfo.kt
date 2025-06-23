package com.medtronic.mitg.shruti.model

import androidx.annotation.Keep

/**
 * Data class representing Bluetooth device information
 * 
 * @property name Device name (non-null)
 * @property address MAC address of the device (non-null)
 * @property deviceClass Type/category of the device
 * @property isPaired Whether the device is paired with this device
 */
@Keep
data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val deviceClass: String,
    val isPaired: Boolean = false
) {
    
    init {
        require(name.isNotBlank()) { "Device name cannot be blank" }
        require(address.isNotBlank()) { "Device address cannot be blank" }
        require(deviceClass.isNotBlank()) { "Device class cannot be blank" }
    }
    
    /**
     * Returns a display-friendly name for the device
     */
    fun getDisplayName(): String = name.ifBlank { "Unknown Device" }
    
    /**
     * Returns a formatted MAC address
     */
    fun getFormattedAddress(): String {
        return address.replace(":", "").chunked(2).joinToString(":").uppercase()
    }
    
    /**
     * Checks if this device is a printer (based on device class)
     */
    fun isPrinter(): Boolean = deviceClass == "peripheral" || deviceClass == "imaging"
    
    /**
     * Returns device type icon resource based on device class
     */
    fun getDeviceIcon(): Int {
        return when (deviceClass) {
            "computer" -> android.R.drawable.ic_menu_computer
            "phone" -> android.R.drawable.ic_menu_call
            "audio_video" -> android.R.drawable.ic_media_play
            "peripheral", "imaging" -> android.R.drawable.ic_menu_edit
            "wearable" -> android.R.drawable.ic_menu_view
            "health" -> android.R.drawable.ic_menu_help
            else -> android.R.drawable.ic_menu_more
        }
    }
    
    companion object {
        /**
         * Creates a BluetoothDeviceInfo from the legacy BluetoothModel
         */
        fun fromLegacyModel(model: BluetoothModel): BluetoothDeviceInfo {
            return BluetoothDeviceInfo(
                name = model.name,
                address = model.mac,
                deviceClass = model.type,
                isPaired = false
            )
        }
    }
} 