package bmtreuherz.blelocationservice.GattStuff.Server

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import bmtreuherz.blelocationservice.GattStuff.Utilities.MessageFactory
import bmtreuherz.blelocationservice.R
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerActivity
import kotlinx.android.synthetic.main.activity_gatt_server.*
import java.nio.ByteBuffer

class GattServerActivity : UnityPlayerActivity() {

    companion object {
        val TAG = GattServerActivity::class.java.simpleName
    }

    // Bluetooth API
    lateinit private var bluetoothManager: BluetoothManager
    private var bluetoothGattServer: BluetoothGattServer? = null
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null

    // UI Stuff
//    lateinit private var messageET: EditText
//    lateinit private var notifyClientButton: Button
//    lateinit private var statusTV: TextView
//    lateinit private var messageTV: TextView

    // Connected device
    private var centralDevice: BluetoothDevice? = null


    // TODO: THIS SHOULD BE COMMON BETWEEN BOTH CLIENT AND SERVER
    private var positionMap = HashMap<Int, MessageFactory.PositionMessage>()
    private var rotationMap = HashMap<Int, MessageFactory.RotationMessage>()

    fun updateAssetPosition(id: Int, x: Float, y: Float, z: Float){

        var notifyClient = false
        var newPosition = MessageFactory.PositionMessage(id, x, y, z)

        if(!positionMap.containsKey(id)){
            positionMap.put(id, newPosition)
            notifyClient = true
        } else {
            var position = positionMap.get(id)

            if (position?.equals(newPosition) != true){
                notifyClient = true
            }
        }

        if (notifyClient){
            var message = MessageFactory.createPositionValue(newPosition)

            var characteristic = bluetoothGattServer
                    ?.getService(GameObjectProfile.GAME_OBJECT_SERVICE_UUID)
                    ?.getCharacteristic(GameObjectProfile.SERVER_TO_CLIENT_POSITION_CHARACTERISTIC_UUID)
            characteristic?.value = message
            bluetoothGattServer?.notifyCharacteristicChanged(centralDevice, characteristic, true)
        }
    }

    fun updateAssetOrientation(id: Int, x: Float, y: Float, z: Float){

//        var notifyClient = false
//        var newOrientation = MessageFactory.RotationMessage(id, x, y, z)
//
//        if(!rotationMap.containsKey(id)){
//            rotationMap.put(id, newOrientation)
//            notifyClient = true
//        } else {
//            var rotation = rotationMap.get(id)
//
//            if (rotation?.equals(newOrientation) != true){
//                notifyClient = true
//            }
//        }
//
//        if (notifyClient){
//            var message = MessageFactory.createRotationValue(newOrientation)
//
//            var characteristic = bluetoothGattServer
//                    ?.getService(GameObjectProfile.GAME_OBJECT_SERVICE_UUID)
//                    ?.getCharacteristic(GameObjectProfile.SERVER_TO_CLIENT_ROTATION_CHARACTERISTIC_UUID)
//            characteristic?.value = message
//            bluetoothGattServer?.notifyCharacteristicChanged(centralDevice, characteristic, true)
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_gatt_server)

        // Setup the bluetooth device
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothAdapter = bluetoothManager.adapter

        // Check if we have bluetooth support
        if (!checkBluetoothSupport(bluetoothAdapter)){
            finish()
            return
        }

        // Register for system bluetooth events
        var intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothReceiver, intentFilter)

        if (!bluetoothAdapter.isEnabled){
            Log.d(TAG, "Bluetooth currently disabled. Enabling now")
            bluetoothAdapter.enable()
        } else {
            Log.d(TAG, "Bluetooth enabled... starting services")
            startAdvertising()
            startServer()
        }

        // Init UI
