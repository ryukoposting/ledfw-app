package com.evanperrygrove.fwcom.device

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.*

sealed class BleCharacteristic(val characteristicUuid: UUID, val serviceUuid: UUID) {
    companion object {
        private val deviceInfoServiceUuid = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
        private val metadataServiceUuid = UUID.fromString("a2773000-e035-13ae-4647-0e0437dd272a")
        private val userAppServiceUuid = UUID.fromString("a2773100-e035-13ae-4647-0e0437dd272a")
        private val dmxServiceUuid = UUID.fromString("a2775000-e035-13ae-4647-0e0437dd272a")

        internal val notificationDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    object ManufacturerName: BleCharacteristic(
        UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb"),
        deviceInfoServiceUuid)

    object SerialNumber: BleCharacteristic(
        UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb"),
        deviceInfoServiceUuid)

    object RdmUid: BleCharacteristic(
        UUID.fromString("a2773001-e035-13ae-4647-0e0437dd272a"),
        metadataServiceUuid)

    object SystemControl: BleCharacteristic(
        UUID.fromString("a2773002-e035-13ae-4647-0e0437dd272a"),
        metadataServiceUuid)

    object StatusMessage: BleCharacteristic(
        UUID.fromString("a2773040-e035-13ae-4647-0e0437dd272a"),
        metadataServiceUuid)

    object Program: BleCharacteristic(
        UUID.fromString("a2773101-e035-13ae-4647-0e0437dd272a"),
        userAppServiceUuid)

    object AppInfo: BleCharacteristic(
        UUID.fromString("a2773102-e035-13ae-4647-0e0437dd272a"),
        userAppServiceUuid)

    object AppName: BleCharacteristic(
        UUID.fromString("a2773103-e035-13ae-4647-0e0437dd272a"),
        userAppServiceUuid)

    object AppProvider: BleCharacteristic(
        UUID.fromString("a2773104-e035-13ae-4647-0e0437dd272a"),
        userAppServiceUuid)

    object DmxInfo: BleCharacteristic(
        UUID.fromString("a2773110-e035-13ae-4647-0e0437dd272a"),
        userAppServiceUuid)

    object DmxExplorer: BleCharacteristic(
        UUID.fromString("a2773111-e035-13ae-4647-0e0437dd272a"),
        userAppServiceUuid)

    object DmxValues: BleCharacteristic(
        UUID.fromString("a2775001-e035-13ae-4647-0e0437dd272a"),
        dmxServiceUuid)

    object DmxConfig: BleCharacteristic(
        UUID.fromString("a2775010-e035-13ae-4647-0e0437dd272a"),
        dmxServiceUuid)


    fun findIn(services: Collection<BluetoothGattService>?): BluetoothGattCharacteristic? =
        services
            ?.find { service -> service.uuid == serviceUuid }
            ?.characteristics
            ?.find { char -> char.uuid == characteristicUuid}

    fun matches(characteristic: BluetoothGattCharacteristic): Boolean =
        characteristic.uuid == characteristicUuid
}

@SuppressLint("MissingPermission")
fun BluetoothGattCharacteristic.enableNotifications(gatt: BluetoothGatt) {
    gatt.setCharacteristicNotification(this, true)
    val descriptor = getDescriptor(BleCharacteristic.notificationDescriptor)
    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
    gatt.writeDescriptor(descriptor)
}

@SuppressLint("MissingPermission")
fun BluetoothGattCharacteristic.disableNotifications(gatt: BluetoothGatt) {
    gatt.setCharacteristicNotification(this, false)
    val descriptor = getDescriptor(BleCharacteristic.notificationDescriptor)
    descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
    gatt.writeDescriptor(descriptor)
}
