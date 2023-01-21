package com.evanperrygrove.fwcom.device

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import com.evanperrygrove.fwcom.Device
import com.evanperrygrove.fwcom.device.bleprotocol.DmxConfigChar
import com.evanperrygrove.fwcom.device.bleprotocol.DmxExplorerRequest
import com.evanperrygrove.fwcom.device.bleprotocol.DmxExplorerResponse
import com.evanperrygrove.fwcom.device.bleprotocol.DmxInfoChar
import com.evanperrygrove.fwcom.dmx.DmxConfig
import com.evanperrygrove.fwcom.userapp.DmxInfo
import com.evanperrygrove.fwcom.util.Defer
import com.evanperrygrove.fwcom.util.StoredDevice
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class BleDevice(private val context: Context, private val storedDevice: StoredDevice, private val btDevice: BluetoothDevice): Device {
    private val eventQueue = ConcurrentLinkedQueue<QueuedEvent>()

    private var btGatt: BluetoothGatt? = null
    private var btServices: List<BluetoothGattService>? = null
    private var currentDmxInfo: DmxInfo? = null
    private val slotValues = Collections.synchronizedList(arrayListOf<Byte>())

    private val gattCallback = object: BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            Log.d("BleDevice", "onConnectionStateChange $status $newState")

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        onConnect.onConnectFinished(false, status)
                    } else {
                        if (btGatt != gatt) {
                            btServices = null
                            currentDmxInfo = null
                        }
                        btGatt = gatt

                        val gatt2 = gatt!!
                        startConnectionSetup(gatt2)
                    }
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    btGatt = null
                    btServices = null
                    currentDmxInfo = null
                    slotValues.clear()

                    eventQueue.clear()
                    onDisconnect.onDisconnectFinished(true, status)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            Log.d("BleDevice", "onServicesDiscovered $status")

            if (gatt != btGatt) return

            if (status != BluetoothGatt.GATT_SUCCESS) {
                onConnect.onConnectFinished(false, status)
            } else {
                finishConnectionSetup(btGatt!!)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)

            if (gatt != btGatt) return
            if (characteristic == null) return

            val value = characteristic.value?.clone()

            if (BleCharacteristic.SerialNumber.matches(characteristic)) {
                val event = eventQueue.find { it is QueuedEvent.ReadSerialNumber } as QueuedEvent.ReadSerialNumber?
                event?.let { eventQueue.remove(it) }
                event?.callback?.onSerialNumberReceived(value?.toString(Charsets.UTF_8))

            } else if (BleCharacteristic.ManufacturerName.matches(characteristic)) {
                val event = eventQueue.find { it is QueuedEvent.ReadManufacturerName } as QueuedEvent.ReadManufacturerName?
                event?.let { eventQueue.remove(it) }
                event?.callback?.onManufacturerNameReceived(value?.toString(Charsets.UTF_8))

            } else if (BleCharacteristic.DmxInfo.matches(characteristic) && value != null) {
                DmxInfoChar.parse(value)?.let {
                    currentDmxInfo = it
                    onDmxInfoChanged.onDmxInfoChanged(it)
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            if (gatt != btGatt) return
            if (characteristic == null) return

            val value = characteristic.value?.clone()

            if (BleCharacteristic.StatusMessage.matches(characteristic)) {
                value?.toString(Charsets.UTF_8)?.let {
                    onStatusMessage.onStatusMessageReceived(it)
                }

            } else if (BleCharacteristic.DmxExplorer.matches(characteristic) && value != null) {
                DmxExplorerResponse.SlotInfo.parse(value)?.let {
                    while (slotValues.size <= it.slotNumber) {
                        slotValues.add(0)
                    }
                    slotValues[it.slotNumber] = it.value.toByte()
                    onDmxSlotInfo.onDmxSlotInfoReceived(it)
                }
                DmxExplorerResponse.PersonalityInfo.parse(value)?.let {
                    val event = eventQueue.find { it is QueuedEvent.RequestDmxPersonalityInfoRange } as QueuedEvent.RequestDmxPersonalityInfoRange?
                    onDmxPersonalityInfo.onDmxPersonalityInfoReceived(it)

                    if (event != null && event.expecting == it.personalityNumber) {
                        if (event.expecting < event.last) {
                            event.expecting += 1
                            requestDmxPersonalityInfo(event.expecting)
                        } else {
                            Log.d("BleDevice", "Personality info range request finished")
                            eventQueue.remove(event)
                        }
                    }
                }

            } else if (BleCharacteristic.StatusMessage.matches(characteristic) && value != null) {
                val message = value.toString(Charsets.UTF_8)
                onStatusMessage.onStatusMessageReceived(message)

            } else if (BleCharacteristic.DmxValues.matches(characteristic) && value != null) {
                synchronized(slotValues) {
                    value.zip(slotValues).forEachIndexed { index, pair ->
                        val (new, old) = pair
                        if (new != old)
                            onSlotValueChanged.onSlotValueChanged(index, new.toUByte())
                    }

                    value.forEachIndexed { index, new ->
                        if (index >= slotValues.size)
                            onSlotValueChanged.onSlotValueChanged(index, new.toUByte())
                    }

                    slotValues.forEachIndexed { index, _ ->
                        if (index >= value.size)
                            onSlotValueChanged.onSlotValueChanged(index, 0.toUByte())
                    }

                    slotValues.clear()
                    slotValues.addAll(value.toList())
                }

            } else if (BleCharacteristic.DmxInfo.matches(characteristic) && value != null) {
                Log.d("DmxInfo", "received: ${value.toList()}")
                DmxInfoChar.parse(value)?.let {
                    currentDmxInfo = it
                    onDmxInfoChanged.onDmxInfoChanged(it)
                }
            }
        }
    }

    override val isConnected: Boolean
        get() = btGatt != null

    override val name: String
        get() = storedDevice.name

    override val address: String
        get() = storedDevice.address

    override val nickname: String?
        get() = storedDevice.nickname

    override var onConnect = Device.OnConnect { _, _ -> }
    override var onDisconnect = Device.OnDisconnectFinished { _, _ -> }
    override var onDmxPersonalityInfo = Device.OnDmxPersonalityInfoReceived { _ -> }
    override var onDmxSlotInfo = Device.OnDmxSlotInfoReceived { _ -> }
    override var onStatusMessage = Device.OnStatusMessageReceived { _ -> }
    override var onSlotValueChanged = Device.OnSlotValueChanged { _, _ -> }
    override var onDmxInfoChanged = Device.OnDmxInfoChanged { _ -> }

    @SuppressLint("MissingPermission")
    override fun connect() {
        if (isConnected) {
            Log.d("BleDevice", "Already connected, firing callback immediately")
            onConnect.onConnectFinished(true, BluetoothGatt.GATT_SUCCESS)
        } else {
            btDevice.connectGatt(context, false, gattCallback)
        }
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        if (!isConnected) {
            Log.d("BleDevice", "Already disconnected, firing callback immediately")
            onDisconnect.onDisconnectFinished(true, BluetoothGatt.GATT_SUCCESS)
        } else {
            btGatt?.disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    override fun readSerialNumber(callback: Device.OnSerialNumberReceived) {
        if (!isConnected) {
            Log.d("BleDevice", "Not connected, firing readSerialNumber callback immediately")
            callback.onSerialNumberReceived(null)
        } else when (val char = BleCharacteristic.SerialNumber.findIn(btServices)) {
            null -> callback.onSerialNumberReceived(null)
            else -> {
                val event = QueuedEvent.ReadSerialNumber(callback)
                eventQueue.add(event)

                val action = { gatt: BluetoothGatt? ->
                    gatt?.readCharacteristic(char) ?: true
                }
                doReliableOperation(action) { didSucceed ->
                    if (!didSucceed) {
                        eventQueue.remove(event)
                        event.callback.onSerialNumberReceived(null)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun readManufacturerName(callback: Device.OnManufacturerNameReceived) {
        if (!isConnected) {
            Log.d("BleDevice", "Not connected, firing readManufacturerName callback immediately")
            callback.onManufacturerNameReceived(null)
        } else when (val char = BleCharacteristic.ManufacturerName.findIn(btServices)) {
            null -> callback.onManufacturerNameReceived(null)
            else -> {
                val event = QueuedEvent.ReadManufacturerName(callback)
                eventQueue.add(event)

                val action = { gatt: BluetoothGatt? ->
                    gatt?.readCharacteristic(char) ?: true
                }

                doReliableOperation(action) { didSucceed ->
                    if (!didSucceed) {
                        eventQueue.remove(event)
                        event.callback.onManufacturerNameReceived(null)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendSlotValue(slotNumber: Int, value: UByte) {
        if (!isConnected) {
            Log.d("BleDevice", "Not connected, ignoring call to sendSlotValue")
            return
        }

        if (slotNumber >= 32 || slotNumber < 0) throw IllegalArgumentException()

        while (slotValues.size <= slotNumber) {
            slotValues.add(0)
        }
        slotValues[slotNumber] = value.toByte()

        val char = BleCharacteristic.DmxValues.findIn(btServices) ?: return

        char.value = slotValues.toByteArray()
        btGatt?.writeCharacteristic(char)
    }

    @SuppressLint("MissingPermission")
    override fun sendDmxConfig(config: DmxConfig) {
        if (!isConnected) {
            Log.d("BleDevice", "Not connected, ignoring call to sendDmxConfig")
            return
        }

        val request = DmxConfigChar.encode(config) ?: return
        val char = BleCharacteristic.DmxConfig.findIn(btServices) ?: return

        doReliableOperation(5) { gatt ->
            char.value = request
            gatt?.writeCharacteristic(char) ?: true
        }
    }

    @SuppressLint("MissingPermission")
    override fun requestDmxPersonalityInfo(personalityNumber: Int) {
        if (!isConnected) {
            Log.d("BleDevice", "Not connected, ignoring call to requestDmxPersonalityInfo")
            return
        }

        val char = BleCharacteristic.DmxExplorer.findIn(btServices) ?: return
        val request = DmxExplorerRequest.GetPersonalityInfo(personalityNumber).toBytes()

        doReliableOperation { gatt ->
            char.value = request
            gatt?.writeCharacteristic(char) ?: true
        }
    }

    @SuppressLint("MissingPermission")
    override fun requestDmxPersonalityInfo(range: IntRange) {
        if (!isConnected) {
            Log.d("BleDevice", "Not connected, ignoring call to requestDmxPersonalityInfo")
            return
        }

        val event = QueuedEvent.RequestDmxPersonalityInfoRange(range.first, range.last)
        eventQueue.add(event)

        val char = BleCharacteristic.DmxExplorer.findIn(btServices) ?: return
        val request = DmxExplorerRequest.GetPersonalityInfo(range.first).toBytes()

        val action = { gatt: BluetoothGatt? ->
            char.value = request
            gatt?.writeCharacteristic(char) ?: true
        }

        doReliableOperation(action) { didSucceed ->
            if (!didSucceed) {
                eventQueue.remove(event)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun requestDmxSlotInfo(personalityNumber: Int) {
        if (!isConnected) {
            Log.d("BleDevice", "Not connected, ignoring call to requestDmxSlotInfo")
            return
        }

        val char = BleCharacteristic.DmxExplorer.findIn(btServices) ?: return
        val request = DmxExplorerRequest.GetPersonalityAndSlotInfo(personalityNumber).toBytes()

        doReliableOperation { gatt ->
            char.value = request
            gatt?.writeCharacteristic(char) ?: true
        }
    }

    @SuppressLint("MissingPermission")
    override fun requestDmxInfo() {
        if (!isConnected) {
            Log.d("BleDevice", "Not connected, ignoring call to requestDmxInfo")
            return
        }

        val char = BleCharacteristic.DmxInfo.findIn(btServices) ?: return

        doReliableOperation { gatt ->
            gatt?.readCharacteristic(char) ?: true
        }
    }

    @SuppressLint("MissingPermission")
    private fun startConnectionSetup(gatt: BluetoothGatt) {
        val actions = listOf(
            Runnable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    btGatt?.setPreferredPhy(
                        BluetoothDevice.PHY_LE_2M_MASK,
                        BluetoothDevice.PHY_LE_2M_MASK,
                        BluetoothDevice.PHY_OPTION_NO_PREFERRED
                    )
                }
            },
            Runnable {
                val serviceDiscoveryStarted = btGatt!!.discoverServices()
                if (!serviceDiscoveryStarted) {
                    finishConnectionSetup(gatt)
                }
            }
        )

        val continueActions = { -> gatt == btGatt }

        Defer.postSequential(context.mainLooper, 170, continueActions, actions)
    }

    @SuppressLint("MissingPermission")
    private fun finishConnectionSetup(gatt: BluetoothGatt) {
        val actions = listOf(
            Runnable {
                BleCharacteristic.StatusMessage.findIn(btServices)?.enableNotifications(btGatt!!)
            },
            Runnable {
                BleCharacteristic.DmxInfo.findIn(btServices)?.enableNotifications(btGatt!!)
            },
            Runnable {
                BleCharacteristic.DmxExplorer.findIn(btServices)?.enableNotifications(btGatt!!)
            },
            Runnable {
                BleCharacteristic.DmxConfig.findIn(btServices)?.enableNotifications(btGatt!!)
            },
            Runnable {
                BleCharacteristic.DmxValues.findIn(btServices)?.enableNotifications(btGatt!!)
            },
            Runnable {
                onConnect.onConnectFinished(true, BluetoothGatt.GATT_SUCCESS)
            }
        )

        val continueActions = { -> gatt == btGatt }

        btServices = btGatt?.services

        Defer.postSequential(context.mainLooper, 170, continueActions, actions)
    }

    private fun doReliableOperation(retries: Int, action: (BluetoothGatt?) -> Boolean, completion: (Boolean) -> Unit) {
        val gatt = btGatt
        val predicate = { -> gatt == btGatt }
        val doActn = { -> action(gatt) }

        Defer.retryUntilSucceed(context.mainLooper, 100, retries, predicate, doActn, completion)
    }

    private fun doReliableOperation(action: (BluetoothGatt?) -> Boolean, completion: (Boolean) -> Unit) =
        doReliableOperation(Int.MAX_VALUE, action, completion)

    private fun doReliableOperation(action: (BluetoothGatt?) -> Boolean) =
        doReliableOperation(Int.MAX_VALUE, action) { _ -> }

    private fun doReliableOperation(retries: Int, action: (BluetoothGatt?) -> Boolean) =
        doReliableOperation(retries, action) { _ -> }
}