//        messageET = findViewById(R.id.messageET)
//        notifyClientButton = findViewById(R.id.notifyClientButton)
//        notifyClientButton.setOnClickListener { notifyCentralDevice() }
//        statusTV = findViewById(R.id.statusTV)
//        messageTV = findViewById(R.id.clientMessageTV)
    }

    override fun onDestroy() {
        super.onDestroy()

        var bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter.isEnabled){
            stopServer()
            stopAdvertising()
        }

        unregisterReceiver(bluetoothReceiver)
    }

    // Helper methods
    private fun checkBluetoothSupport(bluetoothAdapter: BluetoothAdapter?): Boolean{
        if (bluetoothAdapter == null){
            Log.d(TAG, "Bluetooth not supported")
            return false
        }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Log.d(TAG, "Bluetooth LE is not supported")
            return false
        }

        return true
    }

    // Listens for buetooth adapter events to enable/ disable advertising and server
    // functionality
    private var bluetoothReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            var state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)

            when(state){
                BluetoothAdapter.STATE_ON -> {
                    startAdvertising()
                    startServer()
                }
                BluetoothAdapter.STATE_OFF -> {
                    stopServer()
                    stopAdvertising()
                }
            }
        }

    }

    private fun startAdvertising(){
        var bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        if (bluetoothLeAdvertiser == null){
            Log.d(TAG, "Failed to create advertiser")
            return
        }

        var settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build()

        var data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false) // TODO: May need for distance
                .addServiceUuid(ParcelUuid(GameObjectProfile.GAME_OBJECT_SERVICE_UUID))
                .build()

        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
        Log.d(TAG, "Advertising Now Yo!")
    }

    private fun stopAdvertising(){
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
    }

    private fun startServer(){
        bluetoothGattServer = bluetoothManager.openGattServer(this, gattServerCallback)

        if (bluetoothGattServer == null) {
            Log.d(TAG, "Unable to create GATT server")
            return
        }

        bluetoothGattServer?.addService(GameObjectProfile.createGameObjectService())
        Log.d(TAG, "Server Started Yo Yo!")

    }

    private fun stopServer(){
        bluetoothGattServer?.close()
    }

    private var advertiseCallback = object: AdvertiseCallback(){
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "LE Advertise Started")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.d(TAG, "LE Advertise Failed: " + errorCode)
        }
    }

    private fun notifyCentralDevice(){
        if (centralDevice == null){
            return
        }
//
//        var message = messageET.text.toString()
//        var bytes = message.toByteArray()
//
//        Log.d(TAG, "Sending notification to " + centralDevice)
//        var characteristic = bluetoothGattServer
//                ?.getService(GameObjectProfile.GAME_OBJECT_SERVICE_UUID)
//                ?.getCharacteristic(GameObjectProfile.SERVER_TO_CLIENT_CHARACTERISTIC_UUID)
//        characteristic?.value = bytes
//        bluetoothGattServer?.notifyCharacteristicChanged(centralDevice, characteristic, true)
//        // TODO: Figure out how to actualy get the confirmation that the client has received this.
    }

    private var gattServerCallback = object: BluetoothGattServerCallback(){
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED){
                Log.d(TAG, "Bluetooth CONNECTED to GATT Server: " + device)
//                runOnUiThread {statusTV.text = "Status: Connected" }
                centralDevice = device
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.d(TAG, "Device DIESCONNECTED from GATT Server: " + device)
//                runOnUiThread {statusTV.text = "Status: Not Connected" }
                centralDevice = null
                centralDevice = null
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int,
                                                 offset: Int, characteristic: BluetoothGattCharacteristic?) {
//            if (GameObjectProfile.SERVER_TO_CLIENT_CHARACTERISTIC_UUID.equals(characteristic?.uuid)) {
//                // Give the client the data!
//                // TODO: Implement this in a meaningful way
//                Log.d(TAG, "READ REQUEST RECEIVED")
//                bluetoothGattServer?.sendResponse(device,
//                        requestId,
//                        BluetoothGatt.GATT_SUCCESS,
//                        0,
//                        "Here is a response for you".toByteArray())
//            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int,
                                                  characteristic: BluetoothGattCharacteristic?,
                                                  preparedWrite: Boolean, responseNeeded: Boolean,
                                                  offset: Int, value: ByteArray?) {

            Log.d(TAG, "A WRITE REQUEST WAS JUST MADE")

            if (GameObjectProfile.CLIENT_TO_SERVER_POSITION_CHARACTERISTIC_UUID.equals(characteristic?.uuid)){
                if (value == null) {
                    return
                }

                var id = ByteBuffer.wrap(value, 1, 4).int
                var x = ByteBuffer.wrap(value, 5, 4).float
                var y = ByteBuffer.wrap(value, 9, 4).float
                var z = ByteBuffer.wrap(value, 13, 4).float

                var payload = id.toString() + "," + x + "," + y + "," + z
                UnityPlayer.UnitySendMessage("ExampleController", "PlaceObject",  payload)
                Log.d(TAG, "IN JAVA JUST TRIED TO CALLED C# METHOD FROM JAVA WITH PAYLOAD: " + payload)
            }

//            when(characteristic?.uuid){
//                GameObjectProfile.CLIENT_TO_SERVER_POSITION_CHARACTERISTIC_UUID -> {
//                    var positionMessage = MessageFactory.createPositonFromBytes(value!!)
//                    // TODO: Implement when receiving a position
//                    var payload = positionMessage.id.toString() + "," + positionMessage.x + "," + positionMessage.y + "," + positionMessage.z
////                    UnityPlayer.UnitySendMessage("ExampleController", "PlaceObject", payload)
//                    Log.d(TAG, "JUST CALLED C# METHOD FROM JAVA WITH PAYLOAD: " + payload)
//                }
////                GameObjectProfile.CLIENT_TO_SERVER_ROTATION_CHARACTERISTIC_UUID -> {
////                    var rotationMessage = MessageFactory.createRotationFromBytes(value!!)
////                    // TODO: Implement when receiving a rotation
////                }
//            }

//            if (GameObjectProfile.CLIENT_TO_SERVER_CHARACTERISTIC_UUID.equals(characteristic?.uuid)){
//                Log.d(TAG, "RECEIVED WRITE FROM CLIENT!")
//                Log.d(TAG, String(value!!))
//
//                if (responseNeeded){
//                    bluetoothGattServer?.sendResponse(device,
//                            requestId,
//                            BluetoothGatt.GATT_SUCCESS,
//                            0,
//                            null)
//                }
//                runOnUiThread { clientMessageTV.text = "Message: " + String(value!!) }
//            }
        }
    }
}


// TODO: May want to do some of the server start/ stopping on different lifecycle methods
// TODO: I think I need more to enable notifications