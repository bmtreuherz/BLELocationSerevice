package bmtreuherz.blelocationservice

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle

import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import bmtreuherz.blelocationservice.utilities.calculateDistanceAltBeaconAlgorithm
import bmtreuherz.blelocationservice.utilities.getBytesFromUUID
import bmtreuherz.blelocationservice.utilities.getUUIDFromBytes
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Bradley on 1/17/18.
 */
class NewListenerActivity : Activity() {

    // Bluetooth Members
    private var bluetoothAdapter: BluetoothAdapter? = null
    lateinit private var bluetoothLeScanner: BluetoothLeScanner
    lateinit private var scanSettings: ScanSettings
    lateinit private var scanFilter: ScanFilter
    private var isScanning = false
    private val BT_REQUEST_ENABLE = 1
    private val REQUEST_ACCESS_FINE_LOCATION = 2

    // UI Members
    lateinit private var scanButton: Button
    private lateinit var foundTV: TextView
    private lateinit var UUIDTV: TextView
    private lateinit var payloadTV: TextView
    private lateinit var majorTV: TextView
    private lateinit var minorTV: TextView
    private lateinit var distanceTV: TextView
    private lateinit var rssiTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listener)

        // Check if this device supports BLE
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Instantiate bluetoothAdapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Initialize UI Components
//        scanButton = findViewById(R.id.startListeningButton)
//        scanButton.setOnClickListener { scanLeDevice(!isScanning) }
//        foundTV = findViewById(R.id.foundTV)
//        UUIDTV = findViewById(R.id.UUIDTV)
//        payloadTV = findViewById(R.id.payloadTV)
//        majorTV = findViewById(R.id.majorTV)
//        minorTV = findViewById(R.id.minorTV)
//        distanceTV = findViewById(R.id.distanceTV)
//        rssiTV = findViewById(R.id.rssiTV)
    }

    // TODO: REQUEST BT AND LOCATION PERMISSIONS IN THE NEW OFFICIAL ANDROID WAY
    override fun onResume(){
        super.onResume()

        // Check if we have bluetooth permissions
        if (bluetoothAdapter?.isEnabled == true){

            // Create the scanner
            bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner

            // Setup the scanner
            setScanSettings()
            setScanFilter()

        } else{
            // Request bt permissions
            var enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, BT_REQUEST_ENABLE)
        }

        // Check if we have location permissions
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            // Request permissions if we don't
//            ActivityCompat.requestPermissions(this,
//                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                    REQUEST_ACCESS_FINE_LOCATION)
//
//        }
    }

    private fun setScanSettings(){
        scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
    }

    private fun setScanFilter(){
        var manufacturerData = ByteBuffer.allocate(23)
        var manufacturerDataMask = ByteBuffer.allocate(23)


        // Set the beacon protocol identifier
        manufacturerData.put(0, 0x02)
        manufacturerData.put(1, 0x15)


        // Set the UUID
        var uuid = getBytesFromUUID(UUID.fromString("3F643DCB-DD1E-4300-8FD6-91543CD0E648"))


        for (i in 2..17){
            manufacturerData.put(i, uuid[i-2])
        }

        // Create a bit mask to indicate filtering based on only the first 17 bits
        for (i in 0..17) {

            manufacturerDataMask.put(0x11.toByte())
        }

        // Set the filter
        scanFilter = ScanFilter.Builder()
                .setManufacturerData(76,manufacturerData.array(), manufacturerDataMask.array() )
                .build()
    }

    override fun onPause(){
        super.onPause()

        // Stop scanning
        if (bluetoothAdapter?.isEnabled == true){
            scanLeDevice(false)
        }
    }

    // TODO: Probably do this the same way the other permissions are being requested
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BT_REQUEST_ENABLE){
            // Exit if the user declined to give permissions
            if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "This app cannot be used without Bluetooth.", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                           permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "This app cannot be used without location permissions.", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
    }

    private fun scanLeDevice(enable: Boolean){
        when(enable){
            true -> {
                bluetoothLeScanner.startScan(Arrays.asList(scanFilter), scanSettings, scanCallback)
                scanButton.text = "Stop Scanning"
                clearUI()
            }
            false -> {
                bluetoothLeScanner.stopScan(scanCallback)
                scanButton.text = "Start Scanning"
            }
        }
        isScanning = enable
    }

    private var scanCallback = object: ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d("ADVERTISEMENT_FOUND", result?.toString())

            handleResult(result)
            scanLeDevice(false)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d("SCAN FAILED", "Error Code: " + errorCode)
        }
    }

    private fun handleResult(result: ScanResult?): Boolean{

        if (result == null) return false

        var scanRecord = result.scanRecord
        var manufacturerData: ByteArray? = scanRecord.getManufacturerSpecificData(76) ?: return false

        // Extract the protocol information from the data
        var bufferedData = ByteBuffer.wrap(manufacturerData)

        var firstProtocolByte = bufferedData.get()
        var secondProtoclByte = bufferedData.get()

        // Extract out the UUID
        var uuidBytes = ByteArray(16)
        bufferedData.get(uuidBytes, 0, 16)
        var uuid = getUUIDFromBytes(uuidBytes)

        // Get the Major and Minor data
        var major = ByteArray(2)
        bufferedData.get(major, 0, 2)
        var minor = ByteArray(2)
        bufferedData.get(minor, 0, 2)

        var rssi = result.rssi
        var txPowerLevel = scanRecord.txPowerLevel
        var distance = calculateDistanceAltBeaconAlgorithm(txPowerLevel, rssi)

        // TODO: May not need this after testing.. just want payload
        var majorInt = ByteBuffer.wrap(major).char.toInt()
        var minorInt = ByteBuffer.wrap(minor).char.toInt()

        var payloadArray = ByteArray(8)
        payloadArray[4] = major[0]
        payloadArray[5] = major[1]
        payloadArray[6] = minor[0]
        payloadArray[7] = minor[1]

        var payload = bytesToLong(payloadArray)

        // SET UI ELEMENTS
        foundTV.text = "Found: Yes"
        UUIDTV.text = "UUID: " + uuid.toString()
        payloadTV.text = "Payload: " + payload.toString()
        majorTV.text = "Major: " + majorInt.toString()
        minorTV.text = "Minor: " + minorInt.toString()
        distanceTV.text = "Distance: " + distance.toString()
        rssiTV.text = "RSSI: " + rssi.toString()

        return true
    }

    private fun clearUI(){
        foundTV.text = "Found: No"
        UUIDTV.text = "UUID: N/A"
        payloadTV.text = "Payload: N/A"
        majorTV.text = "Major N/A: "
        minorTV.text = "Minor N/A: "
        distanceTV.text = "Distance: N/A"
        rssiTV.text = "RSSI: N/A"
    }

    // TODO: PUT IN UTILITY
    fun bytesToLong(bytes: ByteArray): Long {
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.put(bytes)
        buffer.flip()//need flip
        return buffer.long
    }
}