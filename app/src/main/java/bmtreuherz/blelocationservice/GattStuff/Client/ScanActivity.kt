package bmtreuherz.blelocationservice.GattStuff.Client

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import bmtreuherz.blelocationservice.GattStuff.Server.GameObjectProfile
import bmtreuherz.blelocationservice.GattStuff.Utilities.MessageFactory
import bmtreuherz.blelocationservice.R
import com.unity3d.player.UnityPlayerActivity
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ScanActivity : UnityPlayerActivity() {

    companion object {
        val TAG = ScanActivity::class.java.simpleName
        val SCAN_PERIOD: Long = 10000
        val REQUEST_ENABLE_BT = 1
        val REQUEST_FINE_LOCATION = 2
    }

    // Bluetooth API
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var isScanning = false
    private var isConnected = false
    private var handler: Handler? = null
    private var gatt: BluetoothGatt? = null

    private var scanResults = HashMap<String, BluetoothDevice>()

    // UI stuff
//    lateinit private var startScanButton: Button
//    lateinit private var statusTV: TextView
//    lateinit private var serverMessageTV: TextView
//    lateinit private var sendToServerButton: Button
//    lateinit private var messageET: EditText

    // TODO: THIS SHOULD BE COMMON BETWEEN BOTH CLIENT AND SERVER
    private var positionMap = HashMap<Int, MessageFactory.PositionMessage>()
    private var rotationMap = HashMap<Int, MessageFactory.RotationMessage>()

    fun updateAssetPosition(id: Int, x: Float, y: Float, z: Float){

        var notifyServer = false
        var newPosition = MessageFactory.PositionMessage(id, x, y, z)

        if(!positionMap.containsKey(id)){
            positionMap.put(id, newPosition)
            notifyServer = true
        } else {
            var position = positionMap.get(id)

            if (position?.equals(newPosition) != true){
                notifyServer = true
            }
        }

        if (notifyServer){
            var service = gatt?.getService(GameObjectProfile.GAME_OBJECT_SERVICE_UUID)
            var characteristic = service?.getCharacteristic(GameObjectProfile.CLIENT_TO_SERVER_POSITION_CHARACTERISTIC_UUID)
            var message = MessageFactory.createPositionValue(newPosition)
            characteristic?.value = message
            var success = gatt?.writeCharacteristic(characteristic)
        }
    }

    fun updateAssetOrientation(id: Int, x: Float, y: Float, z: Float){

//        var notifyServer = false
//        var newOrientation = MessageFactory.RotationMessage(id, x, y, z)
//
//        if(!rotationMap.containsKey(id)){
//            rotationMap.put(id, newOrientation)
//            notifyServer = true
//        } else {
//            var rotation = rotationMap.get(id)
//
//            if (rotation?.equals(newOrientation) != true){
//                notifyServer = true
//            }
//        }
//
//        if (notifyServer){
//            var service = gatt?.getService(GameObjectProfile.GAME_OBJECT_SERVICE_UUID)
//            var characteristic = service?.getCharacteristic(GameObjectProfile.CLIENT_TO_SERVER_ROTATION_CHARACTERISTIC_UUID)
//            var message = MessageFactory.createRotationValue(newOrientation)
//            characteristic?.value = message
//            var success = gatt?.writeCharacteristic(characteristic)
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_scan)

        var bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
//
//        sendToServerButton = findViewById(R.id.sendToServerButton)
//        messageET = findViewById(R.id.messageET)
//        sendToServerButton.setOnClickListener {
//            sendMessageToServer()
//        }

//        statusTV = findViewById(R.id.scanStatusTV)
//        startScanButton = findViewById(R.id.startScanButton)
//        serverMessageTV = findViewById(R.id.serverMessageTV)
//        startScanButton.setOnClickListener {
//            if (isScanning){
//                stopScan()
//            } else{
//                startScan()
//            }
//        }

        startScan()
    }

    override fun onResume() {
        super.onResume()
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            finish()
            return
        }
    }

    private fun startScan(){
        Log.d(TAG, "STARTED SCAN A")
        if (!hasPermissions() || isScanning){
            return
        }

        var filters = ArrayList<ScanFilter>()
        var scanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(GameObjectProfile.GAME_OBJECT_SERVICE_UUID))
                .build()
        filters.add(scanFilter)

        var settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        isScanning = true

        handler = Handler()
        handler?.postDelayed(this::stopScan, SCAN_PERIOD)
