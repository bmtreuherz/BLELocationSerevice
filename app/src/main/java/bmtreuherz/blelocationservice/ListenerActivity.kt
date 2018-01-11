package bmtreuherz.blelocationservice

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import bmtreuherz.blelocationservice.utilities.BeaconProtocol
import bmtreuherz.blelocationservice.utilities.getBytesFromUUID
import bmtreuherz.blelocationservice.utilities.getUUIDFromBytes
import java.nio.ByteBuffer
import java.util.*

class ListenerActivity : AppCompatActivity() {

    // Configuration members
    private val appUUID = UUID.fromString("7b334cce-f705-11e7-8c3f-9a214cf093ae")
    private lateinit var beaconProtocol: BeaconProtocol

    // Bluetooth related members
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var blueoothLeScanner: BluetoothLeScanner
    private lateinit var blueoothLeAdvertiser: BluetoothLeAdvertiser
    private lateinit var scanFilter: ScanFilter
    private lateinit var scanSettings: ScanSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listener)

        // Set the beacon protocl
        beaconProtocol = BeaconProtocol.ALT_BEACON

        // Instantiated bluetooth related stuff
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        blueoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        blueoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        // Setup the BLE Scanner
        setScanFilter()
        setScanSettings()

    }

    // Scanner Setup
    private fun setScanFilter(){
        var builder = ScanFilter.Builder()
        var manufacturerData = ByteBuffer.allocate(23)
        var manufacturerDataMask = ByteBuffer.allocate(24)
        var uuid = getBytesFromUUID(appUUID)

        // Set the beacon protocol identifier
        manufacturerData.put(0, beaconProtocol.firstByte)
        manufacturerData.put(1, beaconProtocol.secondByte)

        // Set the UUID
        for (i in 2..17){
            manufacturerData.put(i, uuid[i-2])
        }

        // Create a bit mask to indicate filtering based on only the first 17 bits
        for (i in 0..17){
            manufacturerDataMask.put(0x01.toByte())
        }

        // Set the filter
        builder.setManufacturerData(beaconProtocol.manufacturerID, manufacturerData.array(), manufacturerDataMask.array())
        scanFilter = builder.build()
    }

    private fun setScanSettings(){
        var builder = ScanSettings.Builder()
        builder.setReportDelay(0)
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        scanSettings = builder.build()
    }


    // Starts Scanning
    private fun startScan(){
        blueoothLeScanner.startScan(Arrays.asList(scanFilter), scanSettings, object: ScanCallback(){
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                onScanResult(result)
            }
        })
    }

    private fun onScanResult(result: ScanResult?){
        if (result == null) return

        var scanRecord = result.scanRecord
        var manufacturerData = scanRecord.getManufacturerSpecificData(beaconProtocol.manufacturerID)

        // Extract the protocol information from the data
        var bufferedData = ByteBuffer.wrap(manufacturerData)
        var firstProtocolByte = bufferedData.get()
        var secondProtoclByte = bufferedData.get()

        // Validate the protocol information
        if (firstProtocolByte != beaconProtocol.firstByte || secondProtoclByte != beaconProtocol.secondByte) return

        // Extract out the UUID
        var uuidBytes = ByteArray(16)
        bufferedData.get(uuidBytes, 0, 16)
        var uuid = getUUIDFromBytes(uuidBytes)

        // Validate the UUID
        if (uuid != appUUID) return

        // Get the Major and Minor data
        var major = ByteArray(2)
        bufferedData.get(major, 0, 2)
        var minor = ByteArray(2)
        bufferedData.get(minor, 0, 2)

        var rssi = result.rssi
    }

}



// TODO:
// Extract out validation to validators (maybe)