//        startScanButton.text = "Stop Scanning"
        Log.d(TAG, "STARTED SCAN B")

    }

    private fun stopScan(){
        if (isScanning && bluetoothAdapter != null && bluetoothAdapter?.isEnabled == true && bluetoothLeScanner != null){
            bluetoothLeScanner?.stopScan(scanCallback)
            scanComplete()
        }
        isScanning = false
        handler = null
//        startScanButton.text = "Start Scanning"
        Log.d(TAG, "STOPPED SCANNING")
    }

    private fun scanComplete(){
        Log.d(TAG, "SCAN COMPLETE")
        if (scanResults.isEmpty()){
            return
        }

        for (deviceAddress in scanResults.keys){
            Log.d(TAG, "Found device: " + deviceAddress)
        }
    }

    private fun hasPermissions(): Boolean{
        if (bluetoothAdapter?.isEnabled != true){
            requestBluetoothEnable()
            return false
        } else if (!hasLocationPermissions()){
            requestLocationPermissions()
            return false
        }
        return true
    }

    private fun requestBluetoothEnable(){
        var enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        Log.d(TAG, "Requested enable bluetooth")
    }

    private fun hasLocationPermissions(): Boolean =
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions(){
        var permissions = Array(1, { android.Manifest.permission.ACCESS_FINE_LOCATION })
        requestPermissions(permissions, REQUEST_FINE_LOCATION)
    }

    private var scanCallback = object: ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            addScanResult(result!!)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            for (result in results!!){
                addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d(TAG, "BLE Scan Failed with code: " + errorCode)
        }

        private fun addScanResult(result: ScanResult){
            Log.d(TAG, "BLE Scan Result Found!!!! ")
            stopScan()
            scanResults.put(result.device.address, result.device)
            connectToDevice(result.device)
        }
    }

    private var gattClientCallback = object: BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (newState == BluetoothProfile.STATE_CONNECTED){
                isConnected = true
//                runOnUiThread {statusTV.text = "Status: Connected" }
                Log.d(TAG, "Client Connected!!!")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.d(TAG, "Client Disconnected!!!")
//                runOnUiThread {statusTV.text = "Status: Not Connected" }
                disconnectGattServer()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status != BluetoothGatt.GATT_SUCCESS){
                return
            }

            var service = gatt?.getService(GameObjectProfile.GAME_OBJECT_SERVICE_UUID)

            var outPositionCharacteristic = service?.getCharacteristic(GameObjectProfile.CLIENT_TO_SERVER_POSITION_CHARACTERISTIC_UUID)
//            var outRotationCharacteristic = service?.getCharacteristic(GameObjectProfile.CLIENT_TO_SERVER_ROTATION_CHARACTERISTIC_UUID)
            var inPositionCharacteristic = service?.getCharacteristic(GameObjectProfile.SERVER_TO_CLIENT_POSITION_CHARACTERISTIC_UUID)
//            var inRotationCharacteristic = service?.getCharacteristic(GameObjectProfile.SERVER_TO_CLIENT_ROTATION_CHARACTERISTIC_UUID)



            outPositionCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
//            outRotationCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT


            gatt?.setCharacteristicNotification(inPositionCharacteristic, true)
//            gatt?.setCharacteristicNotification(inRotationCharacteristic, true)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d(TAG, "WRITE SUCCESSFULL YO!")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)

            when(characteristic?.uuid){
                GameObjectProfile.SERVER_TO_CLIENT_POSITION_CHARACTERISTIC_UUID -> {
                    var positionMessage = MessageFactory.createPositionFromBytes(characteristic?.value!!)
                    // TODO: Do action when position changes
                }
//                GameObjectProfile.SERVER_TO_CLIENT_ROTATION_CHARACTERISTIC_UUID -> {
//                    var rotationMessage = MessageFactory.createRotationFromBytes(characteristic?.value!!)
//                    // TODO: Do action when rotation changes
//                }
            }
        }
    }

    private fun disconnectGattServer(){
        isConnected = false
        gatt?.disconnect()
        gatt?.close()
    }

    private fun connectToDevice(device: BluetoothDevice){
        Log.d(TAG, "Attempting to connect to device!")
        gatt = device.connectGatt(this, false, gattClientCallback)
    }

    private fun sendMessageToServer(){
        if (!isConnected){
            return
        }

//        var service = gatt?.getService(GameObjectProfile.GAME_OBJECT_SERVICE_UUID)
//        var characteristic = service?.getCharacteristic(GameObjectProfile.CLIENT_TO_SERVER_CHARACTERISTIC_UUID)
//        var message = messageET.text.toString()
//        var messageBytes = message.toByteArray()
//
//        characteristic?.value = messageBytes
//        var success = gatt?.writeCharacteristic(characteristic)
    }
}
